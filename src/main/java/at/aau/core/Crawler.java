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

public class Crawler extends Thread {
    HashSet<String> visitedUrls = new HashSet<>();
    private CrawlerConfig config;
    private MarkdownWriter writer;

    public Crawler(CrawlerConfig config) {
        this.config = config;
        this.writer = new MarkdownWriter("output.md");
    }

    public Crawler(CrawlerConfig config, MarkdownWriter writer) {
        this.config = config;
        this.writer = writer;
    }

    @Override
    public void run() {
        super.run();
        startCrawling();
    }

    public void startCrawling() {
        try {
            writer.appendCrawlDetails(config.startUrl(), config.depthLimit(), config.targetLang());
            crawl(config.startUrl(), 0);
        } catch (IOException e) {
            System.err.println("Error during crawling: " + e.getMessage());
        }
    }

    protected void crawl(String url, int depth) throws IOException {
        url = CrawlerUtils.removeFragment(url);
        if (depth > config.depthLimit() || !visitedUrls.add(url)) {
            return;
        }

        Document doc = Jsoup.connect(url).get();
        LinkExtractor extractor = new LinkExtractor(doc);
        LinkResults links = extractor.validateLinks(extractor.extractLinks());
        Elements headings = extractor.extractHeadings();

        if (isDomainMatch(url)) {
            writer.appendContent(url, headings, links, depth, config.targetLang());
            links.validLinks.forEach(link -> crawlIfNotVisited(link, depth));
        }
    }

    protected void crawlIfNotVisited(String url, int depth) {
        if (!visitedUrls.contains(url)) {
            try {
                crawl(url, depth + 1);
            } catch (IOException e) {
                System.err.println("Failed to crawl " + url + ": " + e.getMessage());
            }
        }
    }

    protected boolean isDomainMatch(String url) {
        return Pattern.matches(config.domainFilter(), url);
    }

    public String getOutput() {
        return writer.getOutput();
    }
}
