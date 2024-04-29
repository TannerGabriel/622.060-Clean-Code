package at.aau.utils;

import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CrawlerUtilsTest {

    @Test
    public void testGetHeaderLevel() {
        Element h1 = new Element("h1");
        Element h2 = new Element("h2");
        Element h3 = new Element("h3");

        assertEquals(1, CrawlerUtils.getHeaderLevel(h1));
        assertEquals(2, CrawlerUtils.getHeaderLevel(h2));
        assertEquals(3, CrawlerUtils.getHeaderLevel(h3));
    }

    @Test
    public void testSanitizeURL() {
        assertEquals("https://example.com", CrawlerUtils.sanitizeURL("https://example.com/"));
        assertEquals("https://example.com", CrawlerUtils.sanitizeURL("https://example.com/#fragment"));
        assertEquals("https://example.com", CrawlerUtils.sanitizeURL("https://example.com"));
    }

    @Test
    public void testRemoveTailingSlash() {
        assertEquals("https://example.com", CrawlerUtils.removeTailingSlash("https://example.com/"));
        assertEquals("https://example.com", CrawlerUtils.removeTailingSlash("https://example.com"));
      }

    @Test
    public void testRemoveFragment() {
        assertEquals("https://example.com/", CrawlerUtils.removeFragment("https://example.com/#fragment"));
        assertEquals("https://example.com", CrawlerUtils.removeFragment("https://example.com#fragment"));
        assertEquals("https://example.com/", CrawlerUtils.removeFragment("https://example.com/#"));
        assertEquals("https://example.com", CrawlerUtils.removeFragment("https://example.com"));
    }
}