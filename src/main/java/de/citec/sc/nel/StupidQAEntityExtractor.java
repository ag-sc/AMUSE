/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.nel;

import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.main.PageRankScorer;
import de.citec.sc.parser.DependencyParse;
import de.citec.sc.query.CandidateRetriever;
import de.citec.sc.query.Instance;
import de.citec.sc.utils.SortUtils;
import de.citec.sc.utils.Stopwords;
import de.citec.sc.utils.StringSimilarityUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author sherzod
 */
public class StupidQAEntityExtractor {

    protected static List<EntityAnnotation> getAnnotations(AnnotatedDocument document, CandidateRetriever retriever, Set<String> linkingValidPOSTags) {
        List<EntityAnnotation> annotations = new ArrayList<>();
        
        HashMap<String, Double> candidates = new LinkedHashMap<>();
        
        DependencyParse parseTree = document.getParse();
        
        for (Integer nodeId : parseTree.getNodes().keySet()) {
            String searchText = parseTree.getNodes().get(nodeId);
            
            String postag = parseTree.getPOSTag(nodeId);
            
            if(!linkingValidPOSTags.contains(postag)){
                continue;
            }
            
            if(Stopwords.isStopWord(searchText, CandidateRetriever.Language.EN)){
                continue;
            }

            //get the DBpedia candidates
            List<Instance> instances = retriever.getAllResources(searchText, 10, CandidateRetriever.Language.EN);
            if (instances.isEmpty()) {
                continue;
            }
            
            

            int sum = 0;
            double sumPageRankScores = 0;
            for (Instance i : instances) {
                sum += i.getFreq();
                double p_e = PageRankScorer.getPageRankScore(i.getUri());
                
                if(Double.isNaN(p_e)){
                    p_e = 0;
                }
                
                sumPageRankScores += p_e;
            }
            
            
            //compute score = P(e) * P(m|e)
            for (Instance i : instances) {
//                double p_m_e = StringSimilarityUtils.getSimilarityScore(searchText, i.getUri());

                //normalized mention to uri score
                double p_m_e = i.getFreq()/(double) sum; 
                //page rank score
                double p_e = PageRankScorer.getPageRankScore(i.getUri()) / sumPageRankScores;

                double score = p_e * p_m_e;
                
                if(Double.isNaN(score)){
                    score = 0;
                }
                
                candidates.put(i.getUri(), score);
            }

            candidates = SortUtils.sortByDoubleValue(candidates);
            
            
            int beginIndex = document.getQuestionString().indexOf(searchText);
            int endIndex  = beginIndex + searchText.length();
            Span span = new Span(beginIndex, endIndex);

            for (String key : candidates.keySet()) {
                Set<String> v1 = new HashSet<>();
                v1.add(key);

                EntityAnnotation anno = new EntityAnnotation();
                anno.setSpan(span);
                anno.setType("DBPedia_Resource");
                anno.setValues(v1);
                anno.setProvenance(new Provenance("EntityExtractor", candidates.get(key)));
                annotations.add(anno);
            }

//            Set<String> values = new HashSet<>();
//            Instance instance = instances.get(0);
//            //check the string similarity
//            double similarity = StringSimilarityUtils.getSimilarityScore(searchText, instance.getUri());
//            if (similarity >= firstPassThreshold) {
//
//                values.add(instance.getUri());
//
//                EntityAnnotation anno = new EntityAnnotation();
//                anno.setSpan(e.getSpan());
//                anno.setType("DBPedia_Resource");
//                anno.setValues(values);
//                anno.setProvenance(new Provenance("EntityExtractor", similarity));
//                annotations.add(anno);
//
//            }
        }

        return annotations;
    }

}
