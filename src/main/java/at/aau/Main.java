package at.aau;

import okhttp3.*;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Scanner;

public class Main {

    private static final HashSet<String> visitedUrls = new HashSet<>();
    private static int depthLimit;
    private static String domainFilter;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the starting URL:");
        String startUrl = scanner.nextLine();
        System.out.println("Enter depth limit:");
        depthLimit = Integer.parseInt(scanner.nextLine());
        System.out.println("Enter domain filter:");
        domainFilter = scanner.nextLine();

        try (PrintWriter writer = new PrintWriter("output.md", StandardCharsets.UTF_8)) {
            writer.println("input: <a>" + startUrl + "</a>");
            writer.println("depth: " + depthLimit);
            writer.println("source language: english");
            writer.println("target language: english");
            writer.println("summary: ");
            crawl(startUrl, 0, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void crawl(String url, int depth, PrintWriter writer) {
        url = removeFragment(url);

        if (depth > depthLimit || visitedUrls.contains(url)) {
            return;
        }
        String indent = "";
        try {
            Document doc = Jsoup.connect(url).get();
            visitedUrls.add(url);
            Elements links = doc.select("a[href]");
            indent = "  ".repeat(depth);

            Elements headings = doc.select("h1, h2, h3, h4, h5, h6");
            for (Element heading : headings) {
                writer.println(indent + "#".repeat(getHeaderLevel(heading)) + translate(heading.text(), "de-de"));
            }

            for (Element link : links) {
                String absUrl = link.attr("abs:href");
                if (!isBrokenLink(absUrl)) {
                    writer.println(indent + "--> link to <a>" + absUrl + "</a>");
                    if (!visitedUrls.contains(absUrl)) {
                        crawl(absUrl, depth + 1, writer);
                    }
                } else {
                    writer.println(indent + "--> broken link <a>" + absUrl + "</a>");
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to retrieve " + url);
            e.printStackTrace();
        }
    }

    private static int getHeaderLevel(Element heading) {
        return Integer.parseInt(heading.tagName().substring(1));
    }

    private static boolean isBrokenLink(String url) {
        try {
            int statusCode = Jsoup.connect(url).ignoreHttpErrors(true).timeout(10000).method(Connection.Method.HEAD).execute().statusCode();
            if (statusCode >= 400) {
                return true;
            }
        } catch (Exception e) {
            return true;
        }
        return false;
    }

    private static String removeFragment(String url) {
        int fragmentIndex = url.indexOf('#');
        return fragmentIndex < 0 ? url : url.substring(0, fragmentIndex);
    }

    private static String translate(String text, String targetLang) throws IOException {

        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create("{\"q\": \"" + text + "\",\"source\": \"en\",\"target\": \"" + targetLang + "\",\"format\": \"text\"}",mediaType);
        Request request = new Request.Builder()
                .url("https://google-translator9.p.rapidapi.com/v2")
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("X-RapidAPI-Key", "3da7b257d7msh9c65f8753ebe9d7p1daf4ajsn956c62c45dcc")
                .addHeader("X-RapidAPI-Host", "google-translator9.p.rapidapi.com")
                .build();

        Response response = client.newCall(request).execute();

        JSONObject jsonObject = new JSONObject(response.body().string());

        return jsonObject.getJSONObject("data").getJSONArray("translations").getJSONObject(0).getString("translatedText");
    }
}