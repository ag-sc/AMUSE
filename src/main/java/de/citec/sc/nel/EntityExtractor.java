/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.nel;

import de.citec.sc.query.CandidateRetriever;
import de.citec.sc.query.Instance;
import de.citec.sc.utils.SortUtils;
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
public class EntityExtractor {

    protected static List<EntityAnnotation> getAnnotations(List<EntityAnnotation> nGrams, String text, double firstPassThreshold, Set<String> coveredTypes, CandidateRetriever retriever) {
        List<EntityAnnotation> annotations = new ArrayList<>();
        for (EntityAnnotation e : nGrams) {
            String searchText = e.getSpannedText(text);
            
            //get the DBpedia candidates
            List<Instance> instances = retriever.getAllResources(searchText, 10, CandidateRetriever.Language.EN);
            if (instances.isEmpty()) {
                continue;
            }
            
//            HashMap<String, Double> candidates = new LinkedHashMap<>();
//
//            int sum = 0;
//            for (Instance i : instances) {
//                sum += i.getFreq();
//            }
//            //compute score = P(e) * P(m|e)
//            for (Instance i : instances) {
//                double p_m_e = StringSimilarityUtils.getSimilarityScore(searchText, i.getUri());
//                double p_e = i.getFreq() / (double) sum;
//
//                double score = p_e * p_m_e;
//                candidates.put(i.getUri(), score);
//            }
//
//            candidates = SortUtils.sortByDoubleValue(candidates);
//
//            for (String key : candidates.keySet()) {
//                Set<String> v1 = new HashSet<>();
//                v1.add(key);
//
//                EntityAnnotation anno = new EntityAnnotation();
//                anno.setSpan(e.getSpan());
//                anno.setType("DBPedia_Resource");
//                anno.setValues(v1);
//                anno.setProvenance(new Provenance("EntityExtractor", candidates.get(key)));
//                annotations.add(anno);
//                
//                if(candidates.get(key) >= firstPassThreshold){
//                    break;
//                }
//            }

            Set<String> values = new HashSet<>();
            Instance instance = instances.get(0);
            //check the string similarity
            double similarity = StringSimilarityUtils.getSimilarityScore(searchText, instance.getUri());
            if (similarity >= firstPassThreshold) {

                values.add(instance.getUri());

                EntityAnnotation anno = new EntityAnnotation();
                anno.setSpan(e.getSpan());
                anno.setType("DBPedia_Resource");
                anno.setValues(values);
                anno.setProvenance(new Provenance("EntityExtractor", similarity));
                annotations.add(anno);

        }
        }

        return annotations;
    }

}
