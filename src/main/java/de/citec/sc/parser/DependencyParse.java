package de.citec.sc.parser;

import de.citec.sc.main.Main;
import de.citec.sc.query.CandidateRetriever;
import de.citec.sc.query.Search;
import de.citec.sc.utils.ProjectConfiguration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author sherzod
 */
public class DependencyParse {

    private HashMap<Integer, String> nodes;
    private HashMap<Integer, Integer> relations;
    private HashMap<Integer, String> edgeStrings;
    private HashMap<Integer, String> POSTAG;

    private String treeString;
//    private List<Token> tokens;
    private int headNode;

    public DependencyParse() {
        nodes = new HashMap<Integer, String>();
        relations = new LinkedHashMap<>();
        edgeStrings = new HashMap<Integer, String>();
        POSTAG = new HashMap<Integer, String>();
//        this.tokens = new ArrayList<>();
    }

    public void mergeEdges() {

//        System.out.println("Before\n\n" + toString()+"\n");
//        mergeCompountEdges();
        mergePatterns();

//        System.out.println("After mergePatterns\n\n" + toString()+"\n");
        mergeAmodEdges();
//
//        System.out.println("After mergeAmodEdges\n\n" + toString()+"\n");
//        int stop=1;
//
////        System.out.println("After mergeDepEdges\n\n" + toString()+"\n");
//        mergeDetEdges();
//
////        System.out.println("After mergeDetEdges\n\n" + toString()+"\n");

//        System.out.println("After mergePatterns\n\n" + toString()+"\n");
    }

