/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.sampling;

import de.citec.sc.main.Main;
import de.citec.sc.query.Candidate;
import de.citec.sc.query.EmbeddingLexicon;
import de.citec.sc.query.Instance;
import de.citec.sc.query.ManualLexicon;

import de.citec.sc.query.Search;
import de.citec.sc.utils.DBpediaEndpoint;
import de.citec.sc.utils.ProjectConfiguration;
import de.citec.sc.utils.Stopwords;
import de.citec.sc.variable.State;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import sampling.Explorer;

/**
 *
 * @author sherzod
 */
public class L2KBEdgeExplorer implements Explorer<State> {

    private static Map<Integer, String> semanticTypes;
    private static Set<String> validPOSTags;
    private static Set<String> validEdges;
    private static Set<String> acceptedDUDES;

    public L2KBEdgeExplorer(Map<Integer, String> assignedDUDES, Set<String> validPOSTags, Set<String> edges) {
        this.semanticTypes = assignedDUDES;
        this.validPOSTags = validPOSTags;
        this.validEdges = edges;
    }

    @Override
    public List getNextStates(State currentState) {
        Set<State> newStates = new HashSet<>();

        if (acceptedDUDES == null) {
            acceptedDUDES = new HashSet<>();
            acceptedDUDES.add("RestrictionClass");
            acceptedDUDES.add("UnderSpecifiedClass");
            acceptedDUDES.add("Property");
            acceptedDUDES.add("When");
        }
        if (acceptedDUDES.isEmpty()) {
            acceptedDUDES = new HashSet<>();
            acceptedDUDES.add("RestrictionClass");
            acceptedDUDES.add("UnderSpecifiedClass");
            acceptedDUDES.add("Property");
            acceptedDUDES.add("When");
        }

        for (int indexOfHeadNode : currentState.getDocument().getParse().getNodes().keySet()) {
            String node = currentState.getDocument().getParse().getNodes().get(indexOfHeadNode);

            String pos = currentState.getDocument().getParse().getPOSTag(indexOfHeadNode);

            if (!validPOSTags.contains(pos)) {
                continue;
            }
            //assign all dudes
            for (Integer indexOfHeadDude : semanticTypes.keySet()) {

                String headDudeName = semanticTypes.get(indexOfHeadDude);

                Set<Candidate> headNodeCandidates = getDBpediaMatches(headDudeName, node, pos);

                if (headNodeCandidates.isEmpty()) {
                    continue;
                }
                
                String samplingLevel = ProjectConfiguration.getLinkingSamplingLevel();
                
                //1 = direct children, 2 = children of children with 2 depth, 3 = siblings

                List<Integer> childNodes_Level_1 = new ArrayList<>();
                List<Integer> childNodes_Level_2 = new ArrayList<>();
                List<Integer> siblings = new ArrayList<>();
                
                if(samplingLevel.contains("1")){
                    childNodes_Level_1 = currentState.getDocument().getParse().getDependentEdges(indexOfHeadNode, validPOSTags, 1);
                }
                if(samplingLevel.contains("2")){
                    childNodes_Level_2 = currentState.getDocument().getParse().getDependentEdges(indexOfHeadNode, validPOSTags, 2);
                }
                if(samplingLevel.contains("3")){
                    siblings = currentState.getDocument().getParse().getSiblings(indexOfHeadNode, validPOSTags);
                }


                boolean hasValidDepNode = false;

                for (Integer indexOfDepNode : childNodes_Level_1) {
                    //greedy exploring, skip nodes with assigned URI
//                    if (!currentState.getHiddenVariables().get(depNodeIndex).getCandidate().getUri().equals("EMPTY_STRING")) {
//                        continue;
//                    }
                    //consider certain edges, skip others
                    String depRelation = currentState.getDocument().getParse().getDependencyRelation(indexOfDepNode);

                    if (!validEdges.contains(depRelation)) {
                        continue;
                    }

                    String depNode = currentState.getDocument().getParse().getNodes().get(indexOfDepNode);

                    for (Integer indexOfDepDude : semanticTypes.keySet()) {

                        Set<State> statesFromEdge = getStatesFromEdge(depNode, indexOfDepNode, indexOfDepDude, headNodeCandidates, currentState, indexOfHeadNode, indexOfHeadDude, headDudeName);

                        newStates.addAll(statesFromEdge);
                    }
                }

                if (!newStates.isEmpty()) {
                    continue;
                }

                for (Integer indexOfDepNode : siblings) {
                    //greedy exploring, skip nodes with assigned URI
//                    if (!currentState.getHiddenVariables().get(depNodeIndex).getCandidate().getUri().equals("EMPTY_STRING")) {
//                        continue;
//                    }
                    //consider certain edges, skip others
                    String depRelation = currentState.getDocument().getParse().getDependencyRelation(indexOfDepNode);

                    if (!validEdges.contains(depRelation)) {
                        continue;
                    }

                    String depNode = currentState.getDocument().getParse().getNodes().get(indexOfDepNode);

                    for (Integer indexOfDepDude : semanticTypes.keySet()) {

                        Set<State> statesFromEdge = getStatesFromEdge(depNode, indexOfDepNode, indexOfDepDude, headNodeCandidates, currentState, indexOfHeadNode, indexOfHeadDude, headDudeName);

                        newStates.addAll(statesFromEdge);
                    }
                }

                //if there are some states created from dependent nodes(level 1) and siblings no need to explore level2 dep nodes.
                if (!newStates.isEmpty()) {
                    continue;
                }

                for (Integer indexOfDepNode : childNodes_Level_2) {
                    //consider certain edges, skip others
                    String depRelation = currentState.getDocument().getParse().getDependencyRelation(indexOfDepNode);

                    if (!validEdges.contains(depRelation)) {
                        continue;
                    }

                    String depNode = currentState.getDocument().getParse().getNodes().get(indexOfDepNode);

                    for (Integer indexOfDepDude : semanticTypes.keySet()) {

                        Set<State> statesFromEdge = getStatesFromEdge(depNode, indexOfDepNode, indexOfDepDude, headNodeCandidates, currentState, indexOfHeadNode, indexOfHeadDude, headDudeName);

                        newStates.addAll(statesFromEdge);
                    }
                }

                //if the dependent nodes have been explored, then no need to explore the siblings
//                if (!hasValidDepNode) {
//                    for (Integer depNodeIndex : siblings) {
//
////                        //greedy exploring, skip nodes with assigned URI
////                        if (!currentState.getHiddenVariables().get(depNodeIndex).getCandidate().getUri().equals("EMPTY_STRING")) {
////                            continue;
////                        }
//                        //consider certain edges, skip others
//                        String depRelation = currentState.getDocument().getParse().getDependencyRelation(depNodeIndex);
//
//                        if (!validEdges.contains(depRelation)) {
//                            continue;
//                        }
//
//                        String depNode = currentState.getDocument().getParse().getNodes().get(depNodeIndex);
//
//                        for (Integer indexOfDepDude : semanticTypes.keySet()) {
//
//                            String depDudeName = semanticTypes.get(indexOfDepDude);
//
//                            Set<Candidate> depNodeCandidates = getDBpediaMatches(depDudeName, depNode);
//
//                            if (depNodeCandidates.isEmpty()) {
//                                continue;
//                            }
//
//                            for (Candidate headNodeCandidate : headNodeCandidates) {
//                                for (Candidate depNodeCandidate : depNodeCandidates) {
//
////                                    if(depNodeCandidate.getUri().equals("http://dbpedia.org/resource/Rolls-Royce_Motors") && headNodeCandidate.getUri().equals("http://dbpedia.org/ontology/owner")){
////                                        int z=1;
////                                    }
//                                    boolean isSubject = DBpediaEndpoint.isSubjectTriple(headNodeCandidate.getUri(), depNodeCandidate.getUri());
//                                    boolean isObject = DBpediaEndpoint.isObjectTriple(headNodeCandidate.getUri(), depNodeCandidate.getUri());
//
//                                    if (isSubject) {
//
//                                        List<Integer> usedSlots = currentState.getUsedSlots(indexOfNode);
//
//                                        State s = new State(currentState);
//
//                                        s.addHiddenVariable(indexOfNode, indexOfDude, headNodeCandidate);
//                                        s.addHiddenVariable(depNodeIndex, indexOfDepDude, depNodeCandidate);
//
//                                        //Argument is 1 => subj
//                                        if (depDudeName.equals("RestrictionClass") || depDudeName.equals("UnderSpecifiedClass") || depDudeName.equals("Property") || dudeName.equals("Property") || dudeName.equals("RestrictionClass") || dudeName.equals("UnderSpecifiedClass")) {
//
//                                            if (usedSlots.contains(1)) {
//
//                                                s.addSlotVariable(depNodeIndex, indexOfNode, 2);
//
//                                            } else {
//                                                s.addSlotVariable(depNodeIndex, indexOfNode, 1);
//                                            }
//                                        }
//
//                                        if (!s.equals(currentState) && !newStates.contains(s)) {
//                                            newStates.add(s);
//                                        }
//
//                                        hasValidDepNode = true;
//                                    }
//                                    if (isObject) {
//                                        List<Integer> usedSlots = currentState.getUsedSlots(indexOfNode);
//
//                                        State s = new State(currentState);
//
//                                        s.addHiddenVariable(indexOfNode, indexOfDude, headNodeCandidate);
//                                        s.addHiddenVariable(depNodeIndex, indexOfDepDude, depNodeCandidate);
//
//                                        //Argument number is 2 => obj
//                                        if (depDudeName.equals("RestrictionClass") || depDudeName.equals("UnderSpecifiedClass") || depDudeName.equals("Property") || dudeName.equals("Property") || dudeName.equals("RestrictionClass") || dudeName.equals("UnderSpecifiedClass")) {
//
//                                            if (usedSlots.contains(2)) {
//                                                s.addSlotVariable(depNodeIndex, indexOfNode, 1);
//                                            } else {
//                                                s.addSlotVariable(depNodeIndex, indexOfNode, 2);
//                                            }
//                                        }
//
//                                        if (!s.equals(currentState) && !newStates.contains(s)) {
//                                            newStates.add(s);
//                                        }
//
//                                        hasValidDepNode = true;
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
            }
        }

//        Map<String, State> uniqueStates = new HashMap<>();
//        for(State s1 : newStates){
//            if(!uniqueStates.containsKey(s1.toString())){
//                uniqueStates.put(s1.toString(), s1);
//            }
//        }
        List<State> states = new ArrayList<>(newStates);

        return states;
    }

