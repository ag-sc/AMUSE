/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.evaluator;

import de.citec.sc.qald.SPARQLParser;
import de.citec.sc.utils.DBpediaEndpoint;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author sherzod
 */
public class AnswerEvaluator {
    
    /**
     * if normalizeCandidateAnswers is set to true, then the uris will be converted to normal strings.
     * e.g. dbr:Bielefeld_University -> Bielefeld University
     * 
     */
    public static double evaluate(String derived, String goldStandard, boolean normalizeCandidateAnswers) {

        //if the body doesn't have any triples the similarity is zero
        if (SPARQLParser.extractTriplesFromQuery(derived).isEmpty()) {
            return 0;
        }

        Set<String> candidateAnswers = DBpediaEndpoint.runQuery(derived, false);
        Set<String> goldAnswers = DBpediaEndpoint.runQuery(goldStandard, false);
        if(normalizeCandidateAnswers){
            candidateAnswers = normalize(candidateAnswers);
            goldAnswers = normalize(goldAnswers);
        }

        double f1 = getF1(candidateAnswers, goldAnswers);

        return f1;
    }
    /**
     * if normalizeCandidateAnswers is set to true, then the uris will be converted to normal strings.
     * e.g. dbr:Bielefeld_University -> Bielefeld University
     * 
     */
    public static double evaluate(String derived, List<String> expectedAnswers, boolean normalizeCandidateAnswers) {

        //if the body doesn't have any triples the similarity is zero
        if (SPARQLParser.extractTriplesFromQuery(derived).isEmpty()) {
            return 0;
        }

        Set<String> candidateAnswers = DBpediaEndpoint.runQuery(derived, false);
        Set<String> goldAnswers = new HashSet<>(expectedAnswers);
        
        if(normalizeCandidateAnswers){
            candidateAnswers = normalize(candidateAnswers);
            goldAnswers = normalize(goldAnswers);
        }

        double f1 = getF1(candidateAnswers, goldAnswers);

        return f1;
    }

    private static double evaluate(String derived, String goldStandard) {

        //if the body doesn't have any triples the similarity is zero
        if (SPARQLParser.extractTriplesFromQuery(derived).isEmpty()) {
            return 0;
        }

        Set<String> candidateAnswers = DBpediaEndpoint.runQuery(derived, false);

        Set<String> goldAnswers = DBpediaEndpoint.runQuery(goldStandard, false);

        double f1 = getF1(candidateAnswers, goldAnswers);

        return f1;
    }
    
    public static Set<String> normalize(Set<String> set) {
        Set<String> normalized = new HashSet<>();

        for (String s : set) {
            s = s.replace("http://dbpedia.org/resource/", "");
            s = s.replace("_", " ");
            s = s.replace("\"", "");
            s = s.replace("@en", "");
            s = s.trim();
            s = s.toLowerCase();

            normalized.add(s);
        }

        return normalized;
    }

    private static double getF1(Set<String> a, Set<String> b) {
        
//        Set<String> first = new HashSet<>();
//        first.addAll(a);
//        Set<String> second = new HashSet<>();
//        second.addAll(b);

        int r = 0;

        for (String s : a) {
            if (b.contains(s)) {
                r++;
            }
        }

//        if(a.isEmpty() && b.isEmpty()){
//            return 1.0;
//            
//        }
        double recall = (double) r / (double) b.size();
        double precision = (double) r / (double) a.size();

        double f1 = (2 * precision * recall) / (precision + recall);

        if (Double.isNaN(f1)) {

            f1 = 0;
        }

        return f1;

    }
}
