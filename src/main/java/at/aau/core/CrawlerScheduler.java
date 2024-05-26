package at.aau.core;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CrawlerScheduler {
    private final List<Crawler> crawlers;
    private String output;

    public CrawlerScheduler(SchedulerConfig config) {
        crawlers = new ArrayList<>();
        addCrawlerFromConfig(config);
    }

    public void startCrawler(CrawlerConfig config) {
        Crawler crawler = new Crawler(config);
        crawler.start();
        crawlers.add(crawler);
    }

    public void startCrawlers() {
        for (Crawler crawler : crawlers) {
            crawler.start();
        }
        waitForCrawlers();
        printOutput(getOutputFromCrawlers());
    }

    protected void addCrawlerFromConfig(SchedulerConfig config) {
        for (String url : config.getUrls()) {
            crawlers.add(new Crawler(new CrawlerConfig(url, config.getDepthLimit(), config.getDomainFilter(), config.getTargetLang())));
        }
    }

    protected void waitForCrawlers() {
        for (Crawler crawler : crawlers) {
            try {
                crawler.join();
            } catch (InterruptedException e) {
                System.out.println("Error while joining thread: " + e);
            }
        }
    }

    protected String getOutputFromCrawlers() {
        StringBuilder output = new StringBuilder();
        for (Crawler crawler : crawlers) {
            output.append(crawler.getOutput());
            output.append("\n\n");
        }
        return output.toString();
    }

    protected void printOutput(String content) {
        try {
            PrintWriter writer = new PrintWriter(Files.newBufferedWriter(Paths.get("output.md"), StandardCharsets.UTF_8));
            writer.println(content);
            writer.flush();
            writer.close();
        } catch (IOException | InvalidPathException e) {
            System.err.println("Failed to initialize file writer: " + e.getMessage());
        }
    }
}