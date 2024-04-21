package at.aau;

import at.aau.core.Crawler;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the starting URL:");
        String startUrl = scanner.nextLine();
        System.out.println("Enter depth limit:");
        int depthLimit = Integer.parseInt(scanner.nextLine());
        System.out.println("Enter domain filter:");
        String domainFilter = scanner.nextLine();
        System.out.println("Enter target Language:");
        String targetLang = scanner.nextLine();

        Crawler crawler = new Crawler(startUrl, depthLimit, domainFilter, targetLang);
        crawler.startCrawling();
    }
}