    private void mergePatterns() {

        List<String> patterns = new ArrayList<>();
//        patterns.add("NOUN ADP PROPN");//Battle (NN) 		5,of (IN) 		6,Gettysburg (NNP)
        patterns.add("PROPN PROPN");//John (PROPN) 		6,F (PROPN) 		7,. (PUNCT) 		8,Kennedy (PROPN)
        patterns.add("PROPN PROPN PUNCT PROPN");//John (PROPN) 		6,F (PROPN) 		7,. (PUNCT) 		8,Kennedy (PROPN)
        patterns.add("PROPN PROPN PROPN PROPN");//West (PROPN) 		10,African (PROPN) 		11,CFA (PROPN) 		12,franc (PROPN)
        patterns.add("PROPN PROPN PROPN");//West (PROPN) 		10,African (PROPN) 		11,CFA (PROPN) 		12,franc (PROPN)
        patterns.add("PROPN PROPN");//West (PROPN) 		10,African (PROPN) 		11,CFA (PROPN) 		12,franc (PROPN)
        patterns.add("PROPN PUNCT PROPN");//Melbourne (PROPN) 		8,, (PUNCT) 		9,Florida (PROPN)
        patterns.add("ADJ PROPN ADP PROPN");//Free (ADJ) 		7,University (PROPN) 		8,in (ADP) 		9,Amsterdam (PROPN) 
        patterns.add("PROPN ADP PROPN");//Lawrence (PROPN) 		7,of (ADP) 		8,Arabia (PROPN)
        patterns.add("NOUN NOUN ADP NOUN");//nobel (NOUN) 		6,prize (NOUN) 		7,in (ADP) 		8,physics (NOUN)
        patterns.add("PROPN PROPN ADP NOUN");//Nobel (PROPN) 		9,Prize (PROPN) 		10,in (ADP) 		11,literature (NOUN)
        patterns.add("PROPN NUM");//7,Chile Route (NNP) 		8,68 (CD) 
        patterns.add("NOUN NOUN ADP NOUN");//nobel (NOUN) 		6,prize (NOUN) 		7,in (ADP) 		8,physics (NOUN) 
        patterns.add("NOUN ADP PROPN");// Game (NOUN) 		5,of (ADP) 		6,Thrones (PROPN)
        patterns.add("PROPN PROPN PUNCT NOUN");//5,Park (PROPN) 		6,Chan (PROPN) 		7,- (PUNCT) 		8,wook (NOUN)

//        patterns.add("DET PROPN");//The (DET) 		14,Sopranos (PROPN)
        List<String> edges = new ArrayList<>();
        edges.add("obj");
        edges.add("obl");
        edges.add("flat");
        edges.add("compound");
        edges.add("nummod");
        edges.add("appos");
        edges.add("subj");
        edges.add("nsubj");
        edges.add("dobj");
        edges.add("iobj");
        edges.add("nsubjpass");
        edges.add("csubj");
        edges.add("csubjpass");
        edges.add("nmod:poss");
        edges.add("ccomp");
        edges.add("nmod");
        edges.add("amod");
        edges.add("xcomp");
        edges.add("vocative");
        edges.add("det");

        int counter = 0;
        int max = nodes.size();
        boolean merged = false;

        while (counter <= max) {

            List<Integer> allNodes = new ArrayList<>();
            allNodes.addAll(nodes.keySet());

            //sort the nodes
            Collections.sort(allNodes);

            counter++;
            merged = false;

            for (int i = 0; i < allNodes.size(); i++) {

                //get 4 tokens
                for (int r = 4; r >= 0; r--) {

                    if (i + r < allNodes.size()) {

                        List<Integer> mergedNodes = allNodes.subList(i, i + r + 1);
                        String postags = "";
                        String mergedTokens = "";
                        for (Integer m : mergedNodes) {
                            postags += " " + POSTAG.get(m);

                            //no space before punctuations
                            if (POSTAG.get(m).equals("PUNCT")) {
                                mergedTokens += getToken(m);
                            } else {
                                if (POSTAG.containsKey(m-1)) {
                                    if (POSTAG.get(m - 1).equals("PUNCT")) {
                                        mergedTokens += getToken(m);
                                    } else {
                                        mergedTokens += " " + getToken(m);
                                    }
                                } else {
                                    mergedTokens += " " + getToken(m);
                                }

                            }

                        }
                        postags = postags.trim();
                        mergedTokens = mergedTokens.trim();

                        //only for words that start with uppercase
                        boolean hasUppercase = !mergedTokens.equals(mergedTokens.toLowerCase());
                        if (hasUppercase) {

                            String[] tokens = mergedTokens.split(" ");

                            for (String t : tokens) {
                                String character = t.charAt(0) + "";
                                if (character.equals(character.toLowerCase())) {
                                    continue;
                                }
                            }
                        } else {
                            continue;
                        }

                        if (patterns.contains(postags)) {

                            if (mergedTokens.isEmpty()) {
                                continue;
                            }

                            boolean b = Search.matches(mergedTokens.toLowerCase(), CandidateRetriever.Language.valueOf(ProjectConfiguration.getLanguage()));
                            
                            //if matches then remove nodes
                            if (b) {

                                for (Integer depNode : mergedNodes) {
                                    if (depNode == allNodes.get(i)) {
                                        continue;
                                    }

                                    HashMap<Integer, Integer> temp = (HashMap<Integer, Integer>) relations.clone();
                                    HashMap<Integer, String> tempEdgeStrings = (HashMap<Integer, String>) edgeStrings.clone();
                                    //replace every parent relation if it equals to depNode
                                    for (Integer d : relations.keySet()) {
                                        Integer p = relations.get(d);

                                        if (d.equals(depNode)) {

                                            if (!mergedNodes.contains(p)) {

                                                String edgeString = edgeStrings.get(d);

                                                if (edges.contains(edgeString)) {
                                                    temp.put(allNodes.get(i), p);
                                                    tempEdgeStrings.put(allNodes.get(i), edgeString);
                                                }
                                            }
                                        } else if (p.equals(depNode)) {
                                            if (!mergedNodes.contains(d)) {
                                                temp.put(d, allNodes.get(i));
                                            } else {
                                                temp.remove(d);
                                            }
                                        }
                                    }
                                    //clone back the values
                                    relations = (HashMap<Integer, Integer>) temp.clone();
                                    edgeStrings = (HashMap<Integer, String>) tempEdgeStrings.clone();

                                    //check if the root
                                    if (headNode == depNode) {
                                        headNode = allNodes.get(i);
                                    }

                                    edgeStrings.remove(depNode);
                                    relations.remove(depNode);
                                    nodes.remove(depNode);
                                    POSTAG.remove(depNode);
                                }

                                POSTAG.put(allNodes.get(i), "PROPN");
                                nodes.put(allNodes.get(i), mergedTokens);

                                merged = true;
                                break;
                            }
                        }
                    }
                }

                if (merged) {
                    break;
                }
            }
        }
    }

