/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.learning;

import de.citec.sc.dudes.rdf.RDFDUDES;
import de.citec.sc.variable.URIVariable;
import de.citec.sc.variable.SlotVariable;
import de.citec.sc.variable.State;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author sherzod
 */
public class SemanticComposition {

    private static Map<Integer, RDFDUDES> dudeTypes;
    private static Set<String> validPOSTags;
    private static Map<Integer, String> semanticTypes;
    private static Map<Integer, String> specialSemanticTypes;
    
    private static Set<Integer> mergedNodes;

    protected static RDFDUDES compose(State state, Map<Integer, RDFDUDES> instantiatedDudeTypes, Set<String> validPOSs, Map<Integer, String> s, Map<Integer, String> sp) {
        Integer headNode = state.getDocument().getParse().getHeadNode();

        validPOSTags = validPOSs;
        dudeTypes = instantiatedDudeTypes;
        semanticTypes = s;
        specialSemanticTypes = sp;

        mergedNodes = new HashSet<>();

        //merge slot arguments first
        mergeSlots(state);

        //merge edges bottom up
        RDFDUDES merged = mergeChildNodes(state, headNode);

        return merged;
    }

    private static void mergeSlots(State state) {
        //loop over all slot arguments and join them
        for (Integer depNode : state.getSlotVariables().keySet()) {

            if (state.getDocument().getParse().getDependentEdges(depNode, validPOSTags).size() > 0) {
                continue;
            }

            SlotVariable var = state.getSlotVariables().get(depNode);

            RDFDUDES dependent = dudeTypes.get(depNode);
            RDFDUDES head = dudeTypes.get(var.getParentTokenID());

            String argument = var.getSlotNumber() + "";

            String d = "";
            if (dependent != null) {
                d = dependent.toString();
            }
            String h = "";
            if (head != null) {
                h = head.toString();
            }

            //do the merging part
            head = merge(head, dependent, argument);
            String after = head.toString();

//            System.out.println("Before "+d+" \nBefore: "+h+" \nAfter: "+after);
            //update the head node with the newly merged semantics
            dudeTypes.put(var.getParentTokenID(), head);

            //mark the dependent node that it's already merged
            mergedNodes.add(depNode);
        }
    }

    /**
     * merges the edges with slot argument
     */
    private static RDFDUDES mergeChildNodes(State state, Integer headNode) {
        //get dependent nodes

        List<Integer> childNodes = state.getDocument().getParse().getDependentEdges(headNode);

        for (Integer depNodeIndex : childNodes) {

            RDFDUDES dependent = null;
            RDFDUDES head = dudeTypes.get(headNode);

            if (!mergedNodes.contains(depNodeIndex)) {
                try{
                dependent = mergeChildNodes(state, depNodeIndex);
                }
                catch(Exception e){
                    int z=2;
                }

                String argument = state.getSlot(depNodeIndex, headNode);

                if (argument.equals("")) {
                    argument = "1";
                }

                head = merge(head, dependent, argument);

            } else {
            }

            dudeTypes.put(headNode, head);

        }
        return dudeTypes.get(headNode);
    }

    private static RDFDUDES merge(RDFDUDES head, RDFDUDES dependent, String argument) {
        if (head == null) {
            return dependent;
        } else if (dependent == null || argument.equals("-1") || argument.equals("")) {
            return head;
        } else {
            try {

                head = head.merge(dependent, argument);
            } catch (Exception e) {

            }
        }
        return head;
    }
//    private static RDFDUDES childN(State state, Integer node) {
//        //get dependent nodes
//
//        String token = state.getDocument().getParse().getNodes().get(node);
//
//        String pos = state.getDocument().getParse().getPOSTag(node);
//
//        if (!validPOSTags.contains(pos)) {
//            dudeTypes.get(node);
//        }
//        if (frequentWordsToExclude.contains(token.toLowerCase())) {
//            dudeTypes.get(node);
//        }
//
//        List<Integer> childNodes = state.getDocument().getParse().getDependentEdges(node);
//        List<Integer> siblings = state.getDocument().getParse().getSiblings(node);
//
//        List<Integer> dependentNodes = new ArrayList<>();
//        
//        //add child and sibling nodes
//        dependentNodes.addAll(childNodes);
//        
//        for(Integer s : siblings){
//            String siblingToken = state.getDocument().getParse().getToken(s);
//            
//            if(wordsWithSpecialSemanticTypes.contains(siblingToken.toLowerCase())){
//                dependentNodes.add(s);
//            }
//        }
//        
//        //get siblins if there are not dependent nodes
//        if (dependentNodes.isEmpty()) {
//            int headOfHeadNodeIndex = state.getDocument().getParse().getParentNode(node);
//            String headOfHeadToken = state.getDocument().getParse().getToken(headOfHeadNodeIndex);
//
//            HiddenVariable headVar = state.getHiddenVariables().get(node);
//
//            String dudeName = semanticTypes.get(headVar.getDudeId());
//
//            if (frequentWordsToExclude.contains(headOfHeadToken) && dudeName.equals("Property")) {
//                dependentNodes = state.getDocument().getParse().getSiblings(node);
//            }
//        }
//
//        for (Integer depNodeIndex : dependentNodes) {
//
//            RDFDUDES dependent = mergedChildNodes(state, depNodeIndex);
//            RDFDUDES head = dudeTypes.get(node);
//
//            String argument = state.getSlot(depNodeIndex, node);
//
//            if (!argument.isEmpty()) {
//                int z = 1;
//            }
//            head = merge(head, dependent, argument);
//            dudeTypes.put(node, head);
//        }
//        return dudeTypes.get(node);
//    }

}
