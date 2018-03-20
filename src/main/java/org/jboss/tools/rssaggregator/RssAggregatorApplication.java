/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.rssaggregator;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.config.ConfigRetriever;

public class RssAggregatorApplication extends AbstractVerticle {

	private static final Logger LOG = LoggerFactory.getLogger(RssAggregatorApplication.class);

	private String deploymentId;

	private String rssPayload;

	@Override
	public void start(Future<Void> future) {
		// Create a router object.
		Router router = Router.router(vertx);

		router.get("/feed").handler(this::feed);
		router.get("/*").handler(StaticHandler.create());

		ConfigRetriever configurationRetriever = ConfigRetriever.create(vertx);
		configurationRetriever.listen(conf -> {
			if (deploymentId != null) {
				vertx.undeploy(deploymentId);
				deployUpdaterVerticle(conf.getNewConfiguration());
			}
		});

		configurationRetriever.getConfig(ar -> {
			JsonObject configuration = ar.result();
			deployUpdaterVerticle(configuration);
		});

		vertx.eventBus().consumer(Constants.FEED_UPDATE_EVENT, msg -> {
			LOG.info("Received an RSS update event ");
			rssPayload = (String) msg.body();
		});
		// Create the HTTP server and pass the "accept" method to the request handler.
		vertx.createHttpServer().requestHandler(router::accept).listen(
				// Retrieve the port from the configuration, default to 8080.
				config().getInteger("http.port", 8080), ar -> {
					if (ar.succeeded()) {
						LOG.info("Server started on port " + ar.result().actualPort());
					}
					future.handle(ar.mapEmpty());
				});

	}

	private void deployUpdaterVerticle(JsonObject conf) {
		vertx.deployVerticle(UpdaterVerticle.class, new DeploymentOptions().setConfig(conf), deployed -> {
			deploymentId = deployed.result();
		});
	}

	private void feed(RoutingContext rc) {
		if (rssPayload != null) {
			rc.response().putHeader(CONTENT_TYPE, "application/xml; charset=utf-8").end(rssPayload);
		} else {
			rc.response().setStatusCode(404);
		}
	}

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new RssAggregatorApplication());
	}
}
