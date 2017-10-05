/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.utils;



import org.apache.tika.language.LanguageIdentifier;

/**
 *
 * @author sherzod
 */
public class LanguageDetector {
    
    public static String detect(String text) {
        LanguageIdentifier identifier = new LanguageIdentifier(text);
        String language = "en";//identifier.getLanguage();
        
        //if it's not german or english then it has to be spanish
        if(!(language.equals("en") || language.equals("de"))){
            language = "es";
        }
        
        return language.toUpperCase();
    }
}
