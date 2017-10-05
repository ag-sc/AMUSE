/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.learning;

import de.citec.sc.evaluator.BagOfLinksEvaluator;
import de.citec.sc.evaluator.QueryEvaluator;

import de.citec.sc.qald.SPARQLParser;
import de.citec.sc.qald.Triple;
import de.citec.sc.utils.DBpediaEndpoint;
import de.citec.sc.utils.FreshVariable;
import de.citec.sc.variable.URIVariable;

import de.citec.sc.variable.State;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import learning.ObjectiveFunction;

/**
 *
 * @author sherzod
 */
public class QueryTypeObjectiveFunction extends ObjectiveFunction<State, String> implements Serializable {

    public double computeValue(State deptState, String goldState) {

        return computeScore(deptState, goldState);
    }

    public double computeValue(String query, String goldQuery) {

        if (query.trim().isEmpty()) {
            return 0;
        }

        if (!DBpediaEndpoint.isValidQuery(query, true)) {
            return 0;
        }

        if (SPARQLParser.extractTriplesFromQuery(query).isEmpty()) {
            return 0;
        }
        
        
        double value = QueryEvaluator.evaluate(query, goldQuery, true);

        return value;
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
        
        
        double value = QueryEvaluator.evaluate(constructedQuery, goldState, true);

        return value;
    }
}
