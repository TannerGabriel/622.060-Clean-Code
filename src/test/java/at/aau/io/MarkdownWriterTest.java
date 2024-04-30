package at.aau.io;

import at.aau.core.Translator;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MarkdownWriterTest {

    MarkdownWriter markdownWriter = new MarkdownWriter("test.md");
    StringWriter stringWriter = new StringWriter();
    Translator translator = mock(Translator.class);;
    HashSet<String> validLinks = new HashSet<>();
    HashSet<String> brokenLinks = new HashSet<>();
    LinkResults linkResults = new LinkResults(validLinks, brokenLinks);

    @BeforeEach
    void setUp() {
        markdownWriter.writer = new PrintWriter(stringWriter);
        markdownWriter.translator = translator;
    }

    @Test
    void testPrintCrawlDetails() {
        when(translator.getSourceLanguage(anyString())).thenReturn("english");

        markdownWriter.printCrawlDetails("https://example.com", 2, "es");

        String expectedOutput = "input: <a href=\"https://example.com\">https://example.com</a>\n" +
                "<br> depth: 2\n" +
                "<br> source language: english\n" +
                "<br> target language: es\n" +
                "<br> summary: \n";
        assertEquals(expectedOutput, stringWriter.toString());
    }

    @Test
    void testWriteContent() {
        setLinks();

        markdownWriter.writeContent("https://example.com", createHeadings(), linkResults, 1, "es");

        String expectedOutput = "---\n" +
                "Crawled URL: <a href=\"https://example.com\">https://example.com</a>\n" +
                "  # Hello\n" +
                "  ## World\n" +
                "  Valid link: <a href=\"https://example.com/valid\">https://example.com/valid</a>\n" +
                "  Broken link: <a href=\"https://example.org/broken\">https://example.org/broken</a>\n";
        assertEquals(expectedOutput, stringWriter.toString());
    }

    @Test
    void testWriteHeadingsWithValidLanguage() {
        when(translator.isValidTargetLanguage("es")).thenReturn(true);
        when(translator.translate(eq("Hello"), anyString())).thenReturn("Translated Hello");
        when(translator.translate(eq("World"), anyString())).thenReturn("Translated World");

        markdownWriter.writeHeadings(createHeadings(), 0, "es");

        String expectedOutput = """
                # Translated Hello
                ## Translated World
                """;
        assertEquals(expectedOutput, stringWriter.toString());
    }

    @Test
    void testWriteHeadingsWithInvalidLanguage() {
        when(translator.isValidTargetLanguage(anyString())).thenReturn(false);

        markdownWriter.writeHeadings(createHeadings(), 0, "invalidLang");

        String expectedOutput = """
                # Hello
                ## World
                """;

        assertEquals(expectedOutput, stringWriter.toString());
    }

    @Test
    void testWriteLinksValid() {
        linkResults.validLinks.add("https://example.com");
        markdownWriter.writeLinks(linkResults, 0);
        assertEquals("Valid link: <a href=\"https://example.com\">https://example.com</a>\n", stringWriter.toString());
    }

    @Test
    void testWriteLinksBroken() {
        linkResults.brokenLinks.add("https://example.org");
        markdownWriter.writeLinks(linkResults, 0);
        assertEquals("Broken link: <a href=\"https://example.org\">https://example.org</a>\n", stringWriter.toString());
    }

    @Test
    void testClose() throws IOException {
        PrintWriter mockPrintWriter = mock(PrintWriter.class);
        MarkdownWriter mdw = new MarkdownWriter("test.md");
        mdw.writer = mockPrintWriter;

        mdw.close();

        verify(mockPrintWriter).close();
    }

    @Test
    void testCreateIndentation() {
        assertEquals("", markdownWriter.createIndentation(0));
        assertEquals("  ", markdownWriter.createIndentation(1));
        assertEquals("    ", markdownWriter.createIndentation(2));
    }

    private Elements createHeadings(){
        Elements headings = new Elements();
        headings.add(new Element("h1").text("Hello"));
        headings.add(new Element("h2").text("World"));
        return headings;
    }

    private void setLinks(){
        linkResults.validLinks.add("https://example.com/valid");
        linkResults.brokenLinks.add("https://example.org/broken");
    }
}