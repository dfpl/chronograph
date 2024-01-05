package org.dfpl.chronograph.chronoweb;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class Server extends AbstractVerticle {

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		super.start(startPromise);

		final HttpServer server = vertx.createHttpServer();
		final Router router = Router.router(vertx);
		router.route().handler(BodyHandler.create());

		StaticRouter.registerGetElementsRouter(router);
		
		server.requestHandler(router).listen(80);
		
	}

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new Server());
	}
}
