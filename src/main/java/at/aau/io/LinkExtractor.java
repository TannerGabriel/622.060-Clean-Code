package at.aau.io;

import at.aau.utils.CrawlerUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class LinkExtractor {
    private Document document;

    public LinkExtractor(Document document) {
        this.document = document;
    }

    public List<String> extractLinks() {
        return document.select("a[href]").stream()
                .map(element -> CrawlerUtils.sanatizeURL(element.absUrl("href")))
                .collect(Collectors.toList());
    }

    public LinkResults validateLinks(List<String> links) {

        HashSet<String> validLinks = new HashSet<>();

        HashSet<String> brokenLinks = new HashSet<>();

        for (var link : links) {
            if (isBrokenLink(link)) {
                brokenLinks.add(link);
            } else {
                validLinks.add(link);
            }
        }
        return new LinkResults(validLinks, brokenLinks);
    }

    public Elements extractHeadings() {
        return document.select("h1, h2, h3, h4, h5, h6");
    }

    public boolean isBrokenLink(String url) {
        try {
            int statusCode = Jsoup.connect(url).ignoreHttpErrors(true).timeout(3000).method(Connection.Method.HEAD).execute().statusCode();
            return statusCode == 404;
        } catch (Exception e) {
            return true;
        }
    }
}