    private void mergeAmodEdges() {
        //do another merging on amod - Give me all Australian nonprofit organizations. --> merges nonprofit organizations
        int counter = 0;

        List<String> edges = new ArrayList<>();
        edges.add("obj");
        edges.add("obl");
        edges.add("flat");
        edges.add("compound");
        edges.add("nummod");
        edges.add("appos");
        edges.add("subj");
        edges.add("nsubj");
        edges.add("dobj");
        edges.add("iobj");
        edges.add("nsubjpass");
        edges.add("csubj");
        edges.add("csubjpass");
        edges.add("nmod:poss");
        edges.add("ccomp");
        edges.add("nmod");
        edges.add("amod");
        edges.add("xcomp");
        edges.add("vocative");

        List<Integer> traversedDepNodes = new ArrayList<>();

        while (edgeStrings.containsValue("amod") || edgeStrings.containsValue("flat") || edgeStrings.containsValue("compound") || edgeStrings.containsValue("name")) {

            counter++;
            Integer headNode = -1;

            List<Integer> depNodesWithCompoundEdge = new ArrayList<>();

            //get the head node
            for (Integer depNode : edgeStrings.keySet()) {
                if (traversedDepNodes.contains(depNode)) {
                    continue;
                }

                if (edgeStrings.get(depNode).equals("amod") || edgeStrings.get(depNode).equals("flat") || edgeStrings.get(depNode).equals("compound") || edgeStrings.get(depNode).equals("name")) {
                    headNode = getParentNode(depNode);
                    String headPOS = getPOSTag(headNode);

//                    boolean isValidMerge = false;
//                    switch (headPOS) {
//                        case "NNP":
//                            if (depNode.equals("NNP") || depNode.equals("NNPS")) {
//                                isValidMerge = true;
//                            }
//                            break;
//                        case "NN":
//                            if (depNode.equals("NN") || depNode.equals("NNS")) {
//                                isValidMerge = true;
//                            }
//                            else if (depNode.equals("NNP") || depNode.equals("NNPS")) {
//                                isValidMerge = true;
//                            }
//                            break;
//                    }
//
//                    if (!isValidMerge) {
//                        continue;
//                    }
                    depNodesWithCompoundEdge.add(headNode);
                    depNodesWithCompoundEdge.add(depNode);

                    //not to continue with the same node
                    traversedDepNodes.add(depNode);
                    break;
                }
            }

            if (headNode == -1) {
                break;
            }
            if (counter == nodes.size()) {
                break;
            }

            Collections.sort(depNodesWithCompoundEdge);

            //add all indice between the maximum and the minimum value
            //Melon de Bourgogne = not to have sth like this :Melon Bourgogne
            List<Integer> missingIntervals = new ArrayList<>();

            for (int i = 0; i < depNodesWithCompoundEdge.size(); i++) {

                if (i + 1 < depNodesWithCompoundEdge.size()) {
                    Integer node = depNodesWithCompoundEdge.get(i);
                    Integer nextNode = depNodesWithCompoundEdge.get(i + 1);

                    //if there is some index missing
                    if (nextNode - node != 1) {
                        missingIntervals.add(node + 1);
                    }
                }
            }
            //add missing intervals and sort again
            if (!missingIntervals.isEmpty()) {
                depNodesWithCompoundEdge.addAll(missingIntervals);
                Collections.sort(depNodesWithCompoundEdge);
            }

            String mergedTokens = "";
            for (Integer nodeIndex : depNodesWithCompoundEdge) {
                if (nodes.get(nodeIndex) == null) {
                    continue;
                }
                if (nodes.get(nodeIndex).equals("null")) {
                    continue;
                }
                mergedTokens += nodes.get(nodeIndex) + " ";
            }
            mergedTokens = mergedTokens.trim();

            //check if there are any capital letters in the mergedTokens
            //if there are don't expand these tokens
//            boolean hasUppercase = !mergedTokens.equals(mergedTokens.toLowerCase());
//            
//            if(hasUppercase){
//                continue;
//            }
            if (mergedTokens.isEmpty()) {
                continue;
            }
            boolean b = Search.matches(mergedTokens, CandidateRetriever.Language.valueOf(ProjectConfiguration.getLanguage()));

            //if matches then remove nodes
            if (b) {

                for (Integer depNode : depNodesWithCompoundEdge) {
                    if (depNode == headNode) {
                        continue;
                    }

                    HashMap<Integer, Integer> temp = (HashMap<Integer, Integer>) relations.clone();
                    HashMap<Integer, String> tempEdgeStrings = (HashMap<Integer, String>) edgeStrings.clone();
                    //replace every parent relation if it equals to depNode
                    for (Integer d : relations.keySet()) {
                        Integer p = relations.get(d);

                        if (d.equals(depNode)) {

                            if (!depNodesWithCompoundEdge.contains(p)) {

                                String edgeString = edgeStrings.get(d);

                                if (edges.contains(edgeString)) {
                                    temp.put(headNode, p);
                                    tempEdgeStrings.put(headNode, edgeString);
                                }
                            }
                        } else if (p.equals(depNode)) {
                            if (!depNodesWithCompoundEdge.contains(d)) {
                                temp.put(d, headNode);
                            } else {
                                temp.remove(d);
                            }
                        }
                    }
                    //clone back the values
                    relations = (HashMap<Integer, Integer>) temp.clone();
                    edgeStrings = (HashMap<Integer, String>) tempEdgeStrings.clone();

                    //check if the root
                    if (this.headNode == depNode) {
                        this.headNode = headNode;
                    }

                    edgeStrings.remove(depNode);
                    relations.remove(depNode);
                    nodes.remove(depNode);
                    POSTAG.remove(depNode);
                }

                for (Integer depNode : depNodesWithCompoundEdge) {
                    if (depNode == headNode) {
                        continue;
                    }

                    edgeStrings.remove(depNode);
                    relations.remove(depNode);
                    nodes.remove(depNode);
                    POSTAG.remove(depNode);

                }

                nodes.put(headNode, mergedTokens);
            }
        }
    }

