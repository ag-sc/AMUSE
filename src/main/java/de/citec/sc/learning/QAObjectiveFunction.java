/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.learning;

import de.citec.sc.evaluator.AnswerEvaluator;
import de.citec.sc.evaluator.QueryEvaluator;
import de.citec.sc.qald.SPARQLParser;
import de.citec.sc.utils.DBpediaEndpoint;

import de.citec.sc.variable.State;

import java.io.Serializable;
import learning.ObjectiveFunction;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;

/**
 *
 * @author sherzod
 */
public class QAObjectiveFunction extends ObjectiveFunction<State, String> implements Serializable {

    private boolean useQueryEvaluator = true;

    public void setUseQueryEvaluator(boolean useQueryEvaluator) {
        this.useQueryEvaluator = useQueryEvaluator;
    }

    public double computeValue(State deptState, String goldState) {

        return computeScore(deptState, goldState);
    }

    public double computeValue(String query, String goldState) {

        String constructedQuery = query;

        if (constructedQuery.trim().isEmpty()) {
            return 0;
        }

        if (!DBpediaEndpoint.isValidQuery(constructedQuery, true)) {
            return 0;
        }

        if (SPARQLParser.extractTriplesFromQuery(constructedQuery).isEmpty()) {
            return 0;
        }

        double score2 = 0;
        if (useQueryEvaluator) {
            score2 = QueryEvaluator.evaluate(constructedQuery, goldState);
        }

        if (score2 == 1.0) {
            return score2;
        }
        double score1 = AnswerEvaluator.evaluate(constructedQuery, goldState);

        double score = Math.max(score1, score2);
        
        return score;
    }

    @Override
    protected double computeScore(State state, String goldState) {

        String constructedQuery = QueryConstructor.getSPARQLQuery(state);

        if (constructedQuery.trim().isEmpty()) {
            return 0;
        }

        if (!DBpediaEndpoint.isValidQuery(constructedQuery, true)) {
            return 0;
        }

        if (SPARQLParser.extractTriplesFromQuery(constructedQuery).isEmpty()) {
            return 0;
        }

        double score1 = AnswerEvaluator.evaluate(constructedQuery, goldState);
        double score2 = 0;
        if (useQueryEvaluator) {
            score2 = QueryEvaluator.evaluate(constructedQuery, goldState);
        }

        double score = Math.max(score1, score2);
        
        return score;
    }
}
