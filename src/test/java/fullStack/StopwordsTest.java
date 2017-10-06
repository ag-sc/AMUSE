/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fullStack;

import de.citec.sc.query.CandidateRetriever;
import de.citec.sc.utils.Stopwords;
import junit.framework.Assert;
import org.junit.Test;

/**
 *
 * @author sherzod
 */
public class StopwordsTest {
    
    @Test
    public void test(){
        
        boolean isStopWord = Stopwords.isStopWord("the", CandidateRetriever.Language.EN);
        Assert.assertEquals(true, isStopWord);
    }
}
