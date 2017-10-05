/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.template;

import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.learning.QueryConstructor;
import de.citec.sc.main.Main;
import de.citec.sc.query.CandidateRetriever;
import de.citec.sc.query.DBpediaLabelRetriever;
import de.citec.sc.utils.DBpediaEndpoint;
import de.citec.sc.utils.ProjectConfiguration;
import de.citec.sc.utils.StringSimilarityUtils;
import de.citec.sc.variable.URIVariable;

import de.citec.sc.variable.State;
import factors.Factor;
import factors.FactorScope;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import learning.Vector;
import net.ricecode.similarity.SimilarityStrategy;
import net.ricecode.similarity.StringSimilarityMeasures;
import org.eclipse.jetty.util.ConcurrentHashSet;
import templates.AbstractTemplate;

/**
 *
 * @author sherzod
 */
public class QAEdgeAdvTemplate extends AbstractTemplate<AnnotatedDocument, State, StateFactorScope<State>> {

    private Set<String> validPOSTags;
    private Set<String> validEdges;
    private Map<Integer, String> semanticTypes;
    private Map<Integer, String> specialSemanticTypes;

    public QAEdgeAdvTemplate(Set<String> validPOSTags, Set<String> edges, Map<Integer, String> s, Map<Integer, String> sp) {

        semanticTypes = new ConcurrentHashMap<>();
        for (Integer key : s.keySet()) {
            semanticTypes.put(key, s.get(key));
        }

        specialSemanticTypes = new ConcurrentHashMap<>();
        for (Integer key : sp.keySet()) {
            specialSemanticTypes.put(key, sp.get(key));
        }

        this.validPOSTags = new ConcurrentHashSet<>();
        for (String v : validPOSTags) {
            this.validPOSTags.add(v);
        }

        this.validEdges = new ConcurrentHashSet<>();
        for (String v : edges) {
            this.validEdges.add(v);
        }
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

        Map<String, Double> depFeatures = getDependencyFeatures(state, ProjectConfiguration.getFeatureGroup());
        Map<String, Double> queryTypeFeatures = getQueryTypeFeatures(state);
//        Map<String, Double> siblingFeatures = getSiblingFeatures(state, ProjectConfiguration.getFeatureGroup());

        for (String k : depFeatures.keySet()) {
            featureVector.addToValue(k, depFeatures.get(k));
        }
        for (String k : queryTypeFeatures.keySet()) {
            featureVector.addToValue(k, queryTypeFeatures.get(k));
        }

//        for (String k : siblingFeatures.keySet()) {
//            featureVector.addToValue(k, siblingFeatures.get(k));
//        }
    }

