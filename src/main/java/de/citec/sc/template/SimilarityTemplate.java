/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.template;

import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.variable.URIVariable;

import de.citec.sc.variable.State;
import factors.Factor;
import factors.FactorScope;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import learning.Vector;
import net.ricecode.similarity.SimilarityStrategy;
import net.ricecode.similarity.StringSimilarityMeasures;
import templates.AbstractTemplate;

/**
 *
 * @author sherzod
 */
public class SimilarityTemplate extends AbstractTemplate<AnnotatedDocument, State, StateFactorScope<State>> {

    private Set<String> validPOSTags;
    private Set<String> frequentWordsToExclude;
    private Map<Integer, String> semanticTypes;

    public SimilarityTemplate(Set<String> validPOSTags, Set<String> frequentWordsToExclude, Map<Integer, String> s) {
        this.validPOSTags = validPOSTags;
        this.semanticTypes = s;
        this.frequentWordsToExclude = frequentWordsToExclude;
    }

    @Override
    public List<StateFactorScope<State>> generateFactorScopes(State state) {
        List<StateFactorScope<State>> factors = new ArrayList<>();

        for (Integer key : state.getDocument().getParse().getNodes().keySet()) {

            URIVariable a = state.getHiddenVariables().get(key);

            factors.add(new StateFactorScope<>(this, state));
        }

        return factors;
    }

    @Override
    public void computeFactor(Factor<StateFactorScope<State>> factor) {
        State state = factor.getFactorScope().getState();

        Vector featureVector = factor.getFeatureVector();

        //add dependency feature between tokens
        for (Integer tokenID : state.getDocument().getParse().getNodes().keySet()) {
            String headToken = state.getDocument().getParse().getToken(tokenID);
            String headPOS = state.getDocument().getParse().getPOSTag(tokenID);
            String headURI = state.getHiddenVariables().get(tokenID).getCandidate().getUri();
            Integer dudeID = state.getHiddenVariables().get(tokenID).getDudeId();
            String dudeName = "EMPTY";
            if (dudeID != -1) {
                dudeName = semanticTypes.get(dudeID);
            }

            if (headURI.equals("EMPTY_STRING")) {
                continue;
            }

            List<Integer> dependentNodes = state.getDocument().getParse().getDependentNodes(tokenID, validPOSTags);
            List<Integer> siblings = state.getDocument().getParse().getSiblings(tokenID, validPOSTags);

            if (!dependentNodes.isEmpty()) {

                for (Integer depNodeID : dependentNodes) {
                    String depToken = state.getDocument().getParse().getToken(depNodeID);
                    String depURI = state.getHiddenVariables().get(depNodeID).getCandidate().getUri();
                    Integer depDudeID = state.getHiddenVariables().get(depNodeID).getDudeId();
                    String depDudeName = "EMPTY";
                    if (depDudeID != -1) {
                        depDudeName = semanticTypes.get(depDudeID);
                    }

                    if (!depURI.equals("EMPTY_STRING")) {
                        featureVector.addToValue("LEXICAL DEP FEATURE: HEAD_URI: " + headURI + " HEAD_TOKEN: " + headToken + " SEM-TYPE: " + dudeName + " CHILD_URI: " + depURI + " CHILD_TOKEN: " + depToken + " DEP-SEM-TYPE: " + depDudeName, 1.0);
                    }
                }
            }
            if (!siblings.isEmpty()) {

                for (Integer depNodeID : siblings) {
                    String depToken = state.getDocument().getParse().getToken(depNodeID);
                    String depURI = state.getHiddenVariables().get(depNodeID).getCandidate().getUri();
                    Integer depDudeID = state.getHiddenVariables().get(depNodeID).getDudeId();
                    String depDudeName = "EMPTY";
                    if (depDudeID != -1) {
                        depDudeName = semanticTypes.get(depDudeID);
                    }

                    if (!depURI.equals("EMPTY_STRING")) {
                        featureVector.addToValue("LEXICAL SIBLING FEATURE: HEAD_URI: " + headURI + " HEAD_TOKEN: " + headToken + " SEM-TYPE: " + dudeName + " CHILD_URI: " + depURI + " CHILD_TOKEN: " + depToken + " SIBLING-SEM-TYPE: " + depDudeName, 1.0);
                    }
                }
            }
        }
    }

}
