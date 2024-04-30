package at.aau.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CrawlerConfigTest {

    @Test
    void testCrawlerConfigConstructor() {
        String startUrl = "https://example.com";
        int depthLimit = 5;
        String domainFilter = "example.com";
        String targetLang = "en";

        CrawlerConfig config = new CrawlerConfig(startUrl, depthLimit, domainFilter, targetLang);

        assertEquals(startUrl, config.getStartUrl());
        assertEquals(depthLimit, config.getDepthLimit());
        assertEquals(domainFilter, config.getDomainFilter());
        assertEquals(targetLang, config.getTargetLang());
    }
}