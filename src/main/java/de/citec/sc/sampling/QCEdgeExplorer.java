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
public class QCEdgeExplorer implements Explorer<State> {

    private Map<Integer, String> semanticTypes;
    private Map<Integer, String> specialSemanticTypes;
    private Set<String> validPOSTags;
    private Set<String> validEdges;

    public QCEdgeExplorer(Map<Integer, String> s, Map<Integer, String> sp, Set<String> validPOSTags, Set<String> edges) {
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
        List<State> newStates = new ArrayList<>();

        for (int indexOfNode : currentState.getDocument().getParse().getNodes().keySet()) {
            String node = currentState.getDocument().getParse().getNodes().get(indexOfNode);

            String pos = currentState.getDocument().getParse().getPOSTag(indexOfNode);

            if (!validPOSTags.contains(pos)) {
                continue;
            }

            URIVariable headVar = currentState.getHiddenVariables().get(indexOfNode);

            String dudeName = semanticTypes.get(headVar.getDudeId());

            if (dudeName == null) {
                continue;
            }
            //assign extra dude for dependent nodes if the head dude is class or property
            if (!(dudeName.equals("Property") || dudeName.equals("RestrictionClass") || dudeName.equals("Class") || dudeName.equals("UnderSpecifiedClass"))) {
                continue;
            }

            List<Integer> childNodes = currentState.getDocument().getParse().getDependentNodes(indexOfNode);
            List<Integer> siblings = currentState.getDocument().getParse().getSiblings(indexOfNode);

            Set<Integer> depNodes = new HashSet<>();

            //loop over each dependent node and get the dependent nodes of those
            //add the child node itself
            for (Integer childNode : childNodes) {
                List<Integer> childOfChildNodes = currentState.getDocument().getParse().getDependentNodes(childNode);

                depNodes.addAll(childOfChildNodes);
                depNodes.add(childNode);
            }
            for (Integer sibling : siblings) {
                List<Integer> childOfSiblingNodes = currentState.getDocument().getParse().getDependentNodes(sibling);

                depNodes.addAll(childOfSiblingNodes);
                depNodes.add(sibling);
            }

            //get the head node
            Integer headNode = currentState.getDocument().getParse().getParentNode(indexOfNode);

            List<Integer> siblingsOfHeadNode = currentState.getDocument().getParse().getSiblings(headNode);

            //get a list of siblings of the head node, and add the head node itself.
            Set<Integer> headNodes = new HashSet<>();
            headNodes.addAll(siblingsOfHeadNode);
            headNodes.add(headNode);

            boolean hasValidSpecialNode = false;

            for (Integer depNodeIndex : depNodes) {

                //greedy exploring, skip nodes with assigned URI
                if (!currentState.getHiddenVariables().get(depNodeIndex).getCandidate().getUri().equals("EMPTY_STRING")) {
                    continue;
                }

                String depNode = currentState.getDocument().getParse().getNodes().get(depNodeIndex);
                String depPOS = currentState.getDocument().getParse().getPOSTag(depNodeIndex);

                if (!validPOSTags.contains(depPOS)) {
                    continue;
                }

                //assign special semantic types to certain words  such as : who, which, where, when ...
                //if the the node has been found, no need to explore longer the head nodes
                hasValidSpecialNode = true;

                for (Integer indexOfDepDude : specialSemanticTypes.keySet()) {

                    List<Integer> usedSlots = currentState.getUsedSlots(indexOfNode);

                    String depDudeName = specialSemanticTypes.get(indexOfDepDude);

                    if (usedSlots.contains(1) && usedSlots.contains(2)) {

                        if (depDudeName.equals("Which")) {

                            State s = new State(currentState);

                            Candidate emptyInstance = new Candidate(new Instance("EMPTY_STRING", 0), 0, 0, 0);

                            //add as slot 2 since 1 taken by another node
                            s.addSlotVariable(depNodeIndex, indexOfNode, 1);
                            s.addHiddenVariable(depNodeIndex, indexOfDepDude, emptyInstance);

                            if (!s.equals(currentState) && !newStates.contains(s)) {
                                newStates.add(s);
                            }
                        } else {

                            State s = new State(currentState);

                            Candidate emptyInstance = new Candidate(new Instance("EMPTY_STRING", 0), 0, 0, 0);

                            s.addHiddenVariable(depNodeIndex, indexOfDepDude, emptyInstance);

                            if (!s.equals(currentState) && !newStates.contains(s)) {
                                newStates.add(s);
                            }
                        }

                    } else if (usedSlots.contains(1) && !usedSlots.contains(2)) {
                        State s = new State(currentState);

                        Candidate emptyInstance = new Candidate(new Instance("EMPTY_STRING", 0), 0, 0, 0);

                        s.addHiddenVariable(depNodeIndex, indexOfDepDude, emptyInstance);

                        //add as slot 2 since 1 taken by another node
                        s.addSlotVariable(depNodeIndex, indexOfNode, 2);

                        if (!s.equals(currentState) && !newStates.contains(s)) {
                            newStates.add(s);
                        }
                    } else if (!usedSlots.contains(1) && usedSlots.contains(2)) {
                        State s = new State(currentState);

                        Candidate emptyInstance = new Candidate(new Instance("EMPTY_STRING", 0), 0, 0, 0);

                        s.addHiddenVariable(depNodeIndex, indexOfDepDude, emptyInstance);

                        //add as slot 2 since 1 taken by another node
                        s.addSlotVariable(depNodeIndex, indexOfNode, 1);

                        if (!s.equals(currentState) && !newStates.contains(s)) {
                            newStates.add(s);
                        }
                    } else {
                        State s = new State(currentState);

                        Candidate emptyInstance = new Candidate(new Instance("EMPTY_STRING", 0), 0, 0, 0);

                        s.addHiddenVariable(depNodeIndex, indexOfDepDude, emptyInstance);

                        //add as slot 2 since 1 taken by another node
                        s.addSlotVariable(depNodeIndex, indexOfNode, 1);

                        if (!s.equals(currentState) && !newStates.contains(s)) {
                            newStates.add(s);
                        }
                    }
                }

            }

            if (hasValidSpecialNode) {
                continue;
            }
            for (Integer hNode : headNodes) {

                if (hNode == -1) {
                    continue;
                }

                //greedy exploring, skip nodes with assigned URI
                if (!currentState.getHiddenVariables().get(hNode).getCandidate().getUri().equals("EMPTY_STRING")) {
                    continue;
                }

                String hNodeToken = currentState.getDocument().getParse().getNodes().get(hNode);

                String depPOS = currentState.getDocument().getParse().getPOSTag(hNode);

                if (!validPOSTags.contains(depPOS)) {
                    continue;
                }

                //assign special semantic types to certain words  such as : who, which, where, when ...
                hasValidSpecialNode = true;
                for (Integer indexOfDepDude : specialSemanticTypes.keySet()) {

                    String depDudeName = specialSemanticTypes.get(indexOfDepDude);

                    List<Integer> usedSlots = currentState.getUsedSlots(indexOfNode);

                    if (usedSlots.contains(1) && usedSlots.contains(2)) {
                        if (depDudeName.equals("Which")) {

                            State s = new State(currentState);

                            Candidate emptyInstance = new Candidate(new Instance("EMPTY_STRING", 0), 0, 0, 0);

                            //add as slot 2 since 1 taken by another node
                            s.addSlotVariable(hNode, indexOfNode, 1);
                            s.addHiddenVariable(hNode, indexOfDepDude, emptyInstance);

                            if (!s.equals(currentState) && !newStates.contains(s)) {
                                newStates.add(s);
                            }
                        } else {

                            State s = new State(currentState);

                            Candidate emptyInstance = new Candidate(new Instance("EMPTY_STRING", 0), 0, 0, 0);

                            s.addHiddenVariable(hNode, indexOfDepDude, emptyInstance);

                            if (!s.equals(currentState) && !newStates.contains(s)) {
                                newStates.add(s);
                            }
                        }
                    } else if (usedSlots.contains(1) && !usedSlots.contains(2)) {
                        State s = new State(currentState);

                        Candidate emptyInstance = new Candidate(new Instance("EMPTY_STRING", 0), 0, 0, 0);

                        s.addHiddenVariable(hNode, indexOfDepDude, emptyInstance);

                        //add as slot 2 since 1 taken by another node
                        s.addSlotVariable(indexOfNode, hNode, 2);

                        if (!s.equals(currentState) && !newStates.contains(s)) {
                            newStates.add(s);
                        }
                    } else if (!usedSlots.contains(1) && usedSlots.contains(2)) {
                        State s = new State(currentState);

                        Candidate emptyInstance = new Candidate(new Instance("EMPTY_STRING", 0), 0, 0, 0);

                        s.addHiddenVariable(hNode, indexOfDepDude, emptyInstance);

                        //add as slot 2 since 1 taken by another node
                        s.addSlotVariable(indexOfNode, hNode, 1);

                        if (!s.equals(currentState) && !newStates.contains(s)) {
                            newStates.add(s);
                        }
                    } else {
                        State s = new State(currentState);

                        Candidate emptyInstance = new Candidate(new Instance("EMPTY_STRING", 0), 0, 0, 0);

                        s.addHiddenVariable(hNode, indexOfDepDude, emptyInstance);

                        //add as slot 2 since 1 taken by another node
                        s.addSlotVariable(indexOfNode, hNode, 1);

                        if (!s.equals(currentState) && !newStates.contains(s)) {
                            newStates.add(s);
                        }
                    }
                }

            }

            if (!hasValidSpecialNode) {
                //desperate times require desperate measures

                for (int indexOfNode1 : currentState.getDocument().getParse().getNodes().keySet()) {
                    String someToken = currentState.getDocument().getParse().getNodes().get(indexOfNode1);

                    //if the the node has been found, no need to explore longer the head nodes
                    hasValidSpecialNode = true;

                    for (Integer indexOfDepDude : specialSemanticTypes.keySet()) {

                        String depDudeName = specialSemanticTypes.get(indexOfDepDude);

                        List<Integer> usedSlots = currentState.getUsedSlots(indexOfNode);

                        if (usedSlots.contains(1) && usedSlots.contains(2)) {
                            if (depDudeName.equals("Which")) {

                                State s = new State(currentState);

                                Candidate emptyInstance = new Candidate(new Instance("EMPTY_STRING", 0), 0, 0, 0);

                                //add as slot 2 since 1 taken by another node
                                s.addSlotVariable(indexOfNode1, indexOfNode, 1);
                                s.addHiddenVariable(indexOfNode1, indexOfDepDude, emptyInstance);

                                if (!s.equals(currentState) && !newStates.contains(s)) {
                                    newStates.add(s);
                                }
                            } else {

                                State s = new State(currentState);

                                Candidate emptyInstance = new Candidate(new Instance("EMPTY_STRING", 0), 0, 0, 0);

                                s.addHiddenVariable(indexOfNode1, indexOfDepDude, emptyInstance);

                                if (!s.equals(currentState) && !newStates.contains(s)) {
                                    newStates.add(s);
                                }
                            }
                        } else if (usedSlots.contains(1) && !usedSlots.contains(2)) {
                            State s = new State(currentState);

                            Candidate emptyInstance = new Candidate(new Instance("EMPTY_STRING", 0), 0, 0, 0);

                            s.addHiddenVariable(indexOfNode1, indexOfDepDude, emptyInstance);

                            //add as slot 2 since 1 taken by another node
                            s.addSlotVariable(indexOfNode1, indexOfNode, 2);

                            if (!s.equals(currentState) && !newStates.contains(s)) {
                                newStates.add(s);
                            }
                        } else if (!usedSlots.contains(1) && usedSlots.contains(2)) {
                            State s = new State(currentState);

                            Candidate emptyInstance = new Candidate(new Instance("EMPTY_STRING", 0), 0, 0, 0);

                            s.addHiddenVariable(indexOfNode1, indexOfDepDude, emptyInstance);

                            //add as slot 2 since 1 taken by another node
                            s.addSlotVariable(indexOfNode1, indexOfNode, 1);

                            if (!s.equals(currentState) && !newStates.contains(s)) {
                                newStates.add(s);
                            }
                        } else {
                            State s = new State(currentState);

                            Candidate emptyInstance = new Candidate(new Instance("EMPTY_STRING", 0), 0, 0, 0);

                            s.addHiddenVariable(indexOfNode1, indexOfDepDude, emptyInstance);

                            //add as slot 2 since 1 taken by another node
                            s.addSlotVariable(indexOfNode1, indexOfNode, 1);

                            if (!s.equals(currentState) && !newStates.contains(s)) {
                                newStates.add(s);
                            }
                        }
                    }

                }
            }
        }
        
        List<State> sampedStatesWithQueryTypes = sampleQueryTypes(newStates);

        return sampedStatesWithQueryTypes;//newStates
    }
    
