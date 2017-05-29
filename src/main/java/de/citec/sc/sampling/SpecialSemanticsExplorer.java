/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.sampling;

import de.citec.sc.query.Candidate;
import de.citec.sc.query.Instance;
import de.citec.sc.query.ManualLexicon;

import de.citec.sc.query.Search;
import de.citec.sc.utils.DBpediaEndpoint;
import de.citec.sc.utils.Stopwords;
import de.citec.sc.variable.HiddenVariable;
import de.citec.sc.variable.State;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import sampling.Explorer;

/**
 *
 * @author sherzod
 */
public class SpecialSemanticsExplorer implements Explorer<State> {

    private Map<Integer, String> semanticTypes;
    private Map<Integer, String> specialSemanticTypes;
    private Set<String> validPOSTags;
    private Set<String> frequentWordsToExclude;
    private Set<String> wordsWithSpecialSemanticTypes;

    public SpecialSemanticsExplorer(Map<Integer, String> s, Map<Integer, String> sp, Set<String> validPOSTags, Set<String> wordToExclude, Set<String> wS) {
        this.specialSemanticTypes = sp;
        this.semanticTypes = s;
        this.validPOSTags = validPOSTags;
        this.frequentWordsToExclude = wordToExclude;
        this.wordsWithSpecialSemanticTypes = wS;
    }

    @Override
    public List getNextStates(State currentState) {
        List<State> newStates = new ArrayList<>();

        for (int indexOfNode : currentState.getDocument().getParse().getNodes().keySet()) {
            String node = currentState.getDocument().getParse().getNodes().get(indexOfNode);

            String pos = currentState.getDocument().getParse().getPOSTag(indexOfNode);

            if (wordsWithSpecialSemanticTypes.contains(node.toLowerCase())) {

                for (Integer indexOfDepDude : specialSemanticTypes.keySet()) {

                    State s = new State(currentState);

                    Candidate emptyInstance = new Candidate(new Instance("EMPTY_STRING", 0), 0, 0, 0);

                    s.addHiddenVariable(indexOfNode, indexOfDepDude, emptyInstance);

                    if (!s.equals(currentState) && !newStates.contains(s)) {
                        newStates.add(s);
                    }
                }
            }
        }

        return newStates;
    }
}
