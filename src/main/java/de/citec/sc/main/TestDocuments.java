/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.main;

import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.corpus.QALDCorpus;
import de.citec.sc.qald.CorpusLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sherzod
 */
public class TestDocuments {

    public static void main(String[] args) {

        boolean includeYAGO = false;
        boolean includeAggregation = false;
        boolean includeUNION = false;
        boolean onlyDBO = true;
        boolean isHybrid = false;

        QALDCorpus corpus7 = CorpusLoader.load(CorpusLoader.Dataset.qald7Train, includeYAGO, includeAggregation, includeUNION, onlyDBO, isHybrid);
        QALDCorpus corpus6 = CorpusLoader.load(CorpusLoader.Dataset.qald6Train, includeYAGO, includeAggregation, includeUNION, onlyDBO, isHybrid);

        List<AnnotatedDocument> doc6 = corpus6.getDocuments();
        List<AnnotatedDocument> doc7 = corpus7.getDocuments();

        Map<String, AnnotatedDocument> map6 = new HashMap<>();
        Map<String, AnnotatedDocument> map7 = new HashMap<>();

        for (AnnotatedDocument d1 : doc6) {
            map6.put(d1.getQuestionString(), d1);
        }

        for (AnnotatedDocument d1 : doc7) {
            map7.put(d1.getQuestionString(), d1);
        }

        int c = 0;
        for (AnnotatedDocument d1 : doc7) {
            if (!map6.containsKey(d1.getQuestionString())) {
//                System.out.println(d1.getQuestionString() + " "+ d1.getGoldQueryString());
                c++;
            }
        }
        int c2 = 0;
        for (AnnotatedDocument d1 : doc6) {
            if (!map7.containsKey(d1.getQuestionString())) {
//                System.out.println(d1.getQuestionString() + " "+ d1.getGoldQueryString());
                c2++;
            }
        }

        System.out.println("# of instances added to qald6Train - qald7Train: " + c + "/" + doc7.size());
        System.out.println("# of instances removed from qald6Train - qald7Train : " + c2 + "/" + doc6.size());
    }
}
