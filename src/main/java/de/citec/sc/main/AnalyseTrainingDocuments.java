package de.citec.sc.main;

import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.corpus.QALDCorpus;
import de.citec.sc.qald.CorpusLoader;
import de.citec.sc.query.CandidateRetriever;
import de.citec.sc.query.CandidateRetrieverOnLucene;
import de.citec.sc.query.ManualLexicon;
import de.citec.sc.query.Search;
import de.citec.sc.utils.DBpediaEndpoint;
import de.citec.sc.utils.SortUtils;
import de.citec.sc.wordNet.WordNetAnalyzer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AnalyseTrainingDocuments {

    private static final Logger log = LogManager.getFormatterLogger();

    public static void main(String[] args) {
        analysePOSTAGs();
    }

    private static void analysePOSTAGs() {
        CandidateRetriever retriever = new CandidateRetrieverOnLucene(true, "luceneIndexe");

        WordNetAnalyzer wordNet = new WordNetAnalyzer("src/main/resources/WordNet-3.0/dict");

        Search.load(retriever, wordNet);
        Search.useMatoll(true);
        ManualLexicon.useManualLexicon(true);

        boolean includeYAGO = false;
        boolean includeAggregation = false;
        boolean includeUNION = false;
        boolean onlyDBO = true;
        boolean isHybrid = false;

        QALDCorpus trainCorpus = CorpusLoader.load(CorpusLoader.Dataset.qald6Train, includeYAGO, includeAggregation, includeUNION, onlyDBO, isHybrid);
        QALDCorpus testCorpus = CorpusLoader.load(CorpusLoader.Dataset.qald6Test, includeYAGO, includeAggregation, includeUNION, onlyDBO, isHybrid);

        List<AnnotatedDocument> trainDocuments = trainCorpus.getDocuments();
        List<AnnotatedDocument> testDocuments = testCorpus.getDocuments();

        trainDocuments.addAll(testDocuments);

        int c = 0;

        HashMap<String, HashMap<String, Integer>> postagMAP = new LinkedHashMap<>();

        for (AnnotatedDocument d : trainDocuments) {

            String before = d.toString();
            d.getParse().mergeEdges();
            String after = d.toString();

//            System.out.println(d);
            for (Integer node : d.getParse().getNodes().keySet()) {

                String pos = d.getParse().getPOSTag(node);

                String token = d.getParse().getToken(node);
                token = token.toLowerCase();

//                if (pos.startsWith("NN") || pos.startsWith("JJ") || pos.startsWith("VB")) {
                if (postagMAP.containsKey(pos)) {
                    HashMap<String, Integer> values = postagMAP.get(pos);
                    values.put(token, values.getOrDefault(token, 1) + 1);
                    postagMAP.put(pos, values);
                } else {
                    HashMap<String, Integer> values = new HashMap<>();
                    values.put(token, 1);

                    postagMAP.put(pos, values);
                }

//                }
            }
        }

        for (String pos : postagMAP.keySet()) {
            if (pos.startsWith("NN") || pos.startsWith("JJ") || pos.startsWith("VB")) {
                continue;
            }
            System.out.println("POS: " + pos);

            HashMap<String, Integer> map = postagMAP.get(pos);
            map = SortUtils.sortByValue(map);

            int count = 0;
            for (String s : map.keySet()) {
                if (count >= 5) {
                    break;
                }
                if (map.get(s) > 0) {
                    System.out.print(s + " : " + map.get(s) + " -- ");
                    count++;
                }
            }

            System.out.println("============================\n");
        }

//        System.out.println("POSTAGS: " + postags);
//        
//        frequentWords = SortUtils.sortByValue(frequentWords);
//        
//        for(String w : frequentWords.keySet()){
//            if(frequentWords.get(w) > 3){
//                System.out.println("Word: " +w + "  Freq: " + frequentWords.get(w));
//            }
//        }
    }

    private static void analyseDependency() {
        CandidateRetriever retriever = new CandidateRetrieverOnLucene(true, "luceneIndex");

        WordNetAnalyzer wordNet = new WordNetAnalyzer("src/main/resources/WordNet-3.0/dict");

        Search.load(retriever, wordNet);
        Search.useMatoll(true);
        ManualLexicon.useManualLexicon(true);

        boolean includeYAGO = false;
        boolean includeAggregation = false;
        boolean includeUNION = false;
        boolean onlyDBO = true;
        boolean isHybrid = false;

        QALDCorpus trainCorpus = CorpusLoader.load(CorpusLoader.Dataset.qald6Train, includeYAGO, includeAggregation, includeUNION, onlyDBO, isHybrid);
        QALDCorpus testCorpus = CorpusLoader.load(CorpusLoader.Dataset.qald6Test, includeYAGO, includeAggregation, includeUNION, onlyDBO, isHybrid);

        List<AnnotatedDocument> trainDocuments = trainCorpus.getDocuments();
        List<AnnotatedDocument> testDocuments = testCorpus.getDocuments();

        int c = 0;
        for (AnnotatedDocument d : trainDocuments) {
            boolean hasRelation = false;

            for (Integer node : d.getParse().getNodes().keySet()) {

                String pos = d.getParse().getPOSTag(node);

                if (pos.startsWith("NN") || pos.startsWith("JJ")) {
                    Integer headNode = d.getParse().getParentNode(node);

                    try {
                        if (headNode != -1) {
                            String headPOS = d.getParse().getPOSTag(headNode);

                            if (!headPOS.startsWith("VB")) {
                                c++;

                                System.out.println(node + " -- " + headNode);

                                hasRelation = true;
                            }
                        }
                    } catch (Exception e) {

                        System.out.println(d);
                        int z = 1;
                    }
                }
            }
            if (hasRelation) {
                System.out.println(d);
                System.out.println("==========================================================");
            }
        }

        System.out.println(c);
    }

    private static List<AnnotatedDocument> getDocuments(QALDCorpus corpus) {
        List<AnnotatedDocument> documents = new ArrayList<>();

        for (AnnotatedDocument d1 : corpus.getDocuments()) {
//            String question = d1.getQuestionString();

//            if (d1.getQaldInstance().getAggregation().equals("false") && d1.getQaldInstance().getOnlyDBO().equals("true") && d1.getQaldInstance().getHybrid().equals("false")) {
            if (DBpediaEndpoint.isValidQuery(d1.getGoldQueryString(), false)) {

                d1.getParse().mergeEdges();
                documents.add(d1);
            }
//            }
        }

        return documents;
    }
}
