package at.aau.utils;

import org.jsoup.nodes.Element;

public class CrawlerUtils {
    public static int getHeaderLevel(Element heading) {
        return Integer.parseInt(heading.tagName().substring(1));
    }




    public static String sanatizeURL(String url) {
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
