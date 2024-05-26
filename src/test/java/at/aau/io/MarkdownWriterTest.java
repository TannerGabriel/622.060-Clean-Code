package at.aau.io;

import at.aau.core.Translator;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MarkdownWriterTest {

    MarkdownWriter markdownWriter = new MarkdownWriter("test.md");
    Translator translator = mock(Translator.class);;
    HashSet<String> validLinks = new HashSet<>();
    HashSet<String> brokenLinks = new HashSet<>();
    LinkResults linkResults = new LinkResults(validLinks, brokenLinks);

    @BeforeEach
    void setUp() {
        markdownWriter.writer = new StringBuilder();
        markdownWriter.translator = translator;
    }

    @Test
    void testAppendCrawlDetails() {
        when(translator.getSourceLanguage(anyString())).thenReturn("english");

        markdownWriter.appendCrawlDetails("https://example.com", 2, "es");

        String expectedOutput = "input: <a href=\"https://example.com\">https://example.com</a>\n" +
                "<br> depth: 2\n" +
                "<br> source language: english\n" +
                "<br> target language: es\n" +
                "<br> summary: \n";
        assertEquals(expectedOutput, markdownWriter.writer.toString());
    }

    @Test
    void testAppendContent() {
        setLinks();

        markdownWriter.appendContent("https://example.com", createHeadings(), linkResults, 1, "es");

        String expectedOutput = "---\n" +
                "Crawled URL: <a href=\"https://example.com\">https://example.com</a>\n" +
                "  # Hello\n" +
                "  ## World\n" +
                "  Valid link: <a href=\"https://example.com/valid\">https://example.com/valid</a>\n" +
                "  Broken link: <a href=\"https://example.org/broken\">https://example.org/broken</a>\n";
        assertEquals(expectedOutput, markdownWriter.writer.toString());
    }

    @Test
    void testAppendHeadingsWithValidLanguage() {
        when(translator.isValidTargetLanguage("es")).thenReturn(true);
        when(translator.translate(eq("Hello"), anyString())).thenReturn("Translated Hello");
        when(translator.translate(eq("World"), anyString())).thenReturn("Translated World");

        markdownWriter.appendHeadings(createHeadings(), 0, "es");

        String expectedOutput = """
                # Translated Hello
                ## Translated World
                """;
        assertEquals(expectedOutput, markdownWriter.writer.toString());
    }

    @Test
    void testAppendHeadingsWithInvalidLanguage() {
        when(translator.isValidTargetLanguage(anyString())).thenReturn(false);

        markdownWriter.appendHeadings(createHeadings(), 0, "invalidLang");

        String expectedOutput = """
                # Hello
                ## World
                """;

        assertEquals(expectedOutput, markdownWriter.writer.toString());
    }

    @Test
    void testAppendLinksValid() {
        linkResults.validLinks.add("https://example.com");
        markdownWriter.appendLinks(linkResults, 0);
        assertEquals("Valid link: <a href=\"https://example.com\">https://example.com</a>\n", markdownWriter.writer.toString());
    }

    @Test
    void testAppendLinksBroken() {
        linkResults.brokenLinks.add("https://example.org");
        markdownWriter.appendLinks(linkResults, 0);
        assertEquals("Broken link: <a href=\"https://example.org\">https://example.org</a>\n", markdownWriter.writer.toString());
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