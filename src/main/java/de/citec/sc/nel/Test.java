/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.nel;

import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.corpus.QALDCorpus;
import de.citec.sc.qald.QALDCorpusLoader;
import de.citec.sc.query.CandidateRetriever;
import de.citec.sc.query.CandidateRetrieverOnLucene;
import java.util.List;

/**
 *
 * @author sherzod
 */
public class Test {

    public static void main(String[] args) {
        CandidateRetriever retriever = new CandidateRetrieverOnLucene(false, "luceneIndex");

        int count = 0;

        String text = "what did robert boyle accomplish?";
        count++;

        System.out.println(text);

        List<EntityAnnotation> annotations = AnnotationExtractor.getAnnotations(text, 4, 0.90, 0.60, retriever);

        for (EntityAnnotation e : annotations) {
            System.out.println("\t" + e.print(text));
        }

    }
}
