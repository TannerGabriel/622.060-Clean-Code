package at.aau;

import at.aau.core.Crawler;

import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        String startUrl;
        int depthLimit;
        String domainFilter;
        String targetLang;

        if (args.length == 4) {
            startUrl = args[0];
            depthLimit = Integer.parseInt(args[1]);
            domainFilter = args[2];
            targetLang = args[3];
        } else {
            startUrl = getInput("Enter the starting URL:", Main::validateUrl);
            depthLimit = getIntInput("Enter depth limit:", Main::validatePositiveNumber);
            domainFilter = getInput("Enter domain filter:", s -> !s.isEmpty());
            targetLang = getInput("Enter target Language:", s -> !s.isEmpty());
        }

        Crawler crawler = new Crawler(startUrl, depthLimit, domainFilter, targetLang);
        crawler.startCrawling();
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