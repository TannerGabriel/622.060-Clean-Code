package at.aau.core;

public class CrawlerConfig {
    private final String startUrl;
    private final int depthLimit;
    private final String domainFilter;
    private final String targetLang;

    public CrawlerConfig(String startUrl, int depthLimit, String domainFilter, String targetLang) {
        this.startUrl = startUrl;
        this.depthLimit = depthLimit;
        this.domainFilter = domainFilter;
        this.targetLang = targetLang;
    }

    public String getStartUrl() {
        return startUrl;
    }

    public int getDepthLimit() {
        return depthLimit;
    }

    public String getDomainFilter() {
        return domainFilter;
    }

    public String getTargetLang() {
        return targetLang;
    }
}
