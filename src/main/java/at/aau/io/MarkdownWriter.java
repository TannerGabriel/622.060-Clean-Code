package at.aau.io;

import at.aau.core.Translator;
import at.aau.utils.CrawlerUtils;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MarkdownWriter {
    private PrintWriter writer;
    private Translator translator;

    public MarkdownWriter(String fileName) {
        this.translator = new Translator();
        initializeWriter(fileName);
    }

    private void initializeWriter(String fileName) {
        try {
            writer = new PrintWriter(Files.newBufferedWriter(Paths.get(fileName), StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("Failed to initialize file writer: " + e.getMessage());
        }
    }

    public void printCrawlDetails(String startUrl, int depth, String targetLanguage) {
        writer.println("input: <a href=\"" + startUrl + "\">" + startUrl + "</a>");
        writer.println("<br> depth: " + depth);
        // TODO: Use real text for source language
        writer.println("<br> source language: " + translator.getSourceLanguage("english"));
        writer.println("<br> target language: " + targetLanguage);
        writer.println("<br> summary: ");
    }

    public void writeContent(String url, Elements headings, LinkResults links, int depth, String targetLang) {
        writer.println("---");
        writer.println("Crawled URL: <a href=\"" + url + "\">" + url + "</a>");
        writeHeadings(headings, depth, targetLang);
        writeLinks(links, depth);
    }

    public void writeHeadings(Elements headings, int depth, String targetLang) {
        String indentation = "  ".repeat(depth);
        boolean isValidLanguage = translator.isValidTargetLanguage(targetLang);

        if (!isValidLanguage) {
            System.out.println("Target language is invalid. Continuing with source language instead!");
        }

        headings.forEach(heading -> {
            String headingText = isValidLanguage ?
                    translator.translate(heading.text(), targetLang) :
                    heading.text();
            writer.println(indentation + "#".repeat(CrawlerUtils.getHeaderLevel(heading)) + " " + headingText);
        });
    }

    public void writeLinks(LinkResults links, int depth) {
        String indentation = createIndentation(depth);
        links.validLinks.forEach(link -> writer.println(indentation + "Valid link: <a href=\"" + link + "\">" + link + "</a>"));
        links.brokenLinks.forEach(link -> writer.println(indentation + "Broken link: <a href=\"" + link + "\">" + link + "</a>"));
    }

    public void close() throws IOException {
        writer.close();
    }

    private String createIndentation(int depth) {
        return "  ".repeat(depth);
    }
}