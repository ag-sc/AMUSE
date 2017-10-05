/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.utils;

import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.corpus.QALDCorpus;
import de.citec.sc.main.Main;
import static de.citec.sc.main.Main.lang;
import de.citec.sc.qald.QALDCorpusLoader;
import de.citec.sc.query.CandidateRetriever;
import de.citec.sc.query.CandidateRetrieverOnLucene;
import de.citec.sc.query.CandidateRetrieverOnMemory;
import de.citec.sc.query.Instance;
import de.citec.sc.query.ManualLexicon;
import de.citec.sc.query.Search;
import de.citec.sc.wordNet.WordNetAnalyzer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author sherzod
 */
public class EntityExtractorUtils {

    public static void main(String[] args) {
        CandidateRetriever retriever = new CandidateRetrieverOnLucene(false, "luceneIndex");

        QALDCorpus corpus = QALDCorpusLoader.load(QALDCorpusLoader.Dataset.webQuestionsTrain, false, false, false, true, false);
        
        System.out.println(retriever.getAllResources("grand bahama", 10, CandidateRetriever.Language.EN));
        System.out.println(retriever.getAllResources("bahamas", 10, CandidateRetriever.Language.EN));

        int count = 0;
        for (AnnotatedDocument doc : corpus.getDocuments()) {
            String text = doc.getQuestionString();
            
            System.out.println(text+"\n");
            List<String> ngrams1 = extractNamedEntities(text, 4, retriever, 0.85);
            List<String> ngrams2 = extractNamedEntities(text, 4, retriever, 0.65);

            Set<String> ngrams = new HashSet<>();
            ngrams.addAll(ngrams1);
            ngrams.addAll(ngrams2);
            
            
            for (String n : ngrams) {

                if (Stopwords.isStopWord(n)) {
                    continue;
                }

                List<Instance> instances = retriever.getAllResources(n, 10, CandidateRetriever.Language.EN);
                if (instances.isEmpty()) {
                    continue;
                }
                System.out.println(n);
                for (Instance i : instances) {
                    System.out.println("\t" + i);
                }
            }
            count ++;
            
            System.out.println("\n=================================================================================\n");
            if(count == 10){
                break;
            }
        }
    }

    public static List<String> extractNamedEntities(String text, int maxNgramSize, CandidateRetriever retriever, double minSimThreshold) {
        List<String> ngrams = new ArrayList<>();

        String[] unigrams = text.split("\\s");

        for (int i = 0; i < unigrams.length; i++) {

            for (int n = maxNgramSize; n > 0; n--) {

                if (i + n <= unigrams.length) {

                    String ngram = "";

                    for (int k = i; k < i + n; k++) {
                        ngram += unigrams[k] + " ";
                    }

                    ngram = StringPreprocessor.preprocess(ngram, CandidateRetriever.Language.EN);

                    if (Stopwords.isStopWord(ngram)) {
                        continue;
                    }

                    List<Instance> instances = retriever.getAllResources(ngram, 10, CandidateRetriever.Language.EN);
                    if (instances.isEmpty()) {
                        continue;
                    }

                    Instance instance = instances.get(0);
                    double similarity = StringSimilarityUtils.getSimilarityScore(ngram, instance.getUri());
                    if (similarity >= minSimThreshold) {
                        ngrams.add(ngram);

                        i = i + n;
                        break;
                    }

                }
            }
        }

        return ngrams;
    }
}