    /**
     * returns features for query type
     */
    private Map<String, Double> getQueryTypeFeatures(State state) {
        Map<String, Double> features = new HashMap<>();
        
        String query = QueryConstructor.getSPARQLQuery(state);
        String constructedQueryType = "";
        if(query.contains("SELECT")){
            if(query.contains("COUNT")){
                constructedQueryType = "COUNT";
            }
            else{
                constructedQueryType = "SELECT";
            }
        }
        else{
            constructedQueryType = "ASK";
        }
        
        List<Integer> tokenIDs = new ArrayList<>(state.getDocument().getParse().getNodes().keySet());
        Collections.sort(tokenIDs);
        
        Integer firstTokenId = tokenIDs.get(0);
        String firstPOS = state.getDocument().getParse().getPOSTag(firstTokenId);
        String firstToken = state.getDocument().getParse().getToken(firstTokenId);
        
        String queryType = state.getQueryTypeVariable().toString();
        
        features.put("QUERY TYPE FEATURE: TOKEN:"+firstToken+" POS:"+firstPOS+" "+queryType, 1.0);
//        features.put("QUERY TYPE FEATURE: TOKEN:"+firstToken+" POS:"+firstPOS+" "+queryType +" Constructed Type: " +constructedQueryType, 1.0);
        
        return features;
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
            if (semanticTypes.containsKey(dudeID)) {
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
                    String depRelation = "";
                    //if it's parent-child relation, if not then it's sibling relation
                    if (state.getDocument().getParse().getHeadNode() == tokenID) {
                        depRelation = state.getDocument().getParse().getDependencyRelation(depNodeID);
                    } else {
                        depRelation = state.getDocument().getParse().getSiblingDependencyRelation(depNodeID, tokenID);
                    }
                    String depDudeName = "EMPTY";
                    String slotNumber = state.getSlot(depNodeID, tokenID);

                    if (specialSemanticTypes.containsKey(depDudeID)) {
                        depDudeName = specialSemanticTypes.get(depDudeID);
                    }

//                    if (depURI.equals("EMPTY_STRING")) {
//                        continue;
//                    }
                    //GROUP 1
                    if (featureGroup.contains("1")) {
                        // features.put("NEL  GROUP 1 PARENT : Lemma & URI : " + headToken + " & " + headURI, 1.0);
//                        features.put("NEL  GROUP 1 PARENT : POS & SEM-TYPE : " + headPOS + " & " + dudeName, 1.0);

                        // features.put("NEL  GROUP 1 CHILD : Lemma & URI : " + depToken + " & " + depURI, 1.0);
//                        features.put("NEL  GROUP 1 CHILD : POS & SEM-TYPE : " + depPOS + " & " + depDudeName, 1.0);
//                        features.put("NEL  GROUP 1 DEP-REL & SLOT : " + depRelation + " & " + slotNumber, 1.0);
//                        features.put("QA  GROUP 1 PARENT POS & PARENT SEM-TYPE & CHILD POS & CHILD SEM-TYPE & DEP-REL : " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + depRelation, 1.0);
//                        features.put("QA  GROUP 1 PARENT POS & PARENT SEM-TYPE & CHILD POS & CHILD SEM-TYPE & SLOT : " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + slotNumber, 1.0);
//                        features.put("QA  GROUP 1 PARENT POS & PARENT SEM-TYPE & CHILD LEMMA & CHILD SEM-TYPE & DEP-REL & SLOT : " + headPOS + " & " + dudeName + " & " + depToken + " & " + depDudeName + " & " + depRelation + " & " + slotNumber, 1.0);    
                        features.put("QA  GROUP 1 PARENT POS & PARENT SEM-TYPE & CHILD POS & CHILD SEM-TYPE & DEP-REL & SLOT : " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + depRelation + " & " + slotNumber, 1.0);
                        features.put("QA  GROUP 1 PARENT POS & PARENT SEM-TYPE & CHILD LEMMA & CHILD POS & CHILD SEM-TYPE & DEP-REL & SLOT : " + headPOS + " & " + dudeName + " & " + depToken + " & " + depPOS + " & " + depDudeName + " & " + depRelation + " & " + slotNumber, 1.0);
                    }

                    //GROUP 3
                    if (featureGroup.contains("2")) {

                        if (dudeName.equals("Property")) {
                            String range = DBpediaEndpoint.getRange(headURI);
                            String domain = DBpediaEndpoint.getDomain(headURI);

                            List<Integer> tokenIDs = new ArrayList<>(state.getDocument().getParse().getNodes().keySet());
                            Collections.sort(tokenIDs);

                            Integer firstTokenID = tokenIDs.get(0);
                            String firstToken = state.getDocument().getParse().getToken(firstTokenID);
                            String query = QueryConstructor.getSPARQLQuery(state);

                            if (slotNumber.equals("1")) {
                                features.put("QA  GROUP 2 PARENT POS & PARENT SEM-TYPE & CHILD POS & CHILD SEM-TYPE & SLOT & DOMAIN: " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + slotNumber + " & " + domain, 1.0);
                                features.put("QA  GROUP 2 PARENT POS & PARENT SEM-TYPE & CHILD POS & CHILD SEM-TYPE & SLOT & DEP-REL & DOMAIN: " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + slotNumber + " & " + depRelation+" & " + domain, 1.0);
                                
                                if(query.contains("COUNT")){
                                    features.put("QA  GROUP 2 PARENT POS & PARENT SEM-TYPE & CHILD POS & CHILD SEM-TYPE & SLOT & DEP-REL & DOMAIN & FIRST_TOKEN & TYPE " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + slotNumber + " & " +depRelation+ " & " + domain  + " & "+firstToken +" & COUNT", 1.0);
                                }
                                else{
                                    features.put("QA  GROUP 2 PARENT POS & PARENT SEM-TYPE & CHILD POS & CHILD SEM-TYPE & SLOT & DEP-REL & DOMAIN & FIRST_TOKEN & TYPE " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + slotNumber + " & " +depRelation+ " & " + domain  + " & "+firstToken +" & STANDARD", 1.0);
                                }
                                
                                
                            } else if (slotNumber.equals("2")) {
                                features.put("QA  GROUP 2 PARENT POS & PARENT SEM-TYPE & CHILD POS & CHILD SEM-TYPE & SLOT & RANGE: " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + slotNumber + " & " + range, 1.0);
                                features.put("QA  GROUP 2 PARENT POS & PARENT SEM-TYPE & CHILD POS & CHILD SEM-TYPE & SLOT & DEP-REL & RANGE: " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + slotNumber + " & " + depRelation+" & " + range, 1.0);
                                
                                if(query.contains("COUNT")){
                                    features.put("QA  GROUP 2 PARENT POS & PARENT SEM-TYPE & CHILD POS & CHILD SEM-TYPE & SLOT & DEP-REL & RANGE & FIRST_TOKEN & TYPE " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + slotNumber + " & " +depRelation+ " & " + range  + " & "+firstToken +" & COUNT", 1.0);
                                }
                                else{
                                    features.put("QA  GROUP 2 PARENT POS & PARENT SEM-TYPE & CHILD POS & CHILD SEM-TYPE & SLOT & DEP-REL & RANGE & FIRST_TOKEN & TYPE " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + slotNumber + " & " +depRelation+ " & " + range  + " & "+firstToken +" & STANDARD", 1.0);
                                }
                            }
                        }

                        if (depDudeName.equals("Property")) {
                            String range = DBpediaEndpoint.getRange(depURI);
                            String domain = DBpediaEndpoint.getDomain(depURI);

                            if (slotNumber.equals("1")) {
                                features.put("QA  GROUP 2 CHILD POS & CHILD SEM-TYPE & PARENT POS & PARENT SEM-TYPE & SLOT & DOMAIN: " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + slotNumber + " & " + domain, 1.0);
                            } else if (slotNumber.equals("2")) {
                                features.put("QA  GROUP 2 CHILD POS & CHILD SEM-TYPE & PARENT POS & PARENT SEM-TYPE & SLOT & RANGE: " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + slotNumber + " & " + range, 1.0);
                            }
                        }
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

            if (semanticTypes.containsKey(dudeID)) {
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

                    if (specialSemanticTypes.containsKey(depDudeID)) {
                        depDudeName = specialSemanticTypes.get(depDudeID);
                    }
                    
                    //GROUP 7
                    if (featureGroup.contains("7")) {
                        features.put("QA  GROUP 1 BROTHER : Lemma & URI : " + headToken + " & " + headURI, 1.0);
                        features.put("QA  GROUP 1 BROTHER : Lemma & SEM-TYPE : " + depToken + " & " + dudeName, 1.0);
                        features.put("QA  GROUP 1 BROTHER : POS & SEM-TYPE : " + headPOS + " & " + dudeName, 1.0);

                        features.put("QA  GROUP 1 SIBLING : Lemma & URI : " + depToken + " & " + depURI, 1.0);
                        features.put("QA  GROUP 1 SIBLING : Lemma & SEM-TYPE : " + depToken + " & " + depDudeName, 1.0);
                        features.put("QA  GROUP 1 SIBLING : POS & SEM-TYPE : " + depPOS + " & " + depDudeName, 1.0);
                        features.put("QA  GROUP 1 DEP-REL & SLOT : " + depRelation + " & " + slotNumber, 1.0);

                    }

                    //GROUP 1
                    if (featureGroup.contains("1")) {
                        features.put("QA  GROUP 1 BROTHER : Lemma & URI : " + headToken + " & " + headURI, 1.0);
                        features.put("QA  GROUP 1 BROTHER : Lemma & SEM-TYPE : " + depToken + " & " + dudeName, 1.0);
                        features.put("QA  GROUP 1 BROTHER : POS & SEM-TYPE : " + headPOS + " & " + dudeName, 1.0);

                        features.put("QA  GROUP 1 SIBLING : Lemma & URI : " + depToken + " & " + depURI, 1.0);
                        features.put("QA  GROUP 1 SIBLING : Lemma & SEM-TYPE : " + depToken + " & " + depDudeName, 1.0);
                        features.put("QA  GROUP 1 SIBLING : POS & SEM-TYPE : " + depPOS + " & " + depDudeName, 1.0);
                        features.put("QA  GROUP 1 DEP-REL & SLOT : " + depRelation + " & " + slotNumber, 1.0);

                    }

                    //GROUP 2
                    if (featureGroup.contains("2")) {
                        features.put("QA  GROUP 2 BROTHER POS & BROTHER SEM-TYPE & SIBLING POS & SIBLING SEM-TYPE : " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName, 1.0);
                        features.put("QA  GROUP 2 BROTHER POS & BROTHER SEM-TYPE & SIBLING POS & SIBLING SEM-TYPE & DEP-REL : " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + depRelation, 1.0);
                        features.put("QA  GROUP 2 BROTHER POS & BROTHER SEM-TYPE & SIBLING POS & SIBLING SEM-TYPE & SLOT : " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + slotNumber, 1.0);
                        features.put("QA  GROUP 2 BROTHER POS & BROTHER SEM-TYPE & SIBLING POS & SIBLING SEM-TYPE & DEP-REL & SLOT : " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + depRelation + " & " + slotNumber, 1.0);
                    }

                    //GROUP 3
                    if (featureGroup.contains("3")) {
                        features.put("QA  GROUP 3 BROTHER POS & BROTHER SEM-TYPE & SIBLING POS & SIBLING SEM-TYPE & BROTHER URI: " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + headURI, 1.0);
                        features.put("QA  GROUP 3 BROTHER POS & BROTHER SEM-TYPE & SIBLING POS & SIBLING SEM-TYPE & SIBLING URI: " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + depURI, 1.0);
                        features.put("QA  GROUP 3 BROTHER POS & BROTHER SEM-TYPE & SIBLING POS & SIBLING SEM-TYPE & BROTHER URI & SIBLING URI: " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + headURI + " & " + depURI, 1.0);
                    }

                    //GROUP 4
                    if (featureGroup.contains("4")) {
                        double headSimilarityScore = getSimilarityScore(headToken, headURI);
                        double depSimilarityScore = getSimilarityScore(depToken, depURI);

                        features.put("QA  GROUP 4 SIM (BROTHER URI, PARENT LEMMA) : ", headSimilarityScore);
                        features.put("QA  GROUP 4 SIM (SIBLING URI, SIBLING LEMMA) : ", depSimilarityScore);
                    }

                    //GROUP 5
                    if (featureGroup.contains("5")) {

                        if (dudeName.equals("Property")) {
                            String range = DBpediaEndpoint.getRange(headURI);
                            String domain = DBpediaEndpoint.getDomain(headURI);

                            if (slotNumber.equals("1")) {
                                features.put("QA  GROUP 5 BROTHER POS & BROTHER SEM-TYPE & SIBLING POS & SIBLING SEM-TYPE & SLOT & DOMAIN: " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + slotNumber + " & " + domain, 1.0);
                            } else if (slotNumber.equals("2")) {
                                features.put("QA  GROUP 5 BROTHER POS & BROTHER SEM-TYPE & SIBLING POS & SIBLING SEM-TYPE & SLOT & RANGE: " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + slotNumber + " & " + range, 1.0);
                            }
                        }
                    }

                    //GROUP 6
                    if (featureGroup.contains("6")) {
                        double depFrequencyScore = 0;
                        double headFrequency = 0;

                        if (depDudeName.equals("Individual")) {
                            depFrequencyScore = state.getHiddenVariables().get(depNodeID).getCandidate().getDbpediaScore();
                        } else if (depDudeName.equals("Property")) {
                            depFrequencyScore = state.getHiddenVariables().get(depNodeID).getCandidate().getMatollScore();
                        }

                        if (depDudeName.equals("Individual")) {
                            headFrequency = state.getHiddenVariables().get(tokenID).getCandidate().getDbpediaScore();
                        } else if (depDudeName.equals("Property")) {
                            headFrequency = state.getHiddenVariables().get(tokenID).getCandidate().getMatollScore();
                        }

                        features.put("QA  GROUP 6 BROTHER FREQUENCY SCORE : ", headFrequency);
                        features.put("QA  GROUP 6 SIBLING FREQUENCY SCORE : ", depFrequencyScore);
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
