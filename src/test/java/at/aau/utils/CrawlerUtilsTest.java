package at.aau.utils;

import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CrawlerUtilsTest {

    @Test
    void testGetHeaderLevel() {
        String h1 = "<h1></h1>";
        String h2 = "<h2></h2>";
        String h3 = "<h3></h3>";

        assertEquals(1, CrawlerUtils.getHeaderLevel(h1));
        assertEquals(2, CrawlerUtils.getHeaderLevel(h2));
        assertEquals(3, CrawlerUtils.getHeaderLevel(h3));
    }

    @Test
    void testSanitizeURL() {
        assertEquals("https://example.com", CrawlerUtils.sanitizeURL("https://example.com/"));
        assertEquals("https://example.com", CrawlerUtils.sanitizeURL("https://example.com/#fragment"));
        assertEquals("https://example.com", CrawlerUtils.sanitizeURL("https://example.com"));
    }

    @Test
    void testRemoveTailingSlash() {
        assertEquals("https://example.com", CrawlerUtils.removeTailingSlash("https://example.com/"));
        assertEquals("https://example.com", CrawlerUtils.removeTailingSlash("https://example.com"));
      }

    @Test
    void testRemoveFragment() {
        assertEquals("https://example.com/", CrawlerUtils.removeFragment("https://example.com/#fragment"));
        assertEquals("https://example.com", CrawlerUtils.removeFragment("https://example.com#fragment"));
        assertEquals("https://example.com/", CrawlerUtils.removeFragment("https://example.com/#"));
        assertEquals("https://example.com", CrawlerUtils.removeFragment("https://example.com"));
    }
}