    private Set<State> getStatesFromEdge(String depNode, Integer indexOfDepNode, Integer indexOfDepDude, Set<Candidate> headNodeCandidates, State currentState, Integer indexOfHeadNode, Integer indexOfHeadDude, String headDUDEName) {

        Set<State> states = new HashSet<>();

        String depDudeName = semanticTypes.get(indexOfDepDude);

        String depPOS = currentState.getDocument().getParse().getPOSTag(indexOfDepNode);

        Set<Candidate> depNodeCandidates = getDBpediaMatches(depDudeName, depNode, depPOS);

        if (depNodeCandidates.isEmpty()) {
            return states;
        }

        for (Candidate headNodeCandidate : headNodeCandidates) {

            for (Candidate depNodeCandidate : depNodeCandidates) {

//                if (headNodeCandidate.getUri().equals("http://dbpedia.org/ontology/creator") && depNodeCandidate.getUri().equals("http://dbpedia.org/resource/Battle_of_Gettysburg")) {
//                    int z = 2;
//                }

                boolean isChildSubject = DBpediaEndpoint.isSubjectTriple(headNodeCandidate.getUri(), depNodeCandidate.getUri());
                boolean isChildObject = DBpediaEndpoint.isObjectTriple(headNodeCandidate.getUri(), depNodeCandidate.getUri());

//                boolean isParentSubject = DBpediaEndpoint.isSubjectTriple(depNodeCandidate.getUri(), headNodeCandidate.getUri());
//                boolean isParentObject = DBpediaEndpoint.isObjectTriple(depNodeCandidate.getUri(), headNodeCandidate.getUri());
                Set<State> s1 = getValidStates(currentState, indexOfHeadNode, indexOfHeadDude, indexOfDepNode, indexOfDepDude, headDUDEName, depDudeName, headNodeCandidate, depNodeCandidate, isChildSubject, isChildObject);
//                Set<State> s2 = getValidStates(currentState, indexOfHeadNode, indexOfHeadDude, indexOfDepNode, indexOfDepDude, headDUDEName, depDudeName, headNodeCandidate, depNodeCandidate, isParentSubject, isParentObject);

                states.addAll(s1);
//                states.addAll(s2);
            }
        }

        return states;
    }