    public void removePunctuations() {
        HashMap<Integer, String> tempEdgeStrings = (HashMap<Integer, String>) edgeStrings.clone();

        for (Integer node : tempEdgeStrings.keySet()) {
            String edgeString = tempEdgeStrings.get(node);

            if (edgeString.equals("punct")) {
                relations.remove(node);
                edgeStrings.remove(node);
                nodes.remove(node);
                POSTAG.remove(node);

            }
        }
    }

    /**
     * removes loops from edges that linking to itself
     */
    public void removeLoops() {
        boolean isLoop = true;

        while (isLoop) {
            Integer loopRelationKey = -1;
            Integer loopRelationValue = -1;

            for (Integer k : getRelations().keySet()) {
                Integer v = getRelations().get(k);

                if (k.equals(v)) {
                    isLoop = true;

                    loopRelationKey = k;
                    loopRelationValue = v;
                    break;
                }
                //check if any relations has v as Key, and k as Value

                if (getRelations().containsKey(v)) {

                    Integer otherK = getRelations().get(v);

                    if (k.equals(otherK)) {
                        isLoop = true;

                        loopRelationKey = k;
                        loopRelationValue = v;
                        break;
                    }
                }
            }

            if (loopRelationKey != -1 && loopRelationValue != -1) {
                System.out.println("Removed loop from edge: " + loopRelationKey + ", " + loopRelationValue);
                relations.remove(loopRelationKey, loopRelationValue);
            } else {
                isLoop = false;
            }

        }
    }

    public void addNode(int index, String label, String pos, int beginPosition, int endPosition) {
        if (!nodes.containsKey(index)) {
            nodes.put(new Integer(index), label);
            POSTAG.put(new Integer(index), pos);

//            Token t = new Token(index, beginPosition, endPosition, label);
//            tokens.add(t);
        }
//            else {
//            if (nodes.get(index).length() < label.length()) {
//
//                for (Token t1 : tokens) {
//                    if (t1.getText().equals(nodes.get(index))) {
//                        tokens.remove(t1);
//                        break;
//                    }
//                }
//                Token newToken = new Token(index, beginPosition, endPosition, label);
//
//                tokens.add(newToken);
//                nodes.put(new Integer(index), label);
//                POSTAG.put(new Integer(index), pos);
//            }
//        }
    }

    public void addEdge(int dependent, int head, String depRelation) {
        this.relations.put(new Integer(dependent), new Integer(head));
        this.edgeStrings.put(new Integer(dependent), depRelation);
    }

