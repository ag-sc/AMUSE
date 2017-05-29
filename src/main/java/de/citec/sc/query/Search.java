/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.query;

import de.citec.sc.main.Main;
import de.citec.sc.parser.StanfordParser;
import de.citec.sc.query.CandidateRetriever.Language;
import de.citec.sc.utils.FileFactory;
import de.citec.sc.wordNet.WordNetAnalyzer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author sherzod
 */
public class Search {

    private static CandidateRetriever retriever;
    private static WordNetAnalyzer wordNet;
    private static boolean useMatoll = false;

    private static HashMap<String, Set<Candidate>> cacheProperties;
    private static HashMap<String, Set<Candidate>> cacheResources;
    private static HashMap<String, Set<Candidate>> cacheClasses;
    private static HashMap<String, Set<Candidate>> cacheRestrictionClasses;
    private static HashMap<String, Integer> propertyFrequency;

    private static final Comparator<Candidate> dbpediaScoreComparator = new Comparator<Candidate>() {

        @Override
        public int compare(Candidate s1, Candidate s2) {

            if (s1.getDbpediaScore() > s2.getDbpediaScore()) {
                return -1;
            } else if (s1.getDbpediaScore() < s2.getDbpediaScore()) {
                return 1;
            }

            return 0;
        }
    };

    public static void useMatoll(boolean b) {
        useMatoll = b;
    }

    private static final Comparator<Candidate> matollScoreComparator = new Comparator<Candidate>() {

        @Override
        public int compare(Candidate s1, Candidate s2) {

            if (s1.getMatollScore() > s2.getMatollScore()) {
                return -1;
            } else if (s1.getMatollScore() < s2.getMatollScore()) {
                return 1;
            }

            return 0;
        }
    };

    private static void loadFrequency() {

        propertyFrequency = new HashMap<>();

        String fileName = "rawIndexFiles/frequencyFiles/propertyFrequency.ttl";
        Set<String> lines = FileFactory.readFile(fileName);
        String maxURI = "";
        for (String line : lines) {

            String property = line.split("\t")[0];
            int freq = Integer.parseInt(line.split("\t")[1]);

            propertyFrequency.put(property, freq);
        }
    }

    public static int getPriorScore(String uri) {
        int score = -1;

        if (propertyFrequency.containsKey(uri)) {
            score = propertyFrequency.get(uri);
        }

        return score;
    }

    public static void load() {
        if (retriever == null) {
            retriever = new CandidateRetrieverOnLucene(false, "luceneIndex");
        }

        if (wordNet == null) {
            wordNet = new WordNetAnalyzer("src/main/resources/WordNet-3.0/dict");
        }

        loadFrequency();
    }

    public static void load(CandidateRetriever r, WordNetAnalyzer w) {
        retriever = r;

        wordNet = w;

        loadFrequency();
    }

    public static boolean matches(String mergedTokens, Language lang) {

        //if the merged tokens contain any upper case character, then all tokens must start with uppercase
        //else return false
        boolean hasUppercase = !mergedTokens.equals(mergedTokens.toLowerCase());
        if (hasUppercase) {

            String[] tokens = mergedTokens.split(" ");

            for (String t : tokens) {
                String character = t.charAt(0) + "";
                if (character.equals(character.toLowerCase())) {
                    return false;
                }
            }
        }
        
        if(!ManualLexicon.getClasses(mergedTokens, lang).isEmpty()){
            return true;
        }
        if(!ManualLexicon.getProperties(mergedTokens, lang).isEmpty()){
            return true;
        }
        if(!ManualLexicon.getResources(mergedTokens, lang).isEmpty()){
            return true;
        }
        if(!ManualLexicon.getRestrictionClasses(mergedTokens, lang).isEmpty()){
            return true;
        }

        Set<Candidate> r1 = getResources(mergedTokens, 10, false, false, false, lang);
        Set<Candidate> r2 = getResources(mergedTokens, 10, false, false, false, CandidateRetriever.Language.EN);

        if (!r1.isEmpty() || !r2.isEmpty()) {
            return true;
        }

        Set<Candidate> p = getPredicates(mergedTokens, 10, true, false, false, lang);

        if (!p.isEmpty()) {
            return true;
        }

        Set<Candidate> c = getClasses(mergedTokens, 10, true, false, true, lang);
        if (!c.isEmpty()) {
            return true;
        }

        Set<Candidate> rc = getRestrictionClasses(mergedTokens, 10, true, false, false, lang);
        if (!rc.isEmpty()) {
            return true;
        }

        return false;
    }

