/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.main;

import de.citec.sc.utils.FileFactory;
import de.citec.sc.utils.SortUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 *
 * @author sherzod
 */
public class ObjectivePerformance {

    public static void main(String[] args) {

        File outerFolder = new File("trainResult");
        File[] listOfLanguageFiles = outerFolder.listFiles();

        System.out.println("Detected " + listOfLanguageFiles.length + " language dirs.");

        HashMap<String, String> deNEL = new HashMap<>();
        HashMap<String, String> enNEL = new HashMap<>();
        HashMap<String, String> esNEL = new HashMap<>();

        HashMap<String, String> deQA = new HashMap<>();
        HashMap<String, String> enQA = new HashMap<>();
        HashMap<String, String> esQA = new HashMap<>();
        
        String dataset = "qald6Train";

        for (File langFile : listOfLanguageFiles) {

            if (langFile.isFile() && langFile.getName().startsWith("unParsedInstances_") && langFile.getName().contains(dataset)) {
                Set<String> content = FileFactory.readFile(langFile);

                List<String> contentAsList = new ArrayList<>(content);
                if (langFile.getName().contains("NEL")) {
                    for (int i = 0; i < contentAsList.size(); i++) {

                        String s = contentAsList.get(i);

                        if (s.startsWith("ID:")) {
                            String question = contentAsList.get(i + 1);

                            if (langFile.getName().contains("Language_DE")) {

                                deNEL.put(question, "DE");
                            } else if (langFile.getName().contains("Language_EN")) {

                                enNEL.put(question, "EN");
                            } else if (langFile.getName().contains("Language_ES")) {

                                esNEL.put(question, "ES");
                            }

                        }
                    }
                } else if (langFile.getName().contains("QA")) {
                    for (int i = 0; i < contentAsList.size(); i++) {

                        String s = contentAsList.get(i);

                        if (s.startsWith("ID:")) {
                            String question = contentAsList.get(i + 1);

                            if (langFile.getName().contains("Language_DE")) {

                                deQA.put(question, "DE");
                            } else if (langFile.getName().contains("Language_EN")) {

                                enQA.put(question, "EN");
                            } else if (langFile.getName().contains("Language_ES")) {

                                esQA.put(question, "ES");
                            }

                        }
                    }
                }

            }
        }

        System.out.println("NEL vs QA\n");
        System.out.println("German : ");
        for (String s : deNEL.keySet()) {
            if (!deQA.containsKey(s)) {
                System.out.println("\t" + s);
            }
        }

        System.out.println("\n\nEnglish : ");
        for (String s : enNEL.keySet()) {
            if (!enQA.containsKey(s)) {
                System.out.println("\t" + s);
            }
        }

        System.out.println("\n\nSpanish : ");
        for (String s : esNEL.keySet()) {
            if (!esQA.containsKey(s)) {
                System.out.println("\t" + s);
            }
        }
        
        
        System.out.println("\nQA vs NEL\n");
        System.out.println("German : ");
        for (String s : deQA.keySet()) {
            if (!deNEL.containsKey(s)) {
                System.out.println("\t" + s);
            }
        }

        System.out.println("\n\nEnglish : ");
        for (String s : enQA.keySet()) {
            if (!enNEL.containsKey(s)) {
                System.out.println("\t" + s);
            }
        }

        System.out.println("\n\nSpanish : ");
        for (String s : esQA.keySet()) {
            if (!esNEL.containsKey(s)) {
                System.out.println("\t" + s);
            }
        }

    }
}
