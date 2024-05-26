package at.aau.core;

public class SchedulerConfig {
    private final String[] urls;
    private final int depthLimit;
    private final String domainFilter;
    private final String targetLang;

    public SchedulerConfig(String[] urls, int depthLimit, String domainFilter, String targetLang) {
        this.urls = urls;
        this.depthLimit = depthLimit;
        this.domainFilter = domainFilter;
        this.targetLang = targetLang;
    }

    public String[] getUrls() {
        return urls;
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
