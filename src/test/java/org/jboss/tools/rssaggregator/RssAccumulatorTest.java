package org.jboss.tools.rssaggregator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.jboss.tools.rssaggregator.RssTools.load;

import java.io.IOException;
import org.junit.Test;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;

public class RssAccumulatorTest {

	@Test
	public void testEntriesAreOrdered() throws IOException, FeedException {
		SyndFeed feed = load("rss-jboss.org.xml");
		assertNotNull(feed);
		RssAccumulator accumulator = new RssAccumulator(10);
		for(Object entry : feed.getEntries()) {
			accumulator.addEntry((SyndEntry) entry);
		}
		SyndFeed aggregatedFeed = accumulator.getFeed();
		assertEquals(10, aggregatedFeed.getEntries().size());
		SyndEntry first = (SyndEntry) aggregatedFeed.getEntries().get(0);
		SyndEntry second = (SyndEntry) aggregatedFeed.getEntries().get(1);
		assertTrue(first.getUpdatedDate().getTime() > second.getUpdatedDate().getTime());
	}
	
	@Test
	public void testSizeDontExceeedWithMultipleFeed() throws IOException, FeedException {
		SyndFeed feed = load("rss-jboss.org.xml");
		assertNotNull(feed);
		RssAccumulator accumulator = new RssAccumulator(10);
		for(Object entry : feed.getEntries()) {
			accumulator.addEntry((SyndEntry) entry);
		}
		feed = load("rss-rhdp.xml");
		for(Object entry : feed.getEntries()) {
			accumulator.addEntry((SyndEntry) entry);
		}
		SyndFeed aggregatedFeed = accumulator.getFeed();
		assertEquals(10, aggregatedFeed.getEntries().size());
	}
}
