package at.aau;


import at.aau.core.CrawlerScheduler;
import at.aau.core.SchedulerConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Main {
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        SchedulerConfig config = getConfig(args);
        CrawlerScheduler crawlerScheduler = new CrawlerScheduler(config);
        crawlerScheduler.startCrawlers();
    }

    protected static SchedulerConfig getConfig(String[] args) {
        if (args.length == 4) {
            return new SchedulerConfig(args[0].split(","), Integer.parseInt(args[1]), args[2], args[3]);
        } else {
            int urlAmount = getIntInput("Enter the amount of urls you want to scan:", Main::validatePositiveNumber);

            return new SchedulerConfig(
                    getInputs("Enter the starting URL:", urlAmount, Main::validateUrl),
                    getIntInput("Enter depth limit:", Main::validatePositiveNumber),
                    getInput("Enter domain filter:", Main::validRegex),
                    getInput("Enter target Language:", s -> !s.isEmpty())
            );
        }
    }

    protected static String[] getInputs(String prompt, int inputAmount, Validator<String> validator) {
        List<String> inputs = new ArrayList<>();

        for (int i = 0; i < inputAmount; i++) {
            inputs.add(getInput(prompt, validator));
        }
        return inputs.toArray(String[]::new);
    }

    protected static String getInput(String prompt, Validator<String> validator) {
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

    protected static int getIntInput(String prompt, Validator<Integer> validator) {
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

    protected static boolean validateUrl(String url) {
        return url.matches("https?://.*");
    }

    protected static boolean validatePositiveNumber(int number) {
        return number >= 0;
    }

    protected static boolean validRegex(String regex) {
        try {
            Pattern.compile(regex);
            return true;
        } catch (PatternSyntaxException exception) {
            return false;
        }
    }

    @FunctionalInterface
    interface Validator<T> {
        boolean validate(T value);
    }
}