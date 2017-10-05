/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.query;

import de.citec.sc.query.CandidateRetriever.Language;
import de.citec.sc.utils.FileFactory;
import de.citec.sc.utils.ProjectConfiguration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author sherzod
 */
public class EmbeddingLexicon {

    private static HashMap<String, LinkedHashSet<String>> lexiconPropertiesEN;
    private static HashMap<String, LinkedHashSet<String>> lexiconClassesEN;
    private static HashMap<String, LinkedHashSet<String>> lexiconRestrictionClassesEN;
    private static HashMap<String, LinkedHashSet<String>> lexiconResourcesEN;

    private static HashMap<String, LinkedHashSet<String>> lexiconPropertiesDE;
    private static HashMap<String, LinkedHashSet<String>> lexiconClassesDE;
    private static HashMap<String, LinkedHashSet<String>> lexiconRestrictionClassesDE;
    private static HashMap<String, LinkedHashSet<String>> lexiconResourcesDE;

    private static HashMap<String, LinkedHashSet<String>> lexiconPropertiesES;
    private static HashMap<String, LinkedHashSet<String>> lexiconClassesES;
    private static HashMap<String, LinkedHashSet<String>> lexiconRestrictionClassesES;
    private static HashMap<String, LinkedHashSet<String>> lexiconResourcesES;

    public static boolean useEmbedding = false;

    private static boolean loaded = false;

    public static void useEmbdeding(boolean b) {
        useEmbedding = b;
        if (b) {
            load();
        }
    }

    public static void load() {
        lexiconPropertiesEN = new LinkedHashMap<>();
        lexiconClassesEN = new LinkedHashMap<>();
        lexiconRestrictionClassesEN = new LinkedHashMap<>();
        lexiconResourcesEN = new LinkedHashMap<>();

        lexiconPropertiesDE = new LinkedHashMap<>();
        lexiconClassesDE = new LinkedHashMap<>();
        lexiconRestrictionClassesDE = new LinkedHashMap<>();
        lexiconResourcesDE = new LinkedHashMap<>();

        lexiconPropertiesES = new LinkedHashMap<>();
        lexiconClassesES = new LinkedHashMap<>();
        lexiconRestrictionClassesES = new LinkedHashMap<>();
        lexiconResourcesES = new LinkedHashMap<>();

        if (ProjectConfiguration.useEmbeddingLexicon() || useEmbedding) {

            String[] languages = new String[3];
            languages[0] = "EN";
            languages[1] = "DE";
            languages[2] = "ES";

            for (String language : languages) {
                //w2v+matoll-de-100-plain-exclude-stopwords=True-ranking.txt
                Set<String> content = FileFactory.readFile("w2v_output/w2v-" + language.toLowerCase() + "-100-plain-exclude-stopwords=True-ranking.txt");

                for (String c : content) {
                    //calories	http://dbpedia.org/ontology/totalPopulation	0.548456	datatype_original
                    String[] data = c.split("\t");

                    String surfaceForm = data[0];
                    String uri = data[1];
                    double similarity = Double.parseDouble(data[2]);
                    String source = data[3];

                    if (similarity <= 0.4) {
                        continue;
                    }
//                    if (similarity <= 0.4 && language.equals("DE")) {
//                        continue;
//                    }
//                    if (similarity <= 0.4 && language.equals("ES")) {
//                        continue;
//                    }

                    if (uri.split(",").length > 1) {
                        continue;
                    }

                    switch (language) {
                        case "EN":
                            //classes, restriction classes
                            if (uri.contains("###")) {
                                if (uri.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#type###")) {
                                    addLexicon(surfaceForm, uri, lexiconClassesEN);
                                } else {
                                    addLexicon(surfaceForm, uri, lexiconRestrictionClassesEN);
                                }
                            } else {
                                if (uri.startsWith("http://dbpedia.org/ontology/") || uri.startsWith("http://dbpedia.org/property/")) {
                                    addLexicon(surfaceForm, uri, lexiconPropertiesEN);
                                } else if (uri.startsWith("http://dbpedia.org/resource/")) {
                                    addLexicon(surfaceForm, uri, lexiconResourcesEN);
                                }
                            }

                            break;
                        case "DE":
                            //classes, restriction classes
                            if (uri.contains("###")) {
                                if (uri.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#type###")) {
                                    addLexicon(surfaceForm, uri, lexiconClassesDE);
                                } else {
                                    addLexicon(surfaceForm, uri, lexiconRestrictionClassesDE);
                                }
                            } else {
                                if (uri.startsWith("http://dbpedia.org/ontology/") || uri.startsWith("http://dbpedia.org/property/")) {
                                    addLexicon(surfaceForm, uri, lexiconPropertiesDE);
                                } else if (uri.startsWith("http://dbpedia.org/resource/")) {
                                    addLexicon(surfaceForm, uri, lexiconResourcesDE);
                                }
                            }

                            break;
                        case "ES":
                            //classes, restriction classes
                            if (uri.contains("###")) {
                                if (uri.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#type###")) {
                                    addLexicon(surfaceForm, uri, lexiconClassesES);
                                } else {
                                    addLexicon(surfaceForm, uri, lexiconRestrictionClassesES);
                                }
                            } else {
                                if (uri.startsWith("http://dbpedia.org/ontology/") || uri.startsWith("http://dbpedia.org/property/")) {
                                    addLexicon(surfaceForm, uri, lexiconPropertiesES);
                                } else if (uri.startsWith("http://dbpedia.org/resource/")) {
                                    addLexicon(surfaceForm, uri, lexiconResourcesES);
                                }
                            }

                            break;

                    }
                }
            }

        }

        loaded = true;

    }

