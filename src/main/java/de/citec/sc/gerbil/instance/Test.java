/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.gerbil.instance;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author sherzod
 */
public class Test {
    public static void main(String[] args) throws IOException {
        String quesionString  = "Who created Goofy?";
        String queryString  = "SELECT DISTINCT ?uri WHERE { <http://dbpedia.org/resource/Minecraft> <http://dbpedia.org/ontology/developer>  ?uri . }";
        String languageString = "en";
        
        QALDInstance qaldInstance = Convert.convert(quesionString, queryString, languageString);
        
         ObjectMapper mapper = new ObjectMapper();
         mapper.writeValue(new File("test.json"), qaldInstance);
         
         
         
    }
}
