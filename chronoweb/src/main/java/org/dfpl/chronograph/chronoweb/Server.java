package org.dfpl.chronograph.chronoweb;

import java.net.Inet4Address;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.dfpl.chronograph.chronoweb.router.ManipulationRouter;
import org.dfpl.chronograph.chronoweb.router.SubscriptionRouter;
import org.dfpl.chronograph.kairos.KairosEngine;
import org.dfpl.chronograph.khronos.memory.manipulation.MChronoGraph;
import org.dfpl.chronograph.khronos.persistent.manipulation.PChronoGraph;

import com.tinkerpop.blueprints.Graph;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
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
	public static JsonObject configuration;
	public static int port = 80;
	public static String backendType;
	public static String dbName;
	public static String connectionString;
	public static int numberOfVerticles;
	private Graph graph;
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

	public void setModules() {
		if(backendType.equals("memory")) {
			graph = new MChronoGraph(eventBus);
		}else {
			graph = new PChronoGraph(connectionString, dbName, eventBus);
		}
		kairos = new KairosEngine(graph, eventBus);		
	}
	
	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		super.start(startPromise);

		final HttpServer server = vertx.createHttpServer();
		this.router = Router.router(vertx);
		router.route().handler(BodyHandler.create().setBodyLimit(BodyHandler.DEFAULT_BODY_LIMIT * 2)
				.setDeleteUploadedFilesOnEnd(true));
		this.eventBus = vertx.eventBus();
		
		setModules();

		registerManipulationRouter();
		registerSubscriptionRouter();

		server.requestHandler(router).listen(port);
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

	public static void main(String[] args) {
		Bootstrap.bootstrap(args);
		Vertx.vertx().deployVerticle("org.dfpl.chronograph.chronoweb.Server", new DeploymentOptions().setInstances(Server.numberOfVerticles));
	}
}
