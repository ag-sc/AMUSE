/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.utils;

import de.citec.sc.qald.QALDCorpusLoader;
import de.citec.sc.query.CandidateRetriever;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tagging.de.GermanTagger;
import org.languagetool.tagging.en.EnglishTagger;
import org.languagetool.tagging.es.SpanishTagger;

/**
 *
 * @author sherzod
 *
 * https://github.com/languagetool-org/languagetool
 *
 * https://languagetool.org/
 */
public class Lemmatizer {

    private static Map<String, Set<String>> lemmaMapEN;
    private static Map<String, Set<String>> lemmaMapES;
    private static Map<String, Set<String>> lemmaMapDE;

    private static GermanTagger germanTagger;
    private static EnglishTagger englishTagger;
    private static SpanishTagger spanishTagger;
    private static final String outputDirectory = "lemmas";

    /**
     * writes lemmas to the file
     */
    private static void writeContent(Map<String, Set<String>> map, String fileName) {
        
        File dir = new File(outputDirectory);
        if(!dir.exists()){
            dir.mkdir();
        }
        
        String content = "";

        for (String key : map.keySet()) {
            String c = key;

            for (String s : map.get(key)) {
                c += "\t" + s;
            }

            content += c + "\n";
        }

        FileFactory.writeListToFile(fileName, content, false);

    }

    /**
     * loads saved lemmas from file
     */
    private static void loadContent() {

        lemmaMapEN = new HashMap<>();
        lemmaMapES = new HashMap<>();
        lemmaMapDE = new HashMap<>();

        InputStream inputStream = Lemmatizer.class.getClassLoader().getResourceAsStream(outputDirectory + "/DE_lemmas.txt");
         
        Set<String> content = FileFactory.readFile(inputStream);
        for (String c : content) {
            String[] data = c.split("\t");

            String token = data[0];

            Set<String> lemmas = new HashSet<>();
            if (data.length > 1) {
                for (int i = 1; i < data.length; i++) {
                    lemmas.add(data[i]);
                }
            }

            lemmaMapDE.put(token, lemmas);
        }

        inputStream = Lemmatizer.class.getClassLoader().getResourceAsStream(outputDirectory + "/EN_lemmas.txt");
        
        content = FileFactory.readFile(inputStream);
        for (String c : content) {
            String[] data = c.split("\t");

            String token = data[0];

            Set<String> lemmas = new HashSet<>();
            if (data.length > 1) {
                for (int i = 1; i < data.length; i++) {
                    lemmas.add(data[i]);
                }
            }

            lemmaMapEN.put(token, lemmas);
        }

        inputStream = Lemmatizer.class.getClassLoader().getResourceAsStream(outputDirectory + "/ES_lemmas.txt");
        
        content = FileFactory.readFile(inputStream);
        for (String c : content) {
            String[] data = c.split("\t");

            String token = data[0];

            Set<String> lemmas = new HashSet<>();
            if (data.length > 1) {
                for (int i = 1; i < data.length; i++) {
                    lemmas.add(data[i]);
                }
            }

            lemmaMapES.put(token, lemmas);
        }

    }

    /**
     * @param token
     * @param lang
     * @return Set<String> lemmas
     *
     * returns lemmas for the given token
     */
    public static Set<String> lemmatize(String token, CandidateRetriever.Language lang) {
        Set<String> lemmas = new HashSet<>();

        if (lemmaMapDE == null) {
            loadContent();
        }

        switch (lang) {
            case EN:
                if (lemmaMapEN.containsKey(token.toLowerCase())) {
                    lemmas = lemmaMapEN.get(token.toLowerCase());
                } else {
                    lemmas = lemmatizeNew(token, lang);

                    lemmaMapEN.put(token, lemmas);

                    writeContent(lemmaMapEN, outputDirectory + "/" + lang.name() + "_lemmas.txt");
                }
                break;
            case DE:
                if (lemmaMapDE.containsKey(token.toLowerCase())) {
                    lemmas = lemmaMapDE.get(token.toLowerCase());
                } else {
                    lemmas = lemmatizeNew(token, lang);

                    lemmaMapDE.put(token, lemmas);

                    writeContent(lemmaMapDE, outputDirectory + "/" + lang.name() + "_lemmas.txt");
                }
                break;
            case ES:
                if (lemmaMapES.containsKey(token.toLowerCase())) {
                    lemmas = lemmaMapES.get(token.toLowerCase());
                } else {
                    lemmas = lemmatizeNew(token, lang);

                    lemmaMapES.put(token, lemmas);

                    writeContent(lemmaMapES, outputDirectory + "/" + lang.name() + "_lemmas.txt");
                }
                break;
        }

        return lemmas;
    }

    /**
     * to lemmatize tokens that haven't been lemmatized before
     */
    private static Set<String> lemmatizeNew(String token, CandidateRetriever.Language lang) {
        Set<String> lemmas = new HashSet<>();

        germanTagger = new GermanTagger();
        englishTagger = new EnglishTagger();
        spanishTagger = new SpanishTagger();

        try {

            List<String> tokens = new ArrayList<>();
            tokens.add(token);

            BaseTagger tagger = null;

            switch (lang) {
                case EN:
                    tagger = englishTagger;
                    break;
                case DE:
                    tagger = germanTagger;
                    break;
                case ES:
                    tagger = spanishTagger;
                    break;
            }

            List<AnalyzedTokenReadings> tags = tagger.tag(tokens);

            for (AnalyzedTokenReadings a : tags) {
                List<AnalyzedToken> analyzedTokens = a.getReadings();

                for (AnalyzedToken a1 : analyzedTokens) {

                    if (a1.getLemma() == null) {
                        continue;
                    }
                    if (a1.getLemma().equals(token)) {
                        continue;
                    }

                    lemmas.add(a1.getLemma());
                }
            }

            switch (lang) {
                case EN:
                    lemmaMapEN.put(token, lemmas);
                    break;
                case DE:
                    lemmaMapDE.put(token, lemmas);
                    break;
                case ES:
                    lemmaMapES.put(token, lemmas);
                    break;
            }

        } catch (IOException ex) {
            Logger.getLogger(Lemmatizer.class.getName()).log(Level.SEVERE, null, ex);
        }

        return lemmas;
    }
}
