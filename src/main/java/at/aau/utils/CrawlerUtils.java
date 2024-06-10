package at.aau.utils;

public class CrawlerUtils {
    public static int getHeaderLevel(String heading) {
        return Integer.parseInt(heading.substring(2,3));
    }

    public static String sanitizeURL(String url) {
        return removeTailingSlash(removeFragment(url));
    }

    public static String removeTailingSlash(String url) {
        return url.charAt(url.length() - 1) == '/' ? url.substring(0, url.length() - 1) : url;
    }

    public static String removeFragment(String url) {
        int fragmentIndex = url.indexOf('#');
        return fragmentIndex < 0 ? url : url.substring(0, fragmentIndex);
    }
}
