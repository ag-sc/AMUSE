/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.utils;

import de.citec.sc.query.CandidateRetriever.Language;
import java.text.Normalizer;

/**
 *
 * @author sherzod
 */
public class StringPreprocessor {

    public static String preprocess(String token, Language lang) {
        String s = token;

        s = s.toLowerCase();

        //add to index
        if (s.contains(",")) {
            s = s.substring(0, s.indexOf(","));
        }
        if (s.contains("(") && s.contains(")")) {
            s = s.substring(0, s.indexOf("(")).trim();
        }

        switch (lang) {

            case DE:

                s = s.replaceAll("ö", "oe")
                        .replaceAll("ä", "ae")
                        .replaceAll("ü", "ue")
                        .replaceAll("ß", "ss");

                break;

            case EN:

                s = s.replaceAll("n't", " not")
                        .replaceAll("'re", " are")
                        .replaceAll("'s", " is")
                        .replaceAll("'m", " am");

                break;
                
            case ES:

                s = s.replaceAll("í", "i")
                        .replaceAll("é", "e")
                        .replaceAll("ó", "o")
                        .replaceAll("á", "a")
                        .replaceAll("ñ", "ny")
                        .replaceAll("ü", "ue")
                        .replaceAll("ú", "u");

                break;
        }

        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("\\p{M}", "");
        s = s.replaceAll("[!?,.;:'\"]", "");
        s = s.replaceAll("\\s+", " ");
        s = s.replaceAll("-", " ");
        s = s.replace("\\", "");
        
        s = s.replace("- ", "-");
        s = s.replace(" - ", "-");
        s = s.replace(" -", "-");

        s = s.trim();

        return s;
    }
}
