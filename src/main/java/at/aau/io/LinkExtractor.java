package at.aau.io;

import at.aau.utils.CrawlerUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class LinkExtractor {
    private Document document;

    public LinkExtractor(Document document) {
        this.document = document;
    }

    public List<String> extractLinks() {
        return document.select("a[href]").stream()
                .map(element -> CrawlerUtils.sanitizeURL(element.absUrl("href")))
                .toList();
    }

    public LinkResults validateLinks(List<String> links) {
        HashSet<String> validLinks = new HashSet<>();
        HashSet<String> brokenLinks = new HashSet<>();

        List<CompletableFuture<Boolean>> linkValidationFutures = links.stream()
                .map(this::isBrokenLinkAsync)
                .toList();

        for (int i = 0; i < links.size(); i++) {
            try {
                boolean isBroken = linkValidationFutures.get(i).get();
                if (isBroken) {
                    brokenLinks.add(links.get(i));
                } else {
                    validLinks.add(links.get(i));
                }
            } catch (InterruptedException | ExecutionException e) {
                brokenLinks.add(links.get(i));
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

    public CompletableFuture<Boolean> isBrokenLinkAsync(String url) {
        return CompletableFuture.supplyAsync(() -> isBrokenLink(url));
    }
}

