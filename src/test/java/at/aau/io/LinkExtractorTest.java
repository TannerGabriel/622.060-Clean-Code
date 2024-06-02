package at.aau.io;

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

    Document mockDocument = mock(Document.class);
    LinkExtractor linkExtractor = new LinkExtractor(mockDocument);

    @Test
    void testValidateLinks() {
        LinkExtractor linkExtractorMock = mock(LinkExtractor.class,CALLS_REAL_METHODS);
        List<String> links = Arrays.asList("http://example.com/valid", "http://example.com/broken");
        when(linkExtractorMock.isBrokenLink("http://example.com/valid")).thenReturn(false);
        when(linkExtractorMock.isBrokenLink("http://example.com/broken")).thenReturn(true);

        LinkResults linkResults = linkExtractorMock.validateLinks(links);

        HashSet<String> expectedValidLinks = new HashSet<>(Arrays.asList("http://example.com/valid"));
        HashSet<String> expectedBrokenLinks = new HashSet<>(Arrays.asList("http://example.com/broken"));
        assertEquals(expectedValidLinks, linkResults.validLinks);
        assertEquals(expectedBrokenLinks, linkResults.brokenLinks);
    }


    @Test
    void testExtractHeadings() {
        when(mockDocument.select("h1, h2, h3, h4, h5, h6")).thenReturn(createHeadings());

        Elements headings = linkExtractor.extractHeadings();

        assertEquals(createHeadings().toString(), headings.toString());
    }

    private Elements createHeadings(){
        Elements headings = new Elements();
        headings.add(new Element("h1").text("Hello"));
        headings.add(new Element("h2").text("World"));
        return headings;
    }

}
