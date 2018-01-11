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
import de.citec.sc.query.CandidateRetriever;
import de.citec.sc.query.CandidateRetrieverOnLucene;
import de.citec.sc.query.CandidateRetrieverOnMemory;
import de.citec.sc.query.DBpediaLabelRetriever;
import de.citec.sc.query.Search;
import de.citec.sc.utils.DBpediaEndpoint;
import de.citec.sc.utils.EmbeddingUtil;
import de.citec.sc.utils.ProjectConfiguration;
import de.citec.sc.wordNet.WordNetAnalyzer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author sherzod
 */
public class StupidQA {

    public static void main(String[] args) {

        CandidateRetriever retriever = null;

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
        QALDCorpus c = CorpusLoader.load(CorpusLoader.Dataset.webQuestionsSubset, true, true, true, true, true);

        double macroF1 = 0;

        int counter = 0;
        for (AnnotatedDocument d : c.getDocuments()) {

            System.out.println(d.toString());

            if (d.getParse() == null) {
                continue;
            }

            if (d.getParse().getToken(1) == null) {
                continue;
            }

            counter++;

            String firstWord = d.getParse().getToken(1);

            String classURI = getType(firstWord);

            List<EntityAnnotation> annotations = StupidQAAnnotationExtractor.getAnnotations(d,retriever, linkingValidPOSTags);

            if (annotations.isEmpty()) {
                continue;
            }
            

            for (EntityAnnotation a : annotations) {
                System.out.println(a.print(d.getQuestionString()));
            }
            System.out.println("\n\n");
            

            EntityAnnotation maxAnnotation = annotations.get(0);

            String constructedQuery = "";
            double maxQueryScore = 0;
            
            for(EntityAnnotation e : annotations){
                
                String entityURI = "";
                for(String v : e.getValues()){
                    entityURI = v;
                }
                
                
                String propertyQuery = "SELECT DISTINCT ?p WHERE { {<" + entityURI + "> ?p ?o. } UNION {?s ?p <" + entityURI + "> .}}";
                Set<String> queryResults = DBpediaEndpoint.runQuery(propertyQuery, true);

            }

            for (Integer nodeId : d.getParse().getNodes().keySet()) {
                String nodeText = d.getParse().getNodes().get(nodeId);

                String spannedText = maxAnnotation.getSpannedText(d.getQuestionString());

                String entityURI = (String) maxAnnotation.getValues().toArray()[0];

                //get all properties from DBpedia
                String propertyQuery = "SELECT DISTINCT ?l ?p WHERE { {<" + entityURI + "> ?p ?o. } UNION {?s ?p <" + entityURI + "> .}}";
                Set<String> queryResults = DBpediaEndpoint.runQuery(propertyQuery, true);

                if (nodeText.equals(spannedText)) {

                    //get all dependent nodes
                    List<Integer> depNodes = d.getParse().getDependentNodes(nodeId, linkingValidPOSTags);
//                        List<Integer> siblings = d.getParse().getSiblings(nodeId, linkingValidPOSTags);
                    Integer headNode = d.getParse().getParentNode(nodeId);

                    List<Integer> connectedNodes = new ArrayList<>();
                    connectedNodes.addAll(depNodes);
//                        connectedNodes.addAll(siblings);
                    connectedNodes.add(headNode);

                    String maxDepNodeURI = "";
                    for (Integer depNodeId : connectedNodes) {

                        //compare the depNodeText via EmbeddingService to find the maximum
                        String depNodeText = d.getParse().getNodes().get(depNodeId);

                        for (String p : queryResults) {

                            if (!p.startsWith("http://dbpedia.org/ontology/")) {
                                continue;
                            }

                            if (p.startsWith("http://dbpedia.org/ontology/wiki")) {
                                continue;
                            }

                            String label = DBpediaLabelRetriever.getLabel(p, CandidateRetriever.Language.EN);

                            double embeddingSimilarity = EmbeddingUtil.similarity(depNodeText, label, "EN");

                            String query = getQuery(classURI, entityURI, p);

                            if (!query.isEmpty()) {
//                                    System.out.println(entityURI + "  " + p + "   " + embeddingSimilarity);
//                                    System.out.println(query);

                                if (embeddingSimilarity > maxQueryScore) {
                                    maxQueryScore = embeddingSimilarity;
                                    constructedQuery = query;
                                }
                            }
                        }
                    }
                }
            }

//                System.out.println("\t" + e.print(d.getQuestionString()));
            double f1 = AnswerEvaluator.evaluate(d.getGoldQueryString(), constructedQuery, false);
            System.out.println(constructedQuery + "   score : " + f1);
            System.out.println("\n=================================================================================================================\n");

            macroF1 += f1;
        }

        System.out.println("\n\nMacroF1 : " + (macroF1 / counter));
    }

    private static String getType(String word) {
        String type = "";

        switch (word) {
            case "where":
                type = "http://dbpedia.org/ontology/Place";
                break;
            case "when":
                type = "date";
                break;
            default:
                type = "http://www.w3.org/2002/07/owl#Thing";
                break;
        }

        return type;
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
