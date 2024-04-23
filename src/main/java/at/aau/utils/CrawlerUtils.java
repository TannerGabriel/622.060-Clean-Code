package at.aau.utils;

import org.jsoup.nodes.Element;

public class CrawlerUtils {
    public static int getHeaderLevel(Element heading) {
        return Integer.parseInt(heading.tagName().substring(1));
    }

    public static String removeFragment(String url) {
        int fragmentIndex = url.indexOf('#');
        return fragmentIndex < 0 ? url : url.substring(0, fragmentIndex);
    }
}
