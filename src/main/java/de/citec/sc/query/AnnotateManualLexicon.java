/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.query;

import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.corpus.QALDCorpus;
import de.citec.sc.main.Main;
import de.citec.sc.qald.QALDCorpusLoader;
import de.citec.sc.qald.SPARQLParser;
import de.citec.sc.qald.Triple;
import de.citec.sc.query.CandidateRetriever.Language;
import de.citec.sc.utils.FileFactory;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 *
 * @author sherzod
 */
public class AnnotateManualLexicon {

    public static void main(String[] args) throws UnsupportedEncodingException {

        Scanner scannner = new Scanner(System.in);

        boolean includeYAGO = true;
        boolean includeAggregation = true;
        boolean includeUNION = true;
        boolean onlyDBO = false;
        boolean isHybrid = false;

        List<CandidateRetriever.Language> languages = new ArrayList<>();
//        languages.add(CandidateRetriever.Language.EN);
//        languages.add(CandidateRetriever.Language.DE);
        languages.add(CandidateRetriever.Language.ES);

        String content = "";

        //Manual_Lexicon.txt
        Set<String> prevContent = FileFactory.readFile("Manual_Lexicon.txt");

        for (CandidateRetriever.Language l : languages) {

            Set<String> coveredTrainIds = new HashSet<>();
            Set<String> coveredTestIds = new HashSet<>();

            for (String s : prevContent) {
                content += s + "\n";

                if (s.split("\t")[4].equals("train") && s.split("\t")[3].equals(l.name())) {
                    coveredTrainIds.add(s.split("\t")[0]);
                }
                if (s.split("\t")[4].equals("test") && s.split("\t")[3].equals(l.name())) {
                    coveredTestIds.add(s.split("\t")[0]);
                }
            }

            QALDCorpus corpusTrain = QALDCorpusLoader.load(QALDCorpusLoader.Dataset.qald6Train, includeYAGO, includeAggregation, includeUNION, onlyDBO, isHybrid);
            QALDCorpus corpusTest = QALDCorpusLoader.load(QALDCorpusLoader.Dataset.qald6Test, includeYAGO, includeAggregation, includeUNION, onlyDBO, isHybrid);

            System.out.println("Left docs: Train: " + (corpusTrain.getDocuments().size() - coveredTrainIds.size()));
            System.out.println("Left docs: Test: " + (corpusTest.getDocuments().size() - coveredTestIds.size()));

            content = getContent(coveredTrainIds, corpusTrain, content, scannner, l, "train");
//            content = getContent(coveredTestIds, corpusTest, content, scannner, l, "test");

        }
        FileFactory.writeListToFile("Manual_Lexicon.txt", content, false);
    }

