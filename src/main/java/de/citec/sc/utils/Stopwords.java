package de.citec.sc.utils;

import de.citec.sc.main.Main;
import de.citec.sc.query.CandidateRetriever;
import java.io.IOException;
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
    private static String outputDirectory;

    /**
     * Read stopwords from file.
     *
     * @return
     */
    private static Set<String> readLines(String lang) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(outputDirectory + "/stopwords_" + lang + ".txt"));
            Set<String> stopwords = new HashSet<>();
            for (String s : lines) {
                stopwords.add(s.toLowerCase());
            }
            return stopwords;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isStopWord(String word, CandidateRetriever.Language lang) {

        word = word.toLowerCase();

        //load
        if (ENGLISH_STOP_WORDS == null) {
            outputDirectory = Stopwords.class.getClassLoader().getResource("stopwords").getPath();

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
