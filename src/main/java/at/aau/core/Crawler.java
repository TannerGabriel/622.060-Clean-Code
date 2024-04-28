package at.aau.core;

import at.aau.io.LinkExtractor;
import at.aau.io.LinkResults;
import at.aau.io.MarkdownWriter;
import at.aau.utils.CrawlerUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.regex.Pattern;

public class Crawler {
    private HashSet<String> visitedUrls = new HashSet<>();
    private CrawlerConfig config;
    private MarkdownWriter writer;

    public Crawler(CrawlerConfig config) {
        this.config = config;
        this.writer = new MarkdownWriter("output.md");
    }

    public void startCrawling() {
        try {
            writer.printCrawlDetails(config.getStartUrl(), config.getDepthLimit(), config.getTargetLang());
            crawl(config.getStartUrl(), 0);
            writer.close();
        } catch (IOException e) {
            System.err.println("Error during crawling: " + e.getMessage());
        }
    }

    private void crawl(String url, int depth) throws IOException {
        url = CrawlerUtils.removeFragment(url);
        if (depth > config.getDepthLimit() || !visitedUrls.add(url)) {
            return;
        }

        Document doc = Jsoup.connect(url).get();
        LinkExtractor extractor = new LinkExtractor(doc);
        LinkResults links = extractor.validateLinks(extractor.extractLinks());
        Elements headings = extractor.extractHeadings();

        if (isDomainMatch(url)) {
            writer.writeContent(url, headings, links, depth, config.getTargetLang());
            links.validLinks.forEach(link -> crawlIfNotVisited(link, depth));
        }
    }

    private void crawlIfNotVisited(String url, int depth) {
        if (!visitedUrls.contains(url)) {
            try {
                crawl(url, depth + 1);
            } catch (IOException e) {
                System.err.println("Failed to crawl " + url + ": " + e.getMessage());
            }
        }
    }

    private boolean isDomainMatch(String url) {
        return Pattern.matches(config.getDomainFilter(), url);
    }
}