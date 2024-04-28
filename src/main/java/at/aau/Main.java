package at.aau;

import at.aau.core.CrawlerConfig;
import at.aau.core.Crawler;

import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        CrawlerConfig config = getConfig(args);
        Crawler crawler = new Crawler(config);
        crawler.startCrawling();
    }

    private static CrawlerConfig getConfig(String[] args) {
        if (args.length == 4) {
            return new CrawlerConfig(args[0], Integer.parseInt(args[1]), args[2], args[3]);
        } else {
            return new CrawlerConfig(
                    getInput("Enter the starting URL:", Main::validateUrl),
                    getIntInput("Enter depth limit:", Main::validatePositiveNumber),
                    getInput("Enter domain filter:", s -> !s.isEmpty()),
                    getInput("Enter target Language:", s -> !s.isEmpty())
            );
        }
    }

    private static String getInput(String prompt, Validator<String> validator) {
        String input;
        while (true) {
            System.out.println(prompt);
            input = scanner.nextLine();
            if (validator.validate(input)) {
                return input;
            }
            System.out.println("Invalid input, please try again.");
        }
    }

    private static int getIntInput(String prompt, Validator<Integer> validator) {
        while (true) {
            try {
                String input = getInput(prompt, s -> s.matches("\\d+"));
                int value = Integer.parseInt(input);
                if (validator.validate(value)) {
                    return value;
                }
                System.out.println("Invalid input, please try again.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format, please try again.");
            }
        }
    }

    private static boolean validateUrl(String url) {
        return url.matches("https?://.*");
    }

    private static boolean validatePositiveNumber(int number) {
        return number >= 0;
    }

    @FunctionalInterface
    interface Validator<T> {
        boolean validate(T value);
    }
}