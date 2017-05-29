/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.template;

import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.variable.HiddenVariable;

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
public class QALexicalTemplate extends AbstractTemplate<AnnotatedDocument, State, StateFactorScope<State>> {

    private Set<String> validPOSTags;
    private Set<String> frequentWordsToExclude;
    private Map<Integer, String> semanticTypes;
    private Map<Integer, String> specialSemanticTypes;

    public QALexicalTemplate(Set<String> validPOSTags, Set<String> frequentWordsToExclude, Map<Integer, String> s, Map<Integer, String> sp) {
        this.validPOSTags = validPOSTags;
        this.semanticTypes = s;
        this.specialSemanticTypes = sp;
        this.frequentWordsToExclude = frequentWordsToExclude;
    }

    @Override
    public List<StateFactorScope<State>> generateFactorScopes(State state) {
        List<StateFactorScope<State>> factors = new ArrayList<>();

        for (Integer key : state.getDocument().getParse().getNodes().keySet()) {

            HiddenVariable a = state.getHiddenVariables().get(key);

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

            List<Integer> childNodes = state.getDocument().getParse().getDependentEdges(tokenID);
            List<Integer> siblingNodes = state.getDocument().getParse().getSiblings(tokenID);

            Set<Integer> dependentNodes = new HashSet<>();
            Set<Integer> siblings = new HashSet<>();
            Set<Integer> headNodes = new HashSet<>();

            for (Integer childNode : childNodes) {
                List<Integer> childOfChildNodes = state.getDocument().getParse().getDependentEdges(childNode);

                dependentNodes.addAll(childOfChildNodes);
                dependentNodes.add(childNode);
            }
            for (Integer sibling : siblingNodes) {
                List<Integer> childOfSiblingNodes = state.getDocument().getParse().getDependentEdges(sibling);

                siblings.addAll(childOfSiblingNodes);
                siblings.add(sibling);
            }

            Integer headNode = state.getDocument().getParse().getParentNode(tokenID);
            List<Integer> siblingsOfHeadNode = state.getDocument().getParse().getSiblings(headNode);

            //get a list of siblings of the head node, and add the head node itself.
            headNodes.addAll(siblingsOfHeadNode);
            headNodes.add(headNode);

            if (!dependentNodes.isEmpty()) {

                for (Integer depNodeID : dependentNodes) {
                    String depToken = state.getDocument().getParse().getToken(depNodeID);

                    Integer depDudeID = state.getHiddenVariables().get(depNodeID).getDudeId();

                    if (specialSemanticTypes.containsKey(depDudeID)) {

                        String depDudeName = specialSemanticTypes.get(depDudeID);

                        String relation = state.getDocument().getParse().getDependencyRelation(depNodeID);

                        featureVector.addToValue("QA LEXICAL DEP FEATURE: HEAD_URI: " + " CHILD_TOKEN: " + depToken + " DEP-SEM-TYPE: " + depDudeName + " DEP-REL: " + relation, 1.0);
                    }
                }
            }
            if (!siblings.isEmpty()) {

                for (Integer depNodeID : siblings) {
                    String depToken = state.getDocument().getParse().getToken(depNodeID);

                    Integer depDudeID = state.getHiddenVariables().get(depNodeID).getDudeId();

                    if (specialSemanticTypes.containsKey(depDudeID)) {

                        String depDudeName = specialSemanticTypes.get(depDudeID);

                        String relation = state.getDocument().getParse().getDependencyRelation(depNodeID);

                        featureVector.addToValue("QA LEXICAL SIBLING FEATURE: HEAD_URI: " + headURI + " HEAD_TOKEN: " + headToken + " SEM-TYPE: " + dudeName + " CHILD_TOKEN: " + depToken + " DEP-SEM-TYPE: " + depDudeName + " DEP-REL: " + relation, 1.0);
                    }
                }
            }
            if (!headNodes.isEmpty()) {

                for (Integer headNodeID : headNodes) {

                    if (headNodeID == -1) {
                        continue;
                    }

                    String depToken = state.getDocument().getParse().getToken(headNodeID);

                    Integer depDudeID = state.getHiddenVariables().get(headNodeID).getDudeId();

                    if (specialSemanticTypes.containsKey(depDudeID)) {

                        String depDudeName = specialSemanticTypes.get(depDudeID);

                        String relation = state.getDocument().getParse().getDependencyRelation(headNodeID);

                        featureVector.addToValue("QA LEXICAL HEAD FEATURE: HEAD_URI: " + headURI + " HEAD_TOKEN: " + headToken + " SEM-TYPE: " + dudeName + " CHILD_TOKEN: " + depToken + " DEP-SEM-TYPE: " + depDudeName + " DEP-REL: " + relation, 1.0);
                    }
                }
            }
        }
    }

}
