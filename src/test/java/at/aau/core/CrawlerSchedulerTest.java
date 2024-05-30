package at.aau.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class CrawlerSchedulerTest {
    private SchedulerConfig mockConfig;
    private Crawler mockCrawler;
    private CrawlerScheduler crawlerScheduler;
    private PrintWriter mockWriter;

    @BeforeEach
    public void setUp() {
        mockConfig = mock(SchedulerConfig.class);
        when(mockConfig.urls()).thenReturn(new String[]{"http://example.com"});
        when(mockConfig.depthLimit()).thenReturn(2);
        when(mockConfig.domainFilter()).thenReturn("example.com");
        when(mockConfig.targetLang()).thenReturn("en");

        mockCrawler = mock(Crawler.class);
        when(mockCrawler.getOutput()).thenReturn("Crawler Output");

        mockWriter = mock(PrintWriter.class);

        crawlerScheduler = new CrawlerScheduler(mockConfig) {
            @Override
            protected void addCrawlerFromConfig(SchedulerConfig config) {
                crawlers.add(mockCrawler);
            }

            @Override
            protected PrintWriter createPrintWriter(Path path) throws IOException {
                return mockWriter;
            }
        };
    }

    @Test
    public void testStartCrawlers() {
        crawlerScheduler.startCrawlers();

        verify(mockCrawler, times(1)).start();
        assertEquals("Crawler Output\n\n", crawlerScheduler.getOutputFromCrawlers());
    }

    @Test
    public void testAddCrawlerFromConfig() {
        SchedulerConfig config = new SchedulerConfig(new String[]{"http://example.com"}, 2, "example.com", "en");
        CrawlerScheduler scheduler = new CrawlerScheduler(config);
        assertEquals(1, scheduler.crawlers.size());
    }

    @Test
    public void testGetOutputFromCrawlers() {
        assertEquals("Crawler Output\n\n", crawlerScheduler.getOutputFromCrawlers());
    }

    @Test
    public void testPrintOutput() throws IOException {
        String expectedContent = "Test Content";

        crawlerScheduler.printOutput(expectedContent);

        verify(mockWriter, times(1)).println(expectedContent);
        verify(mockWriter, times(1)).flush();
        verify(mockWriter, times(1)).close();
    }
}
