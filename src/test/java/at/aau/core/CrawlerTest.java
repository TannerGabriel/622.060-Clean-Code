package at.aau.core;

import at.aau.io.LinkExtractor;
import at.aau.io.LinkResults;
import at.aau.io.MarkdownWriter;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CrawlerTest {
    private Crawler crawler;
    private CrawlerConfig config;
    private Crawler crawlerSpy;

    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
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
        MockitoAnnotations.initMocks(this);
        config = new CrawlerConfig("https://google.com", 3, "^https?://([\\w\\d]+\\.)?google\\.com$", "en");
        crawler = new Crawler(config, writer);
        crawlerSpy = spy(crawler);
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    public void teardown() {
        System.setErr(originalErr);

        if (mockedJsoup != null)
            mockedJsoup.close();
    }

    @Test
    public void testStartCrawlingSuccessful() throws IOException {
        mockJsoup();
        doNothing().when(writer).printCrawlDetails(anyString(), anyInt(), anyString());
        doNothing().when(writer).close();
        doNothing().when(crawlerSpy).crawl(anyString(), anyInt());

        assertDoesNotThrow(() -> crawlerSpy.startCrawling());
        verify(writer).printCrawlDetails("https://google.com", 3, "en");
        verify(crawlerSpy).crawl("https://google.com", 0);
        verify(writer).close();
    }

    @Test
    public void testStartCrawlingFailedWithIOException() throws IOException {
        IOException toThrow = new IOException("Failed to connect");
        doThrow(toThrow).when(crawlerSpy).crawl(anyString(), anyInt());

        crawlerSpy.startCrawling();

        assertTrue(errContent.toString().contains("Error during crawling: Failed to connect"));
    }

    @Test
    public void testCrawlMethodSkipsWhenDepthLimitExceeded() throws IOException {
        crawler.crawl("https://google.com", 4);

        verifyNoInteractions(writer);
    }

    @Test
    public void testCrawlMethodSkipsAlreadyVisitedURL() throws IOException {
        crawler.visitedUrls.add("https://google.com");

        crawler.crawl("https://google.com", 0);

        verifyNoInteractions(writer);
    }

    @Test
    public void testCrawlAddsUrlToVisited() throws IOException {
        mockJsoup();
        when(crawlerSpy.isDomainMatch(anyString())).thenReturn(false);
        try (MockedConstruction<LinkExtractor> mocked = Mockito.mockConstruction(LinkExtractor.class,
                (mock, context) -> {
                    when(mock.extractLinks()).thenReturn(new ArrayList<>());
                    when(mock.extractHeadings()).thenReturn(new Elements());
                    when(mock.validateLinks(any())).thenReturn(links);
                })) {
            crawlerSpy.crawl("https://google.com", 0);
            assertTrue(crawlerSpy.visitedUrls.contains("https://google.com"));
        }
    }

    @Test
    public void testCrawlWritesContent() throws IOException {
        mockJsoup();
        doNothing().when(writer).writeContent(anyString(), any(), any(), anyInt(), anyString());
        doNothing().when(crawlerSpy).crawlIfNotVisited(anyString(), anyInt());

        crawlerSpy.crawl("https://google.com", 0);

        verify(writer).writeContent(eq("https://google.com"), any(Elements.class), any(LinkResults.class), eq(0), eq("en"));
    }

    @Test
    public void testCrawlIfNotVisitedSuccessful() throws IOException {
        doNothing().when(crawlerSpy).crawl(anyString(), anyInt());

        crawlerSpy.crawlIfNotVisited("https://google.at", 0);

        verify(crawlerSpy).crawl("https://google.at", 1);
    }

    @Test
    public void testCrawlIfNotVisitedFailedToCrawl() throws IOException {
        IOException toThrow = new IOException("Failed to connect");
        doThrow(toThrow).when(crawlerSpy).crawl(anyString(), anyInt());

        crawlerSpy.crawlIfNotVisited("https://google.com", 0);

        assertTrue(errContent.toString().contains("Failed to crawl https://google.com: Failed to connect"));
    }

    @Test
    public void testCrawlIfNotVisitedFailedVisited() throws IOException {
        crawlerSpy.visitedUrls.add("https://google.com");

        crawlerSpy.crawlIfNotVisited("https://google.com", 0);

        verify(crawlerSpy, never()).crawl("https://google.com", 1);
    }


    @Test
    public void testDomainMatch() {
        assertTrue(crawler.isDomainMatch("https://google.com"));
        assertTrue(crawler.isDomainMatch("https://service.google.com"));
        assertFalse(crawler.isDomainMatch("https://something.youtube.com"));
    }

    @Test
    public void testConstructor() {
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
