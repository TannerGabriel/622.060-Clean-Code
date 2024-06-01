package at.aau.core;

import at.aau.utils.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CrawlerScheduler {
    private static final Logger logger = Logger.getInstance();
    protected final List<Crawler> crawlers;

    public CrawlerScheduler(SchedulerConfig config) {
        crawlers = new ArrayList<>();
        addCrawlerFromConfig(config);
    }

    public void startCrawlers() {
        for (Crawler crawler : crawlers) {
            crawler.start();
        }
        waitForCrawlers();
        printOutput(getOutputFromCrawlers(),logger.getLogsString());
    }

    protected void addCrawlerFromConfig(SchedulerConfig config) {
        for (String url : config.urls()) {
            crawlers.add(new Crawler(new CrawlerConfig(url, config.depthLimit(), config.domainFilter(), config.targetLang())));
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
            output.append(crawler.getOutput()).append("\n\n");
        }
        return output.toString();
    }

    protected void printOutput(String content,String logs) {
        try {
            PrintWriter writer = createPrintWriter(Paths.get("output.md"));
            writer.println(content);
            writer.println(logs);
            writer.flush();
            writer.close();
        } catch (IOException | InvalidPathException e) {
            System.err.println("Failed to write output file: " + e.getMessage());
        }
    }

    protected PrintWriter createPrintWriter(Path path) throws IOException {
        return new PrintWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8));
    }
}
