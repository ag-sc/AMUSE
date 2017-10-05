/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.main;

import de.citec.sc.index.DBpediaIndex;
import de.citec.sc.query.Candidate;
import de.citec.sc.query.CandidateRetriever;
import de.citec.sc.query.CandidateRetrieverOnLucene;
import de.citec.sc.query.ManualLexicon;
import de.citec.sc.query.Search;
import de.citec.sc.wordNet.WordNetAnalyzer;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author sherzod
 */
public class SearchTerms {

    public static void main(String[] args) {
        
        CandidateRetriever r = new CandidateRetrieverOnLucene(false, "luceneIndex");
        System.out.println(r.getAllResources("obama", 10, CandidateRetriever.Language.EN));
        

        WordNetAnalyzer wordNet = new WordNetAnalyzer("src/main/resources/WordNet-3.0/dict");

        Search.load(r, wordNet);
        
        Search.useMatoll(true);

        ManualLexicon.useManualLexicon(true);
        
        CandidateRetriever.Language lang = CandidateRetriever.Language.DE;
        
        System.out.println(ManualLexicon.getProperties("fließt", CandidateRetriever.Language.DE));
        System.out.println(ManualLexicon.getProperties("gestorben", CandidateRetriever.Language.DE));
        System.out.println(ManualLexicon.getProperties("nacido", CandidateRetriever.Language.ES));
        System.out.println(ManualLexicon.getResources("michael jordan", CandidateRetriever.Language.ES));
        System.out.println(ManualLexicon.getClasses("filmen", CandidateRetriever.Language.DE));
        System.out.println(ManualLexicon.getProperties("die", CandidateRetriever.Language.EN));
        System.out.println(ManualLexicon.getResources("nordsee", CandidateRetriever.Language.DE));
        

        String word = "stewie griffin";
        int topK = 100;
        boolean lemmatize = true;
        boolean useWordNet = true;
        boolean mergePartialMatches = false;

        

        Set<Candidate> result = new LinkedHashSet<>();

        long start = System.currentTimeMillis();
        result.addAll(Search.getResources(word, topK, lemmatize, mergePartialMatches, useWordNet, lang));
        System.out.println("Resources: \n");
        result.forEach(System.out::println);

        result = new LinkedHashSet<>();

        System.out.println("======================================\nProperties:\n DBpedia + MATOLL");
        result.addAll(Search.getPredicates(word, topK, lemmatize, mergePartialMatches, useWordNet, lang));
        result.forEach(System.out::println);
        result.clear();

        result = new LinkedHashSet<>();

        result.addAll(Search.getClasses(word, topK, lemmatize, mergePartialMatches, useWordNet, lang));
        System.out.println("======================================\nClasses:\n");
        result.forEach(System.out::println);

        result = new LinkedHashSet<>();

        result.addAll(Search.getRestrictionClasses(word, topK, lemmatize, mergePartialMatches, useWordNet, lang));
        System.out.println("======================================\nRestriction Classes:\n");
        result.forEach(System.out::println);

        long end = System.currentTimeMillis();
        System.out.println((end - start) + " ms");

    }
}