    private Set<State> getValidStates(State currentState, Integer indexOfHeadNode, Integer indexOfHeadDude, Integer indexOfDepNode, Integer indexOfDepDude, String headDUDEName, String depDudeName, Candidate headNodeCandidate, Candidate depNodeCandidate, boolean isSubject, boolean isObject) {

        Set<State> states = new HashSet<>();
        if (isSubject) {

            //check if the slot 1 has been occupied before
            List<Integer> usedSlots = currentState.getUsedSlots(indexOfHeadNode);

            State s = new State(currentState);

            s.addHiddenVariable(indexOfHeadNode, indexOfHeadDude, headNodeCandidate);
            s.addHiddenVariable(indexOfDepNode, indexOfDepDude, depNodeCandidate);

            //Argument is 1 => subj
            if (acceptedDUDES.contains(headDUDEName) || acceptedDUDES.contains(depDudeName)) {

//                if (usedSlots.contains(1)) {
//
//                    s.addSlotVariable(indexOfDepNode, indexOfHeadNode, 2);
//
//                } else {
                s.addSlotVariable(indexOfDepNode, indexOfHeadNode, 1);
//                }
            }

            if (!s.equals(currentState)) {
                states.add(s);
            }

        }
        if (isObject) {
            List<Integer> usedSlots = currentState.getUsedSlots(indexOfHeadNode);

            State s = new State(currentState);

            s.addHiddenVariable(indexOfHeadNode, indexOfHeadDude, headNodeCandidate);
            s.addHiddenVariable(indexOfDepNode, indexOfDepDude, depNodeCandidate);

            //Argument number is 2 => obj
            if (acceptedDUDES.contains(headDUDEName) || acceptedDUDES.contains(depDudeName)) {

//                if (usedSlots.contains(2)) {
//                    s.addSlotVariable(indexOfDepNode, indexOfHeadNode, 1);
//                } else {
                s.addSlotVariable(indexOfDepNode, indexOfHeadNode, 2);
//                }
            }

            if (!s.equals(currentState)) {
                states.add(s);
            }
        }

        return states;
    }

