/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.utils;

import de.citec.sc.query.CandidateRetriever.Language;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import org.languagetool.AnalyzedToken;
//import org.languagetool.AnalyzedTokenReadings;
//import org.languagetool.tagging.BaseTagger;
//import org.languagetool.tagging.de.GermanTagger;
//import org.languagetool.tagging.en.EnglishTagger;
//import org.languagetool.tagging.es.SpanishTagger;

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

//    private static GermanTagger germanTagger;
//    private static EnglishTagger englishTagger;
//    private static SpanishTagger spanishTagger;
    private static void load() {
        
        lemmaMapEN = new HashMap<>();
        lemmaMapES = new HashMap<>();
        lemmaMapDE = new HashMap<>();

        Set<String> content = FileFactory.readFile("DE_lemmas.txt");
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

        content = FileFactory.readFile("EN_lemmas.txt");
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

        content = FileFactory.readFile("ES_lemmas.txt");
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
//        germanTagger = new GermanTagger();
//        englishTagger = new EnglishTagger();
//        spanishTagger = new SpanishTagger();
    }

    public static Set<String> lemmatize(String token, Language lang) {
        Set<String> lemmas = new HashSet<>();
        
        if(lemmaMapDE == null){
            load();
        }

        switch (lang) {
            case EN:
                if (lemmaMapEN.containsKey(token.toLowerCase())) {
                    lemmas = lemmaMapEN.get(token.toLowerCase());
                }
                break;
            case DE:
                if (lemmaMapDE.containsKey(token.toLowerCase())) {
                    lemmas = lemmaMapDE.get(token.toLowerCase());
                }
                break;
            case ES:
                if (lemmaMapES.containsKey(token.toLowerCase())) {
                    lemmas = lemmaMapES.get(token.toLowerCase());
                }
                break;
        }

        return lemmas;
    }

//    public static Set<String> lemmatize(String token, Language lang) {
//        Set<String> lemmas = new HashSet<>();
//
//        if(englishTagger == null){
//            load();
//        }
//        
//        try {
//
//            List<String> tokens = new ArrayList<>();
//            tokens.add(token);
//
//            BaseTagger tagger = null;
//
//            switch (lang) {
//                case EN:
//                    tagger = englishTagger;
//                    break;
//                case DE:
//                    tagger = germanTagger;
//                    break;
//                case ES:
//                    tagger = spanishTagger;
//                    break;
//            }
//
//            List<AnalyzedTokenReadings> tags = tagger.tag(tokens);
//
//            for (AnalyzedTokenReadings a : tags) {
//                List<AnalyzedToken> analyzedTokens = a.getReadings();
//
//                for (AnalyzedToken a1 : analyzedTokens) {
//                    
//                    if(a1.getLemma() == null){
//                        continue;
//                    }
//                    if(a1.getLemma().equals(token)){
//                        continue;
//                    }
//                    
//                    lemmas.add(a1.getLemma());
//                }
//            }
//
//        } catch (IOException ex) {
//            Logger.getLogger(Lemmatizer.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        return lemmas;
//    }
}
