/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.ricecode.similarity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author sherzod
 */
public class CosineSimilarity {

    public static void main(String[] args) {
        HashMap<String, Double> vector1 = new HashMap<>();
        HashMap<String, Double> vector2 = new HashMap<>();

        CosineSimilarity c = new CosineSimilarity();

        vector1.put("A", 1.1d);
        vector1.put("B", 0.4d);
        vector1.put("C", 1.8d);
        vector1.put("D", 0.78d);

        vector2.put("A", 2.1d);
        vector2.put("B", 0.24d);
        vector2.put("E", 0.83d);
        vector2.put("F", 1.13d);
        System.out.println("Vector 1 = " + vector1);
        System.out.println("Vector 2 = " + vector2);
        System.out.println("Cosine similarity  = " + c.cosineDistance(vector1, vector2));

    }

    /**
     * This method calculates the similarity of two given vectors using the
     * cosine-similarity method.
     *
     * @param vector1 the first vector.
     * @param vector2 the second vector.
     * @return a score between 0 and 1 that measures the similarity of the given
     * vectors.
     */
    public double cosineDistance(Map<String, Double> vector1, Map<String, Double> vector2) {

        /*
         * A set of all words that are in vector1 and vector2.
         */
        Set<String> words = new HashSet<String>();
        words.addAll(vector1.keySet());
        words.addAll(vector2.keySet());

        /*
         * numerator of the cosine-soimilarity function.
         */
        double numerator = 0;

        /*
         * first denominator value.
         */
        double d1 = 0;

        /*
         * second denominator value.
         */
        double d2 = 0;

        /*
         * Loop over all words in both vectors and adjust the numerator and both
         * denominator values.
         */
        for (final String word : words) {
            boolean inV1 = false;
            boolean inV2 = false;
            if (inV1 = vector1.keySet().contains(word)) {
                d1 += Math.pow(vector1.get(word), 2);
            }

            if (inV2 = vector2.keySet().contains(word)) {
                d2 += Math.pow(vector2.get(word), 2);
            }

            if (inV1 && inV2) {
                numerator += vector1.get(word) * vector2.get(word);
            }

        }
        final double denominator = Math.sqrt(d1) * Math.sqrt(d2);
        final double result = numerator / denominator;

        if (Double.isNaN(result)) {
            return 0;
        }

        return result;
    }

}
