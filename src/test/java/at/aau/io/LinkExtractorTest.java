package at.aau.io;

import at.aau.wrapper.DocumentWrapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class LinkExtractorTest {

    DocumentWrapper mockDocument = mock(DocumentWrapper.class);

    @Test
    void testValidateLinks() {
        LinkExtractor linkExtractorMock = spy(new LinkExtractor(mockDocument));
        List<String> links = Arrays.asList("http://example.com/valid", "http://example.com/broken");
        when(linkExtractorMock.isBrokenLink("http://example.com/valid")).thenReturn(false);
        when(linkExtractorMock.isBrokenLink("http://example.com/broken")).thenReturn(true);

        when(mockDocument.extractLinks()).thenReturn(links);

        LinkResults linkResults = linkExtractorMock.validateLinks();

        HashSet<String> expectedValidLinks = new HashSet<>(List.of("http://example.com/valid"));
        HashSet<String> expectedBrokenLinks = new HashSet<>(List.of("http://example.com/broken"));
        assertEquals(expectedValidLinks, linkResults.validLinks);
        assertEquals(expectedBrokenLinks, linkResults.brokenLinks);
    }
}
