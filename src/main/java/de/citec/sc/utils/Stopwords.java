package de.citec.sc.utils;

import de.citec.sc.main.Main;
import de.citec.sc.query.CandidateRetriever;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
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
    private static Set<String> ENGLISH_STOP_WORDS;
    private static Set<String> GERMAN_STOP_WORDS;
    private static Set<String> SPANISH_STOP_WORDS;
    private static String outputDirectory  = "stopwords";

    /**
     * Read stopwords from file.
     *
     * @return
     */
    private static Set<String> readLines(String lang) {
        InputStream inputStream = Stopwords.class.getClassLoader().getResourceAsStream(outputDirectory + "/stopwords_" + lang + ".txt");
        Set<String> content = FileFactory.readFile(inputStream);
        Set<String> stopwords = new HashSet<>();
        for (String s : content) {
            stopwords.add(s.toLowerCase());
        }
        return stopwords;
    }

    public static boolean isStopWord(String word, CandidateRetriever.Language lang) {

        word = word.toLowerCase();

        //load
        if (ENGLISH_STOP_WORDS == null) {

            ENGLISH_STOP_WORDS = readLines("EN");
            GERMAN_STOP_WORDS = readLines("DE");
            SPANISH_STOP_WORDS = readLines("ES");
        }

        if (lang.equals(CandidateRetriever.Language.EN) && ENGLISH_STOP_WORDS.contains(word)) {
            return true;
        }
        if (lang.equals(CandidateRetriever.Language.DE) && GERMAN_STOP_WORDS.contains(word)) {
            return true;
        }
        if (lang.equals(CandidateRetriever.Language.ES) && SPANISH_STOP_WORDS.contains(word)) {
            return true;
        }

        return false;
    }

}