    public static Set<Candidate> getResources(String searchTerm, int topK, boolean lemmatize, boolean partialMatch, boolean useWordNet, Language lang) {
        Set<Candidate> instances = new LinkedHashSet<>();

        if (cacheResources == null) {
            cacheResources = new HashMap<>();
        }

        if (cacheResources.containsKey(searchTerm)) {
            instances = cacheResources.get(searchTerm);

            return instances;
        }

        Set<String> queryTerms = getQueryTerms(searchTerm, lemmatize, useWordNet, lang);

        List<Instance> result = new ArrayList<>();

        for (String queryTerm : queryTerms) {

            List<Instance> matches = retriever.getAllResources(queryTerm, topK, lang);

            for (Instance i : matches) {
                if (!result.contains(i)) {
                    result.add(i);
                }
            }
        }

        //normalize and sort descending order
        instances = normalize(result, true, topK);

        cacheResources.put(searchTerm, instances);

        return instances;
    }

    public static Set<Candidate> getPredicates(String searchTerm, int topK, boolean lemmatize, boolean partialMatch, boolean useWordNet, Language lang) {

        Set<Candidate> instances = new LinkedHashSet<>();

        if (cacheProperties == null) {
            cacheProperties = new HashMap<>();
        }

        if (cacheProperties.containsKey(searchTerm)) {
            instances = cacheProperties.get(searchTerm);

            return instances;
        }

        Set<String> queryTerms = getQueryTerms(searchTerm, lemmatize, useWordNet, lang);

        List<Instance> resultDBpedia = new ArrayList<>();
        List<Instance> resultMATOLL = new ArrayList<>();

        //get from dbpedia index
        for (String queryTerm : queryTerms) {

            List<Instance> matches = retriever.getPredicatesInDBpedia(queryTerm, topK, partialMatch, lang);

            for (Instance i : matches) {
                if (!resultDBpedia.contains(i)) {
                    resultDBpedia.add(i);
                }
            }
        }

        //if it's not set to true return predicates in DBpedia, normalize all scores
        if (!useMatoll) {

            //normalize and sort descending order
            instances = normalize(resultDBpedia, true, topK);

            return instances;
        } else {
            instances = normalize(resultDBpedia, true, topK);
        }

        //get MATOLL predicates
        for (String queryTerm : queryTerms) {

            List<Instance> matches = retriever.getPredicatesInMatoll(queryTerm, topK, CandidateRetriever.Language.EN);

            for (Instance i : matches) {
                if (!resultMATOLL.contains(i)) {
                    resultMATOLL.add(i);
                }
            }
        }

        //normalize and sort descending order
        Set<Candidate> matollInstances = normalize(resultMATOLL, false, topK);

        for (Candidate i : matollInstances) {

            instances.add(i);

            if (instances.size() == topK) {
                break;
            }
        }

        cacheProperties.put(searchTerm, instances);

        return instances;
    }

