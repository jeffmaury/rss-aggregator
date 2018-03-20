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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.jboss.tools.rssaggregator.RssTools.load;

import java.io.IOException;
import org.junit.Test;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;

public class RssFeedTest {

	@Test
	public void testJbossOrgfeed() throws IOException, FeedException {
		SyndFeed feed = load("rss-jboss.org.xml");
		assertNotNull(feed);
		assertEquals("atom_1.0", feed.getFeedType());
		assertEquals("JBoss Developer Recent Posts", feed.getTitle());
		assertNotNull(feed.getPublishedDate());
		assertEquals(20, feed.getEntries().size());

	}

	@Test
	public void testToolsJbossOrgfeed() throws IOException, FeedException {
		SyndFeed feed = load("rss-tools.jboss.org.xml");
		assertNotNull(feed);
		assertEquals("atom_1.0", feed.getFeedType());
		assertEquals("JBoss Tools", feed.getTitle());
		assertNotNull(feed.getPublishedDate());
		assertEquals(50, feed.getEntries().size());

	}

	@Test
	public void testDevelopersRedhatComfeed() throws IOException, FeedException {
		SyndFeed feed = load("rss-rhdp.xml");
		assertNotNull(feed);
		assertEquals("rss_2.0", feed.getFeedType());
		assertEquals("RHD Blog", feed.getTitle());
		assertNotNull(feed.getPublishedDate());
		assertEquals(10, feed.getEntries().size());

	}
}
