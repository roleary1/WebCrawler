package assignment;
import java.net.URL;
import java.net.URLStreamHandler;
import java.util.*;

/**
 * A query engine which holds an underlying web index and can answer textual queries with a
 * collection of relevant pages.
 *
 * TODO: Implement this!
 */
public class WebQueryEngine {
    private WebIndex currentIndex;

    public WebQueryEngine() {
        currentIndex = new WebIndex();
    }

    public WebQueryEngine(WebIndex index) {
        currentIndex = index;
    }

    /**
     * Returns a WebQueryEngine that uses the given Index to constructe answers to queries.
     *
     * @param index The WebIndex this WebQueryEngine should use.
     * @return A WebQueryEngine ready to be queried.
     */
    public static WebQueryEngine fromIndex(WebIndex index) {
        // TODO: Implement this!
        return new WebQueryEngine(index);
    }

    /**
     * Returns a Collection of URLs (as Strings) of web pages satisfying the query expression.
     *
     * @param query A query expression.
     * @return A collection of web pages satisfying the query.
     */
    public LinkedList<Page> query(String query) {
        // TODO: Implement this!
        LinkedList<Page> results = new LinkedList<>();
        //only searches for query results if non-whitespace input has been entered
        if(!query.matches(" +") && !query.equals("")) {
            HashMap<String, HashSet<URL>> indexWords = currentIndex.getWords();
            HashMap<URL, ArrayList<String>> urlWords = currentIndex.getUrlWords();
            //adds all URLs found by handleQuery()
            for(URL url : handleQuery(query, indexWords, urlWords)) {
                results.add(new Page(url));
            }
        }
        return results;
    }

    //performs the logic of representing a query and returning the desired URLs
    private LinkedList<URL> handleQuery(String query, HashMap<String, HashSet<URL>> indexWords, HashMap<URL, ArrayList<String>> urlWords) {
        //formats the String query such that it can easily be converted to postfix notation
        query = query.replaceAll("\\(", "( ");
        query = query.replaceAll("\\)", " )");
        query = query.replaceAll("\"", " \" ");
        query = query.trim().replaceAll(" +", " ");
        //inserts &s into the query where an implicit and exists
        query = formatImplicitAnds(query.split(" "));
        query = query.replaceAll("!", "! ");
        //converts query to postfix notation for ease of operations
        String[] queryWords = postFixConverter(query.toLowerCase().split(" "));
        Stack<HashSet<URL>> results = new Stack<>();
        //iterates through the query and performs the offered query operations while storing the results found
        for(int i = 0; i < queryWords.length; i++) {
            if (queryWords[i].matches("[a-zA-Z0-9]+")) {
                results.push(findWords(queryWords[i], indexWords));
            } else if (queryWords[i].equals("\"")) { //performs a phrase query
                StringBuilder phraseQuery = new StringBuilder();
                do {
                    phraseQuery.append(queryWords[i]);
                    if(!queryWords[i].equals("\""))
                        phraseQuery.append(" ");
                    i++;
                } while (i < queryWords.length && !queryWords[i].equals("\""));
                results.push(findPhraseURLS(phraseQuery.toString().trim().substring(1).split(" "), indexWords, urlWords));
            } else {
                switch (queryWords[i]) {
                    //finds the intersection of results between the two queries words/set of URLs
                    case "&":
                        if (results.size() >= 2) {
                            results.push(findIntersection(results.pop(), results.pop()));
                        } else {
                            System.err.println("Invalid Operation: Too Few Operands");
                        }
                        break;
                    //finds all results contained in either set of URLs (essentially adds the two query results together)
                    case "|":
                        if (results.size() >= 2) {
                            results.push(findEither(results.pop(), results.pop()));
                        } else {
                            System.err.println("Invalid Operation: Too Few Operands");
                        }
                        break;
                    //performs a negation - finding all URLs in the index that do not contain the specified query
                    case "!":
                        if (results.size() > 0) {
                            results.push(findNegation(results.pop(), indexWords));
                        } else {
                            System.err.println("Invalid Operation: Too Few Operands");
                        }
                        break;
                    }
                }
            }
            //returns a linked list initialized to the result of all the queries/query operations
            return new LinkedList<>(results.pop());
        }

    //finds all URLS associated with a query - used by token operators to find intersections, etc.
    private HashSet<URL> findWords(String query, HashMap<String, HashSet<URL>> indexWords) {
        if(indexWords.get(query) == null)
            return new HashSet<>();
        return new HashSet<>(indexWords.get(query));
    }

