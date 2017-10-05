/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.utils;

import de.citec.sc.variable.URIVariable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author sherzod
 */
public class StateEntityMap {
    private static Map<String, Set<URIVariable>> doc2EntityMap = new HashMap<>();
    
    public static Set<URIVariable> getEntities(String doc){
        if(doc2EntityMap.containsKey(doc)){
            return doc2EntityMap.get(doc);
        }
        
        return new HashSet<>();
    }
    
    public static void addEntities(String doc, Set<URIVariable> entities){
        doc2EntityMap.put(doc, entities);
    }
            
}
