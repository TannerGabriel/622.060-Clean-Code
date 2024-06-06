package at.aau.io;

import at.aau.wrapper.DocumentWrapper;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class LinkExtractorTest {

    DocumentWrapper mockDocument = mock(DocumentWrapper.class);
    LinkExtractor linkExtractor = new LinkExtractor(mockDocument);

    @Test
    void testValidateLinks() {
        LinkExtractor linkExtractorMock = mock(LinkExtractor.class,CALLS_REAL_METHODS);
        List<String> links = Arrays.asList("http://example.com/valid", "http://example.com/broken");
        when(linkExtractorMock.isBrokenLink("http://example.com/valid")).thenReturn(false);
        when(linkExtractorMock.isBrokenLink("http://example.com/broken")).thenReturn(true);


        when(mockDocument.extractLinks()).thenReturn(links);

        LinkResults linkResults = linkExtractorMock.validateLinks();

        HashSet<String> expectedValidLinks = new HashSet<>(Arrays.asList("http://example.com/valid"));
        HashSet<String> expectedBrokenLinks = new HashSet<>(Arrays.asList("http://example.com/broken"));
        assertEquals(expectedValidLinks, linkResults.validLinks);
        assertEquals(expectedBrokenLinks, linkResults.brokenLinks);
    }

    private Elements createHeadings(){
        Elements headings = new Elements();
        headings.add(new Element("h1").text("Hello"));
        headings.add(new Element("h2").text("World"));
        return headings;
    }

}
