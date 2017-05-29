/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.corpus;

import corpus.LabeledInstance;
import de.citec.sc.main.Main;

import de.citec.sc.parser.DependencyParse;
import de.citec.sc.qald.Question;
import de.citec.sc.query.CandidateRetriever.Language;
import java.util.Objects;

/**
 *
 * @author sherzod
 */
public class AnnotatedDocument implements LabeledInstance<AnnotatedDocument, String> {

    private DependencyParse parse;
    private Question qaldInstance;

    public AnnotatedDocument(DependencyParse parse, Question qaldIns) {

        this.parse = parse;
        this.qaldInstance = qaldIns;
    }

    @Override
    public String toString() {
        return "\nID:" + qaldInstance.getId() + "\n\n" + qaldInstance.getQuestionText().get(Main.lang) + "\n\n" + qaldInstance.getQueryText() + "\n\n" + parse + "\n\n";
    }

    public DependencyParse getParse() {
        return parse;
    }

    public String getGoldQueryString() {
        return qaldInstance.getQueryText();
    }

    public String getQuestionString() {
        return qaldInstance.getQuestionText().get(Main.lang);
    }

    public Question getQaldInstance() {
        return qaldInstance;
    }

    public void setQaldInstance(Question qaldInstance) {
        this.qaldInstance = qaldInstance;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + Objects.hashCode(this.parse);
        hash = 43 * hash + Objects.hashCode(this.qaldInstance);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AnnotatedDocument other = (AnnotatedDocument) obj;
        if (!Objects.equals(this.parse, other.parse)) {
            return false;
        }
        if (!Objects.equals(this.qaldInstance, other.qaldInstance)) {
            return false;
        }
        return true;
    }

    @Override
    public AnnotatedDocument getInstance() {
        return this;
    }

    @Override
    public String getResult() {
        return this.qaldInstance.getQueryText();
    }

}
