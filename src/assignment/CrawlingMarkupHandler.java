package assignment;

import java.util.*;
import java.net.*;
import org.attoparser.simple.*;

/**
 * A markup handler which is called by the Attoparser markup parser as it parses the input;
 * responsible for building the actual web index.
 *
 * TODO: Implement this!
 */
public class CrawlingMarkupHandler extends AbstractSimpleMarkupHandler {
    private LinkedList<URL> newURLs;
    private Set<URL> urlsVisited;
    private WebIndex currentIndex;
    private URL currentURL;
    private Stack<String> pageElements;
    private StringBuilder documentWords;

    public CrawlingMarkupHandler() {
        newURLs = new LinkedList<>();
        urlsVisited = new HashSet<>();
        pageElements = new Stack<>();
        currentIndex = new WebIndex();
    }

    /**
    * This method returns the complete index that has been crawled thus far when called.
    */
    public Index getIndex() {
        // TODO: Implement this!
        return currentIndex;
    }

    /**
    * This method returns any new URLs found to the Crawler; upon being called, the set of new URLs
    * should be cleared.
    */
    public List<URL> newURLs() {
        // TODO: Implement this!
        for(URL url : currentIndex.getAttributes().get("href")) {
            if(!urlsVisited.contains(url))
                newURLs.add((url));
        }
        LinkedList<URL> copy = new LinkedList<>(newURLs);
        urlsVisited.addAll(newURLs);
        newURLs.clear();
        return copy;
    }

    /**
    * These are some of the methods from AbstractSimpleMarkupHandler.
    * All of its method implementations are NoOps, so we've added some things
    * to do; please remove all the extra printing before you turn in your code.
    *
    * Note: each of these methods defines a line and col param, but you probably
    * don't need those values. You can look at the documentation for the
    * superclass to see all of the handler methods.
    */

    /**
    * Called when the parser first starts reading a document.
    * @param startTimeNanos  the current time (in nanoseconds) when parsing starts
    * @param line            the line of the document where parsing starts
    * @param col             the column of the document where parsing starts
    */
    public void handleDocumentStart(long startTimeNanos, int line, int col) {
        // TODO: Implement this.
        documentWords = new StringBuilder();
        pageElements.push("html");
    }

    /**
    * Called when the parser finishes reading a document.
    * @param endTimeNanos    the current time (in nanoseconds) when parsing ends
    * @param totalTimeNanos  the difference between current times at the start
    *                        and end of parsing
    * @param line            the line of the document where parsing ends
    * @param col             the column of the document where the parsing ends
    */
    public void handleDocumentEnd(long endTimeNanos, long totalTimeNanos, int line, int col) {
        // TODO: Implement this.
        try {
            pageElements.clear();
        } catch (Exception e) {
            System.err.println("Stack clear failed");
        }
        String wordsToAdd = documentWords.toString().replaceAll(" +", " ");
        currentIndex.storeWords(wordsToAdd.split(" "), currentURL);
    }

    /**
    * Called at the start of any tag.
    * @param elementName the element name (such as "div")
    * @param attributes  the element attributes map, or null if it has no attributes
    * @param line        the line in the document where this elements appears
    * @param col         the column in the document where this element appears
    */
    public void handleOpenElement(String elementName, Map<String, String> attributes, int line, int col) {
        // TODO: Implement this.
        try {
            pageElements.push(elementName);
        } catch (Exception e) {
            System.err.println("Invalid Element Name");
        }
        if(attributes == null)
            return;

        for(String attribute : attributes.keySet()) {
            currentIndex.storeAttributes(attribute.toLowerCase(), attributes.get(attribute).toLowerCase(), currentURL);
        }
    }

    /**
    * Called at the end of any tag.
    * @param elementName the element name (such as "div").
    * @param line        the line in the document where this elements appears.
    * @param col         the column in the document where this element appears.
    */
    public void handleCloseElement(String elementName, int line, int col) {
        // TODO: Implement this.
        try {
            pageElements.pop();
        } catch (Exception e) {
            System.err.println("Element Stack Empty");
        }
    }

    /**
    * Called whenever characters are found inside a tag. Note that the parser is not
    * required to return all characters in the tag in a single chunk. Whitespace is
    * also returned as characters.
    * @param ch      buffer containint characters; do not modify this buffer
    * @param start   location of 1st character in ch
    * @param length  number of characters in ch
    */
    public void handleText(char ch[], int start, int length, int line, int col) {
        // TODO: Implement this.
        try {
            if (pageElements.peek().equals("style") || pageElements.peek().equals("script"))
                return;
        } catch (Exception e) {
            System.err.println("Empty Stack");
        }

        for(int i = start; i < start+length; i++) {
            if(ch[i] == ' ' || Character.isAlphabetic(ch[i]) || Character.isDigit(ch[i]))
                documentWords.append(ch[i]);
            else documentWords.append(" ");
        }
        documentWords.append(" ");
    }

    //HELPER METHODS

    void setCurrentURL(URL url) {
        urlsVisited.add(url);
        currentURL = url;
    }
}
