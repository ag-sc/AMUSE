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
import de.citec.sc.utils.StringSimilarityUtils;
import de.citec.sc.variable.URIVariable;

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
public class NELEdgeTemplate extends AbstractTemplate<AnnotatedDocument, State, StateFactorScope<State>> {

    private Set<String> validPOSTags;
    private Set<String> validEdges;
    private Map<Integer, String> semanticTypes;

    public NELEdgeTemplate(Set<String> validPOSTags, Set<String> edges, Map<Integer, String> s) {
        semanticTypes = new ConcurrentHashMap<>();
        for (Integer key : s.keySet()) {
            semanticTypes.put(key, s.get(key));
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

        String featureGroup = ProjectConfiguration.getFeatureGroup();

        Map<String, Double> depFeatures = getDependencyFeatures(state, featureGroup);
//        Map<String, Double> siblingFeatures = getSiblingFeatures(state, featureGroup);

        for (String k : depFeatures.keySet()) {
            featureVector.addToValue(k, depFeatures.get(k));
        }

//        for (String k : siblingFeatures.keySet()) {
//            featureVector.addToValue(k, siblingFeatures.get(k));
//        }
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
            List<Integer> dependentNodes = state.getDocument().getParse().getDependentNodes(tokenID, validPOSTags);

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

                        features.put("NEL  GROUP 1 PARENT POS & PARENT LEMMA & CHILD POS & CHILD LEMMA: " + headPOS + " & " + headToken + " & " + depPOS + " & " + depToken, 1.0);

//                        features.put("NEL  GROUP 2 PARENT POS & PARENT SEM-TYPE & CHILD POS & CHILD SEM-TYPE & DEP-REL : " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + depRelation, 1.0);
//                        features.put("NEL  GROUP 2 PARENT POS & PARENT SEM-TYPE & CHILD POS & CHILD SEM-TYPE & SLOT : " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + slotNumber, 1.0);
                        features.put("NEL GROUP 1 PARENT POS & PARENT SEM-TYPE & CHILD POS & CHILD SEM-TYPE & DEP-REL & SLOT : " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + depRelation + " & " + slotNumber, 1.0);
                    }

                    //GROUP 2
                    if (featureGroup.contains("2")) {
                        double headSimilarityScore = StringSimilarityUtils.getSimilarityScore(headToken, headURI);
                        double depSimilarityScore = StringSimilarityUtils.getSimilarityScore(depToken, depURI);

                        if (dudeName.equals("Property")) {
                            if (headURI.contains("ontology")) {

                                if (headSimilarityScore > 0.8) {
                                    features.put("NEL GROUP 2 PARENT SIM > 0.8 : ONTOLOGY " + dudeName, 1.0);
                                }
                                if (headSimilarityScore > 0.9) {
                                    features.put("NEL GROUP 2 PARENT SIM > 0.9 : ONTOLOGY " + dudeName, 1.0);
                                }
                                if (headSimilarityScore > 0.95) {
                                    features.put("NEL GROUP 2 PARENT SIM > 0.95 : ONTOLOGY " + dudeName, 1.0);
                                }
                                if (headSimilarityScore == 1.0) {
                                    features.put("NEL GROUP 2 PARENT SIM = 1.0 : ONTOLOGY " + dudeName, 1.0);
                                }
                            } else {
                                if (headSimilarityScore > 0.8) {
                                    features.put("NEL GROUP 2 PARENT SIM > 0.8 : RDF " + dudeName, 1.0);
                                }
                                if (headSimilarityScore > 0.9) {
                                    features.put("NEL GROUP 2 PARENT SIM > 0.9 : RDF " + dudeName, 1.0);
                                }
                                if (headSimilarityScore > 0.95) {
                                    features.put("NEL GROUP 2 PARENT SIM > 0.95 : RDF " + dudeName, 1.0);
                                }
                                if (headSimilarityScore == 1.0) {
                                    features.put("NEL GROUP 2 PARENT SIM = 1.0 : RDF " + dudeName, 1.0);
                                }
                            }
                        } else {
                            if (headSimilarityScore > 0.8) {
                                features.put("NEL GROUP 2 PARENT SIM > 0.8 : " + dudeName, 1.0);
                            }
                            if (headSimilarityScore > 0.9) {
                                features.put("NEL GROUP 2 PARENT SIM > 0.9 : " + dudeName, 1.0);
                            }
                            if (headSimilarityScore > 0.95) {
                                features.put("NEL GROUP 2 PARENT SIM > 0.95 : " + dudeName, 1.0);
                            }
                            if (headSimilarityScore == 1.0) {
                                features.put("NEL GROUP 2 PARENT SIM = 1.0 : " + dudeName, 1.0);
                            }
                        }

                        //depDudeName
                        if (depDudeName.equals("Property")) {
                            if (depURI.contains("ontology")) {

                                if (depSimilarityScore > 0.8) {
                                    features.put("NEL GROUP 2 CHILD SIM > 0.8 : ONTOLOGY " + depDudeName, 1.0);
                                }
                                if (depSimilarityScore > 0.9) {
                                    features.put("NEL GROUP 2 CHILD SIM > 0.9 : ONTOLOGY " + depDudeName, 1.0);
                                }
                                if (depSimilarityScore > 0.95) {
                                    features.put("NEL GROUP 2 CHILD SIM > 0.95 : ONTOLOGY " + depDudeName, 1.0);
                                }
                                if (depSimilarityScore == 1.0) {
                                    features.put("NEL GROUP 2 CHILD SIM = 1.0 : ONTOLOGY " + depDudeName, 1.0);
                                }
                            } else {
                                if (depSimilarityScore > 0.8) {
                                    features.put("NEL GROUP 2 CHILD SIM > 0.8 : RDF " + depDudeName, 1.0);
                                }
                                if (depSimilarityScore > 0.9) {
                                    features.put("NEL GROUP 2 CHILD SIM > 0.9 : RDF " + depDudeName, 1.0);
                                }
                                if (depSimilarityScore > 0.95) {
                                    features.put("NEL GROUP 2 CHILD SIM > 0.95 : RDF " + depDudeName, 1.0);
                                }
                                if (depSimilarityScore == 1.0) {
                                    features.put("NEL GROUP 2 CHILD SIM = 1.0 : RDF " + depDudeName, 1.0);
                                }
                            }
                        } else {
                            if (depSimilarityScore > 0.8) {
                                features.put("NEL GROUP 2 CHILD SIM > 0.8 : " + depDudeName, 1.0);
                            }
                            if (depSimilarityScore > 0.9) {
                                features.put("NEL GROUP 2 CHILD SIM > 0.9 : " + depDudeName, 1.0);
                            }
                            if (depSimilarityScore > 0.95) {
                                features.put("NEL GROUP 2 CHILD SIM > 0.95 : " + depDudeName, 1.0);
                            }
                            if (depSimilarityScore == 1.0) {
                                features.put("NEL GROUP 2 CHILD SIM = 1.0 : " + depDudeName, 1.0);
                            }
                        }

                        // features.put("NEL  GROUP 4 SIM (PARENT URI, PARENT LEMMA) : ", headSimilarityScore);
                        // features.put("NEL  GROUP 4 SIM (CHILD URI, CHILD LEMMA) : ", depSimilarityScore);
//                        if (headSimilarityScore > 0.8) {
//                            features.put("NEL GROUP 2 PARENT SIM > 0.8 : ", 1.0);
//                        }
//                        if (depSimilarityScore > 0.8) {
//                            features.put("NEL GROUP 2 CHILD SIM > 0.8 " + depDudeName, 1.0);
//                        }
//
//                        if (headSimilarityScore > 0.9) {
//                            features.put("NEL GROUP 2 PARENT SIM > 0.9 : ", 1.0);
//                        }
//                        if (depSimilarityScore > 0.9) {
//                            features.put("NEL GROUP 2 CHILD SIM > 0.9 " + depDudeName, 1.0);
//                        }
//
//                        if (headSimilarityScore == 1.0) {
//                            features.put("NEL GROUP 2 PARENT SIM = 1.0 ", 1.0);
//                        }
//                        if (depSimilarityScore == 1.0) {
//                            features.put("NEL GROUP 2 CHILD SIM == 1.0 " + depDudeName, 1.0);
//                        }
                    }

                    //GROUP 3
                    if (featureGroup.contains("3")) {

                        if (dudeName.equals("Property")) {
                            String range = DBpediaEndpoint.getRange(headURI);
                            String domain = DBpediaEndpoint.getDomain(headURI);

                            if (slotNumber.equals("1")) {

                                if (headURI.contains("ontology")) {
                                    features.put("NEL GROUP 3 ONTOLOGY NAMESPACE : PARENT POS & PARENT SEM-TYPE & CHILD POS & CHILD SEM-TYPE & SLOT & DOMAIN: " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + slotNumber + " & " + domain, 1.0);
                                } else {
                                    features.put("NEL GROUP 3 RDF NAMESPACE : PARENT POS & PARENT SEM-TYPE & CHILD POS & CHILD SEM-TYPE & SLOT & NO_DOMAIN: " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + slotNumber, 1.0);

                                }

                            } else if (slotNumber.equals("2")) {

                                if (headURI.contains("ontology")) {
                                    features.put("NEL GROUP 3 ONTOLOGY NAMESPACE : PARENT POS & PARENT SEM-TYPE & CHILD POS & CHILD SEM-TYPE & SLOT & RANGE: " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + slotNumber + " & " + range, 1.0);
                                } else {
                                    features.put("NEL GROUP 3 RDF NAMESPACE : PARENT POS & PARENT SEM-TYPE & CHILD POS & CHILD SEM-TYPE & SLOT & NO_RANGE: " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + slotNumber, 1.0);
                                }

                            }
                        }

                        if (depDudeName.equals("Property")) {
                            String range = DBpediaEndpoint.getRange(depURI);
                            String domain = DBpediaEndpoint.getDomain(depURI);

                            if (slotNumber.equals("1")) {
                                if (depURI.contains("ontology")) {
                                    features.put("NEL GROUP 3 ONTOLOGY NAMESPANCE : CHILD POS & CHILD SEM-TYPE & PARENT POS & PARENT SEM-TYPE & SLOT & DOMAIN: " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + slotNumber + " & " + domain, 1.0);
                                } else {
                                    features.put("NEL GROUP 3 RDF NAMESPANCE : CHILD POS & CHILD SEM-TYPE & PARENT POS & PARENT SEM-TYPE & SLOT & NO_DOMAIN: " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + slotNumber, 1.0);
                                }

                            } else if (slotNumber.equals("2")) {
                                if (depURI.contains("ontology")) {
                                    features.put("NEL GROUP 3 ONTOLOGY NAMESPANCE : CHILD POS & CHILD SEM-TYPE & PARENT POS & PARENT SEM-TYPE & SLOT & RANGE: " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + slotNumber + " & " + range, 1.0);
                                } else {
                                    features.put("NEL GROUP 3 RDF NAMESPANCE : CHILD POS & CHILD SEM-TYPE & PARENT POS & PARENT SEM-TYPE & SLOT & NO_RANGE: " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + slotNumber, 1.0);
                                }

                            }
                        }
                    }

                    //GROUP 4
                    if (featureGroup.contains("4")) {
                        double depFrequencyScore = 0;
                        double headFrequency = 0;

                        if (depDudeName.equals("Individual")) {
                            depFrequencyScore = state.getHiddenVariables().get(depNodeID).getCandidate().getDbpediaScore();
                        } else if (depDudeName.equals("Property")) {
                            depFrequencyScore = state.getHiddenVariables().get(depNodeID).getCandidate().getMatollScore();
                        }

                        if (dudeName.equals("Individual")) {
                            headFrequency = state.getHiddenVariables().get(tokenID).getCandidate().getDbpediaScore();
                        } else if (dudeName.equals("Property")) {
                            headFrequency = state.getHiddenVariables().get(tokenID).getCandidate().getMatollScore();
                        }

                        double headSimilarityScore = StringSimilarityUtils.getSimilarityScore(headToken, headURI);
                        double depSimilarityScore = StringSimilarityUtils.getSimilarityScore(depToken, depURI);

                        double score = (Math.max(depSimilarityScore, depFrequencyScore)) * 0.7 + 0.3 * (Math.max(headFrequency, headSimilarityScore));

//                        if (score > 0.6) {
//                            features.put("NEL GROUP 4 JOINT SIM > 0.6 : ", 1.0);
//                        }
//                        if (score > 0.7) {
//                            features.put("NEL GROUP 4 JOINT SIM > 0.7 : ", 1.0);
//                        }
                        if (score > 0.8) {
                            features.put("NEL GROUP 4 JOINT SIM > 0.8 : ", 1.0);
                        }
                        if (score > 0.9) {
                            features.put("NEL GROUP 4 JOINT SIM > 0.9 : ", 1.0);
                        }
                        if (score > 0.95) {
                            features.put("NEL GROUP 4 JOINT SIM > 0.95 : ", 1.0);
                        }

//                        if (Math.max(headFrequency, headSimilarityScore) > 0.8) {
//                            features.put("NEL GROUP 4 PARENT SIM > 0.8 : ", 1.0);
//                        }
//                        if (Math.max(depSimilarityScore, depFrequencyScore) > 0.8) {
//                            features.put("NEL GROUP 4 CHILD SIM > 0.8 " + depDudeName, 1.0);
//                        }
//
//                        if (Math.max(headFrequency, headSimilarityScore) > 0.9) {
//                            features.put("NEL GROUP 4 PARENT SIM > 0.9 : ", 1.0);
//                        }
//                        if (Math.max(depSimilarityScore, depFrequencyScore) > 0.9) {
//                            features.put("NEL GROUP 4 CHILD SIM > 0.9 "+ depDudeName, 1.0);
//                        }
//
//                        if (Math.max(headFrequency, headSimilarityScore) == 1.0) {
//                            features.put("NEL GROUP 4 PARENT SIM = 1.0 ", 1.0);
//                        }
//                        if (Math.max(depSimilarityScore, depFrequencyScore) == 1.0) {
//                            features.put("NEL GROUP 4 CHILD SIM == 1.0 "+depDudeName, 1.0);
//                        }
//                        if(depURI.contains("ontology") || headURI.contains("ontology")){
////                            features.put("NEL GROUP 4 - DEP FEATURE: ONTOLOGY Namespace " + " head: " + dudeName + ":" + headPOS + "   dep: " + depDudeName + ":" + depPOS + " dep-relation: " + depRelation + " score = ", score);
//                        }
//                        else{
////                            features.put("NEL GROUP 4 - DEP FEATURE: RDF Namespace " + " head: " + dudeName + ":" + headPOS + "   dep: " + depDudeName + ":" + depPOS + " dep-relation: " + depRelation + " score = ", score);
//                        }
//                        if (depDudeName.equals("Individual")) {
//                            double individualScore = depFrequencyScore * 0.3 + depSimilarityScore * 0.7;
//
//                            features.put("NEL EDGE - DEP FEATURE: Individual" + "   dep: " + depDudeName + ":" + depPOS + " dep-relation: " + depRelation + " score = ", individualScore);
//                        }
//
//                        if (dudeName.equals("Property")&& headURI.contains("ontology")) {
//
//                            features.put("NEL EDGE - DEP FEATURE: ONTOLOGY Namespace " + " head: " + dudeName + ":" + headPOS + "   dep: " + depDudeName + ":" + depPOS + " dep-relation: " + depRelation + " score = ", score);
//                        } 
//                        else if (dudeName.equals("Property")&& headURI.contains("property")) {
//
//                            features.put("NEL EDGE - DEP FEATURE: RDF Namespace" + " head: " + dudeName + ":" + headPOS + "   dep: " + depDudeName + ":" + depPOS + " dep-relation: " + depRelation + " score = ", score);
//                        } 
//                        else {
//
//                            features.put("NEL EDGE - DEP FEATURE: RDF Namespace" + " head: " + dudeName + ":" + headPOS + "   dep: " + depDudeName + ":" + depPOS + " dep-relation: " + depRelation + " score = ", score);
//                        }
//                        if (headFrequency > 0.8) {
//                            features.put("NEL GROUP 4 PARENT KB SCORE > 0.8 : ", 1.0);
//                        }
//                        if (depFrequencyScore > 0.8) {
//                            features.put("NEL GROUP 4 CHILD KB SCORE > 0.8 : ", 1.0);
//                        }
//
//                        if (headFrequency > 0.9) {
//                            features.put("NEL GROUP 4 PARENT KB SCORE > 0.9 : ", 1.0);
//                        }
//                        if (depFrequencyScore > 0.9) {
//                            features.put("NEL GROUP 4 CHILD KB SCORE > 0.9 : ", 1.0);
//                        }
//
//                        if (headFrequency == 1.0) {
//                            features.put("NEL GROUP 4 PARENT KB SCORE = 1.0 : ", 1.0);
//                        }
//                        if (depFrequencyScore == 1.0) {
//                            features.put("NEL GROUP 4 CHILD KB SCORE == 1.0 : ", 1.0);
//                        }
                    }

                    //GROUP 5
                    if (featureGroup.contains("5")) {

                        if (dudeName.equals("Property")) {
                            String range = DBpediaEndpoint.getRange(headURI);
                            String domain = DBpediaEndpoint.getDomain(headURI);

                            String prevPOS = state.getDocument().getParse().getPreviousPOSTag(tokenID);
                            String nextPOS = state.getDocument().getParse().getNextPOSTag(tokenID);

                            if (slotNumber.equals("1")) {

                                if (headURI.contains("ontology")) {
                                    features.put("NEL GROUP 3 ONTOLOGY NAMESPACE : PARENT POS & PARENT SEM-TYPE & PREV POS & NEXT POS & CHILD POS & CHILD SEM-TYPE & SLOT & DOMAIN: " + headPOS + " & " + dudeName + " & " + prevPOS + " & " + nextPOS + " & " + depPOS + " & " + depDudeName + " & " + slotNumber + " & " + domain, 1.0);
                                } else {

                                }

                            } else if (slotNumber.equals("2")) {

                                if (headURI.contains("ontology")) {
                                    features.put("NEL GROUP 3 ONTOLOGY NAMESPACE : PARENT POS & PARENT SEM-TYPE & PREV POS & NEXT POS & CHILD POS & CHILD SEM-TYPE & SLOT & RANGE: " + headPOS + " & " + dudeName + " & " + prevPOS + " & " + nextPOS + " & " + depPOS + " & " + depDudeName + " & " + slotNumber + " & " + range, 1.0);
                                } else {
                                }
                            }
                        }

                        if (depDudeName.equals("Property")) {
                            String range = DBpediaEndpoint.getRange(depURI);
                            String domain = DBpediaEndpoint.getDomain(depURI);

                            String prevPOS = state.getDocument().getParse().getPreviousPOSTag(depNodeID);
                            String nextPOS = state.getDocument().getParse().getNextPOSTag(depNodeID);

                            if (slotNumber.equals("1")) {
                                if (depURI.contains("ontology")) {
                                    features.put("NEL GROUP 3 ONTOLOGY NAMESPANCE : CHILD POS & CHILD SEM-TYPE & PREV POS & NEXT POS & PARENT POS & PARENT SEM-TYPE & SLOT & DOMAIN: " + headPOS + " & " + dudeName + " & " + prevPOS + " & " + nextPOS + " & " + depPOS + " & " + depDudeName + " & " + slotNumber + " & " + domain, 1.0);
                                } else {
                                }

                            } else if (slotNumber.equals("2")) {
                                if (depURI.contains("ontology")) {
                                    features.put("NEL GROUP 3 ONTOLOGY NAMESPANCE : CHILD POS & CHILD SEM-TYPE & PREV POS & NEXT POS & PARENT POS & PARENT SEM-TYPE & SLOT & RANGE: " + headPOS + " & " + dudeName + " & " + prevPOS + " & " + nextPOS + " & " + depPOS + " & " + depDudeName + " & " + slotNumber + " & " + range, 1.0);
                                } else {
                                }
                            }
                        }
                    }
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
                        double headSimilarityScore = StringSimilarityUtils.getSimilarityScore(headToken, headURI);
                        double depSimilarityScore = StringSimilarityUtils.getSimilarityScore(depToken, depURI);

                        features.put("NEL  GROUP 4 SIM (BROTHER URI, PARENT LEMMA) : ", headSimilarityScore);
                        features.put("NEL  GROUP 4 SIM (SIBLING URI, SIBLING LEMMA) : ", depSimilarityScore);
                    }

                    //GROUP 5
                    if (featureGroup.contains("5")) {

                        if (dudeName.equals("Property")) {
                            String range = DBpediaEndpoint.getRange(headURI);
                            String domain = DBpediaEndpoint.getDomain(headURI);

                            if (slotNumber.equals("1")) {
                                features.put("NEL  GROUP 5 BROTHER POS & BROTHER SEM-TYPE & SIBLING POS & SIBLING SEM-TYPE & SLOT & DOMAIN: " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + slotNumber + " & " + domain, 1.0);
                            } else if (slotNumber.equals("2")) {
                                features.put("NEL  GROUP 5 BROTHER POS & BROTHER SEM-TYPE & SIBLING POS & SIBLING SEM-TYPE & SLOT & RANGE: " + headPOS + " & " + dudeName + " & " + depPOS + " & " + depDudeName + " & " + slotNumber + " & " + range, 1.0);
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

}
