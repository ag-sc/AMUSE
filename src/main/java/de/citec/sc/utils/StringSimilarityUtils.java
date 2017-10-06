/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.utils;

import de.citec.sc.main.Main;
import de.citec.sc.query.CandidateRetriever;
import de.citec.sc.query.DBpediaLabelRetriever;
import net.ricecode.similarity.StringSimilarityMeasures;

/**
 *
 * @author sherzod
 */
public class StringSimilarityUtils {
    /**
     * levenstein sim
     */
    public static double getSimilarityScore(String node, String uri) {

        String label = DBpediaLabelRetriever.getLabel(uri, CandidateRetriever.Language.valueOf(ProjectConfiguration.getLanguage()));

        //compute levenstein edit distance similarity and normalize
        final double weightedEditSimilarity = StringSimilarityMeasures.score(label, node);

        return weightedEditSimilarity;
    }
}
