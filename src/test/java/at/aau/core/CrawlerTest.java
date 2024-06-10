package at.aau.core;

import at.aau.io.Heading;
import at.aau.io.LinkExtractor;
import at.aau.io.LinkResults;
import at.aau.io.MarkdownWriter;
import at.aau.utils.Logger;
import at.aau.wrapper.DocumentWrapperImpl;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CrawlerTest {
    private static final Logger logger = Logger.getInstance();
    private Crawler crawler;
    private CrawlerConfig config;
    private Crawler crawlerSpy;

    private final PrintStream originalErr = System.err;

    @Mock
    private MarkdownWriter writer;
    @Mock
    private Document doc;
    @Mock
    private LinkResults links;

    MockedStatic<Jsoup> mockedJsoup;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        config = new CrawlerConfig("https://google.com", 3, "^https?://([\\w\\d]+\\.)?google\\.com$", "en");
        crawler = new Crawler(config, writer);
        crawlerSpy = spy(crawler);
    }

    @AfterEach
    void teardown() {
        System.setErr(originalErr);

        if (mockedJsoup != null)
            mockedJsoup.close();
    }

    @Test
    void testStartCrawlingSuccessful() throws IOException {
        mockJsoup();
        doNothing().when(writer).appendCrawlDetails(anyString(), anyInt());
        doNothing().when(crawlerSpy).crawl(anyString(), anyInt());

        assertDoesNotThrow(() -> crawlerSpy.startCrawling());
        verify(writer).appendCrawlDetails("https://google.com", 3);
        verify(crawlerSpy).crawl("https://google.com", 0);
    }

    @Test
    void testStartCrawlingFailedWithIOException() throws IOException {
        IOException toThrow = new IOException("Failed to connect");
        doThrow(toThrow).when(crawlerSpy).crawl(anyString(), anyInt());

        crawlerSpy.startCrawling();

        assertTrue(logger.getLogsString().contains("Error during crawling: Failed to connect"));
    }

    @Test
    void testCrawlMethodSkipsWhenDepthLimitExceeded() throws IOException {
        crawler.crawl("https://google.com", 4);

        verifyNoInteractions(writer);
    }

    @Test
    void testCrawlMethodSkipsAlreadyVisitedURL() throws IOException {
        crawler.visitedUrls.add("https://google.com");

        crawler.crawl("https://google.com", 0);

        verifyNoInteractions(writer);
    }

    @Test
    void testCrawlAddsUrlToVisited() throws IOException {
        mockJsoup();
        when(crawlerSpy.isDomainMatch(anyString())).thenReturn(false);
        try (MockedConstruction<LinkExtractor> mocked = Mockito.mockConstruction(LinkExtractor.class,
                (mock, context) -> {
                    when(mock.validateLinks()).thenReturn(links);
                })) {

            try (MockedConstruction<DocumentWrapperImpl> mockedDw = Mockito.mockConstruction(DocumentWrapperImpl.class,
                    (mock, context) -> {
                        when(mock.extractLinks()).thenReturn(new ArrayList<>());
                        when(mock.extractHeadings()).thenReturn(new Heading[]{});
                    })) {
                crawlerSpy.crawl("https://google.com", 0);
                assertTrue(crawlerSpy.visitedUrls.contains("https://google.com"));
            }
            crawlerSpy.crawl("https://google.com", 0);
            assertTrue(crawlerSpy.visitedUrls.contains("https://google.com"));
        }
    }

    @Test
    void testCrawlWritesContent() throws IOException {
        mockJsoup();
        doNothing().when(writer).appendContent(anyString(), any(), any(), anyInt());
        doNothing().when(crawlerSpy).crawlIfNotVisited(anyString(), anyInt());

        crawlerSpy.crawl("https://google.com", 0);

        verify(writer).appendContent(eq("https://google.com"), any(Heading[].class), any(LinkResults.class), eq(0));
    }

    @Test
    void testCrawlIfNotVisitedSuccessful() throws IOException {
        doNothing().when(crawlerSpy).crawl(anyString(), anyInt());

        crawlerSpy.crawlIfNotVisited("https://google.at", 0);

        verify(crawlerSpy).crawl("https://google.at", 1);
    }

    @Test
    void testCrawlIfNotVisitedFailedToCrawl() throws IOException {
        IOException toThrow = new IOException("Failed to connect");
        doThrow(toThrow).when(crawlerSpy).crawl(anyString(), anyInt());

        crawlerSpy.crawlIfNotVisited("https://google.com", 0);

        assertTrue(logger.getLogsString().contains("Failed to crawl https://google.com: Failed to connect"));
    }

    @Test
    void testCrawlIfNotVisitedFailedVisited() throws IOException {
        crawlerSpy.visitedUrls.add("https://google.com");

        crawlerSpy.crawlIfNotVisited("https://google.com", 0);

        verify(crawlerSpy, never()).crawl("https://google.com", 1);
    }


    @Test
    void testDomainMatch() {
        assertTrue(crawler.isDomainMatch("https://google.com"));
        assertTrue(crawler.isDomainMatch("https://service.google.com"));
        assertFalse(crawler.isDomainMatch("https://something.youtube.com"));
    }

    @Test
    void testConstructor() {
        Crawler crawler1 = new Crawler(config);
        assertNotNull(crawler1, "Crawler should be successfully instantiated.");
    }

    private void mockJsoup() throws IOException {
        String htmlContent = "<!DOCTYPE html><html><head><title>Test Page</title></head>"
                + "<body><h1>First Heading</h1><h2>Second Heading</h2><a href='https://example.com'>Example Link</a></body></html>";
        Document document = Jsoup.parse(htmlContent);

        Connection mockedConnection = mock(Connection.class);

        when(mockedConnection.get()).thenAnswer(invocationOnMock -> {
            String url = invocationOnMock.getMock().toString();
            switch (url) {
                case "malformed.com":
                    throw new MalformedURLException("Invalid URL");
                case "notfound":
                    throw new IOException("Page not found");
                default:
                    return document;
            }
        });

        mockedJsoup = mockStatic(Jsoup.class);
        mockedJsoup.when(() -> Jsoup.connect(anyString())).thenAnswer(invocationOnMock -> {
            String url = invocationOnMock.getArgument(0);
            when(mockedConnection.toString()).thenReturn(url);
            return mockedConnection;
        });
    }
}