    private List<State> sampleQueryTypes(List<State> states){
        Set<State> sampledStates = new HashSet<>();
        
        for(State s : states){
            int currentType = s.getQueryTypeVariable().getType();
            
            switch(currentType){
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
            
            sampledStates.add(s);
        }
        
        return new ArrayList<>(sampledStates);
    }

//    private List createNewtStates2(State currentState) {
//        List<State> newStates = new ArrayList<>();
//
//        for (int indexOfNode : currentState.getDocument().getParse().getNodes().keySet()) {
//            String node = currentState.getDocument().getParse().getNodes().get(indexOfNode);
//
//            String pos = currentState.getDocument().getParse().getPOSTag(indexOfNode);
//
//            boolean hasValidParent = false;
//
//            if (wordsWithSpecialSemanticTypes.contains(node.toLowerCase())) {
//
//                Integer parentNode = currentState.getDocument().getParse().getParentNode(indexOfNode);
//                Integer parentOfParentNode = currentState.getDocument().getParse().getParentNode(parentNode);
//                Integer parentOfParentOfParentNode = currentState.getDocument().getParse().getParentNode(parentOfParentNode);
//                List<Integer> siblingsOfParentNode = currentState.getDocument().getParse().getSiblings(parentNode, validPOSTags, frequentWordsToExclude);
//                List<Integer> dependentNodes = currentState.getDocument().getParse().getDependentEdges(indexOfNode, validPOSTags, frequentWordsToExclude);
//
//                List<Integer> headNodes = new ArrayList<>();
//                headNodes.add(parentNode);
//                headNodes.add(parentOfParentNode);
//                headNodes.add(parentOfParentOfParentNode);
//                headNodes.addAll(siblingsOfParentNode);
//                headNodes.addAll(dependentNodes);
//
//                for (Integer headNode : headNodes) {
//
//                    if (headNode < 0) {
//                        continue;
//                    }
//
//                    if (hasValidParent) {
//                        break;
//                    }
//
//                    HiddenVariable headVar = currentState.getHiddenVariables().get(headNode);
//
//                    if (headVar == null) {
//                        continue;
//                    }
//
//                    String dudeName = null;
//                    if (semanticTypes.containsKey(headVar.getDudeId())) {
//                        dudeName = semanticTypes.get(headVar.getDudeId());
//                    }
//
//                    if (dudeName != null) {
//                        //assign extra dude for dependent nodes if the head dude is class or property
//                        if ((dudeName.equals("Property") || dudeName.equals("RestrictionClass") || dudeName.equals("Class"))) {
//
//                            for (Integer indexOfDepDude : specialSemanticTypes.keySet()) {
//                                List<Integer> usedSlots = currentState.getUsedSlots(headNode);
//
//                                if (usedSlots.contains(1) && usedSlots.contains(2)) {
//                                    State s = new State(currentState);
//
//                                    Candidate emptyInstance = new Candidate(new Instance("EMPTY_STRING", 0), 0, 0, 0);
//
//                                    s.addHiddenVariable(indexOfNode, indexOfDepDude, emptyInstance);
//
//                                    if (!s.equals(currentState) && !newStates.contains(s)) {
//                                        newStates.add(s);
//                                    }
//                                } else if (usedSlots.contains(1) && !usedSlots.contains(2)) {
//                                    State s = new State(currentState);
//
//                                    Candidate emptyInstance = new Candidate(new Instance("EMPTY_STRING", 0), 0, 0, 0);
//
//                                    s.addHiddenVariable(indexOfNode, indexOfDepDude, emptyInstance);
//
//                                    if (dependentNodes.contains(headNode)) {
//                                        //add as slot 2 since 1 taken by another node
//                                        s.addSlotVariable(headNode, indexOfNode, 2);
//                                    } else {
//                                        //add as slot 2 since 1 taken by another node
//                                        s.addSlotVariable(indexOfNode, headNode, 2);
//                                    }
//
//                                    if (!s.equals(currentState) && !newStates.contains(s)) {
//                                        newStates.add(s);
//
//                                        hasValidParent = true;
//                                        break;
//                                    }
//                                } else if (!usedSlots.contains(1) && usedSlots.contains(2)) {
//                                    State s = new State(currentState);
//
//                                    Candidate emptyInstance = new Candidate(new Instance("EMPTY_STRING", 0), 0, 0, 0);
//
//                                    s.addHiddenVariable(indexOfNode, indexOfDepDude, emptyInstance);
//
//                                    if (dependentNodes.contains(headNode)) {
//                                        //add as slot 1 since 2 taken by another node
//                                        s.addSlotVariable(headNode, indexOfNode, 1);
//                                    } else {
//                                        //add as slot 1 since 2 taken by another node
//                                        s.addSlotVariable(indexOfNode, headNode, 1);
//                                    }
//
//                                    if (!s.equals(currentState) && !newStates.contains(s)) {
//                                        newStates.add(s);
//
//                                        hasValidParent = true;
//                                        break;
//                                    }
//                                } else {
//                                    State s = new State(currentState);
//
//                                    Candidate emptyInstance = new Candidate(new Instance("EMPTY_STRING", 0), 0, 0, 0);
//
//                                    s.addHiddenVariable(indexOfNode, indexOfDepDude, emptyInstance);
//
//                                    if (dependentNodes.contains(headNode)) {
//                                        //add as slot 1 
//                                        s.addSlotVariable(headNode, indexOfNode, 1);
//                                    } else {
//                                        //add as slot 1 
//                                        s.addSlotVariable(indexOfNode, headNode, 1);
//                                    }
//
//                                    if (!s.equals(currentState) && !newStates.contains(s)) {
//                                        newStates.add(s);
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//
//                if (newStates.isEmpty()) {
//                    for (Integer key : currentState.getHiddenVariables().keySet()) {
//                        HiddenVariable var = currentState.getHiddenVariables().get(key);
//
//                        if (var.getDudeId() == 1) {
//                            String dudeName = semanticTypes.get(var.getDudeId());
//                            if (dudeName != null) {
//                                //assign extra dude for dependent nodes if the head dude is class or property
//                                if ((dudeName.equals("Property") || dudeName.equals("RestrictionClass") || dudeName.equals("Class"))) {
//
//                                    for (Integer indexOfDepDude : specialSemanticTypes.keySet()) {
//                                        List<Integer> usedSlots = currentState.getUsedSlots(key);
//
//                                        if (usedSlots.contains(1) && usedSlots.contains(2)) {
//                                            State s = new State(currentState);
//
//                                            Candidate emptyInstance = new Candidate(new Instance("EMPTY_STRING", 0), 0, 0, 0);
//
//                                            s.addHiddenVariable(indexOfNode, indexOfDepDude, emptyInstance);
//
//                                            if (!s.equals(currentState) && !newStates.contains(s)) {
//                                                newStates.add(s);
//                                            }
//                                        } else if (usedSlots.contains(1) && !usedSlots.contains(2)) {
//                                            State s = new State(currentState);
//
//                                            Candidate emptyInstance = new Candidate(new Instance("EMPTY_STRING", 0), 0, 0, 0);
//
//                                            s.addHiddenVariable(indexOfNode, indexOfDepDude, emptyInstance);
//
//                                            if (dependentNodes.contains(key)) {
//                                                //add as slot 2 since 1 taken by another node
//                                                s.addSlotVariable(key, indexOfNode, 2);
//                                            } else {
//                                                //add as slot 2 since 1 taken by another node
//                                                s.addSlotVariable(indexOfNode, key, 2);
//                                            }
//
//                                            if (!s.equals(currentState) && !newStates.contains(s)) {
//                                                newStates.add(s);
//
//                                                hasValidParent = true;
//                                                break;
//                                            }
//                                        } else if (!usedSlots.contains(1) && usedSlots.contains(2)) {
//                                            State s = new State(currentState);
//
//                                            Candidate emptyInstance = new Candidate(new Instance("EMPTY_STRING", 0), 0, 0, 0);
//
//                                            s.addHiddenVariable(indexOfNode, indexOfDepDude, emptyInstance);
//
//                                            if (dependentNodes.contains(key)) {
//                                                //add as slot 1 since 2 taken by another node
//                                                s.addSlotVariable(key, indexOfNode, 1);
//                                            } else {
//                                                //add as slot 1 since 2 taken by another node
//                                                s.addSlotVariable(indexOfNode, key, 1);
//                                            }
//
//                                            if (!s.equals(currentState) && !newStates.contains(s)) {
//                                                newStates.add(s);
//
//                                                hasValidParent = true;
//                                                break;
//                                            }
//                                        } else {
//                                            State s = new State(currentState);
//
//                                            Candidate emptyInstance = new Candidate(new Instance("EMPTY_STRING", 0), 0, 0, 0);
//
//                                            s.addHiddenVariable(indexOfNode, indexOfDepDude, emptyInstance);
//
//                                            if (dependentNodes.contains(key)) {
//                                                //add as slot 1 
//                                                s.addSlotVariable(key, indexOfNode, 1);
//                                            } else {
//                                                //add as slot 1 
//                                                s.addSlotVariable(indexOfNode, key, 1);
//                                            }
//
//                                            if (!s.equals(currentState) && !newStates.contains(s)) {
//                                                newStates.add(s);
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        return newStates;
//    }
}
