package assignment;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class WebIndexTester {
    WebIndex testIndex;

    @Before
    public void setUp() {
        testIndex = new WebIndex();
    }

    //was having an issue with my URL generation had to change it to simply null
    @Test
    public void TestStoreWords() throws FileNotFoundException, MalformedURLException {
        Scanner sc = new Scanner(new File("words.txt"));
        StringBuilder words = new StringBuilder();
        while(sc.hasNext()) {
            words.append(sc.next()).append(" ");
        }
        //URL testURL = new URL();
        testIndex.storeWords(words.toString().trim().split(" "), null);
    }

    @Test
    public void TestStoreNullAttributes() throws FileNotFoundException {
        Scanner sc = new Scanner(new File("words.txt"));
        StringBuilder words = new StringBuilder();
        while(sc.hasNext()) {
            words.append(sc.next()).append(" ");
        }
        testIndex.storeAttributes("href", null, null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void TestStoreNullURL() throws FileNotFoundException {
        Scanner sc = new Scanner(new File("words.txt"));
//        StringBuilder words = new StringBuilder();
//        while(sc.hasNext()) {
//            words.append(sc.next()).append(" ");
//        }
        testIndex.storeAttributes("href", null, null);
    }

    @Test
    public void TestGetWords() throws FileNotFoundException {
        Scanner sc = new Scanner(new File("words.txt"));
        StringBuilder words = new StringBuilder();
        while(sc.hasNext()) {
            Assert.assertTrue(testIndex.getWords().containsKey(sc.next()));
        }
    }

    @Test
    public void TestGetAttributes() throws FileNotFoundException {
        Scanner sc = new Scanner(new File("words.txt"));
        StringBuilder words = new StringBuilder();
        while(sc.hasNext()) {
            words.append(sc.next()).append(" ");
        }

        sc = new Scanner(new File("words.txt"));
        Assert.assertTrue(testIndex.getAttributes().containsKey("href"));
        while(sc.hasNext()) {
            Assert.assertTrue(testIndex.getWords().containsKey(sc.next()));
        }
    }

    @Test
    public void TestStoreInvalidWords() throws FileNotFoundException {
        Scanner sc = new Scanner(new File("invalidcharacters.txt"));
        StringBuilder words = new StringBuilder();
        while(sc.hasNext()) {
            words.append(sc.next()).append(" ");
        }
        //URL testURL = new URL();
        testIndex.storeWords(words.toString().trim().split(" "), null);
        Assert.assertTrue(testIndex.getWords().isEmpty());
    }
}
