package at.aau.io;

import java.util.HashSet;
import java.util.List;

public class LinkResults {
    public final HashSet<String> validLinks;
    public final HashSet<String> brokenLinks;

    public LinkResults(HashSet<String> validLinks, HashSet<String> brokenLinks) {
        this.validLinks = validLinks;
        this.brokenLinks = brokenLinks;
    }
}
