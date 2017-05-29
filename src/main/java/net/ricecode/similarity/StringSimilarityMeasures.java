/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.ricecode.similarity;

/**
 *
 * @author sherzod
 */
public class StringSimilarityMeasures implements SimilarityStrategy {

    public StringSimilarityMeasures() {
    }

    /**
     * @param first
     * @param second
     *
     * computes F1 measure of DiceCoefficient Similarity and Levenshtein
     * Distance Similarity combined given two strings
     *
     * @return double score
     */
    public static double score(String first, String second) {
        double l = LevenshteinDistanceStrategy.score(first, second);

        double d = DiceCoefficientStrategy.score(first, second);

        double levenshteinContribution = 0.8, diceCoefficientContribution = 0.2;

        if (l == 1.0 || d == 1.0) {
            return 1.0;
        }

        //multiple lines, then use dice coefficient
        if (first.split(" ").length > 2 && second.split(" ").length > 2) {

            levenshteinContribution = 0.1;

            diceCoefficientContribution = 0.9;
        }

        //dice coefficient works better with multiple words and mixed order
        //prefer dice coefficient over levenshtein
        //in other cases prefer levenshtein
        double score = (d * diceCoefficientContribution) + (l * levenshteinContribution);

        if (Double.isNaN(score)) {
            score = 0;
        }

        return score;
    }

}
