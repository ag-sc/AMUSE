/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.index;

import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.corpus.QALDCorpus;
import de.citec.sc.main.Main;
import de.citec.sc.qald.CorpusLoader;
import de.citec.sc.query.CandidateRetriever;
import de.citec.sc.query.CandidateRetriever.Language;
import de.citec.sc.query.CandidateRetrieverOnLucene;
import de.citec.sc.query.Instance;
import de.citec.sc.query.Search;
import de.citec.sc.utils.FileFactory;
import de.citec.sc.utils.Lemmatizer;
import de.citec.sc.utils.SortUtils;
import de.citec.sc.utils.Stopwords;
import de.citec.sc.wordNet.WordNetAnalyzer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 *
 * @author sherzod
 */
public class MergeIndex {

    public static void main(String[] args) {

        merge(Language.EN);
        merge(Language.DE);
        merge(Language.ES);
    }

    private static void merge(Language lang) {

        Set<String> tokens = getTokens();

        System.out.println("Merging index files for " + lang);

        System.out.println("Loading anchor files");

        Map<String, HashMap<String, Integer>> mergedIndexMap = new ConcurrentHashMap<>(5000000);

        int count = 0;

        try (Stream<String> stream = Files.lines(Paths.get("indexData/" + lang.name().toLowerCase() + "_resource_anchorFile.txt"))) {
            stream.parallel().forEach(item -> {

                String[] c = item.toString().split("\t");

                if (c.length == 3) {

                    String label = c[0].toLowerCase();
                    String uri = c[1];
                    int freq = Integer.parseInt(c[2]);

                    if (tokens.contains(label)) {
                        if (mergedIndexMap.containsKey(label)) {
                            HashMap<String, Integer> uri2Freq = mergedIndexMap.get(label);

                            if (uri2Freq.containsKey(uri)) {
                                freq = uri2Freq.get(uri) + freq;
                            }

                            uri2Freq.put(uri, freq);

                            mergedIndexMap.put(label, uri2Freq);
                        } else {
                            HashMap<String, Integer> uri2Freq = new HashMap<>();

                            uri2Freq.put(uri, freq);

                            mergedIndexMap.put(label, uri2Freq);
                        }
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Merging dbpedia files");
        //load dbpedia files and merge keys by adding frequency values
        try (Stream<String> stream = Files.lines(Paths.get("indexData/" + lang.name().toLowerCase() + "_resource_dbpediaFile.txt"))) {
            stream.parallel().forEach(item -> {

                String[] c = item.toString().split("\t");

                String label = "";

                if (c.length == 3) {

                    label = c[0].toLowerCase();
                    String uri = c[1];
                    int freq = Integer.parseInt(c[2]);

                    if (mergedIndexMap.containsKey(label)) {
                        HashMap<String, Integer> uri2Freq = mergedIndexMap.get(label);

                        if (uri2Freq.containsKey(uri)) {
                            freq = uri2Freq.get(uri) + freq;
                        }

                        uri2Freq.put(uri, freq);

                        mergedIndexMap.put(label, uri2Freq);
                    } else {
                        HashMap<String, Integer> uri2Freq = new HashMap<>();

                        uri2Freq.put(uri, freq);

                        mergedIndexMap.put(label, uri2Freq);
                    }

                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

        writeIndex(mergedIndexMap, "rawFiles/" + lang.name().toLowerCase() + "/resourceFiles/merged.ttl");
    }

    private static void writeIndex(Map<String, HashMap<String, Integer>> indexMap, String filePath) {
        System.out.println("Saving the file size: " + indexMap.size());

        try {
            PrintStream p = new PrintStream(new File(filePath));
            int counter = 0;
            for (String s : indexMap.keySet()) {

                Map<String, Integer> uri2Freq = SortUtils.sortByValue(indexMap.get(s));

                int c = 0;
                for (String uri : uri2Freq.keySet()) {

                    String k = s + "\t" + uri + "\t" + uri2Freq.get(uri);
                    p.println(k);
                    c++;

                    if (c == 10) {
                        break;
                    }
                }
//                String k = s + "\t" + indexMap.get(s);
//                if (k.contains("hat is one small step 	Apollo_11	2")) {
//                    int z = 1;
//                }
//                p.println(k);

                counter++;

                if (counter % 700000 == 0) {
                    System.out.println((counter / (double) indexMap.size()) + " are saved.");
                }
            }

            System.out.println("\nFile saved.\n");
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

    }

    private static Set<String> getTokens() {
        CandidateRetriever retriever = new CandidateRetrieverOnLucene(false, "luceneIndex");

        WordNetAnalyzer wordNet = new WordNetAnalyzer("src/main/resources/WordNet-3.0/dict");

        Search.load(retriever, wordNet);
        Search.useMatoll(true);

        List<CandidateRetriever.Language> languages = new ArrayList<>();

        languages.add(CandidateRetriever.Language.EN);
        languages.add(CandidateRetriever.Language.DE);
        languages.add(CandidateRetriever.Language.ES);

        Set<String> tokens = new HashSet<>();

        for (Language l : languages) {

            CandidateRetriever.Language lang = l;

            boolean includeYAGO = true;
            boolean includeAggregation = true;
            boolean includeUNION = true;
            boolean onlyDBO = true;
            boolean isHybrid = false;

            Map<String, Set<String>> lemmaMap = new HashMap<>();

            QALDCorpus corpus1 = CorpusLoader.load(CorpusLoader.Dataset.qald6Test, includeYAGO, includeAggregation, includeUNION, onlyDBO, isHybrid);
            QALDCorpus corpus2 = CorpusLoader.load(CorpusLoader.Dataset.qald6Train, includeYAGO, includeAggregation, includeUNION, onlyDBO, isHybrid);

            corpus1.getDocuments().addAll(corpus2.getDocuments());

            for (AnnotatedDocument d1 : corpus1.getDocuments()) {

                if (d1.getParse() != null) {
                    String before = d1.getParse().toString();

                    d1.getParse().mergeEdges();
                    d1.getParse().removeLoops();
                    d1.getParse().removePunctuations();

                    String after = d1.getParse().toString();

                    for (Integer nodeID : d1.getParse().getNodes().keySet()) {
                        String token = d1.getParse().getNodes().get(nodeID).toLowerCase();
                        String pos = d1.getParse().getPOSTag(nodeID);

                        if (Stopwords.isStopWord(token, l)) {
                            continue;
                        }

                        if (pos.equals("NOUN") || pos.equals("VERB") || pos.equals("ADJ") || pos.equals("ADV")) {

                            Set<String> lemmas = Lemmatizer.lemmatize(token, l);

                            if (lemmaMap.containsKey(token)) {
                                Set<String> old = lemmaMap.get(token);

                                old.addAll(lemmas);

                                lemmaMap.put(token, old);
                            } else {
                                lemmaMap.put(token, lemmas);
                            }
                        }
                    }
                }
            }

            String content = "";
            for (String token : lemmaMap.keySet()) {

                tokens.add(token);
                for (String lemma : lemmaMap.get(token)) {
                    tokens.add(lemma);
                }
            }
        }

        return tokens;
    }

}