    public static Set<Candidate> getClasses(String searchTerm, int topK, boolean lemmatize, boolean partialMatch, boolean useWordNet, Language lang) {
        Set<Candidate> instances = new LinkedHashSet<>();

        if (cacheClasses == null) {
            cacheClasses = new HashMap<>();
        }

        if (cacheClasses.containsKey(searchTerm)) {
            instances = cacheClasses.get(searchTerm);

            return instances;
        }

        Set<String> queryTerms = getQueryTerms(searchTerm, lemmatize, useWordNet, lang);

        List<Instance> result = new ArrayList<>();

        for (String queryTerm : queryTerms) {

            List<Instance> matches = retriever.getAllClasses(queryTerm, topK, partialMatch, lang);

            for (Instance i : matches) {

                i.setUri("http://www.w3.org/1999/02/22-rdf-syntax-ns#type###" + i.getUri());

                if (!result.contains(i)) {
                    result.add(i);
                }
            }
        }

        //normalize and sort descending order
        instances = normalize(result, true, topK);

        //put to cache
        cacheClasses.put(searchTerm, instances);

        return instances;
    }

    private static Set<Candidate> normalize(List<Instance> m, boolean dbpedia, int topK) {
        List<Candidate> result = new ArrayList<>();

        double max = 0;

        for (Instance i : m) {
            if (i.getFreq() > max) {
                max = i.getFreq();
            }
        }

        for (Instance i : m) {
            double score = i.getFreq() / max;
            Candidate ins = null;

            int priorFreqScore = getPriorScore(i.getUri());

            if (dbpedia) {
                ins = new Candidate(i, score, 0, priorFreqScore);
            } else {
                ins = new Candidate(i, 0, score, priorFreqScore);
            }

            result.add(ins);
        }

        if (dbpedia) {
            Collections.sort(result, dbpediaScoreComparator);
        } else {
            Collections.sort(result, matollScoreComparator);
        }

        Set<Candidate> normalizedCandidates = new LinkedHashSet<>();

        for (Candidate c : result) {
            normalizedCandidates.add(c);

            if (normalizedCandidates.size() == topK) {
                break;
            }
        }

        return normalizedCandidates;
    }

    public static Set<Candidate> getRestrictionClasses(String searchTerm, int topK, boolean lemmatize, boolean partialMatch, boolean useWordNet, Language lang) {
        Set<Candidate> instances = new LinkedHashSet<>();

        //if it's not set to true return empty list
        if (!useMatoll) {
            return instances;
        }

        if (cacheRestrictionClasses == null) {
            cacheRestrictionClasses = new HashMap<>();
        }

        if (cacheRestrictionClasses.containsKey(searchTerm)) {
            instances = cacheRestrictionClasses.get(searchTerm);

            return instances;
        }

        Set<String> queryTerms = getQueryTerms(searchTerm, lemmatize, useWordNet, lang);

        List<Instance> result = new ArrayList<>();

        for (String queryTerm : queryTerms) {

            List<Instance> matches = retriever.getRestrictionClasses(queryTerm, topK, lang);

            for (Instance i : matches) {
                if (!result.contains(i)) {
                    result.add(i);
                } else {
                    int old = result.get(result.indexOf(i)).getFreq();
                    //add new value to old
                    result.get(result.indexOf(i)).setFreq(i.getFreq() + old);
                }
            }

        }

        //normalize and sort descending order
        instances = normalize(result, false, topK);

        cacheRestrictionClasses.put(searchTerm, instances);

        return instances;
    }

    private static Set<String> getQueryTerms(String searchTerm, boolean lemmatize, boolean useWordNet, Language lang) {

        Set<String> queryTerms = new LinkedHashSet<>();
        queryTerms.add(searchTerm);
        queryTerms.add(searchTerm + "~");

        //lemmatize 
        if (lemmatize) {
            try {
                String lemmatized = StanfordParser.lemmatize(searchTerm, lang);

                if (!queryTerms.contains(lemmatized)) {
                    queryTerms.add(lemmatized);
                    queryTerms.add(lemmatized + "~");
                }
            } catch (Exception e) {

            }

        }
        if (useWordNet) {
            //wordnet derivational words
            Set<String> wordNetTerms = wordNet.getDerivationalWords(searchTerm);
            for (String w1 : wordNetTerms) {
                if (!queryTerms.contains(w1)) {
                    queryTerms.add(w1);
                }
            }
        }
        return queryTerms;
    }

}
