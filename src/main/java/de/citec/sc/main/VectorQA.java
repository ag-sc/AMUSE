/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.main;

import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.corpus.QALDCorpus;
import de.citec.sc.evaluator.AnswerEvaluator;
import de.citec.sc.nel.AnnotationExtractor;
import de.citec.sc.nel.EntityAnnotation;
import de.citec.sc.nel.StupidQAAnnotationExtractor;
import de.citec.sc.qald.CorpusLoader;
import de.citec.sc.qald.SPARQLParser;
import de.citec.sc.query.CandidateRetriever;
import de.citec.sc.query.CandidateRetrieverOnLucene;
import de.citec.sc.query.CandidateRetrieverOnMemory;
import de.citec.sc.query.DBpediaLabelRetriever;
import de.citec.sc.query.Search;
import de.citec.sc.utils.DBpediaEndpoint;
import de.citec.sc.utils.EmbeddingUtil;
import de.citec.sc.utils.FileFactory;
import de.citec.sc.utils.ProjectConfiguration;
import de.citec.sc.utils.Stopwords;
import de.citec.sc.utils.StringSimilarityUtils;
import de.citec.sc.wordNet.WordNetAnalyzer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author sherzod
 */
public class VectorQA {

    public static void main(String[] args) {

        CandidateRetriever retriever = null;

        String dataPointContent = "@relation SVM_Data\n";
        dataPointContent += "@attribute entityScore numeric\n";
        dataPointContent += "@attribute predicateStringSimScore numeric\n";
        dataPointContent += "@attribute predicateEmbeddingSimScore numeric\n";
        dataPointContent += "@attribute propertyPOSTag {PROPN,VERB,NOUN,ADJ,ADV,ADP}\n";
        dataPointContent += "@attribute class {0,1}\n\n";
        dataPointContent += "@data\n";

        if (ProjectConfiguration.getIndex().equals("lucene")) {
            retriever = new CandidateRetrieverOnLucene(false, "luceneIndex");
        } else {
            retriever = new CandidateRetrieverOnMemory("rawFiles");
        }

        WordNetAnalyzer wordNet = new WordNetAnalyzer("src/main/resources/WordNet-3.0/dict");

        Search.load(retriever, wordNet);

        //accepted postags
        Set<String> linkingValidPOSTags = new HashSet<>();
        linkingValidPOSTags.add("PROPN");
        linkingValidPOSTags.add("VERB");
        linkingValidPOSTags.add("NOUN");
        linkingValidPOSTags.add("ADJ");
        linkingValidPOSTags.add("ADV");
        linkingValidPOSTags.add("ADP");

        //get documents
        QALDCorpus c = CorpusLoader.load(CorpusLoader.Dataset.simpleQuestionsTrain, true, true, true, true, true);

        //create the first file
        FileFactory.writeListToFile(c.getCorpusName() + ".arff", dataPointContent, false);

        int counterValidDocs = 0;
        int counterConstructedDocs = 0;

        List<String> listOfDataPoints = new ArrayList<>();

        for (AnnotatedDocument d : c.getDocuments()) {

            System.out.println(d.toString());

            if (d.getParse() == null) {
                continue;
            }

            counterValidDocs++;

            List<EntityAnnotation> annotations = StupidQAAnnotationExtractor.getAnnotations(d, retriever, linkingValidPOSTags);
            Set<String> expectedURIs = SPARQLParser.extractURIsFromQuery(d.getGoldQueryString());

            if (annotations.isEmpty()) {
                continue;
            }

            for (EntityAnnotation a : annotations) {
                System.out.println(a.print(d.getQuestionString()));
            }
            System.out.println("\n\n");

            boolean hasFound = false;
            //loop over each annotation

            String specificDataPoints = "";
            for (EntityAnnotation e : annotations) {

                //entity URI
                String entityURI = "";
                for (String v : e.getValues()) {
                    entityURI = v;
                }

                String entityNodeText = e.getSpannedText(d.getQuestionString());

                double entityScore = e.getProvenance().getConfidence();

                //get all properties connected to the entity
                String propertyQuery = "SELECT DISTINCT ?p WHERE { {<" + entityURI + "> ?p ?o. } UNION {?s ?p <" + entityURI + "> .}}";
                Set<String> queryResults = DBpediaEndpoint.runQuery(propertyQuery, true);

                //loop over each property
                for (String p : queryResults) {

                    if (!p.startsWith("http://dbpedia.org/ontology/")) {
                        continue;
                    }

                    if (p.startsWith("http://dbpedia.org/ontology/wiki")) {
                        continue;
                    }

                    for (Integer nodeId : d.getParse().getNodes().keySet()) {
                        String nodeText = d.getParse().getNodes().get(nodeId);
                        String postag = d.getParse().getPOSTag(nodeId);

                        if (nodeText.equalsIgnoreCase(entityNodeText)) {
                            continue;
                        }

                        if (!linkingValidPOSTags.contains(postag)) {
                            continue;
                        }
                        if (Stopwords.isStopWord(nodeText, CandidateRetriever.Language.EN)) {
                            continue;
                        }

                        String propertyLabel = DBpediaLabelRetriever.getLabel(p, CandidateRetriever.Language.EN);
                        double embeddingSimilarity = EmbeddingUtil.similarity(nodeText, propertyLabel, CandidateRetriever.Language.EN.name());
                        double stringSimilarity = StringSimilarityUtils.getSimilarityScore(nodeText, propertyLabel);

                        Set<String> extractedURIs = new HashSet<>();
                        extractedURIs.add(entityURI);
                        extractedURIs.add(p);

                        boolean isPositive = isPositiveMatch(expectedURIs, extractedURIs);

                        /**
                         * dataPointContent+="@attribute entityScore numeric\n";
                         * dataPointContent+="@attribute predicateStringSimScore
                         * numeric\n"; dataPointContent+="@attribute
                         * predicateEmbeddingSimScore numeric\n";
                         * dataPointContent+="@attribute propertyPOSTAG
                         * {PROPN,VERB,NOUN,ADJ,ADV,ADP}\n";
                         * dataPointContent+="@attribute class {0,1}\n\n";
                         */
                        String dataPointClass = "0";
                        if (isPositive) {
                            dataPointClass = "1";

                            hasFound = true;
                            System.out.println("EntityScore: " + entityScore + "  PropertyStringSim: " + stringSimilarity + " PropertyEmbeddingSim: " + embeddingSimilarity + " PropPOS: " + postag + "  PropertyURI:" + p.replace("http://dbpedia.org/ontology/", "") + " PropertyNode: " + nodeText + " EntityNode:" + entityNodeText + " EntityURI: " + entityURI.replace("http://dbpedia.org/resource/", ""));
                        }

                        String d1 = entityScore + "," + stringSimilarity + "," + embeddingSimilarity + "," + postag + "," + dataPointClass + "\n";

                        listOfDataPoints.add(d1);

                        if (listOfDataPoints.size() > 1000) {

                            String content = "";
                            for (String s1 : listOfDataPoints) {
                                content += s1;
                            }

                            FileFactory.writeListToFile(c.getCorpusName() + ".arff", content, true);

                            listOfDataPoints.clear();
                        }

//                        specificDataPoints+=entityScore+","+stringSimilarity+","+embeddingSimilarity+","+postag+","+dataPointClass+"\n";
                    }
                }
            }

            if (hasFound) {

                dataPointContent += specificDataPoints;
                counterConstructedDocs++;
            }
        }

        if (!listOfDataPoints.isEmpty()) {
            String content = "";
            for (String s1 : listOfDataPoints) {
                content += s1;
            }

            FileFactory.writeListToFile(c.getCorpusName() + ".arff", content, true);
        }

//        FileFactory.writeListToFile(c.getCorpusName()+".arff", dataPointContent, false);
        System.out.println(counterConstructedDocs + "/" + counterValidDocs + " -> " + (counterConstructedDocs / (double) counterValidDocs));

    }

