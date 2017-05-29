/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.query;

import de.citec.sc.query.CandidateRetriever.Language;
import de.citec.sc.utils.FileFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author sherzod
 */
public class DBpediaLabelRetriever {

    private static Map<String, String> propertyMap;
    private static Map<String, String> classMap;

    private static boolean loaded = false;

    public static void main(String[] args) {
        System.out.println(getLabel("http://dbpedia.org/ontology/band", Language.EN));
    }

    public static void load(Language lang) {
        propertyMap = new HashMap<>();
        classMap = new HashMap<>();

        Set<String> objectProperties = FileFactory.readFile("rawFiles/" + lang.name().toLowerCase() + "/predicateFiles/objectProperties_" + lang.name().toLowerCase() + ".txt");
        Set<String> dataTypeProperties = FileFactory.readFile("rawFiles/" + lang.name().toLowerCase() + "/predicateFiles/dataTypeProperties_" + lang.name().toLowerCase() + ".txt");
        Set<String> classes = FileFactory.readFile("rawFiles/" + lang.name().toLowerCase() + "/classFiles/classes_" + lang.name().toLowerCase() + ".txt");

        for (String o : objectProperties) {
            String uri = o.split("\t")[0];
            String label = o.split("\t")[1];

            label = label.substring(0, label.indexOf("@"));

            if (!propertyMap.containsKey(uri)) {
                propertyMap.put(uri, label);
            }
        }
        for (String o : dataTypeProperties) {
            String uri = o.split("\t")[0];
            String label = o.split("\t")[1];

            label = label.substring(0, label.indexOf("@"));

            if (!propertyMap.containsKey(uri)) {
//                try {
                    propertyMap.put(uri, label);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    int z = 1;
//                }
            }
        }
        for (String o : classes) {

            String uri = o.split("\t")[0];
            String label = o.split("\t")[1];

            label = label.substring(0, label.indexOf("@"));

            if (!classMap.containsKey(uri)) {
                classMap.put(uri, label);
            }
        }
        
        loaded = true;
    }

    public static String getLabel(String uri, Language lang) {

        if (!loaded) {
            load(lang);
        }

        String label = "";

        if (uri.contains("###")) {
            uri = uri.substring(uri.indexOf("###")).replace("###", "");

            if (classMap.containsKey(uri)) {
                label = classMap.get(uri);
            } else {
                label = convertURI2Label(uri);
            }
        } else {
            if (propertyMap.containsKey(uri)) {
                label = propertyMap.get(uri);
            } else {
                label = convertURI2Label(uri);
            }
        }

        return label;
    }

    private static String convertURI2Label(String uri) {

        uri = uri.replace("http://dbpedia.org/resource/", "");
        uri = uri.replace("http://dbpedia.org/property/", "");
        uri = uri.replace("http://dbpedia.org/ontology/", "");
        uri = uri.replace("http://www.w3.org/1999/02/22-rdf-syntax-ns#type###", "");

        uri = uri.replaceAll("@en", "");
        uri = uri.replaceAll("\"", "");
        uri = uri.replaceAll("_", " ");

        //replace capital letters with space
        //to tokenize compount classes e.g. ProgrammingLanguage => Programming Language
        String temp = "";
        for (int i = 0; i < uri.length(); i++) {
            String c = uri.charAt(i) + "";
            if (c.equals(c.toUpperCase())) {
                temp += " ";
            }
            temp += c;
        }

        temp = temp.replaceAll("\\s+", " ");
        uri = temp.trim().toLowerCase();

        return uri;
    }
}
