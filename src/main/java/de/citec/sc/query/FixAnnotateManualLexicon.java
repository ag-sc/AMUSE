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
public class FixAnnotateManualLexicon {

    public static void main(String[] args) throws UnsupportedEncodingException {

        Scanner scannner = new Scanner(System.in);

        boolean includeYAGO = false;
        boolean includeAggregation = false;
        boolean includeUNION = false;
        boolean onlyDBO = true;
        boolean isHybrid = false;

        List<CandidateRetriever.Language> languages = new ArrayList<>();
        languages.add(CandidateRetriever.Language.EN);
        languages.add(CandidateRetriever.Language.DE);
        languages.add(CandidateRetriever.Language.ES);

        String content = "";

        for (CandidateRetriever.Language l : languages) {
            Main.lang = l;

            Set<String> prevContent = FileFactory.readFile(l.name() + "_lexicon.txt");

            QALDCorpus corpusTrain = QALDCorpusLoader.load(QALDCorpusLoader.Dataset.qald6Train, includeYAGO, includeAggregation, includeUNION, onlyDBO, isHybrid);
            QALDCorpus corpusTest = QALDCorpusLoader.load(QALDCorpusLoader.Dataset.qald6Test, includeYAGO, includeAggregation, includeUNION, onlyDBO, isHybrid);

            Set<String> removeList = new HashSet<>();

            for (String p : prevContent) {

                if (removeList.contains(p.split("\t")[0])) {
                    continue;
                }

                boolean found = false;
                for (AnnotatedDocument d : corpusTrain.getDocuments()) {
                    if (p.split("\t")[0].equals(d.getQaldInstance().getId())) {

                        String[] data = p.split("\t")[2].split(",");
                        List<String> uris = new ArrayList<>();

                        for (String u : data) {
                            u = u.trim();
                            if (u.contains("##")) {
                                String prop = u.substring(0, u.indexOf("##"));
                                String res = u.replace(prop, "").replace("##", "");

                                uris.add(prop);
                                uris.add(res);
                            } else {
                                uris.add(u);
                            }
                        }

                        if (d.getQaldInstance().getQuestionText().get(l).contains(p.split("\t")[1])) {
                            content += p + "\t" + "train\n";
                            found = true;
                            break;
                        } else {
                            int c = 0;
                            for (String u : uris) {
                                if (d.getQaldInstance().getQueryText().contains(u)) {
                                    c++;
                                }
                            }

                            if (c == uris.size()) {
                                content += p + "\t" + "train\n";
                                found = true;
                                break;
                            }
                        }
                    }
                }
                for (AnnotatedDocument d : corpusTest.getDocuments()) {
                    if (p.split("\t")[0].equals(d.getQaldInstance().getId())) {

                        String[] data = p.split("\t")[2].split(",");
                        List<String> uris = new ArrayList<>();

                        for (String u : data) {
                            u = u.trim();
                            if (u.contains("##")) {
                                String prop = u.substring(0, u.indexOf("##"));
                                String res = u.replace(prop, "").replace("##", "");

                                uris.add(prop);
                                uris.add(res);
                            } else {
                                uris.add(u);
                            }
                        }

                        if (d.getQaldInstance().getQuestionText().get(l).contains(p.split("\t")[1])) {
                            content += p + "\t" + "test\n";
                            found = true;
                            break;
                        } else {
                            int c = 0;
                            for (String u : uris) {
                                if (d.getQaldInstance().getQueryText().contains(u)) {
                                    c++;
                                }
                            }

                            if (c == uris.size()) {
                                content += p + "\t" + "test\n";
                                found = true;
                                break;
                            }
                        }
                    }
                }

                if (!found) {
                    removeList.add(p.split("\t")[0]);
                }
            }

            for (String s1 : removeList) {
                System.out.println(s1);
            }
            System.out.println(removeList.size());

            FileFactory.writeListToFile(l.name() + "_lexicon.txt", content, false);
        }
    }

}
