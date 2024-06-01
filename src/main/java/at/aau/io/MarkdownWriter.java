package at.aau.io;

import at.aau.core.Translator;
import at.aau.utils.CrawlerUtils;
import at.aau.utils.Logger;
import org.jsoup.select.Elements;

public class MarkdownWriter {
    private static final Logger logger = Logger.getInstance();
    protected StringBuilder writer;
    protected Translator translator;

    public MarkdownWriter(String fileName) {
        this.translator = new Translator();
        this.writer = new StringBuilder();
    }

    public void appendCrawlDetails(String startUrl, int depth, String targetLanguage){
        writer.append("input: <a href=\"").append(startUrl).append("\">").append(startUrl).append("</a>\n");
        writer.append("<br> depth: ").append(depth).append("\n");
        writer.append("<br> source language: ").append(translator.getSourceLanguage(startUrl)).append("\n");
        writer.append("<br> target language: ").append(targetLanguage).append("\n");
        writer.append("<br> summary: \n");
    }

    public void appendContent(String url, Elements headings, LinkResults links, int depth, String targetLang) {
        writer.append("---\n");
        writer.append("Crawled URL: <a href=\"").append(url).append("\">").append(url).append("</a>\n");
        appendHeadings(headings, depth, targetLang);
        appendLinks(links, depth);
    }

    protected void appendHeadings(Elements headings, int depth, String targetLang) {
        String indentation = createIndentation(depth);
        boolean isValidLanguage = translator.isValidTargetLanguage(targetLang);

        if (!isValidLanguage) {
            logger.logError("Target language is invalid. Continuing with source language instead!");
        }

        headings.forEach(heading -> {
            String headingText = isValidLanguage ?
                    translator.translate(heading.text(), targetLang) :
                    heading.text();
            writer.append(indentation).append("#".repeat(CrawlerUtils.getHeaderLevel(heading))).append(" ").append(headingText).append("\n");
        });
    }

    protected void appendLinks(LinkResults links, int depth) {
        String indentation = createIndentation(depth);
        links.validLinks.forEach(link -> writer.append(indentation).append("Valid link: <a href=\"").append(link).append("\">").append(link).append("</a>\n"));
        links.brokenLinks.forEach(link -> writer.append(indentation).append("Broken link: <a href=\"").append(link).append("\">").append(link).append("</a>\n"));
    }

    protected String createIndentation(int depth) {
        return "  ".repeat(depth);
    }

    public String getOutput() {
        return writer.toString();
    }
}
