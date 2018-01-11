/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.main;

import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.corpus.QALDCorpus;
import de.citec.sc.qald.CorpusLoader;
import de.citec.sc.query.CandidateRetriever;
import de.citec.sc.query.CandidateRetrieverOnLucene;
import de.citec.sc.query.CandidateRetrieverOnMemory;
import de.citec.sc.query.Search;
import de.citec.sc.utils.ProjectConfiguration;
import de.citec.sc.wordNet.WordNetAnalyzer;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author sherzod
 */
public class SimpleQuestions {

    public static void main(String[] args) {
        CandidateRetriever retriever = null;

        if (ProjectConfiguration.getIndex().equals("lucene")) {
            retriever = new CandidateRetrieverOnLucene(false, "luceneIndex");
        } else {
            retriever = new CandidateRetrieverOnMemory("rawFiles");
        }

        WordNetAnalyzer wordNet = new WordNetAnalyzer("src/main/resources/WordNet-3.0/dict");

        Search.load(retriever, wordNet);

        //accepted postags
        Set<String> linkingValidPOSTags = new HashSet<>();
        linkingValidPOSTags.add("PROPN");
        linkingValidPOSTags.add("VERB");
        linkingValidPOSTags.add("NOUN");
        linkingValidPOSTags.add("ADJ");
        linkingValidPOSTags.add("ADV");
        linkingValidPOSTags.add("ADP");

        //get documents
        QALDCorpus c = CorpusLoader.load(CorpusLoader.Dataset.simpleQuestionsTrain, true, true, true, true, true);

        

    }
}
