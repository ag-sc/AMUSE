/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fullStack;

import de.citec.sc.utils.Stemmer;
import junit.framework.Assert;
import org.junit.Test;

/**
 *
 * @author sherzod
 */
public class StemmerTest {

    @Test
    public void test() {
        Stemmer stemmer = new Stemmer();

        String s1 = stemmer.process("created");
        String s2 = stemmer.process("creators");
        System.out.println(s1 + " --> created");
        System.out.println(s2 + " --> creators");

        Assert.assertEquals(true, s2.equals("creator"));

    }
}
