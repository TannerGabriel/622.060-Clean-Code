package at.aau.core;

public record CrawlerConfig(String startUrl, int depthLimit, String domainFilter, String targetLang) { }
