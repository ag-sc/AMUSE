/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.corpus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author sherzod
 */
public class QALDCorpus {

    private List<AnnotatedDocument> documents;
    private String corpusName;

    public String getCorpusName() {
        return corpusName;
    }

    public void setCorpusName(String corpusName) {
        this.corpusName = corpusName;
    }

    public QALDCorpus() {
        this.documents = new ArrayList<>();
    }

    public QALDCorpus(List<AnnotatedDocument> documents, String corpusName) {
        this.documents = documents;
        this.corpusName = corpusName;
    }

    public List<AnnotatedDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<AnnotatedDocument> documents) {
        this.documents = documents;
    }

    @Override
    public String toString() {
        String s = "CORPUS: " + corpusName + "\n\n";

        for (AnnotatedDocument d : documents) {
            s += d.toString() + "\n\n";
            s += "===========================================================================\n";
        }
        return s;
    }

    public void addDocument(AnnotatedDocument document) {
        this.documents.add(document);
    }

}
