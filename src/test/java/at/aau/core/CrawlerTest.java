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
        config = new CrawlerConfig("https://google.com", 3, ".*\\.google\\.com.*", "en");
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

        try (MockedConstruction<LinkExtractor> mocked = Mockito.mockConstruction(LinkExtractor.class,
                (mock, context) -> {
                    when(mock.extractLinks()).thenReturn(new ArrayList<>());
                    when(mock.extractHeadings()).thenReturn(new Elements());
                    when(mock.validateLinks(any())).thenReturn(links);
                })) {

            assertDoesNotThrow(() -> crawlerSpy.startCrawling());
            verify(writer).printCrawlDetails("https://google.com", 3, "en");
            verify(crawlerSpy).crawl("https://google.com", 0);
            verify(writer).close();
        }
    }

    @Test
    public void testStartCrawlingWithIOException() throws IOException {
        IOException toThrow = new IOException("Failed to connect");
        doThrow(toThrow).when(crawlerSpy).crawl(anyString(), anyInt());

        crawlerSpy.startCrawling();

        assertTrue(errContent.toString().contains("Error during crawling: Failed to connect"));
    }

    @Test
    public void testCrawlMethodSkipsWhenDepthLimitExceeded() throws IOException {
        crawler.crawl("http://google.com", 4);

        verifyNoInteractions(writer);
    }

    @Test
    public void testCrawlAddsUrlToVisited() throws IOException {
        mockJsoup();
        try (MockedConstruction<LinkExtractor> mocked = Mockito.mockConstruction(LinkExtractor.class,
                (mock, context) -> {
                    when(mock.extractLinks()).thenReturn(new ArrayList<>());
                    when(mock.extractHeadings()).thenReturn(new Elements());
                    when(mock.validateLinks(any())).thenReturn(links);
                })) {
            crawler.crawl("https://google.com", 0);
            assertTrue(crawler.visitedUrls.contains("https://google.com"));
        }
    }


    @Test
    void testDomainMatch() {
        assertTrue(crawler.isDomainMatch("https://service.google.com"));
        assertFalse(crawler.isDomainMatch("https://something.youtube.com"));
    }

    private void mockJsoup() throws IOException {
        Connection mockedConnection = mock(Connection.class);

        when(mockedConnection.get()).thenAnswer(invocationOnMock -> {
            String url = invocationOnMock.getMock().toString();
            switch (url) {
                case "malformed.com":
                    throw new MalformedURLException("Invalid URL");
                case "notfound":
                    throw new IOException("Page not found");
                default:
                    return doc;
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
