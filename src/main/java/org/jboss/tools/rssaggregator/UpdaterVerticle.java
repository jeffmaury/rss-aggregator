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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;

import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.client.WebClient;
import io.vertx.reactivex.RxHelper;

/**
 * @author Jeff MAURY
 *
 */
public class UpdaterVerticle extends AbstractVerticle {
	private static final Logger LOG = LoggerFactory.getLogger(UpdaterVerticle.class);

	private Disposable subscription;

	private String[] urls;

	private WebClient webClient;

	@Override
	public void start() throws Exception {
		LOG.info("Starting " + UpdaterVerticle.class + " verticle");
		urls = config().getString(Constants.URLS_KEY, "").split(",");
		webClient = WebClient.create(vertx);
		Scheduler scheduler = RxHelper.scheduler(vertx);
		Flowable<Long> flowable = Flowable.interval(0, 1, TimeUnit.HOURS, scheduler);
		subscription = flowable.subscribe(id -> {
			LOG.info("Updating aggregated RSS feed");
			aggregate();
		});
	}

	private void aggregate() {
		Set<Single<SyndFeed>> singles = Stream.of(urls).map(url -> url.trim()).filter(url -> url.length() > 0)
				.map(url -> Single.<SyndFeed>create(emitter -> {
					LOG.info("Retrieving RSS from " + url);
					webClient.getAbs(url).send(ar -> {
						if (ar.succeeded()) {
							try (Reader reader = new StringReader(ar.result().bodyAsString())) {
								emitter.onSuccess(new SyndFeedInput().build(reader));
							} catch (IOException | FeedException e) {
								LOG.error("Request for url " + url + " failed");
								emitter.onError(e);
							}
						} else {
							LOG.error("Request for url " + url + " failed");
							emitter.onError(ar.cause());
						}
					});

				})).collect(Collectors.toSet());
		if (!singles.isEmpty()) {
			Single.zip(singles, feeds -> {
				RssAccumulator accumulator = new RssAccumulator();
				for (Object feed : feeds) {
					accumulator.merge((SyndFeed) feed);
				}
				return accumulator;
			}).subscribe(accumulator -> {
				vertx.eventBus().publish(Constants.FEED_UPDATE_EVENT,
						new SyndFeedOutput().outputString(accumulator.getFeed(), false));
			});
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.vertx.core.AbstractVerticle#stop()
	 */
	@Override
	public void stop() throws Exception {
		LOG.info("Stopping " + UpdaterVerticle.class + " verticle");
		subscription.dispose();
	}
}