    @Override
    public String toString() {

        String r = "\nNodes:\n";
        for (int s : nodes.keySet()) {
            r += "\t" + s + "," + nodes.get(s) + " (" + POSTAG.get(s) + ") \t";
        }

        r += "\nEdges:\n";
        for (int s : edgeStrings.keySet()) {
            r += "\t" + s + "," + edgeStrings.get(s) + "\t";
        }

        r += "\n\nParse Tree:\n";
        for (int s : relations.keySet()) {
            r += " (" + s + "," + relations.get(s) + ")\t";
        }

        r += "\nHead node: " + headNode;
        return r;
    }

    public List<String> getWords() {
        return (List<String>) nodes.values();
    }

    /**
     * returns dependent edges given the headNode
     *
     * @param headNode
     * @return List of dependent nodes
     */
    public List<Integer> getDependentEdges(int headNode) {

        List<Integer> list = new ArrayList<>();
        for (Integer k : relations.keySet()) {
            Integer v = relations.get(k);

            if (v == headNode) {
                list.add(k);
            }
        }

        return list;
    }

    /**
     * returns dependent edges given the headNode that have a valid postag
     *
     * @param headNode
     * @param acceptedPOSTAGs set of postags
     * @return List of dependent nodes
     */
    public List<Integer> getDependentEdges(int headNode, Set<String> acceptedPOSTAGs) {

        Set<Integer> set = new HashSet<>();
        for (Integer k : relations.keySet()) {
            Integer v = relations.get(k);

            if (v == headNode) {
                String postag = getPOSTag(k);

                if (!acceptedPOSTAGs.contains(postag)) {
                    continue;
                }

                set.add(k);

            }
        }
        List<Integer> list = new ArrayList<>(set);
        return list;
    }

    /**
     * returns dependent edges given the headNode that have a valid postag based
     * on the dependency level it can return dependent nodes of dependent nodes
     * given the head node
     *
     * @param headNode
     * @param dependencyLevel
     * @param acceptedPOSTAGs set of postags
     * @return List of dependent nodes
     */
    public List<Integer> getDependentEdges(int headNode, Set<String> acceptedPOSTAGs, Integer dependencyLevel) {

        Set<Integer> set = new HashSet<>();
        for (Integer k : relations.keySet()) {
            Integer v = relations.get(k);

            if (v == headNode) {
                String postag = getPOSTag(k);

                if (acceptedPOSTAGs.contains(postag)) {
                    set.add(k);
                }

                if (dependencyLevel > 1) {
                    //decrease the dependency level
                    List<Integer> dependentNodeOfK = getDependentEdges(k, acceptedPOSTAGs, dependencyLevel - 1);
                    set.addAll(dependentNodeOfK);
                }
            }
        }

        List<Integer> list = new ArrayList<>(set);
        return list;
    }

    /**
     * returns sibling edges of given the headNode if the postag of that is in
     * acceptedPOSTAGs
     *
     * @param headNode
     * @return List of sibling nodes
     */
    public List<Integer> getSiblings(int nodeId, Set<String> acceptedPOSTAGs) {

        List<Integer> list = new ArrayList<>();

        Integer parentNode = getParentNode(nodeId);

        if (parentNode != null) {
            List<Integer> allChildren = getDependentEdges(parentNode);

            for (Integer s : allChildren) {

                String postag = getPOSTag(s);

                if (!acceptedPOSTAGs.contains(postag)) {
                    continue;
                }

                if (!s.equals(nodeId)) {
                    list.add(s);
                }

            }
        }

        return list;
    }

    /**
     * returns sibling edges of given the headNode
     *
     * @param headNode
     * @return List of dependent nodes
     */
    public List<Integer> getSiblings(int nodeId) {

        List<Integer> list = new ArrayList<>();

        Integer parentNode = getParentNode(nodeId);

        if (parentNode != null) {
            List<Integer> allChildren = getDependentEdges(parentNode);

            for (Integer s : allChildren) {
                if (!s.equals(nodeId)) {
                    list.add(s);
                }
            }
        }

        return list;
    }

    /**
     * returns parent node of given the node
     *
     * @param node
     * @return parent node
     */
    public Integer getParentNode(int node) {

        if (relations.containsKey(node)) {
            Integer parentNode = relations.get(node);

            return parentNode;
        }
        return -1;

    }

