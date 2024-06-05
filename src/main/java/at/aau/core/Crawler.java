package at.aau.core;

import at.aau.io.Heading;
import at.aau.io.LinkExtractor;
import at.aau.io.LinkResults;
import at.aau.io.MarkdownWriter;
import at.aau.utils.CrawlerUtils;
import at.aau.utils.Logger;

import at.aau.wrapper.DocumentWrapper;
import at.aau.wrapper.WebCrawler;
import at.aau.wrapper.WebCrawlerImpl;

import java.io.IOException;
import java.util.HashSet;
import java.util.regex.Pattern;

public class Crawler extends Thread {
    private final Logger logger = Logger.getInstance();
    HashSet<String> visitedUrls = new HashSet<>();
    private CrawlerConfig config;
    private MarkdownWriter writer;

    private final WebCrawler webCrawler = new WebCrawlerImpl();

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
            logger.logError("Error during crawling: " + e.getMessage());
        }
    }

    protected void crawl(String url, int depth) throws IOException {
        url = CrawlerUtils.removeFragment(url);
        if (depth > config.depthLimit() || !visitedUrls.add(url)) {
            return;
        }

        DocumentWrapper doc = webCrawler.get(url);
        LinkExtractor extractor = new LinkExtractor(doc);
        LinkResults links = extractor.validateLinks();
        Heading[] headings = doc.extractHeadings();

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
                logger.logError("Failed to crawl " + url + ": " + e.getMessage());
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
