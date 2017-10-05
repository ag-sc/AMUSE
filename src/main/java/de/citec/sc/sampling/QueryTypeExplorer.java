/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.sampling;

import de.citec.sc.query.Candidate;
import de.citec.sc.query.Instance;
import de.citec.sc.utils.ProjectConfiguration;
import de.citec.sc.variable.QueryTypeVariable;
import de.citec.sc.variable.URIVariable;
import de.citec.sc.variable.State;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import sampling.Explorer;

/**
 *
 * @author sherzod
 */
public class QueryTypeExplorer implements Explorer<State> {

    private Map<Integer, String> semanticTypes;
    private Map<Integer, String> specialSemanticTypes;
    private Set<String> validPOSTags;
    private Set<String> validEdges;

    public QueryTypeExplorer(Map<Integer, String> s, Map<Integer, String> sp, Set<String> validPOSTags, Set<String> edges) {
        this.specialSemanticTypes = sp;
        this.semanticTypes = s;
        this.validPOSTags = validPOSTags;
        this.validEdges = edges;
    }

    @Override
    public List getNextStates(State currentState) {
        return createNewtStates(currentState);
    }

    private List createNewtStates(State currentState) {

        List<State> sampedStatesWithQueryTypes = sampleQueryTypes(currentState);

        return sampedStatesWithQueryTypes;
    }

    private List<State> sampleQueryTypes(State s) {
        Set<State> sampledStates = new HashSet<>();

        int currentType = s.getQueryTypeVariable().getType();

        switch (currentType) {
            case 1:
                State sampledState1 = new State(s);
                sampledState1.setQueryTypeVariable(new QueryTypeVariable(2));

                State sampledState2 = new State(s);
                sampledState2.setQueryTypeVariable(new QueryTypeVariable(3));

                sampledStates.add(sampledState1);
                sampledStates.add(sampledState2);
                break;
            case 2:
                State sampledState3 = new State(s);
                sampledState3.setQueryTypeVariable(new QueryTypeVariable(1));

                State sampledState4 = new State(s);
                sampledState4.setQueryTypeVariable(new QueryTypeVariable(3));

                sampledStates.add(sampledState3);
                sampledStates.add(sampledState4);
                break;
            case 3:
                State sampledState5 = new State(s);
                sampledState5.setQueryTypeVariable(new QueryTypeVariable(1));

                State sampledState6 = new State(s);
                sampledState6.setQueryTypeVariable(new QueryTypeVariable(2));

                sampledStates.add(sampledState5);
                sampledStates.add(sampledState6);
                break;
        }

        List<State> newStates = new ArrayList<>();
        sampledStates.stream().filter((s1) -> (!s.equals(s1))).forEachOrdered((s1) -> {
            newStates.add(s1);
        });

        return newStates;
    }

}
