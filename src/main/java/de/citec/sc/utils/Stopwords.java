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
    private static final Set<String> ENGLISH_STOP_WORDS = readLines("EN");
    private static final Set<String> GERMAN_STOP_WORDS = readLines("DE");
    private static final Set<String> SPANISH_STOP_WORDS = readLines("ES");

    /**
     * Read stopwords from file.
     *
     * @return
     */
    private static Set<String> readLines(String lang) {
        try {
            List<String> lines = Files.readAllLines(Paths.get("src/main/resources/stopwords_"+lang+".txt"));
            Set<String> stopwords = new HashSet<>();
            for(String s : lines){
                stopwords.add(s.toLowerCase());
            }
            return stopwords;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isStopWord(String word) {
        
        word = word.toLowerCase();

        if (Main.lang.equals(CandidateRetriever.Language.EN) && ENGLISH_STOP_WORDS.contains(word)) {
            return true;
        }
        if (Main.lang.equals(CandidateRetriever.Language.DE) && GERMAN_STOP_WORDS.contains(word)) {
            return true;
        }
        if (Main.lang.equals(CandidateRetriever.Language.ES) && SPANISH_STOP_WORDS.contains(word)) {
            return true;
        }

        return false;
    }

}
