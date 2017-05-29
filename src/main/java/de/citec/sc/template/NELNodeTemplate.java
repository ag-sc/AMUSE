/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.template;

import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.utils.DBpediaEndpoint;
import de.citec.sc.variable.HiddenVariable;

import de.citec.sc.variable.State;
import factors.Factor;
import factors.FactorScope;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
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
public class NELNodeTemplate extends AbstractTemplate<AnnotatedDocument, State, StateFactorScope<State>> {

    private Set<String> validPOSTags;
    private Set<String> validEdges;
    private Map<Integer, String> semanticTypes;

    public NELNodeTemplate(Set<String> validPOSTags, Set<String> edges, Map<Integer, String> s) {
        this.validPOSTags = validPOSTags;
        this.semanticTypes = s;
        this.validEdges = edges;
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

        Map<String, Double> nodeFeatures = getValidNodeFeatures(state);

        for (String k : nodeFeatures.keySet()) {
            featureVector.addToValue(k, nodeFeatures.get(k));
        }

    }

    /**
     * checks whether the node is valid postag, and should have URI or not.
     */
    private Map<String, Double> getValidNodeFeatures(State state) {

        Map<String, Double> features = new HashMap<>();

        //add dependency feature between tokens
        for (Integer tokenID : state.getDocument().getParse().getNodes().keySet()) {
            String headToken = state.getDocument().getParse().getToken(tokenID);
            String headPOS = state.getDocument().getParse().getPOSTag(tokenID);
            String headURI = state.getHiddenVariables().get(tokenID).getCandidate().getUri();
            Integer dudeID = state.getHiddenVariables().get(tokenID).getDudeId();
            String depRelation = state.getDocument().getParse().getDependencyRelation(tokenID);

            String dudeName = "EMPTY";
            if (dudeID != -1) {
                dudeName = semanticTypes.get(dudeID);
            }

            double similarityScore = getSimilarityScore(headToken, headURI);

//            if(similarityScore == 1.0 && dudeName.equals("Individual")){
//                features.put("NEL NODE - FEATURE: PERFECT MATCH Individual" + " SEM-TYPE: " + dudeName + ":" + headPOS + " dep-relation: " + depRelation + " ", 1.0);
//            }
            if (validPOSTags.contains(headPOS)) {
                if (headURI.equals("EMPTY_STRING")) {
                    features.put("NEL NODE - FEATURE: HAS EMPTY_URI " + " SEM-TYPE: " + dudeName + ":" + headPOS + " dep-relation: " + depRelation + " ", 1.0);
                } else {
                    features.put("NEL NODE - FEATURE: HAS VALID_URI " + " SEM-TYPE: " + dudeName + ":" + headPOS + " dep-relation: " + depRelation + " ", 1.0);
                }
            }
        }

        return features;
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

        temp = temp.replaceAll("\\s+", " ");

        uri = temp.trim().toLowerCase();

        //compute levenstein edit distance similarity and normalize
        final double weightedEditSimilarity = StringSimilarityMeasures.score(uri.toLowerCase(), node.toLowerCase());

        return weightedEditSimilarity;
    }

}
