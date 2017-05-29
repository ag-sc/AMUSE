package de.citec.sc.main;

import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.corpus.QALDCorpus;
import de.citec.sc.qald.QALDCorpusLoader;
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

public class PreprocessingAnalysis {

    private static final Logger log = LogManager.getFormatterLogger();

    public static void main(String[] args) {
        analysePOSTAGs();
    }

    private static void analysePOSTAGs() {
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

        QALDCorpus trainCorpus = QALDCorpusLoader.load(QALDCorpusLoader.Dataset.qaldSubset, includeYAGO, includeAggregation, includeUNION, onlyDBO, isHybrid);
        QALDCorpus testCorpus = QALDCorpusLoader.load(QALDCorpusLoader.Dataset.qaldSubset, includeYAGO, includeAggregation, includeUNION, onlyDBO, isHybrid);

        List<AnnotatedDocument> trainDocuments = trainCorpus.getDocuments();
        List<AnnotatedDocument> testDocuments = testCorpus.getDocuments();

//        trainDocuments.addAll(testDocuments);
        for (AnnotatedDocument d : trainDocuments) {

            HashMap<Integer, String> nodes = new HashMap<>();
            for (Integer n : d.getParse().getNodes().keySet()) {
                nodes.put(n, d.getParse().getNodes().get(n));
            }

            String before = d.getParse().toString();

            d.getParse().mergeEdges();

            HashMap<Integer, String> afterNodes = new HashMap<>();
            for (Integer n : d.getParse().getNodes().keySet()) {
                afterNodes.put(n, d.getParse().getNodes().get(n));
            }

            String after = d.getParse().toString();

            if (!nodes.equals(afterNodes)) {
                System.out.println(d.getQuestionString() + "\n\n");

                for (Integer n : nodes.keySet()) {
                    if (!afterNodes.containsKey(n)) {
                        System.out.println("Removed : " + nodes.get(n));
                    } else {
                        if (!nodes.get(n).equals(afterNodes.get(n))) {
                            System.out.println("Changed" + nodes.get(n) + "  --> " + afterNodes.get(n));
                        }
                    }

                }

                System.out.println("============================================================================================\n\n");
            }

            if (!before.equals(after)) {

//                System.out.println("Before : \n" + before);
//                System.out.println("\nAfter: \n" + after + "\n============================================================================================\n\n");
            } else {
//                System.out.println("Before : \n"+before);
//                System.out.println("\nAfter: \n"+after+"\n============================================================================================\n\n");
            }
        }
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

        QALDCorpus trainCorpus = QALDCorpusLoader.load(QALDCorpusLoader.Dataset.qald6Train, includeYAGO, includeAggregation, includeUNION, onlyDBO, isHybrid);
        QALDCorpus testCorpus = QALDCorpusLoader.load(QALDCorpusLoader.Dataset.qald6Test, includeYAGO, includeAggregation, includeUNION, onlyDBO, isHybrid);

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
