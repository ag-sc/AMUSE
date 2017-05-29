/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fullStack;

import de.citec.sc.learning.QueryConstructor;
import de.citec.sc.parser.DependencyParse;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import junit.framework.Assert;
import org.junit.Test;

/**
 *
 * @author sherzod
 */
public class DependencyParseTreeTest {

    @Test
    public void test2() {

        //semantic types to sample from
        Map<Integer, String> semanticTypes = new LinkedHashMap<>();
        semanticTypes.put(1, "Property");
        semanticTypes.put(2, "Individual");
        semanticTypes.put(3, "Class");
        semanticTypes.put(4, "RestrictionClass");

        //semantic types with special meaning
        Map<Integer, String> specialSemanticTypes = new LinkedHashMap<>();
        specialSemanticTypes.put(5, "What");//it should be higher than semantic type size

        Set<String> validPOSTags = new HashSet<>();
        validPOSTags.add("NN");
        validPOSTags.add("NNP");
        validPOSTags.add("NNS");
        validPOSTags.add("NNPS");
        validPOSTags.add("VBZ");
        validPOSTags.add("VBN");
        validPOSTags.add("VBD");
        validPOSTags.add("VBG");
        validPOSTags.add("VBP");
        validPOSTags.add("VB");
        validPOSTags.add("JJ");

        Set<String> frequentWordsToExclude = new HashSet<>();
        //all of this words have a valid POSTAG , so they shouldn't be assigned any URI to these tokens
        frequentWordsToExclude.add("is");
        frequentWordsToExclude.add("was");
        frequentWordsToExclude.add("were");
        frequentWordsToExclude.add("are");
        frequentWordsToExclude.add("do");
        frequentWordsToExclude.add("does");
        frequentWordsToExclude.add("did");
        frequentWordsToExclude.add("give");
        frequentWordsToExclude.add("list");
        frequentWordsToExclude.add("show");
        frequentWordsToExclude.add("me");
        frequentWordsToExclude.add("many");
        frequentWordsToExclude.add("have");
        frequentWordsToExclude.add("belong");

        //these words can have special semantic type
        Set<String> wordsWithSpecialSemanticTypes = new HashSet<>();
        wordsWithSpecialSemanticTypes.add("which");
        wordsWithSpecialSemanticTypes.add("what");
        wordsWithSpecialSemanticTypes.add("who");
        wordsWithSpecialSemanticTypes.add("whom");
        wordsWithSpecialSemanticTypes.add("how");
        wordsWithSpecialSemanticTypes.add("when");
        wordsWithSpecialSemanticTypes.add("where");
        
        
        Set<String> edges = new HashSet<>();
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

        QueryConstructor.initialize(specialSemanticTypes, semanticTypes, validPOSTags,edges);

        /**
         * Text: Who created Family_Guy Nodes : 1 2 3 Edges: (1,2 = subj) (3,2 =
         * dobj)
         */
        DependencyParse parseTree = new DependencyParse();
        parseTree.addNode(1, "How", "WRB", -1, -1);
        parseTree.addNode(2, "tall", "JJ", -1, -1);
        parseTree.addNode(3, "is", "VBZ", -1, -1);
        parseTree.addNode(5, "Michael Jordan", "NNP", -1, -1);

        parseTree.addEdge(1, 2, "advmod");
        parseTree.addEdge(2, 3, "dep");
        parseTree.addEdge(5, 3, "nsubj");

        parseTree.setHeadNode(3);

        Integer currentToken = 3;
        List<Integer> nextTokens = parseTree.getNextTokens(currentToken, 2);

        System.out.println("Next token after current token : " + currentToken + " is -> " + nextTokens);

        String mergedPOSTAGs = parseTree.getPOSTagsMerged(2, 3);
        Set<String> mergedIntervalPOSTAGs = parseTree.getIntervalPOSTagsMerged(5, 5);

        System.out.println("Merged postags : " + mergedPOSTAGs);
        System.out.println("Merged interval postags : " + mergedIntervalPOSTAGs);

        Assert.assertEquals(nextTokens.contains(5), true);

        Assert.assertEquals(mergedPOSTAGs, "JJ VBZ NNP");

    }

}
