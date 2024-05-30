package at.aau.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SchedulerConfigTest {
    @Test
    void testSchedulerConfigConstructor() {
        String[] startUrl = {"https://example.com", "https://test.com"};
        int depthLimit = 5;
        String domainFilter = "example.com";
        String targetLang = "en";

        SchedulerConfig config = new SchedulerConfig(startUrl, depthLimit, domainFilter, targetLang);

        assertEquals(startUrl, config.urls());
        assertEquals(depthLimit, config.depthLimit());
        assertEquals(domainFilter, config.domainFilter());
        assertEquals(targetLang, config.targetLang());
    }
}