    private Set<Candidate> getDBpediaMatches(String dude, String node, String pos) {
        //predicate DUDE
        Set<Candidate> uris = new LinkedHashSet<>();

        //check if given node is of datatype
        //if it's datatype return the node text as URI for the assigned dude
        if (isTokenDataType(node)) {
            Candidate i = new Candidate(new Instance("EMPTY_STRING", 0), 0, 0, 0);
            uris.add(i);

            return uris;
        }

        boolean useLemmatizer = false;
        boolean useWordNet = false;
        boolean mergePartialMatches = false;

        int topK = 80;

        String queryTerm = node;

        Set<String> indexURIs = new HashSet<>();

        switch (dude) {
            case "Property":
                useLemmatizer = true;
                useWordNet = true;
                mergePartialMatches = false;

                topK = 50;

                if (!Stopwords.isStopWord(queryTerm)) {
                    Set<Candidate> propertyURIs = Search.getPredicates(queryTerm, topK, useLemmatizer, mergePartialMatches, useWordNet, Main.lang);

                    for (Candidate c : propertyURIs) {
                        indexURIs.add(c.getUri());
                        uris.add(c.clone());
                    }
                }

                //retrieve manual lexicon even if it's in stop word list
                if (ManualLexicon.useManualLexicon || ProjectConfiguration.getTrainingDatasetName().toLowerCase().contains("train")) {
                    Set<String> definedLexica = ManualLexicon.getProperties(queryTerm, Main.lang);
                    for (String d : definedLexica) {
                        if (!indexURIs.contains(d)) {
                            uris.add(new Candidate(new Instance(d, 10000), 0, 1.0, 1.0));
                        }
                    }
                }

                if (ProjectConfiguration.useEmbeddingLexicon() && (pos.equals("NOUN") || pos.equals("VERB"))) {

                    Set<String> embeddingLexica = EmbeddingLexicon.getProperties(queryTerm, Main.lang);
                    for (String d : embeddingLexica) {
                        if (!indexURIs.contains(d)) {
                            uris.add(new Candidate(new Instance(d, 10000), 0, 1.0, 1.0));
                        }
                    }
                }

                break;

            case "Class":
                useLemmatizer = true;
                mergePartialMatches = false;
                useWordNet = true;
                if (!Stopwords.isStopWord(queryTerm)) {
                    Set<Candidate> classURIs = Search.getClasses(queryTerm, topK, useLemmatizer, mergePartialMatches, useWordNet, Main.lang);

                    for (Candidate c : classURIs) {
                        indexURIs.add(c.getUri());
                        uris.add(c.clone());
                    }
                }

                //retrieve manual lexicon even if it's in stop word list
                if (ManualLexicon.useManualLexicon || ProjectConfiguration.getTrainingDatasetName().toLowerCase().contains("train")) {
                    Set<String> definedLexica = ManualLexicon.getClasses(queryTerm, Main.lang);
                    for (String d : definedLexica) {
                        if (!indexURIs.contains(d)) {
                            uris.add(new Candidate(new Instance(d, 10000), 0, 1.0, 1.0));
                        }
                    }
                }
                break;
            case "RestrictionClass":
                useLemmatizer = true;
                useWordNet = false;
                mergePartialMatches = false;

                topK = 20;

                if (!Stopwords.isStopWord(queryTerm)) {
                    Set<Candidate> restrictionClassURIs = Search.getRestrictionClasses(queryTerm, topK, useLemmatizer, mergePartialMatches, useWordNet, Main.lang);

                    for (Candidate c : restrictionClassURIs) {
                        indexURIs.add(c.getUri());
                        uris.add(c.clone());
                    }
                }

                //check manual lexicon for Restriction Classes
                if (ManualLexicon.useManualLexicon || ProjectConfiguration.getTrainingDatasetName().toLowerCase().contains("train")) {
                    Set<String> definedLexica = ManualLexicon.getRestrictionClasses(queryTerm, Main.lang);

                    if (queryTerm.equals("endangered")) {
                        int z = 1;
                    }

                    for (String d : definedLexica) {
                        if (!indexURIs.contains(d)) {
                            uris.add(new Candidate(new Instance(d, 10000), 0, 1.0, 1.0));
                        }
                    }
                }
                break;
            case "UnderSpecifiedClass":
                topK = 5;
                useLemmatizer = false;
                mergePartialMatches = false;
                useWordNet = false;

                //extract resources
                if (!Stopwords.isStopWord(queryTerm)) {
                    Set<Candidate> resourceURIs = Search.getResources(queryTerm, topK, useLemmatizer, mergePartialMatches, useWordNet, Main.lang);

                    //set some empty propertyy
                    for (Candidate c : resourceURIs) {
                        Candidate c2 = c.clone();

                        indexURIs.add(c.getUri());

                        uris.add(c2);
                    }
                }

                //check manual lexicon for Resources => to make underspecified class
                if (ManualLexicon.useManualLexicon || ProjectConfiguration.getTrainingDatasetName().toLowerCase().contains("train")) {
                    Set<String> definedLexica = ManualLexicon.getResources(queryTerm, Main.lang);
                    for (String d : definedLexica) {
                        if (!indexURIs.contains(d)) {
                            uris.add(new Candidate(new Instance(d, 10000), 0, 1.0, 1.0));
                        }
                    }
                }
                break;
            case "Individual":
                topK = 1;
                useLemmatizer = false;
                mergePartialMatches = false;
                useWordNet = false;

                if (!Stopwords.isStopWord(queryTerm)) {
                    Set<Candidate> resourceCandidates = Search.getResources(queryTerm, topK, useLemmatizer, mergePartialMatches, useWordNet, Main.lang);

                    for (Candidate c : resourceCandidates) {
                        if (c.getUri().contains("List_of")) {
                            continue;
                        }
                        uris.add(c.clone());
                        indexURIs.add(c.getUri());
                    }
                }

                //check manual lexicon
                if (ManualLexicon.useManualLexicon || ProjectConfiguration.getTrainingDatasetName().toLowerCase().contains("train")) {
                    Set<String> definedLexica = ManualLexicon.getResources(queryTerm, Main.lang);
                    for (String d : definedLexica) {
                        if (!indexURIs.contains(d)) {
                            uris.add(new Candidate(new Instance(d, 10000), 0, 1.0, 1.0));
                        }
                    }
                }
                break;
        }

        return uris;
    }

