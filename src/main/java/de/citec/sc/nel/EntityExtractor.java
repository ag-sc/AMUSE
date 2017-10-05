/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.nel;

import de.citec.sc.query.CandidateRetriever;
import de.citec.sc.query.Instance;
import de.citec.sc.utils.Stopwords;
import de.citec.sc.utils.StringPreprocessor;
import de.citec.sc.utils.StringSimilarityUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
