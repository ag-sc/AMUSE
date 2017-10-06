/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fullStack;

import de.citec.sc.query.CandidateRetriever;
import de.citec.sc.utils.Lemmatizer;
import java.io.IOException;
import java.util.Set;
import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author sherzod
 */
public class LemmatizerTest {

    @Test
    public void test() throws IOException {

        Set<String> lemmas1 = Lemmatizer.lemmatize("gespielt", CandidateRetriever.Language.DE);
        System.out.println(lemmas1);
        Assert.assertEquals(true, lemmas1.contains("spielen"));
        
        
        Set<String> lemmas2 = Lemmatizer.lemmatize("people", CandidateRetriever.Language.EN);
        System.out.println(lemmas2);
        Assert.assertEquals(true, lemmas2.contains("person"));
        
        
        Set<String> lemmas3 = Lemmatizer.lemmatize("tocaba", CandidateRetriever.Language.ES);
        System.out.println(lemmas3);
        Assert.assertEquals(true, lemmas3.contains("tocar"));
    }
}
