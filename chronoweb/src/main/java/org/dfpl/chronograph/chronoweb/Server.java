package org.dfpl.chronograph.chronoweb;

import java.net.Inet4Address;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.dfpl.chronograph.chronoweb.router.memory.ManipulationRouter;
import org.dfpl.chronograph.chronoweb.router.memory.SubscriptionRouter;
import org.dfpl.chronograph.kairos.KairosEngine;
import org.dfpl.chronograph.khronos.memory.manipulation.MChronoGraph;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * The in-memory implementation of temporal graph database.
 *
 * @author Jaewook Byun, Ph.D., Assistant Professor, DFPL, Department of
 *         Software, Sejong University
 * 
 * @author Haifa Gaza, Ph.D., Student, DFPL, Sejong University
 * 
 *         Gaza, Haifa, and Jaewook Byun. "Kairos: Enabling prompt monitoring of
 *         information diffusion over temporal networks." IEEE Transactions on
 *         Knowledge and Data Engineering (2023).
 * 
 *         Byun, Jaewook. "Enabling time-centric computation for efficient
 *         temporal graph traversals from multiple sources." IEEE Transactions
 *         on Knowledge and Data Engineering (2020).
 * 
 *         Byun, Jaewook, Sungpil Woo, and Daeyoung Kim. "Chronograph: Enabling
 *         temporal graph traversals for efficient information diffusion
 *         analysis over time." IEEE Transactions on Knowledge and Data
 *         Engineering 32.3 (2019): 424-437.
 * 
 */
public class Server extends AbstractVerticle {

	public static Logger logger;
	public static int port = 80;
	private MChronoGraph graph;
	private KairosEngine kairos;
	private EventBus eventBus;
	private Router router;

	public static Pattern vPattern = Pattern.compile("^[^|_]+$");
	public static Pattern ePattern = Pattern.compile("^[^|_]+\\|[^|_]+\\|[^|_]+$");
	public static Pattern vtPattern = Pattern.compile("^[^|_]+_[+-]?[0-9]+$");
	public static Pattern etPattern = Pattern.compile("^[^|_]+\\|[^|_]+\\|[^|_]+_[+-]?[0-9]+$");

	public static List<String> datasetList = List.of("facebook_combined", "Email-EuAll", "sx-mathoverflow",
			"tcp_sample");

	private ManipulationRouter manipulationRouter;
	private SubscriptionRouter subscriptionRouter;

	public static String baseDirectory = "d:\\kairos";

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		super.start(startPromise);

		final HttpServer server = vertx.createHttpServer();
		this.router = Router.router(vertx);
		router.route().handler(BodyHandler.create().setBodyLimit(BodyHandler.DEFAULT_BODY_LIMIT * 2)
				.setDeleteUploadedFilesOnEnd(true));

		this.eventBus = vertx.eventBus();
		graph = new MChronoGraph(eventBus);
		kairos = new KairosEngine(graph, eventBus);

		registerManipulationRouter();
		registerSubscriptionRouter();

		server.requestHandler(router).listen(80);
		logger.info(
				"Chronoweb runs at http://" + Inet4Address.getLocalHost().getHostAddress() + ":" + port + "/chronoweb");
	}

	public void registerManipulationRouter() {

		manipulationRouter = new ManipulationRouter(graph);
		manipulationRouter.registerAddElementRouter(router, eventBus);
		manipulationRouter.registerGetElementRouter(router, eventBus);
		manipulationRouter.registerGetElementsRouter(router, eventBus);
		manipulationRouter.registerRemoveElementRouter(router, eventBus);
		manipulationRouter.registerGetIncidentEdgesRouter(router, eventBus);
		manipulationRouter.registerGetAdjacentVerticesRouter(router, eventBus);
		manipulationRouter.registerDeleteGraphRouter(router, eventBus);
		manipulationRouter.registerGetEventsRouter(router, eventBus);
		manipulationRouter.registerGetIncidentEdgeEventsRouter(router, eventBus);
		manipulationRouter.registerGetAdjacentVertexEventsRouter(router, eventBus);
		manipulationRouter.registerGetDatasetsRouter(router, eventBus);
		manipulationRouter.registerLoadDatasetRouter(router, eventBus);

	}

	public void registerSubscriptionRouter() {
		subscriptionRouter = new SubscriptionRouter(graph, kairos);
		subscriptionRouter.registerSubscribeVertexEventRouter(router, eventBus);
		
		subscriptionRouter.registerGetSubscriptions(router, eventBus);
		subscriptionRouter.registerGetGammaRouter(router, eventBus);
	}

	public static void setLogger() {
		Configurator.setRootLevel(Level.OFF);
		Configurator.setLevel(Server.class, Level.DEBUG);
		logger = LogManager.getLogger(Server.class);
	}

	public static void main(String[] args) {
		setLogger();
		Vertx.vertx().deployVerticle(new Server());
	}
}
