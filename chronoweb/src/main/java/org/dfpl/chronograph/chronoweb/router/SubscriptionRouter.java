package org.dfpl.chronograph.chronoweb.router;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.NotDirectoryException;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.Document;
import org.dfpl.chronograph.chronoweb.MessageBuilder;
import org.dfpl.chronograph.chronoweb.Server;
import org.dfpl.chronograph.common.KairosProgram;
import org.dfpl.chronograph.common.VertexEvent;
import org.dfpl.chronograph.kairos.AbstractKairosProgram;
import org.dfpl.chronograph.kairos.KairosEngine;
import org.dfpl.chronograph.kairos.gamma.GammaTable;
import org.dfpl.chronograph.kairos.gamma.persistent.file.LongGammaElement;
import org.dfpl.chronograph.kairos.program.path_reachability.OutIsAfterPathReachability;
import org.dfpl.chronograph.kairos.program.reachability.OutIsAfterReachability;
import org.dfpl.chronograph.kairos.gamma.persistent.db.ExpandableGammaTable;
import org.dfpl.chronograph.kairos.gamma.persistent.file.FixedSizedGammaTable;
import org.dfpl.chronograph.khronos.manipulation.memory.MChronoVertex;
import org.dfpl.chronograph.khronos.manipulation.memory.MChronoVertexEvent;
import org.dfpl.chronograph.khronos.manipulation.persistent.PChronoGraph;
import org.dfpl.chronograph.khronos.manipulation.persistent.PChronoVertex;
import org.dfpl.chronograph.khronos.manipulation.persistent.PChronoVertexEvent;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import static org.dfpl.chronograph.chronoweb.Server.*;

public class SubscriptionRouter extends BaseRouter {

	private KairosEngine kairos;

	public SubscriptionRouter(Graph graph, KairosEngine kairos) {
		super(graph);
		this.kairos = kairos;
	}

