package assignment;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * A web-index which efficiently stores information about pages. Serialization is done automatically
 * via the superclass "Index" and Java's Serializable interface.
 *
 * TODO: Implement this!
 */
public class WebIndex extends Index {
    /**
     * Needed for Serialization (provided by Index) - don't remove this!
     */
    private HashMap<String, HashSet<URL>> wordPages = new HashMap<>();
    private HashMap<String, HashSet<URL>> attributes = new HashMap<>();

    //Separate HashMap to find phrase queries
    private HashMap<URL, ArrayList<String>> urlWords = new HashMap<>();

    private static final long serialVersionUID = 1L;

    // TODO: Implement all of this! You may choose your own data structures an internal APIs.
    // You should not need to worry about serialization (just make any other data structures you use
    // here also serializable - the Java standard library data structures already are, for example).

    public HashMap<String, HashSet<URL>> getWords() {
        return wordPages;
    }

    public HashMap<String, HashSet<URL>> getAttributes() {
        return attributes;
    }

    public HashMap<URL, ArrayList<String>> getUrlWords() {
        return urlWords;
    }

    void storeWords(String[] words, URL pageURL) {
        for (String word : words) {
            if (word != null && pageURL != null) {
                word = word.replaceAll("[^a-zA-Z0-9\\s]", "");
                word = word.trim().replaceAll(" +", "").toLowerCase();
                if (wordPages.containsKey(word)) {
                    wordPages.get(word).add(pageURL);
                } else {
                    wordPages.put(word, new HashSet<>());
                    wordPages.get(word).add(pageURL);
                }
                //store words associated with each page
                if (urlWords.containsKey(pageURL)) {
                    urlWords.get(pageURL).add(word);
                } else {
                    urlWords.put(pageURL, new ArrayList<>());
                    urlWords.get(pageURL).add(word);
                }
            }
        }
    }

    void storeAttributes(String attributeName, String attributeValue, URL currentUrl) {
        if(attributeName == null || attributeValue == null) {
            throw new IllegalArgumentException();
        }
        if(!attributeName.matches("\\A\\p{ASCII}*\\z") || !attributeValue.matches("\\A\\p{ASCII}*\\z") || attributeValue.contains("@"))
            return;
        attributeName = attributeName.toLowerCase();
        if(attributeName.equals("href")) {
            if(attributeValue.indexOf("html") != attributeValue.length()-4)
                return;
            try {
                URL path = new URL(currentUrl, attributeValue);
                if(attributes.containsKey(attributeName))
                    attributes.get(attributeName).add(path);
                else {
                    attributes.put(attributeName, new HashSet<>());
                    attributes.get(attributeName).add(path);
                }
            } catch (Exception e) {
                System.err.println("BAD URL " + currentUrl + " " + attributeValue);
            }
        }
    }
}
