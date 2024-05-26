package at.aau.core;

public record SchedulerConfig(String[] urls, int depthLimit, String domainFilter, String targetLang) { }