	public boolean isAvailableProgram(String kairosProgram) {
		try {
			KairosProgram.valueOf(kairosProgram);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public void registerGetSubscriptions(Router router, EventBus eventBus) {
		router.get("/chronoweb/subscribe").handler(routingContext -> {
			JsonArray arr = new JsonArray();
			Set<VertexEvent> set = kairos.getSources();
			for (VertexEvent s : set) {
				arr.add(s.getId());
			}
			sendResult(routingContext, "application/json", arr.toString(), 200);
		});

		Server.logger.info("GET /chronoweb/subscribe router added");
	}

	public void registerSubscribeVertexEventRouter(Router router, EventBus eventBus) {

		router.put("/chronoweb/graph/:time/:kairosProgram/:edgeLabel/:vertexID").handler(routingContext -> {
			long time;
			try {
				time = Long.parseLong(routingContext.pathParam("time"));
			} catch (Exception e) {
				sendResult(routingContext, "application/json", MessageBuilder.invalidTimeSynta1xException, 400);
				return;
			}

			String kairosProgram = routingContext.pathParam("kairosProgram");
			if (!isAvailableProgram(kairosProgram)) {
				sendResult(routingContext, "application/json", MessageBuilder.noSuchProgramException, 404);
				return;
			}

			String vertexID = routingContext.pathParam("vertexID");
			if (!vPattern.matcher(vertexID).matches()) {
				sendResult(routingContext, "application/json", MessageBuilder.invalidVertexIDException, 400);
				return;
			}

			String edgeLabel = routingContext.pathParam("edgeLabel");
			if (edgeLabel == null){
				sendResult(routingContext, "application/json", MessageBuilder.getMissingRequiredURLParameterException("edgeLabel"), 400);
				return;
			}

			Vertex v = null;
			if (Server.backendType.equals("memory"))
				v = new MChronoVertex(graph, vertexID);
			else {
				v = new PChronoVertex(graph, vertexID, ((PChronoGraph) graph).getVertexCollection());
			}

			try {
				if (kairos.getProgram(time, kairosProgram, edgeLabel).getGammaTable().getSources().contains(vertexID)) {
					sendResult(routingContext, "application/json", MessageBuilder.sourceAlreadySubscribedException,
							409);
					return;
				}
			} catch (Exception ignored) {

			}

			VertexEvent ve = null;
			if (Server.backendType.equals("memory"))
				ve = new MChronoVertexEvent(v, time);
			else {
				ve = new PChronoVertexEvent(graph, vertexID, time, ((PChronoGraph) graph).getVertexEventCollection());
			}

			AbstractKairosProgram<?> existing = kairos.getProgram(time, kairosProgram, edgeLabel);
			if (existing != null) {
				if (kairosProgram.equals("OutIsAfterReachability")) {
					GammaTable<String, Long> gammaTable = existing.getGammaTable();
					if (gammaTable.getSources().contains(v.getId())) {
						sendResult(routingContext, 406);
						return;
					} else {
						kairos.addSubscription(v, ve.getTime(), edgeLabel, new OutIsAfterReachability(graph, gammaTable));
						sendResult(routingContext, 200);
						return;
					}
				} else if (kairosProgram.equals("OutIsAfterPathReachability")) {
					GammaTable<String, Document> gammaTable = existing.getGammaTable();
					if (gammaTable.getSources().contains(v.getId())) {
						sendResult(routingContext, 406);
						return;
					} else {
						kairos.addSubscription(v, ve.getTime(), edgeLabel, new OutIsAfterPathReachability(graph, gammaTable));
						sendResult(routingContext, 200);
						return;
					}
				} else {
					sendResult(routingContext, 500);
					return;
				}
			} else {
				if (kairosProgram.equals("OutIsAfterReachability")) {
					String subDirectoryName = Server.gammaBaseDirectory + "\\" + ve.getTime() + "_" + kairosProgram + "_" + edgeLabel;
					File subDirectory = new File(subDirectoryName);
					if (!subDirectory.exists())
						subDirectory.mkdirs();
					FixedSizedGammaTable<String, Long> gammaTable = null;
					try {
						gammaTable = new FixedSizedGammaTable<String, Long>(subDirectoryName, LongGammaElement.class);
					} catch (NotDirectoryException | FileNotFoundException e) {
						sendResult(routingContext, 500);
						return;
					}
					kairos.addSubscription(v, ve.getTime(), edgeLabel, new OutIsAfterReachability(graph, gammaTable));
					sendResult(routingContext, 200);
					return;
				} else if (kairosProgram.equals("OutIsAfterPathReachability")) {
					ExpandableGammaTable gammaTable = null;
					gammaTable = new ExpandableGammaTable(kairos.getGammaClient(), time + "_" + kairosProgram);
					kairos.addSubscription(v, ve.getTime(), edgeLabel,  new OutIsAfterPathReachability(graph, gammaTable));
					sendResult(routingContext, 200);
					return;
				} else {
					sendResult(routingContext, 500);
					return;
				}
			}
		});

		Server.logger.info("POST /chronoweb/subscribe/:resource router added");
	}

	public void registerGetGammaRouter(Router router, EventBus eventBus) {

		router.get("/chronoweb/gammaTable").handler(routingContext -> {
			JsonArray timeArray = new JsonArray();
			for (Long t : kairos.getTimes()) {
				timeArray.add(t);
			}
			sendResult(routingContext, "application/json", timeArray.toString(), 200);
		});

		Server.logger.info("GET /chronoweb/gammaTable router added");

		router.get("/chronoweb/gammaTable/:time").handler(routingContext -> {
			long time;
			try {
				time = Long.parseLong(routingContext.pathParam("time"));
			} catch (Exception e) {
				sendResult(routingContext, "application/json", MessageBuilder.invalidTimeSynta1xException, 400);
				return;
			}

			JsonArray result = new JsonArray();

			Set<AbstractKairosProgram<?>> programs = kairos.getPrograms(time);
			if (programs == null) {
				sendResult(routingContext, "application/json", MessageBuilder.resourceNotFoundException, 404);
				return;
			}

			for (String program : programs.stream().map(p -> p.getName()).collect(Collectors.toSet())) {
				result.add(program);
			}
			sendResult(routingContext, "application/json", result.toString(), 200);
		});

		Server.logger.info("GET /chronoweb/gammaTable/:time router added");

		router.get("/chronoweb/gammaTable/:time/:kairosProgram/:edgeLabel").handler(routingContext -> {

			long time;
			try {
				time = Long.parseLong(routingContext.pathParam("time"));
			} catch (Exception e) {
				sendResult(routingContext, "application/json", MessageBuilder.invalidTimeSynta1xException, 400);
				return;
			}

			String kairosProgram = routingContext.pathParam("kairosProgram");
			if (!isAvailableProgram(kairosProgram)) {
				sendResult(routingContext, "application/json", MessageBuilder.noSuchProgramException, 404);
				return;
			}

			String edgeLabel = routingContext.pathParam("edgeLabel");
			if (edgeLabel == null){
				sendResult(routingContext, "application/json", MessageBuilder.getMissingRequiredURLParameterException("edgeLabel"), 400);
				return;
			}

			try {
				JsonObject result = new JsonObject();
				result.put("time", time);
				result.put("program", kairosProgram);
				result.put("edgeLabel", edgeLabel);
				Set<String> sources = kairos.getProgram(time, kairosProgram, edgeLabel).getGammaTable().getSources();
				JsonArray sourceArray = new JsonArray();
				for (String source : sources) {
					sourceArray.add(source);
				}
				result.put("gammaSources", sourceArray);
				sendResult(routingContext, "application/json", result.toString(), 200);
				return;
			} catch (NullPointerException e) {
				sendResult(routingContext, "application/json", MessageBuilder.resourceNotFoundException, 404);
				return;
			} catch (Exception e) {
				sendResult(routingContext, 500);
				return;
			}

		});

		Server.logger.info("GET /chronoweb/graph/:time/:kairosProgram router added");

		router.get("/chronoweb/gammaTable/:time/:kairosProgram/:edgeLabel/:vertexID").handler(routingContext -> {
			long time;
			try {
				time = Long.parseLong(routingContext.pathParam("time"));
			} catch (Exception e) {
				sendResult(routingContext, "application/json", MessageBuilder.invalidTimeSynta1xException, 400);
				return;
			}

			String kairosProgram = routingContext.pathParam("kairosProgram");
			if (!isAvailableProgram(kairosProgram)) {
				sendResult(routingContext, "application/json", MessageBuilder.noSuchProgramException, 404);
				return;
			}

			String vertexID = routingContext.pathParam("vertexID");
			if (!vPattern.matcher(vertexID).matches()) {
				sendResult(routingContext, "application/json", MessageBuilder.invalidVertexIDException, 400);
				return;
			}

			String edgeLabel = routingContext.pathParam("edgeLabel");
			if (edgeLabel == null){
				sendResult(routingContext, "application/json", MessageBuilder.getMissingRequiredURLParameterException("edgeLabel"), 400);
				return;
			}

			Vertex v = graph.getVertex(vertexID);
			if (v == null) {
				sendResult(routingContext, "application/json", MessageBuilder.resourceNotFoundException, 404);
				return;
			}

			try {
				JsonObject result = new JsonObject();
				result.put("time", time);
				result.put("source", vertexID);
				result.put("program", kairosProgram);
				result.put("edgeLabel", edgeLabel);
				JsonObject gamma = kairos.getProgram(time, kairosProgram, edgeLabel).getGammaTable().getGamma(vertexID)
						.toJson(true);
				if (gamma == null) {
					sendResult(routingContext, "application/json", MessageBuilder.resourceNotFoundException, 404);
					return;
				} else {
					result.put("gamma", gamma);
				}
				sendResult(routingContext, "application/json", result.toString(), 200);
				return;
			} catch (NullPointerException e) {
				sendResult(routingContext, "application/json", MessageBuilder.resourceNotFoundException, 404);
				return;
			} catch (Exception e) {
				sendResult(routingContext, 500);
				return;
			}
		});

		Server.logger.info("GET /chronoweb/graph/:time/:kairosProgram/:vertexID router added");

		router.get("/chronoweb/gammaTable/:time/:kairosProgram/:source/:destination").handler(routingContext -> {
			long time;
			try {
				time = Long.parseLong(routingContext.pathParam("time"));
			} catch (Exception e) {
				sendResult(routingContext, "application/json", MessageBuilder.invalidTimeSynta1xException, 400);
				return;
			}

			String kairosProgram = routingContext.pathParam("kairosProgram");
			if (!isAvailableProgram(kairosProgram)) {
				sendResult(routingContext, "application/json", MessageBuilder.noSuchProgramException, 404);
				return;
			}

			String edgeLabel = routingContext.pathParam("edgeLabel");
			if (edgeLabel == null){
				sendResult(routingContext, "application/json", MessageBuilder.getMissingRequiredURLParameterException("edgeLabel"), 400);
				return;
			}

			String sourceID = routingContext.pathParam("source");
			if (!vPattern.matcher(sourceID).matches()) {
				sendResult(routingContext, "application/json", MessageBuilder.invalidVertexIDException, 400);
				return;
			}

			Vertex source = graph.getVertex(sourceID);
			if (source == null) {
				sendResult(routingContext, "application/json", MessageBuilder.resourceNotFoundException, 404);
				return;
			}

			String destinationID = routingContext.pathParam("destination");
			if (!vPattern.matcher(destinationID).matches()) {
				sendResult(routingContext, "application/json", MessageBuilder.invalidVertexIDException, 400);
				return;
			}

			Vertex destination = graph.getVertex(destinationID);
			if (destination == null) {
				sendResult(routingContext, "application/json", MessageBuilder.resourceNotFoundException, 404);
				return;
			}

			try {
				JsonObject result = new JsonObject();
				result.put("time", time);
				result.put("program", kairosProgram);
				result.put("edgeLabel", edgeLabel);
				result.put("source", sourceID);
				result.put("destination", destinationID);

				Object gammaElement = kairos.getProgram(time, kairosProgram, edgeLabel).getGammaTable().getGamma(sourceID)
						.getElement(destinationID);
				result.put("gammaElement", gammaElement);

				sendResult(routingContext, "application/json", result.toString(), 200);
				return;
			} catch (NullPointerException e) {
				sendResult(routingContext, "application/json", MessageBuilder.resourceNotFoundException, 404);
				return;
			} catch (Exception e) {
				sendResult(routingContext, 500);
				return;
			}
		});

		Server.logger.info("GET /chronoweb/gammaTable/:time/:kairosProgram/:source/:destination router added");

	}

}