    private static boolean isPositiveMatch(Set<String> expected, Set<String> extracted) {

        int c = 0;
        for (String exp : expected) {
            if (extracted.contains(exp)) {
                c++;
            }
        }

        double recall = c / (double) expected.size();
        double precision = c / (double) extracted.size();
        double f1 = (2 * precision * recall) / (recall + precision);

        if (f1 == 1.0) {
            return true;
        }

        return false;
    }

    private static String getQuery(String classURI, String entityURI, String propertyURI) {

        if (classURI.equals("date")) {
            String query1 = "SELECT DISTINCT ?o WHERE { <" + entityURI + "> <" + propertyURI + "> ?o. }";

            Set<String> answers1 = DBpediaEndpoint.runQuery(query1, true);
            if (!answers1.isEmpty()) {
                for (String a : answers1) {
                    //check if matches the date regex
                    if (isTokenDataType(a)) {
                        return query1;
                    }
                }
            }

            return "";
        } else {
            String query1 = "SELECT DISTINCT ?o WHERE { <" + entityURI + "> <" + propertyURI + "> ?o. ?o <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + classURI + "> . }";
            String query2 = "SELECT DISTINCT ?o WHERE { ?o <" + propertyURI + "> <" + entityURI + ">. ?o <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + classURI + ">.  }";