    private static String getContent(Set<String> coveredTrainIds, QALDCorpus corpusTrain, String content, Scanner scanner, CandidateRetriever.Language l, String type) {

        boolean reset = false;
        boolean stop = false;

        AnnotatedDocument lastDoc = null;
        for (AnnotatedDocument d : corpusTrain.getDocuments()) {

            if (!reset) {
                lastDoc = d;
            }

            if (coveredTrainIds.contains(d.getQaldInstance().getId())) {
                continue;
            }

            if (d.getParse() == null) {
//                    System.out.println(d.getQuestionString());

            } else {
                String questionString = d.getQuestionString() + "\n" + d.getQaldInstance().getQuestionText().get(Language.EN);
                String parseTree = d.getParse().toString();
                String query = d.getGoldQueryString();

                Set<Triple> triples = SPARQLParser.extractTriplesFromQuery(query);
                Map<String, String> uriMap = new LinkedHashMap<>();
                Map<String, String> tokenMap = new LinkedHashMap<>();

                System.out.println(questionString + "\n");
                System.out.println(query + "\n\n");
                System.out.println(parseTree + "\n");

                int c = 0;
                for (Triple t : triples) {

                    if (!t.getSubject().isVariable()) {
                        uriMap.put("u" + c, t.getSubject().toString());
                        c++;
                    }
                    if (!t.getObject().isVariable() && !t.getObject().toString().equals("RETURN_VARIABLE")) {
                        uriMap.put("u" + c, t.getObject().toString());
                        c++;
                    }
                    if (!t.getPredicate().isVariable() && !t.getPredicate().getPredicateName().equals("type")) {
                        uriMap.put("u" + c, t.getPredicate().toString());
                        c++;
                    }
                }
                for (Triple t : triples) {

                    if (!t.getObject().isVariable() && !t.getPredicate().isVariable() && !t.getPredicate().getPredicateName().equals("type")) {
                        uriMap.put("u" + c, t.getPredicate() + "##" + t.getObject());
                        c++;
                    }
                }

                for (Integer k : d.getParse().getNodes().keySet()) {
                    tokenMap.put("t" + k, d.getParse().getNodes().get(k));
                }

                System.out.println("\n\ne.g. t3 = u1##u2\n\n");

                boolean isValid = true;
                while (isValid) {
                    System.out.println("\n\n");
                    print(uriMap, tokenMap);
                    System.out.println("\n\n");

                    String input = scanner.nextLine();
                    if (input.startsWith("t")) {
                        try {
                            String tokenInput = input.substring(0, input.indexOf("=")).trim();
                            String uriInput = input.replace(tokenInput, "").replace("=", "").trim();

                            String uri = "";
                            for (String u : uriInput.split(",")) {

                                u = u.trim();
                                if (u.isEmpty()) {
                                    continue;
                                }

                                if (uri.isEmpty()) {
                                    uri += uriMap.get(u);
                                    uriMap.remove(u);
                                } else {
                                    uri += ", " + uriMap.get(u);
                                    uriMap.remove(u);
                                }
                            }
                            String token = "";
                            for (String u : tokenInput.split(",")) {

                                u = u.trim();
                                if (u.isEmpty()) {
                                    continue;
                                }

                                if (token.isEmpty()) {
                                    token += tokenMap.get(u);
                                    tokenMap.remove(u);
                                } else {
                                    token += " " + tokenMap.get(u);
                                    tokenMap.remove(u);
                                }
                            }
                            token = token.replace("?", "");

                            content += d.getQaldInstance().getId() + "\t" + token + "\t" + uri + "\t" + l.name() + "\t" + type + "\n";
                        } catch (Exception e) {

                        }

                    } else {

                        isValid = false;

                        if (input.equals("reset")) {
                            isValid = true;

                            tokenMap.clear();
                            uriMap.clear();

                            System.out.println(questionString + "\n");
                            System.out.println(query + "\n\n");
                            System.out.println(parseTree + "\n");

                            c = 0;
                            for (Triple t : triples) {

                                if (!t.getSubject().isVariable()) {
                                    uriMap.put("u" + c, t.getSubject().toString());
                                    c++;
                                }
                                if (!t.getObject().isVariable() && !t.getObject().toString().equals("RETURN_VARIABLE")) {
                                    uriMap.put("u" + c, t.getObject().toString());
                                    c++;
                                }
                                if (!t.getPredicate().isVariable() && !t.getPredicate().getPredicateName().equals("type")) {
                                    uriMap.put("u" + c, t.getPredicate().toString());
                                    c++;
                                }
                            }
                            for (Triple t : triples) {

                                if (!t.getObject().isVariable() && !t.getPredicate().isVariable() && !t.getPredicate().getPredicateName().equals("type")) {
                                    uriMap.put("u" + c, t.getPredicate() + "##" + t.getObject());
                                    c++;
                                }
                            }

                            for (Integer k : d.getParse().getNodes().keySet()) {
                                tokenMap.put("t" + k, d.getParse().getNodes().get(k));
                            }

                            String temp = "";
                            for (String s : content.split("\n")) {
                                if (!s.split("\t")[0].equals(d.getQaldInstance().getId())) {
                                    temp += s + "\n";
                                }
                            }

                            content = temp;
                        }

                        if (input.equals("stop")) {
                            stop = true;
                        }
                        if (input.equals("discard")) {
                            String temp = "";
                            for (String s : content.split("\n")) {
                                if (!s.split("\t")[0].equals(d.getQaldInstance().getId())) {
                                    temp += s + "\n";
                                }
                            }

                            content = temp;
                            stop = true;
                        }
                    }

                    if (uriMap.isEmpty()) {
                        isValid = false;
                    }
                }
            }

            if (stop) {
                break;
            }
        }

        return content;
    }

    private static void print(Map<String, String> uriMap, Map<String, String> tokenMap) {
        System.out.println("URIs:\n\n");
        for (String k : uriMap.keySet()) {
            System.out.println(k + ": " + uriMap.get(k));
        }
        System.out.println("\nTokens:\n\n");
        for (String k : tokenMap.keySet()) {
            System.out.println(k + ": " + tokenMap.get(k));
        }
    }
}
