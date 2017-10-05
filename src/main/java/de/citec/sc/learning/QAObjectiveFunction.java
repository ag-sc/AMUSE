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
import de.citec.sc.utils.ProjectConfiguration;

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

    public static double computeValue(String query, String goldState) {

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

        double score = QueryEvaluator.evaluate(constructedQuery, goldState, false);

        if (score == 1.0) {
            return score;
        }

        if (!ProjectConfiguration.useDBpediaEndpoint()) {
            return score;
        }

        if (score >= 0.50) {
            double score2 = AnswerEvaluator.evaluate(constructedQuery, goldState, false);

            score = Math.max(score, score2);
        }

        return score;
    }

    @Override
    protected double computeScore(State state, String goldState) {

        String constructedQuery = QueryConstructor.getSPARQLQuery(state);

        //get the query type from the query
        int constructedQueryType = 1;
        if (constructedQuery.contains("SELECT")) {
            if (constructedQuery.contains("COUNT")) {
                constructedQueryType = 2;
            }
        } else {
            constructedQueryType = 3;
        }

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
        double score1 = 0;
        if (useQueryEvaluator) {
            score2 = QueryEvaluator.evaluate(constructedQuery, goldState, false);

            //if it's not the same type, penalize
            if (state.getQueryTypeVariable().getType() != constructedQueryType) {
                score2 = score2 * 0.90;
            }
        }

        if (!ProjectConfiguration.useDBpediaEndpoint()) {
            return score2;
        }

        if (score2 == 1.0) {
            return score2;
        }

        if (useQueryEvaluator && score2 >= 0.65) {
            score1 = AnswerEvaluator.evaluate(constructedQuery, goldState, false);
        }

        double score = Math.max(score1, score2);

        //if it's not the same type, penalize
        if (state.getQueryTypeVariable().getType() != constructedQueryType) {
            score = score * 0.90;
        }

        return score;
    }
}
