package at.aau.wrapper;

import at.aau.io.Heading;

import java.util.List;

public interface DocumentWrapper {
    public List<String> extractLinks();
    public Heading[] extractHeadings();
}
