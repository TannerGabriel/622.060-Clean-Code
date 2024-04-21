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

public class Crawler {
    private HashSet<String> visitedUrls = new HashSet<>();
    private String startUrl;
    private int depthLimit;
    private String domainFilter;
    private String targetLang;
    private MarkdownWriter writer;

    public Crawler(String startUrl, int depthLimit, String domainFilter, String targetLang) {
        this.startUrl = startUrl;
        this.depthLimit = depthLimit;
        this.domainFilter = domainFilter;
        this.targetLang = targetLang;
        this.writer = new MarkdownWriter("output.md");
    }

    public void startCrawling() {
        try {
            crawl(startUrl, 0);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void crawl(String url, int depth) throws IOException {
        url = CrawlerUtils.removeFragment(url);
        if (depth > depthLimit || visitedUrls.contains(url)) {
            return;
        }
        visitedUrls.add(url);
        Document doc = Jsoup.connect(url).get();
        LinkExtractor extractor = new LinkExtractor(doc);
        LinkResults links = extractor.validateLinks(extractor.extractLinks());
        Elements headings = extractor.extractHeadings();

        writer.writeContent(headings, links, depth, targetLang);

        links.validLinks.forEach(link -> {
            if (!visitedUrls.contains(link)) {
                try {
                    crawl(link, depth + 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