    //finds all URLS containing the contiguous set of words inside the double quotation marks
    private HashSet<URL> findPhraseURLS(String[] phraseQuery,  HashMap<String, HashSet<URL>> indexWords, HashMap<URL, ArrayList<String>> urlWords) {
        HashSet<URL> phraseIntersection = new HashSet<>(findWords(phraseQuery[0], indexWords));
        HashSet<URL> results = new HashSet<>();
        //finds the intersection of all the words in the phrase
        for(int i = 1; i < phraseQuery.length; i++) {
            phraseIntersection = findIntersection(phraseIntersection, findWords(phraseQuery[i], indexWords));
        }
        //finds the URLs found by the intersection that contain the contiguous sequence of words in the correct order
        for(URL url : phraseIntersection) {
            boolean correctUrl = false;
            int index = 0;
            for(String str : urlWords.get(url)) {
                if(str.equals(phraseQuery[index])) {
                    if(index == phraseQuery.length-1) {
                        correctUrl = true;
                        break;
                    }
                    index++;
                } else {
                    index = 0;
                }
            }
            if(correctUrl)
                results.add(url);
        }
        return results;
    }

    //called on ! operators, finding all URLS that don't contain the query
    private HashSet<URL> findNegation(HashSet<URL> query1, HashMap<String, HashSet<URL>> indexWords) {
        HashSet<URL> results = new HashSet<>();
        for(String str : indexWords.keySet())
            results.addAll(indexWords.get(str));
        results.removeAll(query1);
        return results;
    }

    //called on AND operators, finding all URLS that contain both queries
    private HashSet<URL> findIntersection(HashSet<URL> query1, HashSet<URL> query2) {
        query1.retainAll(query2);
        return query1;
    }

    //called on OR operators, finding all URLS that contain the first or second query
    private HashSet<URL> findEither(HashSet<URL> query1, HashSet<URL> query2) {
        query1.addAll(query2);
        return query1;
    }

    //converts query to postfix notation to allow for easier computation of query operations
    private String[] postFixConverter(String[] expression){
        StringBuilder result = new StringBuilder();
        Stack<String> tokenStack = new Stack<>();
        for (String str1 : expression) {
            //checks where str1 is an operator
            if (getTokenPrecedence(str1) >= 0) {
                while (!tokenStack.isEmpty() && getTokenPrecedence(tokenStack.peek()) >= getTokenPrecedence(str1)) {
                    result.append(tokenStack.pop()).append(" ");
                }
                tokenStack.push(str1);
            } else if (str1.equals(")")) {
                String str2 = tokenStack.pop();
                while (!str2.equals("(")) {
                    result.append(str2).append(" ");
                    str2 = tokenStack.pop();
                }
            } else if (str1.equals("(")) {
                tokenStack.push(str1);
            } else {
                //string is neither operator nor (
                result.append(str1).append(" ");
            }
        }
        while(!tokenStack.isEmpty()) {
            result.append(tokenStack.pop()).append(" ");
        }
        return result.toString().split(" ");
    }

    //adds implicit ands to the String query where appropriate
    private String formatImplicitAnds(String[] str) {
        StringBuilder result = new StringBuilder();
        boolean implicitAnd = false, insidePhrase = false;
        for(int i = 0; i < str.length; i++) {
            if(str[i].equals("\"")) {
                insidePhrase = !insidePhrase;
            }
            if (!insidePhrase) {
                if (str[i].matches("[a-zA-Z0-9]+")) {
                    if (implicitAnd) {
                        result.append("& ");
                    }
                    implicitAnd = true;
                } else if (str[i].substring(0, 1).equals("!") && str[i].substring(1).matches("[a-zA-Z0-9]+")) {
                    if (implicitAnd) {
                        result.append("& ");
                    }
                    implicitAnd = true;
                } else if (str[i].equals(")")) {
                    implicitAnd = true;
                } else if (str[i].equals("(")) {
                    if (implicitAnd) {
                        result.append("& ");
                    }
                    implicitAnd = false;
                } else {
                    implicitAnd = false;
                }
            }
            result.append(str[i]);
            if(i != str.length-1)
                result.append(" ");
        }
        return result.toString();
    }

    //returns the precedence associated with each token
    private int getTokenPrecedence(String str) {
        switch (str){
            case "&":
            case "|":
                return 0;
            case "!":
                return 1;
        }
        return -1;
    }
}
