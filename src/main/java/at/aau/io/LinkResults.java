package at.aau.io;

import java.util.List;

public class LinkResults {
    public final List<String> validLinks;
    public final List<String> brokenLinks;

    public LinkResults(List<String> validLinks, List<String> brokenLinks) {
        this.validLinks = validLinks;
        this.brokenLinks = brokenLinks;
    }
}
