/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fullStack;

import de.citec.sc.qald.SPARQLParser;
import java.util.List;
import junit.framework.Assert;
import org.junit.Test;

/**
 *
 * @author sherzod
 */
public class SPARQLParserTest {

    @Test
    public void test() {
        List<String> uris = SPARQLParser.extractURIsFromQuery("SELECT DISTINCT ?uri WHERE {  <http://dbpedia.org/resource/Wikipedia> <http://dbpedia.org/ontology/author> ?uri . } ");

        System.out.println("Extracted URIs: " + uris);
        Assert.assertEquals(true, uris.contains("http://dbpedia.org/resource/Wikipedia"));
        Assert.assertEquals(true, uris.contains("http://dbpedia.org/ontology/author"));

    }
}