            Set<String> answers1 = DBpediaEndpoint.runQuery(query1, true);
            if (!answers1.isEmpty()) {
                return query1;
            }
            Set<String> answers2 = DBpediaEndpoint.runQuery(query2, true);
            if (!answers2.isEmpty()) {
                return query2;
            }

            return "";
        }

    }

    private static boolean isTokenDataType(String token) {
        List<String> patterns = new ArrayList<>();

        String monthYearPattern = "((January|Jan|February|Feb|March|Mar|April|Apr|May|June|Jun|July|Jul|August|Aug|September|Sep|October|Oct|November|Nov|December|Dec)\\s+((19|20)\\d\\d))";//March 2015, Aug 1999
        monthYearPattern += "|((0?[1-9]|1[012])/((19|20)\\d\\d))"; //mm/YYYY
        monthYearPattern += "|((0?[1-9]|1[012])-((19|20)\\d\\d))"; //mm-YYYY

        String datePattern = "((0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((19|20)\\d\\d))";//dd/MM/YYYY
        datePattern += "|((0?[1-9]|[12][0-9]|3[01])-(0?[1-9]|1[012])-((19|20)\\d\\d))"; //dd-MM-YYYY
        datePattern += "|((January|Jan|February|Feb|March|Mar|April|Apr|May|June|Jun|July|Jul|August|Aug|September|Sep|October|Oct|November|Nov|December|Dec)\\s+((1st)|(2nd)|(3rd)|(([1-9]|[12][0-9]|3[01])(th)))(,)?\\s+((19|20)\\d\\d))"; //Jan 1st 2015, August 14th 1999
        datePattern += "|((January|Jan|February|Feb|March|Mar|April|Apr|May|June|Jun|July|Jul|August|Aug|September|Sep|October|Oct|November|Nov|December|Dec)\\s+(0?[1-9]|[12][0-9]|3[01])(,)?\\s+((19|20)\\d\\d))"; //Jan 1st 2015, August 14th 1999
        datePattern += "|((0?[1-9]|[12][0-9]|3[01])\\s+(January|Jan|February|Feb|March|Mar|April|Apr|May|June|Jun|July|Jul|August|Aug|September|Sep|October|Oct|November|Nov|December|Dec)\\s+((19|20)\\d\\d))"; //Jan 1st 2015, August 14th 1999
        datePattern += "|((0?[1-9]|1[012])/(0?[1-9]|[12][0-9]|3[01])/((19|20)\\d\\d))"; //MM/dd/YYYY
        datePattern += "|((0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01])-((19|20)\\d\\d))"; //MM-dd-YYYY

        patterns.add(monthYearPattern);
        patterns.add(datePattern);
        patterns.add("^(http://|https://|www.)+.+$");
        patterns.add("^(true|false)+.*$");
        patterns.add("(^[-+]?\\d+)");
        patterns.add("(^[+]?\\d+)");
        patterns.add("(^[+]?\\d+)");
        patterns.add("(^[-+]?\\d+\\.\\d+)");
        patterns.add("(^[-+]?\\d+\\.\\d+)");
        patterns.add("\\d{4}");

        for (String pattern : patterns) {
            if (token.matches(pattern)) {
                return true;
            }
        }

        return false;
    }
}
