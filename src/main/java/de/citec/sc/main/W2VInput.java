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
import de.citec.sc.query.CandidateRetriever.Language;
import de.citec.sc.query.CandidateRetrieverOnLucene;
import de.citec.sc.query.CandidateRetrieverOnMemory;
import de.citec.sc.query.Search;
import de.citec.sc.utils.DBpediaEndpoint;
import de.citec.sc.utils.FileFactory;
import de.citec.sc.utils.Lemmatizer;
import de.citec.sc.utils.ProjectConfiguration;
import de.citec.sc.utils.Stopwords;
import de.citec.sc.wordNet.WordNetAnalyzer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author sherzod
 */
public class W2VInput {

    public static void main(String[] args) {

        System.out.println("Initialization process has started ....");

        CandidateRetriever retriever = new CandidateRetrieverOnLucene(false, "luceneIndex");

        WordNetAnalyzer wordNet = new WordNetAnalyzer("src/main/resources/WordNet-3.0/dict");

        Search.load(retriever, wordNet);
        Search.useMatoll(true);
        
        List<CandidateRetriever.Language> languages = new ArrayList<>();

        languages.add(CandidateRetriever.Language.EN);
        languages.add(CandidateRetriever.Language.DE);
        languages.add(CandidateRetriever.Language.ES);

        for (Language l : languages) {
            
            CandidateRetriever.Language lang = l;
            
            Set<String> tokens = new HashSet<>();
            
            boolean includeYAGO = true;
            boolean includeAggregation = true;
            boolean includeUNION = true;
            boolean onlyDBO = true;
            boolean isHybrid = false;

            QALDCorpus corpus1 = CorpusLoader.load(CorpusLoader.Dataset.qald6Test, includeYAGO, includeAggregation, includeUNION, onlyDBO, isHybrid);
            QALDCorpus corpus2 = CorpusLoader.load(CorpusLoader.Dataset.qald6Train, includeYAGO, includeAggregation, includeUNION, onlyDBO, isHybrid);

            corpus1.getDocuments().addAll(corpus2.getDocuments());

            for (AnnotatedDocument d1 : corpus1.getDocuments()) {

//                if (DBpediaEndpoint.isValidQuery(d1.getGoldQueryString(), false)) {

                    if (d1.getParse() != null) {
                        String before = d1.getParse().toString();

                        d1.getParse().mergeEdges();
                        d1.getParse().removeLoops();
                        d1.getParse().removePunctuations();

                        String after = d1.getParse().toString();

                        for (Integer nodeID : d1.getParse().getNodes().keySet()) {
                            String token = d1.getParse().getNodes().get(nodeID).toLowerCase();
                            String pos = d1.getParse().getPOSTag(nodeID);
                            
                            if(Stopwords.isStopWord(token, l)){
                                continue;
                            }
                            
                            if(pos.equals("NOUN") || pos.equals("VERB") || pos.equals("ADJ") || pos.equals("ADV")){
                                tokens.add(token);
                                Set<String> lemmas = Lemmatizer.lemmatize(token, l);
                                
                                for(String l1 : lemmas){
                                    tokens.add(l1.toLowerCase());
                                }
                            }
                        }
                    }

//                } else {
//                    System.out.println("Invalid query: " + d1.getQuestionString() + " Query: " + d1.getGoldQueryString().replace("\n", " "));
//                }
            }
            
            FileFactory.writeListToFile(l.name()+"_tokens.txt", tokens, false);
        }
    }
}