    private static void addLexicon(String key, String value, HashMap<String, LinkedHashSet<String>> map) {

        key = key.toLowerCase().trim();
        value = value.trim();

        if (map.containsKey(key)) {
            LinkedHashSet<String> set = map.get(key);
            set.add(value);
            map.put(key, set);
        } else {
            LinkedHashSet<String> set = new LinkedHashSet<>();
            set.add(value);
            map.put(key, set);
        }
    }

    public static Set<String> getProperties(String term, Language lang) {

        term = term.toLowerCase();
        List<String> list = new ArrayList<>();

        if (!loaded) {
            load();
        }

        switch (lang) {
            case EN:
                if (lexiconPropertiesEN.containsKey(term)) {
                    list.addAll(lexiconPropertiesEN.get(term));
                }
                break;
            case DE:
                if (lexiconPropertiesDE.containsKey(term)) {
                    list.addAll(lexiconPropertiesDE.get(term));
                }
                break;
            case ES:
                if (lexiconPropertiesES.containsKey(term)) {
                    list.addAll(lexiconPropertiesES.get(term));
                }
                break;
        }
        return getResult(list, lang);
    }
    
    private static Set<String> getResult(List<String> list, Language lang){
        Set<String> result = new LinkedHashSet<>();
        
        switch (lang) {
            case EN:
                if (list.size() > 20) {
                    result.addAll(list.subList(0, 20));
                }
                break;
            case DE:
                if (list.size() > 10) {
                    result.addAll(list.subList(0, 10));
                }
                break;
            case ES:
                if (list.size() > 10) {
                    result.addAll(list.subList(0, 10));
                }
                break;
        }
        
        if(result.isEmpty()){
            result.addAll(list);
        }
        
        return result;
    }

    public static Set<String> getRestrictionClasses(String term, Language lang) {

        term = term.toLowerCase();
        List<String> list = new ArrayList<>();

        if (!loaded) {
            load();
        }

        switch (lang) {
            case EN:
                if (lexiconRestrictionClassesEN.containsKey(term)) {
                    list.addAll(lexiconRestrictionClassesEN.get(term));
                }
                break;
            case DE:
                if (lexiconRestrictionClassesDE.containsKey(term)) {
                    list.addAll(lexiconRestrictionClassesDE.get(term));
                }
                break;
            case ES:
                if (lexiconRestrictionClassesES.containsKey(term)) {
                    list.addAll(lexiconRestrictionClassesES.get(term));
                }
                break;
        }
        return getResult(list, lang);
    }

    public static Set<String> getClasses(String term, Language lang) {

        term = term.toLowerCase();
        List<String> list = new ArrayList<>();

        if (!loaded) {
            load();
        }

        switch (lang) {
            case EN:
                if (lexiconClassesEN.containsKey(term)) {
                    list.addAll(lexiconClassesEN.get(term));
                }
                break;
            case DE:
                if (lexiconClassesDE.containsKey(term)) {
                    list.addAll(lexiconClassesDE.get(term));
                }
                break;
            case ES:
                if (lexiconClassesES.containsKey(term)) {
                    list.addAll(lexiconClassesES.get(term));
                }
                break;
        }

        return getResult(list, lang);
    }

    public static Set<String> getResources(String term, Language lang) {

        term = term.toLowerCase();
        List<String> list = new ArrayList<>();

        if (!loaded) {
            load();
        }

        switch (lang) {
            case EN:
                if (lexiconResourcesEN.containsKey(term)) {
                    list.addAll(lexiconResourcesEN.get(term));
                }
                break;
            case DE:
                if (lexiconResourcesDE.containsKey(term)) {
                    list.addAll(lexiconResourcesDE.get(term));
                }
                break;
            case ES:
                if (lexiconResourcesES.containsKey(term)) {
                    list.addAll(lexiconResourcesES.get(term));
                }
                break;
        }

        return getResult(list, lang);
    }
}
