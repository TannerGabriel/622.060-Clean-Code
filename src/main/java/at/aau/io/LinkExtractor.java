package at.aau.io;

import at.aau.wrapper.DocumentWrapper;
import at.aau.wrapper.WebCrawler;
import at.aau.wrapper.WebCrawlerImpl;


import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class LinkExtractor {
    private DocumentWrapper document;
    private final WebCrawler webCrawler = new WebCrawlerImpl();

    public LinkExtractor(DocumentWrapper document) {
        this.document = document;
    }

    public LinkResults validateLinks() {
        List<String> links = document.extractLinks();

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

    public boolean isBrokenLink(String url) {
        try {
            int statusCode = webCrawler.getStatusCode(url);
            return statusCode == 404;
        } catch (IOException e) {
            return true;
        }
    }

    public CompletableFuture<Boolean> isBrokenLinkAsync(String url) {
        return CompletableFuture.supplyAsync(() -> isBrokenLink(url));
    }
}


