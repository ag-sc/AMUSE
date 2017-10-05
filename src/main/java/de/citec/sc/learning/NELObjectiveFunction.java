/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.learning;

import de.citec.sc.evaluator.BagOfLinksEvaluator;

import de.citec.sc.qald.SPARQLParser;
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
public class NELObjectiveFunction extends ObjectiveFunction<State, String> implements Serializable {

    public double computeValue(State deptState, String goldState) {

        return computeScore(deptState, goldState);
    }

    public static double computeValue(String query, String goldState) {

        Set<String> uris1 = SPARQLParser.extractURIsFromQuery(query);
        Set<String> uris2 = SPARQLParser.extractURIsFromQuery(goldState);

        double value = BagOfLinksEvaluator.evaluate(uris1, uris2);

        return value;
    }

    @Override
    protected double computeScore(State deptState, String goldState) {

        Set<String> derived = new HashSet<>();
        for (URIVariable var : deptState.getHiddenVariables().values()) {
            if (!var.getCandidate().getUri().equals("EMPTY_STRING")) {
                
                if(var.getCandidate().getUri().equals("http://dbpedia.org/ontology/conservationStatus###'CR'^^<http://www.w3.org/2001/XMLSchema#string>")){
                    int z=1;
                }

                if (var.getCandidate().getUri().contains("###")) {
                    String property = var.getCandidate().getUri().substring(0, var.getCandidate().getUri().indexOf("###"));
                    String resource = var.getCandidate().getUri().substring(var.getCandidate().getUri().indexOf("###") + 3);

                    derived.add(resource);
                    derived.add(property);
                }
                //Underspecified property
                else if(var.getDudeId() == 5){
                    String property = "p" + FreshVariable.get();
                    String resource = var.getCandidate().getUri();

                    derived.add(resource);
                    derived.add(property);
                }
                else {
                    derived.add(var.getCandidate().getUri());
                }

            }
        }

        Set<String> uris = SPARQLParser.extractURIsFromQuery(goldState);

        double value = BagOfLinksEvaluator.evaluate(derived, uris);

        //compute query similarity to goldState
        return value;
    }
}
