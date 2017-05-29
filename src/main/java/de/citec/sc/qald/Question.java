/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.qald;

import de.citec.sc.query.CandidateRetriever.Language;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author sherzod
 */
public class Question {

    private Map<Language, String> questionText;
    private String queryText;
    private List<String> answers;
    private String keywords;
    private String id;

    private String onlyDBO;
    private String aggregation;
    private String answerType;
    private String hybrid;

    public String getHybrid() {
        return hybrid;
    }

    public void setHybrid(String hybrid) {
        this.hybrid = hybrid;
    }

    public String getOnlyDBO() {
        return onlyDBO;
    }

    public void setOnlyDBO(String onlyDBO) {
        this.onlyDBO = onlyDBO;
    }

    public String getAggregation() {
        return aggregation;
    }

    public void setAggregation(String aggregation) {
        this.aggregation = aggregation;
    }

    public String getAnswerType() {
        return answerType;
    }

    public void setAnswerType(String answerType) {
        this.answerType = answerType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Question(Map<Language, String> questionText, String queryText) {
        this.questionText = questionText;
        this.queryText = queryText;
    }
//
//    public Question(String questionText, String queryText, String id) {
//        this.questionText = questionText;
//        this.queryText = queryText;
//        this.id = id;
//    }
//
//    public Question(String questionText, String queryText, ArrayList<String> answers) {
//        this.questionText = questionText;
//        this.queryText = queryText;
//        this.answers = answers;
//    }

    public Question(Map<Language, String> questionText, String queryText, String onlyDBO, String aggregation, String answerType, String hybrid, String id) {
        this.questionText = questionText;
        this.queryText = queryText;
        this.id = id;
        this.onlyDBO = onlyDBO;
        this.aggregation = aggregation;
        this.answerType = answerType;
        this.hybrid = hybrid;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.questionText);
        hash = 53 * hash + Objects.hashCode(this.queryText);
        hash = 53 * hash + Objects.hashCode(this.answers);
        hash = 53 * hash + Objects.hashCode(this.keywords);
        hash = 53 * hash + Objects.hashCode(this.id);
        hash = 53 * hash + Objects.hashCode(this.onlyDBO);
        hash = 53 * hash + Objects.hashCode(this.aggregation);
        hash = 53 * hash + Objects.hashCode(this.answerType);
        hash = 53 * hash + Objects.hashCode(this.hybrid);
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
        final Question other = (Question) obj;
        if (!Objects.equals(this.questionText, other.questionText)) {
            return false;
        }
        if (!Objects.equals(this.queryText, other.queryText)) {
            return false;
        }
        if (!Objects.equals(this.answers, other.answers)) {
            return false;
        }
        if (!Objects.equals(this.keywords, other.keywords)) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.onlyDBO, other.onlyDBO)) {
            return false;
        }
        if (!Objects.equals(this.aggregation, other.aggregation)) {
            return false;
        }
        if (!Objects.equals(this.answerType, other.answerType)) {
            return false;
        }
        if (!Objects.equals(this.hybrid, other.hybrid)) {
            return false;
        }
        return true;
    }

    public Map<Language, String> getQuestionText() {
        return questionText;
    }

    public String getQueryText() {
        return queryText;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public void setAnswers(List<String> answers) {
        this.answers = answers;
    }

    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }

    public void setQuestionText(Map<Language, String> questionText) {
        this.questionText = questionText;
    }

    @Override
    public String toString() {
        return "Question{" + "questionText=" + questionText + ", queryText=" + queryText + ", id=" + id + ", onlyDBO=" + onlyDBO + ", aggregation=" + aggregation + ", answerType=" + answerType + ", hybrid=" + hybrid + '}';
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

}
