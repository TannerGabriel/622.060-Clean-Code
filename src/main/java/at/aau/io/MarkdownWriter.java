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
        try {
            this.writer = new PrintWriter(Files.newBufferedWriter(Paths.get(fileName), StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printCrawlDetails(String startUrl, int depth, String targetLanguage) {
        writer.println("input: <a>" + startUrl + "</a>");
        writer.println("depth: " + depth);
        // TODO: Use real text for source language
        writer.println("source language: " + translator.getSourceLanguage("english"));
        writer.println("target language: " + targetLanguage);
        writer.println("summary: ");
    }

    public void writeContent(Elements headings, LinkResults links, int depth, String targetLang) {
        writeHeadings(headings, depth, targetLang);
        writeLinks(links, depth);
    }

    public void writeHeadings(Elements headings, int depth, String targetLang) {
        String indent = "  ".repeat(depth);

        boolean validTargetLanguage = translator.isValidTargetLanguage(targetLang);
        if (!validTargetLanguage) {
            System.out.println("Target language is invalid. Continuing with source language instead!");
        }

        headings.forEach(heading -> {
            if(validTargetLanguage) {
                String translatedHeading = translator.translate(heading.text(), targetLang);
                writer.println(indent + "#".repeat(CrawlerUtils.getHeaderLevel(heading)) + " " + translatedHeading);
            } else {
                writer.println(indent + "#".repeat(CrawlerUtils.getHeaderLevel(heading)) + " " + heading.text());
            }
        });
    }

    public void writeLinks(LinkResults links, int depth) {
        String indent = "  ".repeat(depth);

        for (String link : links.validLinks) {
            writer.println(indent + "--> link to <a>" + link + "</a>");
        }

        for (String link : links.brokenLinks) {
            writer.println(indent + "--> broken link <a>" + link + "</a>");
        }
    }

    public void close() throws IOException {
        writer.close();
    }
}
