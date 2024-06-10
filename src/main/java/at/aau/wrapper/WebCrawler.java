package at.aau.wrapper;

import java.io.IOException;

public interface WebCrawler {

    public DocumentWrapper get(String url) throws IOException;
    public int getStatusCode(String url) throws IOException;
}
