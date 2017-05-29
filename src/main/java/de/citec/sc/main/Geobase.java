/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.main;

import de.citec.sc.utils.FileFactory;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author sherzod
 */
public class Geobase {

    public static void main(String[] args) {
        Set<String> data = FileFactory.readFile("src/main/resources/geobase/geobase880.txt");

        HashMap<String, Integer> map = new HashMap<>();

        String pattern = "\\w+((A|B|C)";

        for (String s : data) {

            String l = s.substring(s.indexOf("answer"));

//            System.out.println(s);
            l = l.replace("answer", "");
            l = l.replace(").", "");

//            System.out.println(l);
            String[] parts = l.split(",");

            for (String p : parts) {
                String property = "";
                if (p.contains("(A")) {
                    property = p.substring(0, p.indexOf("(A"));
                    property = p.replace("(A", "");

                }
                if (p.contains("(B")) {
                    property = p.substring(0, p.indexOf("(B"));
                    property = p.replace("(B", "");

                }

                if (p.contains("(C")) {
                    property = p.substring(0, p.indexOf("(C"));
                    property = p.replace("(C", "");
                }
                property = property.replace("(", "");
                property = property.replace(")", "");
                property = property.replace("\\+", "");
                property = property.trim();

                if (property.length() > 2 && !property.equals("const")) {

                    map.put(property, map.getOrDefault(property, 1) + 1);
//                    System.out.println(property + "  "+ p);
                }
//                System.out.println(p);
            }
        }

        int sum = 0;
        for (String k : map.keySet()) {
            System.out.println(k + "-> " + map.get(k));
            sum += map.get(k);
        }
        System.out.println(map.size());
        System.out.println("Sum : " + sum);
    }
}
