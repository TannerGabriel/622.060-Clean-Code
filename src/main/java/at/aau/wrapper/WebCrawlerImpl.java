package at.aau.wrapper;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class WebCrawlerImpl implements WebCrawler {

    @Override
    public DocumentWrapper get(String url) throws IOException {
        return new DocumentWrapperImpl(Jsoup.connect(url).get());
    }

    @Override
    public int getStatusCode(String url) throws IOException {
        return Jsoup.connect(url).ignoreHttpErrors(true).timeout(3000).method(Connection.Method.HEAD).execute().statusCode();
    }
}
