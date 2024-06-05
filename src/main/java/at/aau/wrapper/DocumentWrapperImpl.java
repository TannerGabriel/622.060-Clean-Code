package at.aau.wrapper;

import at.aau.io.Heading;
import at.aau.utils.CrawlerUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

import static at.aau.utils.CrawlerUtils.getHeaderLevel;

public class DocumentWrapperImpl implements DocumentWrapper {
    private Document document;

    public DocumentWrapperImpl(Document document){
        this.document = document;
    }


    @Override
    public List<String> extractLinks() {
        return document.select("a[href]").stream()
                .map(element -> CrawlerUtils.sanitizeURL(element.absUrl("href")))
                .toList();
    }

    @Override
    public Heading[] extractHeadings() {
        Elements headingElements = document.select("h1, h2, h3, h4, h5, h6");
        Heading[] headings = new Heading[headingElements.size()];
        for (int i = 0; i < headingElements.size(); i++) {
            headings[i] = new Heading(getHeaderLevel(headingElements.get(i).outerHtml()),headingElements.get(i).text()) ;
        }
        return headings;
    }
}