    public HashMap<Integer, String> getNodes() {
        return nodes;
    }

    /**
     * returns relations between nodes in the parse tree
     *
     * return HashMap<Integer, Integer> where keys are dependent nodes and
     * values are parent nodes
     *
     * @return HashMap<Integer, Integer> relations
     */
    public HashMap<Integer, Integer> getRelations() {

        HashMap<Integer, Integer> edges = new LinkedHashMap<>();
        for (Integer k : relations.keySet()) {
            edges.put(k, relations.get(k));
        }
        return edges;
    }

    private HashMap<Integer, String> getEdgeStrings() {
        return edgeStrings;
    }

    /**
     * returns dependency relation for the given dependent node if given
     * dependent node is the root of the parse tree then returns
     * "ThisNodeIsRoot"
     *
     * @param dependentNodeId
     * @return String dependency relation
     */
    public String getDependencyRelation(Integer dependentNodeId) {
        if (edgeStrings.containsKey(dependentNodeId)) {
            return edgeStrings.get(dependentNodeId);
        }
        return "ThisNodeIsRoot";
    }

    /**
     * returns postag for the given node
     *
     * @param dependentNodeId
     * @return String POSTag
     */
    public String getPOSTag(Integer nodeId) {
        return POSTAG.get(nodeId);
    }

    /**
     * returns previous postag for the given node
     *
     * @param dependentNodeId
     * @return String POSTag
     */
    public String getPreviousPOSTag(Integer nodeId) {
        List<Integer> nodeIDs = new ArrayList<>(nodes.keySet());
        Collections.sort(nodeIDs);

        Integer prevNodeID = -1;

        for (int i = 0; i < nodeIDs.size(); i++) {
            if (nodeIDs.get(i).equals(nodeId)) {

                if (i > 0) {
                    prevNodeID = nodeIDs.get(i - 1);
                    break;
                }
            }
        }

        if (prevNodeID != -1) {
            return POSTAG.get(prevNodeID);
        }

        return "NO-PREV-TOKEN-POS";
    }

    /**
     * returns next postag for the given node
     *
     * @param dependentNodeId
     * @return String POSTag
     */
    public String getNextPOSTag(Integer nodeId) {
        List<Integer> nodeIDs = new ArrayList<>(nodes.keySet());
        Collections.sort(nodeIDs);

        Integer nextNodeID = -1;

        for (int i = 0; i < nodeIDs.size(); i++) {
            if (nodeIDs.get(i).equals(nodeId)) {

                if (i + 1 < nodeIDs.size()) {
                    nextNodeID = nodeIDs.get(i + 1);
                    break;
                }
            }
        }

        if (nextNodeID != -1) {
            return POSTAG.get(nextNodeID);
        }

        return "NO-NEXT-TOKEN-POS";
    }

    /**
     * returns token for the given node
     *
     * @param dependentNodeId
     * @return String token
     */
    public String getToken(Integer nodeId) {
        return nodes.get(nodeId);
    }

    public int getHeadNode() {
        return headNode;
    }

    public void setHeadNode(int headNode) {
        this.headNode = headNode;
    }

    public void setNodes(HashMap<Integer, String> nodes) {
        this.nodes = nodes;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.nodes);
        hash = 29 * hash + Objects.hashCode(this.relations);
        hash = 29 * hash + Objects.hashCode(this.edgeStrings);
        hash = 29 * hash + Objects.hashCode(this.POSTAG);
        hash = 29 * hash + Objects.hashCode(this.treeString);
//        hash = 29 * hash + Objects.hashCode(this.tokens);
        hash = 29 * hash + this.headNode;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DependencyParse other = (DependencyParse) obj;
        if (!Objects.equals(this.nodes, other.nodes)) {
            return false;
        }
        if (!Objects.equals(this.relations, other.relations)) {
            return false;
        }
        if (!Objects.equals(this.edgeStrings, other.edgeStrings)) {
            return false;
        }
        if (!Objects.equals(this.POSTAG, other.POSTAG)) {
            return false;
        }
        if (!Objects.equals(this.treeString, other.treeString)) {
            return false;
        }
//        if (!Objects.equals(this.tokens, other.tokens)) {
//            return false;
//        }
        if (this.headNode != other.headNode) {
            return false;
        }
        return true;
    }

