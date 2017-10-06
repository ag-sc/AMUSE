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
public class PropertyTemplate extends AbstractTemplate<AnnotatedDocument, State, StateFactorScope<State>> {

    private Set<String> validPOSTags;
    private Map<Integer, String> semanticTypes;

    public PropertyTemplate(Set<String> validPOSTags, Map<Integer, String> semanticTypes) {
        this.validPOSTags = validPOSTags;
        this.semanticTypes = semanticTypes;
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

        for (Integer tokenID : state.getDocument().getParse().getNodes().keySet()) {
            String headToken = state.getDocument().getParse().getToken(tokenID);
            String headPOS = state.getDocument().getParse().getPOSTag(tokenID);
            String headURI = state.getHiddenVariables().get(tokenID).getCandidate().getUri();
            Integer dudeID = state.getHiddenVariables().get(tokenID).getDudeId();
            String headDudeName = "EMPTY";

            if (dudeID != -1) {
                headDudeName = semanticTypes.get(dudeID);
            }

            if (!headDudeName.equals("Property")) {
                continue;
            }

            if (headURI.equals("EMPTY_STRING")) {
                continue;
            }

            if (headURI.equals("EMPTY_STRING") && validPOSTags.contains(headPOS)) {
                featureVector.addToValue("PROPERTY FEATURE:  EXCLUDE THIS WORD: URI: " + headURI + " TOKEN: " + headToken + " POS : " + headPOS, 1.0);
            }

            List<Integer> dependentNodes = state.getDocument().getParse().getDependentNodes(tokenID);

            //add lexical feature only for nouns, noun phrases etc.
            if (dependentNodes.isEmpty() && headPOS.startsWith("NN")) {
                featureVector.addToValue("PROPERTY FEATURE: URI: " + headURI + " TOKEN: " + headToken + " POS : " + headPOS + " SEM-TYPE: " + headDudeName, 1.0);
            }

            if (!dependentNodes.isEmpty()) {

                for (Integer depNodeID : dependentNodes) {
                    String depToken = state.getDocument().getParse().getToken(depNodeID);
                    String depURI = state.getHiddenVariables().get(depNodeID).getCandidate().getUri();

                    if (!depURI.equals("EMPTY_STRING")) {
                        featureVector.addToValue("PROPERTY DEP FEATURE: HEAD_URI: " + headURI + " HEAD_TOKEN: " + headToken + " SEM-TYPE: " + headDudeName + " CHILD_URI: " + depURI + " CHILD_TOKEN: " + depToken, 1.0);
                    }
                }
            }
        }

        //add dependency feature between tokens
//        for (Integer tokenID : state.getDocument().getParse().getNodes().keySet()) {
//            String headToken = state.getDocument().getParse().getToken(tokenID);
//            String headPOS = state.getDocument().getParse().getPOSTag(tokenID);
//            String headURI = state.getHiddenVariables().get(tokenID).getCandidate().getUri();
//            int headDUDEID = state.getHiddenVariables().get(tokenID).getDudeId();
//            
//            if(headDUDEID == -1){
//                continue;
//            }
//            
//            String headDUDEName = semanticTypes.get(headDUDEID);
//            
//            if (!headDUDEName.equals("Property")) {
//                continue;
//            }
//
//            if (headURI.equals("EMPTY_STRING")) {
//                continue;
//            }
//
//
//            List<Integer> dependentNodes = state.getDocument().getParse().getDependentEdges(tokenID);
//
//            if (!dependentNodes.isEmpty()) {
//
//                for (Integer depNodeID : dependentNodes) {
//                    String depToken = state.getDocument().getParse().getToken(depNodeID);
//                    String depPOS = state.getDocument().getParse().getPOSTag(depNodeID);
//                    String depRelation = state.getDocument().getParse().getDependencyRelation(depNodeID);
//                    String depURI = state.getHiddenVariables().get(depNodeID).getCandidate().getUri();
//                    int depDUDEID = state.getHiddenVariables().get(depNodeID).getDudeId();
//                    String depDUDEName = semanticTypes.get(depDUDEID);
//
//                    if (depURI.equals("EMPTY_STRING")) {
//                        continue;
//                    }
//                    if (!validPOSTags.contains(depPOS)) {
//                        continue;
//                    }
//
//                    featureVector.addToValue("PROPERTY LEXICAL DEP FEATURE: HEAD_TOKEN: " +headToken + " HEAD_URI: " + headURI + " CHILD_TOKEN: " + depToken + " CHILD_URI: " + depURI + " CHILD-SEM-TYPE: "+depDUDEName +" DEP-RELATION: "+depRelation, 1.0);
//                }
//            }
//        }
    }

    /**
     * levenstein sim
     */
    private double getSimilarityScore(String node, String uri) {

        uri = uri.replace("http://dbpedia.org/resource/", "");
        uri = uri.replace("http://dbpedia.org/property/", "");
        uri = uri.replace("http://dbpedia.org/ontology/", "");
        uri = uri.replace("http://www.w3.org/1999/02/22-rdf-syntax-ns#type###", "");

        uri = uri.replaceAll("@en", "");
        uri = uri.replaceAll("\"", "");
        uri = uri.replaceAll("_", " ");

        //replace capital letters with space
        //to tokenize compount classes e.g. ProgrammingLanguage => Programming Language
        String temp = "";
        for (int i = 0; i < uri.length(); i++) {
            String c = uri.charAt(i) + "";
            if (c.equals(c.toUpperCase())) {
                temp += " ";
            }
            temp += c;
        }

        uri = temp.trim().toLowerCase();

        //compute levenstein edit distance similarity and normalize
        final double weightedEditSimilarity = StringSimilarityMeasures.score(uri, node);

        return weightedEditSimilarity;
    }

}