    /**
     * checks if given token is type of data
     *
     * @return true if it's data type
     */
    private boolean isTokenDataType(String tokens) {
        List<String> patterns = new ArrayList<>();

        String monthYearPattern = "((January|Jan|February|Feb|March|Mar|April|Apr|May|June|Jun|July|Jul|August|Aug|September|Sep|October|Oct|November|Nov|December|Dec)\\s+((19|20)\\d\\d))";//March 2015, Aug 1999
        monthYearPattern += "|((0?[1-9]|1[012])/((19|20)\\d\\d))"; //mm/YYYY
        monthYearPattern += "|((0?[1-9]|1[012])-((19|20)\\d\\d))"; //mm-YYYY

        String datePattern = "((0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((19|20)\\d\\d))";//dd/MM/YYYY
        datePattern += "|((0?[1-9]|[12][0-9]|3[01])-(0?[1-9]|1[012])-((19|20)\\d\\d))"; //dd-MM-YYYY
        datePattern += "|((January|Jan|February|Feb|March|Mar|April|Apr|May|June|Jun|July|Jul|August|Aug|September|Sep|October|Oct|November|Nov|December|Dec)\\s+((1st)|(2nd)|(3rd)|(([1-9]|[12][0-9]|3[01])(th)))(,)?\\s+((19|20)\\d\\d))"; //Jan 1st 2015, August 14th 1999
        datePattern += "|((January|Jan|February|Feb|March|Mar|April|Apr|May|June|Jun|July|Jul|August|Aug|September|Sep|October|Oct|November|Nov|December|Dec)\\s+(0?[1-9]|[12][0-9]|3[01])(,)?\\s+((19|20)\\d\\d))"; //Jan 1st 2015, August 14th 1999
        datePattern += "|((0?[1-9]|[12][0-9]|3[01])\\s+(January|Jan|February|Feb|March|Mar|April|Apr|May|June|Jun|July|Jul|August|Aug|September|Sep|October|Oct|November|Nov|December|Dec)\\s+((19|20)\\d\\d))"; //Jan 1st 2015, August 14th 1999
        datePattern += "|((0?[1-9]|1[012])/(0?[1-9]|[12][0-9]|3[01])/((19|20)\\d\\d))"; //MM/dd/YYYY
        datePattern += "|((0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01])-((19|20)\\d\\d))"; //MM-dd-YYYY

        patterns.add(monthYearPattern);
        patterns.add(datePattern);
        patterns.add("^(http://|https://|www.)+.+$");
        patterns.add("^(true|false)+.*$");
        patterns.add("(^[-+]?\\d+)");
        patterns.add("(^[+]?\\d+)");
        patterns.add("(^[+]?\\d+)");
        patterns.add("(^[-+]?\\d+\\.\\d+)");
        patterns.add("(^[-+]?\\d+\\.\\d+)");
        patterns.add("\\d{4}");

        for (String pattern : patterns) {
            if (tokens.matches(pattern)) {
                return true;
            }
        }

        return false;
    }
}
