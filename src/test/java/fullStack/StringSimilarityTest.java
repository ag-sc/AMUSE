/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fullStack;

import junit.framework.Assert;
import net.ricecode.similarity.DiceCoefficientStrategy;
import net.ricecode.similarity.JaroStrategy;
import net.ricecode.similarity.LevenshteinDistanceStrategy;
import net.ricecode.similarity.StringSimilarityMeasures;
import org.junit.Test;

/**
 *
 * @author sherzod
 */
public class StringSimilarityTest {

    @Test
    public void test() {

        String s1 = "team national football";
        String s2 = "national team soccer";

        String s3 = "created";
        String s4 = "creator";

        double l1 = LevenshteinDistanceStrategy.score(s1, s2);
        double l2 = LevenshteinDistanceStrategy.score(s3, s4);

        double d1 = DiceCoefficientStrategy.score(s1, s2);
        double d2 = DiceCoefficientStrategy.score(s3, s4);

        //average of levenshtein and dice coefficient
        double a1 = StringSimilarityMeasures.score(s1, s2);
        double a2 = StringSimilarityMeasures.score(s3, s4);

        //scores get higher with mixing different similarity measures
        System.out.println(s1 + " --> " + s2 + " LevenshteinDistanceStrategy sim score: " + l1);
        System.out.println(s3 + " --> " + s3 + " LevenshteinDistanceStrategy sim score: " + l2);

        System.out.println(s1 + " --> " + s2 + " DiceCoefficientStrategy sim score: " + d1);
        System.out.println(s3 + " --> " + s3 + " DiceCoefficientStrategy sim score: " + d2);

        System.out.println(s1 + " --> " + s2 + " Mixed sim score: " + a1);
        System.out.println(s3 + " --> " + s3 + " Mixed sim score: " + a2);

        Assert.assertEquals(true, a1 > 0.56);
        Assert.assertEquals(true, a2 > 0.66);

    }
}
