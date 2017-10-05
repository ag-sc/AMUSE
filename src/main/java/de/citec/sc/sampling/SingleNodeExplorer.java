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
import de.citec.sc.utils.ProjectConfiguration;
import de.citec.sc.utils.Stopwords;
import de.citec.sc.variable.State;

import java.util.ArrayList;
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
public class SingleNodeExplorer implements Explorer<State> {

    private Map<Integer, String> semanticTypes;
    private Set<String> validPOSTags;

    public SingleNodeExplorer(Map<Integer, String> semanticTypes, Set<String> validPOSTags) {
        this.semanticTypes = semanticTypes;
        this.validPOSTags = validPOSTags;
    }

    @Override
    public List getNextStates(State currentState) {
        List<State> newStates = new ArrayList<>();

        for (int indexOfNode : currentState.getDocument().getParse().getNodes().keySet()) {
            String node = currentState.getDocument().getParse().getNodes().get(indexOfNode);

            String pos = currentState.getDocument().getParse().getPOSTag(indexOfNode);

            if (validPOSTags.contains(pos)) {

                //assign all dudes
                for (Integer indexOfDude : semanticTypes.keySet()) {

                    String dudeName = semanticTypes.get(indexOfDude);

                    Set<State> newCreatedStates = createNewStates(node.toLowerCase(), currentState, indexOfNode, dudeName, indexOfDude);

                    for (State s1 : newCreatedStates) {

                        if (!newStates.contains(s1)) {
                            newStates.add(new State(s1));
                        }
                    }

                }

            }
        }

        return newStates;
    }

    private Set<State> createNewStates(String node, State currentState, int indexOfNode, String dude, Integer indexOfDude) {
        Set<State> newStates = new LinkedHashSet<>();

        String posTag = currentState.getDocument().getParse().getPOSTag(indexOfNode);
        Set<Candidate> uris = getDBpediaMatches(dude, node, posTag);

        //create new State for each URI
        for (Candidate i : uris) {

            State s = new State(currentState);

            s.addHiddenVariable(indexOfNode, indexOfDude, i);

            if (!s.equals(currentState)) {
                newStates.add(s);
            }
        }

        return newStates;
    }

    private Set<Candidate> getDBpediaMatches(String dude, String node, String pos) {
        //predicate DUDE
        Set<Candidate> uris = new LinkedHashSet<>();

        //check if given node is of datatype
        //if it's datatype return the node text as URI for the assigned dude
        if (isTokenDataType(node)) {
            Candidate i = new Candidate(null, 0, 0, 0);
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
                useWordNet = false;
                mergePartialMatches = false;

                topK = 20;

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
