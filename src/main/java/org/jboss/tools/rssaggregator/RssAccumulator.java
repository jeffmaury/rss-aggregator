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

import java.util.Date;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;

public class RssAccumulator {
	private int maxEntries;
	private SortedMap<Date, SyndEntry> entries = new ConcurrentSkipListMap<>((date1, date2) -> date2.compareTo(date1));

	public RssAccumulator(int maxEntries) {
		this.maxEntries = maxEntries;
	}

	public RssAccumulator() {
		this(10);
	}

	public void addEntry(SyndEntry entry) {
		Date date = entry.getPublishedDate()!=null?entry.getPublishedDate():entry.getUpdatedDate();
		entries.put(date, entry);
	}

	public SyndFeed getFeed() {
		SyndFeed feed = new SyndFeedImpl();
		feed.setFeedType("atom_1.0");

		feed.setTitle("JBoss Tools Aggregated Feed");
		feed.setDescription("JBoss Tools Aggregated Feed");
		feed.setAuthor("JBoss Tools");
		feed.setLink("http://tools.jboss.org");
		for (Entry<Date, SyndEntry> entry : entries.entrySet()) {
			if (feed.getEntries().size() < maxEntries) {
				feed.getEntries().add(entry.getValue());
			} else {
				break;
			}
		}
		return feed;
	}

	public void merge(SyndFeed feed) {
		feed.getEntries().forEach(item -> addEntry((SyndEntry) item));
	}

}
