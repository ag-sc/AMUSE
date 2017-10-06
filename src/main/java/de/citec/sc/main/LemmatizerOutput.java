/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.main;

import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.corpus.QALDCorpus;
import de.citec.sc.qald.QALDCorpusLoader;
import de.citec.sc.query.CandidateRetriever;
import de.citec.sc.query.CandidateRetriever.Language;
import de.citec.sc.query.CandidateRetrieverOnLucene;
import de.citec.sc.query.Search;
import de.citec.sc.utils.FileFactory;
import de.citec.sc.utils.Lemmatizer;
import de.citec.sc.utils.Stopwords;
import de.citec.sc.wordNet.WordNetAnalyzer;
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
public class LemmatizerOutput {

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
            
            boolean includeYAGO = true;
            boolean includeAggregation = true;
            boolean includeUNION = true;
            boolean onlyDBO = true;
            boolean isHybrid = false;

            Map<String, Set<String>> lemmaMap = new HashMap<>();
            
            QALDCorpus corpus1 = QALDCorpusLoader.load(QALDCorpusLoader.Dataset.qald6Test, includeYAGO, includeAggregation, includeUNION, onlyDBO, isHybrid);
            QALDCorpus corpus2 = QALDCorpusLoader.load(QALDCorpusLoader.Dataset.qald6Train, includeYAGO, includeAggregation, includeUNION, onlyDBO, isHybrid);

            corpus1.getDocuments().addAll(corpus2.getDocuments());

            for (AnnotatedDocument d1 : corpus1.getDocuments()) {

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
                                
                                
                                Set<String> lemmas = Lemmatizer.lemmatize(token, l);
                                
                                if(lemmaMap.containsKey(token)){
                                    Set<String> old = lemmaMap.get(token);
                                    
                                    old.addAll(lemmas);
                                    
                                    lemmaMap.put(token, old);
                                }
                                else{
                                    lemmaMap.put(token, lemmas);
                                }
                            }
                        }
                    }
            }
            
            String content = "";
            for(String token : lemmaMap.keySet()){
                content += token;
                for(String lemma : lemmaMap.get(token)){
                    content+= "\t" + lemma;
                }
                content += "\n";
            }
            
            FileFactory.writeListToFile(l.name()+"_lemmas.txt", content, false);
        }
    }
}
