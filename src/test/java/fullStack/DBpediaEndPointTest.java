/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fullStack;

import de.citec.sc.utils.DBpediaEndpoint;
import de.citec.sc.utils.FileFactory;
import java.util.List;
import junit.framework.Assert;
import org.junit.Test;

/**
 *
 * @author sherzod
 */
public class DBpediaEndPointTest {

    @Test
    public void test() {
        String range = DBpediaEndpoint.getRange("http://dbpedia.org/ontology/birthPlace");
        String domain = DBpediaEndpoint.getDomain("http://dbpedia.org/ontology/birthPlace");

        System.out.println("Range: " + range);
        System.out.println("Domain: " + domain);
        Assert.assertEquals(true, range.equals("http://dbpedia.org/ontology/Place"));
        Assert.assertEquals(true, domain.equals("http://dbpedia.org/ontology/Person"));

//        String query = "SELECT DISTINCT ?p ?l WHERE {?p a <http://www.w3.org/2002/07/owl#DatatypeProperty>. ?p <http://www.w3.org/2000/01/rdf-schema#label> ?l.}";
//
//        List<String> results = DBpediaEndpoint.runQuery(query);
//
//        String content1 = "";
//        for (String s : results) {
//            content1 += s + "\n";
//        }
//
//        String query2 = "SELECT DISTINCT ?p ?l WHERE {?p a <http://www.w3.org/2002/07/owl#ObjectProperty>. ?p <http://www.w3.org/2000/01/rdf-schema#label> ?l.}";
//
//        List<String> results2 = DBpediaEndpoint.runQuery(query2);
//
//        String content2 = "";
//        for (String s : results2) {
//            content2 += s + "\n";
//        }
//        String query3 = "SELECT DISTINCT ?p ?l WHERE {?p a <http://www.w3.org/2002/07/owl#Class>. ?p <http://www.w3.org/2000/01/rdf-schema#label> ?l.}";
//
//        List<String> results3 = DBpediaEndpoint.runQuery(query3);
//
//        String content3 = "";
//        for (String s : results3) {
//            content3 += s + "\n";
//        }
//
//        FileFactory.writeListToFile("dataTypeProperties.txt", content1, false);
//        FileFactory.writeListToFile("objectProperties.txt", content2, false);
//        FileFactory.writeListToFile("classes.txt", content3, false);
    }
}
