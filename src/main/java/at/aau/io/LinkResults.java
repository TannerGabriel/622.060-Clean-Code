package at.aau.io;

import java.util.Set;

public class LinkResults {
    public final Set<String> validLinks;
    public final Set<String> brokenLinks;

    public LinkResults(Set<String> validLinks, Set<String> brokenLinks) {
        this.validLinks = validLinks;
        this.brokenLinks = brokenLinks;
    }
}