//    public HashMap<Integer, String> getPOSTAG() {
//        return POSTAG;
//    }
//    public List<Token> getTokens() {
//        return tokens;
//    }
    public String getTreeString() {
        return treeString;
    }

    public void setTreeString(String treeString) {
        this.treeString = treeString;
    }

    /**
     * @param currentTokenPosition (Integer) to start from
     * @param numberOfPOSTAGS indicates the number of postags of the next tokens
     * to merge
     * @return string which contains all postags merged.
     */
    public String getPOSTagsMerged(Integer tokenPosition, int numberOfPOSTAGs) {
        String mergedPOSTAGs = "";

        List<Integer> allTokenPositions = new ArrayList<>(nodes.keySet());

        Collections.sort(allTokenPositions);

        int i = allTokenPositions.indexOf(tokenPosition);

        if (i + numberOfPOSTAGs - 1 < allTokenPositions.size()) {
            List<Integer> subList = allTokenPositions.subList(i, i + numberOfPOSTAGs);

            for (Integer s1 : subList) {
                mergedPOSTAGs += getPOSTag(s1) + " ";
            }
        }

        mergedPOSTAGs = mergedPOSTAGs.trim();

        return mergedPOSTAGs;
    }

    /**
     * @param tokenPosition1 start position
     * @param tokenPosition2 end position
     * @return string which contains all postags merged.
     */
    public Set<String> getIntervalPOSTagsMerged(Integer tokenPosition1, Integer tokenPosition2) {
        Set<String> mergedPOSTAGs = new HashSet<>();
//
        List<Integer> allTokenPositions = new ArrayList<>(nodes.keySet());

        Collections.sort(allTokenPositions);

        Integer smallOne = Math.min(tokenPosition1, tokenPosition2);
        Integer bigOne = Math.max(tokenPosition1, tokenPosition2);

        int startPosition = allTokenPositions.indexOf(smallOne);
        int endPosition = allTokenPositions.indexOf(bigOne);

        List<Integer> subList = allTokenPositions.subList(startPosition, endPosition + 1);

        String postag1 = "", postag2 = "";
        for (Integer s1 : subList) {
            postag1 += getPOSTag(s1) + " ";

            //add the token itself if the token position is not the given by arguments
            if (!s1.equals(tokenPosition1) && !s1.equals(tokenPosition2)) {
                postag2 += getToken(s1) + " ";
            } else {
                postag2 += getPOSTag(s1) + " ";
            }
        }

        mergedPOSTAGs.add(postag1.trim());
        mergedPOSTAGs.add(postag2.trim());

        return mergedPOSTAGs;
    }

    /**
     * returns dependency relation for the given dependent node if given
     * dependent node is the root of the parse tree then returns
     * "ThisNodeIsRoot"
     *
     * @param dependentNodeId
     * @return String dependency relation
     */
    public String getSiblingDependencyRelation(Integer node1, Integer node2) {
        if (getParentNode(node1).equals(getParentNode(node2))) {
            String s1 = getDependencyRelation(node1);
            String s2 = getDependencyRelation(node2);
            String headPOS = getPOSTag(getParentNode(node1));

            return s1 + "-" + headPOS + "-" + s2;
        }

        return "NOT-DIRECT-SIBLING";
    }

    /**
     * @param currentTokenPosition (Integer) to start from
     * @param numberOfPOSTAGS indicates the number of postags of the next tokens
     * to merge
     * @return string which contains all postags merged.
     */
    public List<Integer> getNextTokens(Integer tokenID, int numberOfTokens) {
        List<Integer> nextTokens = new ArrayList<>();

        List<Integer> allTokenPositions = new ArrayList<>(nodes.keySet());

        Collections.sort(allTokenPositions);

        int i = allTokenPositions.indexOf(tokenID);

        if (i + numberOfTokens - 1 < allTokenPositions.size()) {
            nextTokens = allTokenPositions.subList(i, i + numberOfTokens);
        }

        return nextTokens;
    }
    public void setPOSTag(Integer nodeId, String newPOSTag){
        if(this.POSTAG.containsKey(nodeId)){
            this.POSTAG.put(nodeId, newPOSTag);
        }
    }
}
