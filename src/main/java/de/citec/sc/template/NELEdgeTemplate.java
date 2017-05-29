/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.template;

import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.main.Main;
import de.citec.sc.query.DBpediaLabelRetriever;
import de.citec.sc.utils.DBpediaEndpoint;
import de.citec.sc.utils.ProjectConfiguration;
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
public class NELEdgeTemplate extends AbstractTemplate<AnnotatedDocument, State, StateFactorScope<State>> {

    private Set<String> validPOSTags;
    private Set<String> validEdges;
    private Map<Integer, String> semanticTypes;

    public NELEdgeTemplate(Set<String> validPOSTags, Set<String> edges, Map<Integer, String> s) {
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
        
        String featureGroup = ProjectConfiguration.getFeatureGroup();

        Map<String, Double> depFeatures = getDependencyFeatures(state, featureGroup);
        Map<String, Double> siblingFeatures = getSiblingFeatures(state, featureGroup);

        for (String k : depFeatures.keySet()) {
            featureVector.addToValue(k, depFeatures.get(k));
        }

        for (String k : siblingFeatures.keySet()) {
            featureVector.addToValue(k, siblingFeatures.get(k));
        }

    }

    /**
     * returns features that involve edge exploration.
     */
    private Map<String, Double> getDependencyFeatures(State state, String featureGroup) {
        Map<String, Double> features = new HashMap<>();

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

//            if (headURI.equals("EMPTY_STRING")) {
//                continue;
//            }

            List<Integer> dependentNodes = state.getDocument().getParse().getDependentEdges(tokenID, validPOSTags);

            if (!dependentNodes.isEmpty()) {

                for (Integer depNodeID : dependentNodes) {
                    String depToken = state.getDocument().getParse().getToken(depNodeID);
                    String depURI = state.getHiddenVariables().get(depNodeID).getCandidate().getUri();
                    String depPOS = state.getDocument().getParse().getPOSTag(depNodeID);
                    Integer depDudeID = state.getHiddenVariables().get(depNodeID).getDudeId();
                    String depRelation = state.getDocument().getParse().getDependencyRelation(depNodeID);
                    String depDudeName = "EMPTY";
                    String slotNumber = state.getSlot(depNodeID, tokenID);

                    if (depDudeID != -1) {
                        depDudeName = semanticTypes.get(depDudeID);
                    }

//                    if (depURI.equals("EMPTY_STRING")) {
//                        continue;
//                    }

                    //GROUP 1
                    if (featureGroup.contains("1")) {
                        features.put("NEL  GROUP 1 PARENT : Lemma & URI : " + headToken + " & " + headURI, 1.0);
                        features.put("NEL  GROUP 1 PARENT : POS & SEM-TYPE : " + headPOS + " & " + dudeName, 1.0);

                        features.put("NEL  GROUP 1 CHILD : Lemma & URI : " + depToken + " & " + depURI, 1.0);
                        features.put("NEL  GROUP 1 CHILD : POS & SEM-TYPE : " + depPOS + " & " + depDudeName, 1.0);
                        features.put("NEL  GROUP 1 DEP-REL & SLOT : " + depRelation + " & " + slotNumber, 1.0);

                    }

                    //GROUP 2
                    if (featureGroup.contains("2")) {
                        features.put("NEL  GROUP 2 PARENT POS & PARENT SEM-TYPE & CHILD POS & CHILD SEM-TYPE : " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName, 1.0);
                        features.put("NEL  GROUP 2 PARENT POS & PARENT SEM-TYPE & CHILD POS & CHILD SEM-TYPE & DEP-REL : " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + depRelation, 1.0);
                        features.put("NEL  GROUP 2 PARENT POS & PARENT SEM-TYPE & CHILD POS & CHILD SEM-TYPE & SLOT : " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + slotNumber, 1.0);
                        features.put("NEL  GROUP 2 PARENT POS & PARENT SEM-TYPE & CHILD POS & CHILD SEM-TYPE & DEP-REL & SLOT : " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + depRelation + " & " + slotNumber, 1.0);
                    }

                    //GROUP 3
                    if (featureGroup.contains("3")) {
                        features.put("NEL  GROUP 3 PARENT POS & PARENT SEM-TYPE & CHILD POS & CHILD SEM-TYPE & PARENT URI: " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + headURI, 1.0);
                        features.put("NEL  GROUP 3 PARENT POS & PARENT SEM-TYPE & CHILD POS & CHILD SEM-TYPE & CHILD URI: " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + depURI, 1.0);
                        features.put("NEL  GROUP 3 PARENT POS & PARENT SEM-TYPE & CHILD POS & CHILD SEM-TYPE & PARENT URI & CHILD URI: " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + headURI + " & " + depURI, 1.0);
                    }

                    //GROUP 4
                    if (featureGroup.contains("4")) {
                        double headSimilarityScore = getSimilarityScore(headToken, headURI);
                        double depSimilarityScore = getSimilarityScore(depToken, depURI);

                        features.put("NEL  GROUP 4 SIM (PARENT URI, PARENT LEMMA) : ", headSimilarityScore);
                        features.put("NEL  GROUP 4 SIM (CHILD URI, CHILD LEMMA) : ", depSimilarityScore);
                    }

                    //GROUP 5
                    if (featureGroup.contains("5")) {
                        
                        if (dudeName.equals("Property")) {
                            String range = DBpediaEndpoint.getRange(headURI);
                            String domain = DBpediaEndpoint.getDomain(headURI);
                            
                            if(slotNumber.equals("1")){
                                features.put("NEL  GROUP 5 PARENT POS & PARENT SEM-TYPE & CHILD POS & CHILD SEM-TYPE & SLOT & DOMAIN: " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + slotNumber +" & "+ domain, 1.0);
                            }
                            else if(slotNumber.equals("2")){
                                features.put("NEL  GROUP 5 PARENT POS & PARENT SEM-TYPE & CHILD POS & CHILD SEM-TYPE & SLOT & RANGE: " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + slotNumber +" & "+ range, 1.0);
                            }
                        }                        
                    }
                    
                    //GROUP 6
                    if (featureGroup.contains("6")) {
                        double depFrequencyScore = 0;
                        double headFrequency = 0;
                        
                        
                        if(depDudeName.equals("Individual")){
                            depFrequencyScore = state.getHiddenVariables().get(depNodeID).getCandidate().getDbpediaScore();
                        }
                        else if(depDudeName.equals("Property")){
                            depFrequencyScore = state.getHiddenVariables().get(depNodeID).getCandidate().getMatollScore();
                        }
                        
                        if(depDudeName.equals("Individual")){
                            headFrequency = state.getHiddenVariables().get(tokenID).getCandidate().getDbpediaScore();
                        }
                        else if(depDudeName.equals("Property")){
                            headFrequency = state.getHiddenVariables().get(tokenID).getCandidate().getMatollScore();
                        }
                        
                        
                        features.put("NEL  GROUP 6 PARENT FREQUENCY SCORE : ", headFrequency);
                        features.put("NEL  GROUP 6 CHILD FREQUENCY SCORE : ", depFrequencyScore);
                    }

//                    Set<String> mergedIntervalPOSTAGs = state.getDocument().getParse().getIntervalPOSTagsMerged(tokenID, depNodeID);
//
//                    double depSimilarityScore = getSimilarityScore(depToken, depURI);
//                    double depDBpediaScore = state.getHiddenVariables().get(depNodeID).getCandidate().getDbpediaScore();
//
//                    double headSimilarityScore = getSimilarityScore(headToken, headURI);
//                    double headMatollScore = state.getHiddenVariables().get(tokenID).getCandidate().getMatollScore();
//
//                    double score = (Math.max(depSimilarityScore, depDBpediaScore)) * 0.7 + 0.3 * (Math.max(headMatollScore, headSimilarityScore));
//
//                    if (depDudeName.equals("Individual")) {
//                        double individualScore = depDBpediaScore * 0.3 + depSimilarityScore * 0.7;
//
////                        featureVector.addToValue("NEL EDGE - DEP FEATURE: Individual" + "   dep: " + depDudeName + ":" + depPOS + " dep-relation: " + depRelation + " score = ", individualScore);
//                    }
//
//                    if (headURI.contains("ontology")) {
//
//                        features.put("NEL EDGE - DEP FEATURE: ONTOLOGY Namespace " + " head: " + dudeName + ":" + headPOS + "   dep: " + depDudeName + ":" + depPOS + " dep-relation: " + depRelation + " score = ", score);
//
//                        //mayor of Tel-Aviv, headquarters of MI6
//                        // NN(s) IN NNP
//                        for (String pattern : mergedIntervalPOSTAGs) {
//                            features.put("NEL EDGE - DEP FEATURE: ONTOLOGY Namespace " + " head: " + dudeName + ":" + headPOS + "   dep: " + depDudeName + ":" + depPOS + " dep-relation: " + depRelation + " Pattern = " + pattern, 1.0);
//                        }
//                    } else {
//
//                        features.put("NEL EDGE - DEP FEATURE: RDF Namespace" + " head: " + dudeName + ":" + headPOS + "   dep: " + depDudeName + ":" + depPOS + " dep-relation: " + depRelation + " score = ", score);
//
//                        for (String pattern : mergedIntervalPOSTAGs) {
//                            features.put("NEL EDGE - DEP FEATURE: RDF Namespace " + " head: " + dudeName + ":" + headPOS + "   dep: " + depDudeName + ":" + depPOS + " dep-relation: " + depRelation + " Pattern = " + pattern, 1.0);
//                        }
//                    }
                }
            }
        }

