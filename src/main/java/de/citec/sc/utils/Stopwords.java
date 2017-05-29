package de.citec.sc.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * This class provides a list of stopwords. Thereto it reads a list of stopwords
 * from a file.
 *
 * @author hterhors
 *
 * Feb 18, 2016
 */
public class Stopwords {

    /**
     * A set of English stopwords.
     */
    private static final Set<String> ENGLISH_STOP_WORDS = readLines();

    /**
     * Read stopwords from file.
     *
     * @return
     */
    private static Set<String> readLines() {
        try {
            return new HashSet<String>(Files.readAllLines(Paths.get("src/main/resources/stopwords.txt")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isStopWord(String word) {

        if (ENGLISH_STOP_WORDS.contains(word)) {
            return true;
        }

        return false;
    }

}
