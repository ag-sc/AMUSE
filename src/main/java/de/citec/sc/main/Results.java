/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.main;

import de.citec.sc.utils.FileFactory;
import de.citec.sc.utils.SortUtils;
import java.io.File;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author sherzod
 */
public class Results {

    public static void main(String[] args) {

        File outerFolder = new File("testResult");
        File[] listOfLanguageFiles = outerFolder.listFiles();

        System.out.println("Detected " + listOfLanguageFiles.length + " language dirs.");

        HashMap<String, Double> deNEL = new HashMap<>();
        HashMap<String, Double> enNEL = new HashMap<>();
        HashMap<String, Double> esNEL = new HashMap<>();

        HashMap<String, Double> deQA = new HashMap<>();
        HashMap<String, Double> enQA = new HashMap<>();
        HashMap<String, Double> esQA = new HashMap<>();

        for (File langFile : listOfLanguageFiles) {

            if (langFile.isFile() && langFile.getName().startsWith("result_")) {
                Set<String> content = FileFactory.readFile(langFile);

                if (langFile.getName().contains("NEL")) {
                    for (String s : content) {
                        if (s.startsWith("Correct predictions")) {
                            String result = s.substring(s.indexOf("=") + 1).trim();
                            double value = Double.parseDouble(result);

                            if (langFile.getName().contains("Language_DE")) {
                                String key = langFile.getName().substring(langFile.getName().indexOf("Manual"));

                                deNEL.put(key, value);
                            } else if (langFile.getName().contains("Language_EN")) {
                                String key = langFile.getName().substring(langFile.getName().indexOf("Manual"));

                                enNEL.put(key, value);
                            } else if (langFile.getName().contains("Language_ES")) {
                                String key = langFile.getName().substring(langFile.getName().indexOf("Manual"));

                                esNEL.put(key, value);
                            }

                            break;
                        }
                    }
                } else if (langFile.getName().contains("QA")) {
                    for (String s : content) {
                        if (s.startsWith("Correct predictions")) {
                            String result = s.substring(s.indexOf("=") + 1).trim();
                            double value = Double.parseDouble(result);

                            if (langFile.getName().contains("Language_DE")) {
                                String key = langFile.getName().substring(langFile.getName().indexOf("Manual"));

                                deQA.put(key, value);
                            } else if (langFile.getName().contains("Language_EN")) {
                                String key = langFile.getName().substring(langFile.getName().indexOf("Manual"));

                                enQA.put(key, value);
                            } else if (langFile.getName().contains("Language_ES")) {
                                String key = langFile.getName().substring(langFile.getName().indexOf("Manual"));

                                esQA.put(key, value);
                            }

                            break;
                        }
                    }
                }

            }
        }

        deNEL = SortUtils.sortByDoubleValue(deNEL);
        esNEL = SortUtils.sortByDoubleValue(esNEL);
        enNEL = SortUtils.sortByDoubleValue(enNEL);

        deQA = SortUtils.sortByDoubleValue(deQA);
        esQA = SortUtils.sortByDoubleValue(esQA);
        enQA = SortUtils.sortByDoubleValue(enQA);

        System.out.println("NEL German : ");
        for (String s : deNEL.keySet()) {
            System.out.println(s + " " + deNEL.get(s));
        }

        System.out.println("\nNEL English : ");
        for (String s : enNEL.keySet()) {
            System.out.println(s + " " + enNEL.get(s));
        }

        System.out.println("\nNEL Spanish : ");
        for (String s : esNEL.keySet()) {
            System.out.println(s + " " + esNEL.get(s));
        }

        System.out.println("\nQA German : ");
        for (String s : deQA.keySet()) {
            System.out.println(s + " " + deQA.get(s));
        }

        System.out.println("\nQA English : ");
        for (String s : enQA.keySet()) {
            System.out.println(s + " " + enQA.get(s));
        }

        System.out.println("\nQA Spanish : ");
        for (String s : esQA.keySet()) {
            System.out.println(s + " " + esQA.get(s));
        }

    }
}