        return features;
    }

    private Map<String, Double> getSiblingFeatures(State state, String featureGroup) {
        Map<String, Double> features = new HashMap<>();

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

//            if (headURI.equals("EMPTY_STRING")) {
//                continue;
//            }

            List<Integer> siblings = state.getDocument().getParse().getSiblings(tokenID, validPOSTags);

            if (!siblings.isEmpty()) {
                for (Integer depNodeID : siblings) {
                    String depToken = state.getDocument().getParse().getToken(depNodeID);
                    String depURI = state.getHiddenVariables().get(depNodeID).getCandidate().getUri();
                    String depPOS = state.getDocument().getParse().getPOSTag(depNodeID);
                    Integer depDudeID = state.getHiddenVariables().get(depNodeID).getDudeId();
                    String depRelation = state.getDocument().getParse().getSiblingDependencyRelation(depNodeID, tokenID);
                    String slotNumber = state.getSlot(depNodeID, tokenID);

                    String depDudeName = "EMPTY";
                    if (depDudeID != -1) {
                        depDudeName = semanticTypes.get(depDudeID);
                    }

                    
                    //GROUP 1
                    if (featureGroup.contains("1")) {
                        features.put("NEL  GROUP 1 BROTHER : Lemma & URI : " + headToken + " & " + headURI, 1.0);
                        features.put("NEL  GROUP 1 BROTHER : POS & SEM-TYPE : " + headPOS + " & " + dudeName, 1.0);

                        features.put("NEL  GROUP 1 SIBLING : Lemma & URI : " + depToken + " & " + depURI, 1.0);
                        features.put("NEL  GROUP 1 SIBLING : POS & SEM-TYPE : " + depPOS + " & " + depDudeName, 1.0);
                        features.put("NEL  GROUP 1 DEP-REL & SLOT : " + depRelation + " & " + slotNumber, 1.0);

                    }

                    //GROUP 2
                    if (featureGroup.contains("2")) {
                        features.put("NEL  GROUP 2 BROTHER POS & BROTHER SEM-TYPE & SIBLING POS & SIBLING SEM-TYPE : " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName, 1.0);
                        features.put("NEL  GROUP 2 BROTHER POS & BROTHER SEM-TYPE & SIBLING POS & SIBLING SEM-TYPE & DEP-REL : " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + depRelation, 1.0);
                        features.put("NEL  GROUP 2 BROTHER POS & BROTHER SEM-TYPE & SIBLING POS & SIBLING SEM-TYPE & SLOT : " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + slotNumber, 1.0);
                        features.put("NEL  GROUP 2 BROTHER POS & BROTHER SEM-TYPE & SIBLING POS & SIBLING SEM-TYPE & DEP-REL & SLOT : " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + depRelation + " & " + slotNumber, 1.0);
                    }

                    //GROUP 3
                    if (featureGroup.contains("3")) {
                        features.put("NEL  GROUP 3 BROTHER POS & BROTHER SEM-TYPE & SIBLING POS & SIBLING SEM-TYPE & BROTHER URI: " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + headURI, 1.0);
                        features.put("NEL  GROUP 3 BROTHER POS & BROTHER SEM-TYPE & SIBLING POS & SIBLING SEM-TYPE & SIBLING URI: " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + depURI, 1.0);
                        features.put("NEL  GROUP 3 BROTHER POS & BROTHER SEM-TYPE & SIBLING POS & SIBLING SEM-TYPE & BROTHER URI & SIBLING URI: " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + headURI + " & " + depURI, 1.0);
                    }

                    //GROUP 4
                    if (featureGroup.contains("4")) {
                        double headSimilarityScore = getSimilarityScore(headToken, headURI);
                        double depSimilarityScore = getSimilarityScore(depToken, depURI);

                        features.put("NEL  GROUP 4 SIM (BROTHER URI, PARENT LEMMA) : ", headSimilarityScore);
                        features.put("NEL  GROUP 4 SIM (SIBLING URI, SIBLING LEMMA) : ", depSimilarityScore);
                    }

                    //GROUP 5
                    if (featureGroup.contains("5")) {
                        
                        if (dudeName.equals("Property")) {
                            String range = DBpediaEndpoint.getRange(headURI);
                            String domain = DBpediaEndpoint.getDomain(headURI);
                            
                            if(slotNumber.equals("1")){
                                features.put("NEL  GROUP 5 BROTHER POS & BROTHER SEM-TYPE & SIBLING POS & SIBLING SEM-TYPE & SLOT & DOMAIN: " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + slotNumber +" & "+ domain, 1.0);
                            }
                            else if(slotNumber.equals("2")){
                                features.put("NEL  GROUP 5 BROTHER POS & BROTHER SEM-TYPE & SIBLING POS & SIBLING SEM-TYPE & SLOT & RANGE: " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + slotNumber +" & "+ range, 1.0);
                            }
                        }                        
                    }
                    
                    //GROUP 6
                    if (featureGroup.contains("6")) {
                        double depFrequencyScore = 0;
                        double headFrequency = 0;
                        
                        
                        if(depDudeName.equals("Individual")){
                            depFrequencyScore = state.getHiddenVariables().get(depNodeID).getCandidate().getDbpediaScore();
                        }
                        else if(depDudeName.equals("Property")){
                            depFrequencyScore = state.getHiddenVariables().get(depNodeID).getCandidate().getMatollScore();
                        }
                        
                        if(depDudeName.equals("Individual")){
                            headFrequency = state.getHiddenVariables().get(tokenID).getCandidate().getDbpediaScore();
                        }
                        else if(depDudeName.equals("Property")){
                            headFrequency = state.getHiddenVariables().get(tokenID).getCandidate().getMatollScore();
                        }
                        
                        
                        features.put("NEL  GROUP 6 BROTHER FREQUENCY SCORE : ", headFrequency);
                        features.put("NEL  GROUP 6 SIBLING FREQUENCY SCORE : ", depFrequencyScore);
                    }
                    
//                    if (depURI.equals("EMPTY_STRING")) {
//                        continue;
//                    }

//                    Set<String> mergedIntervalPOSTAGs = state.getDocument().getParse().getIntervalPOSTagsMerged(tokenID, depNodeID);
//
//                    double depSimilarityScore = getSimilarityScore(depToken, depURI);
//                    double depDBpediaScore = state.getHiddenVariables().get(depNodeID).getCandidate().getDbpediaScore();
//                    double depMatollScore = state.getHiddenVariables().get(depNodeID).getCandidate().getMatollScore();
//
//                    double headSimilarityScore = getSimilarityScore(headToken, headURI);
//                    double headMatollScore = state.getHiddenVariables().get(tokenID).getCandidate().getMatollScore();
//                    double headDBpediaScore = state.getHiddenVariables().get(tokenID).getCandidate().getDbpediaScore();
//
//                    double score = (Math.max(depSimilarityScore, Math.max(depMatollScore, depDBpediaScore))) * 0.7 + 0.3 * (Math.max(Math.max(headDBpediaScore, headMatollScore), headSimilarityScore));
//
//                    if (depDudeName.equals("Individual")) {
//                        double individualScore = depDBpediaScore * 0.3 + depSimilarityScore * 0.7;
//
////                        featureVector.addToValue("NEL EDGE - SIBLING FEATURE: Individual" + "   dep: " + depDudeName + ":" + depPOS + " dep-relation: to sibling node: " + depRelation + " score = ", individualScore);
//                    }
//
//                    if (dudeName.equals("Individual")) {
//                        double individualScore = headDBpediaScore * 0.3 + headSimilarityScore * 0.7;
//
////                        featureVector.addToValue("NEL EDGE - SIBLING FEATURE: Individual" + "   sibling: " + dudeName + ":" + headPOS + " dep-relation: to dependent node: " + depRelation + " score = ", individualScore);
//                    }
//
//                    if (headURI.contains("ontology")) {
//
//                        features.put("NEL EDGE - SIBLING FEATURE: ONTOLOGY Namespace " + " head: " + dudeName + ":" + headPOS + "   dep: " + depDudeName + ":" + depPOS + " dep-relation: " + depRelation + " score = ", score);
//
//                        for (String pattern : mergedIntervalPOSTAGs) {
//                            features.put("NEL EDGE - SIBLING FEATURE: ONTOLOGY Namespace " + " head: " + dudeName + ":" + headPOS + "   dep: " + depDudeName + ":" + depPOS + " dep-relation: " + depRelation + " Pattern = " + pattern, 1.0);
//                        }
//                    } else {
//
//                        features.put("NEL EDGE - SIBLING FEATURE: " + " head: " + dudeName + ":" + headPOS + "   dep: " + depDudeName + ":" + depPOS + " dep-relation: " + depRelation + " score = ", score);
//
//                        for (String pattern : mergedIntervalPOSTAGs) {
//                            features.put("NEL EDGE - SIBLING FEATURE: RDF Namespace " + " head: " + dudeName + ":" + headPOS + "   dep: " + depDudeName + ":" + depPOS + " dep-relation: " + depRelation + " Pattern = " + pattern, 1.0);
//                        }
//                    }
                }
            }
        }

        return features;
    }

    /**
     * levenstein sim
     */
    private double getSimilarityScore(String node, String uri) {

        String label = DBpediaLabelRetriever.getLabel(uri, Main.lang);

        //compute levenstein edit distance similarity and normalize
        final double weightedEditSimilarity = StringSimilarityMeasures.score(label, node);

        return weightedEditSimilarity;
    }

}
