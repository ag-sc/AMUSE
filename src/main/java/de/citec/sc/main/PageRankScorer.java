/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.main;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * @author sherzod
 */
public class PageRankScorer {

    private static Map<String, Double> pageRankMap;
    private static final String path = "pagerank.csv";
//    private static final String path = "sample_pagerank.csv";

    private static void load() {
        System.out.println("Loading PageRank scores into the memory.");

        if (pageRankMap == null) {

            pageRankMap = new ConcurrentHashMap<>(8000000);
            try {
                FileInputStream fstream = new FileInputStream(path);
                BufferedReader indexMappingReader = new BufferedReader(new InputStreamReader(fstream, "UTF-8"));

                // BufferedReader indexMappingReader = new BufferedReader(new
                // FileReader(new File(keyFiles)));
                String line = "";
                while ((line = indexMappingReader.readLine()) != null) {
                    String[] data = line.split("\t");
                    String uri = data[1];

                    Double v = Double.parseDouble(data[2]);

                    if (!(uri.contains("Category:") || uri.contains("(disambiguation)") || uri.contains("File:"))) {

                        uri = StringEscapeUtils.unescapeJava(uri);

                        try {
                            uri = URLDecoder.decode(uri, "UTF-8");
                        } catch (Exception e) {
                        }

                        pageRankMap.put(uri, v);

                    }
                }
                indexMappingReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("Loading finished.");
    }

    public static double getPageRankScore(String uri) {
        if(pageRankMap == null){
            load();
        }
        
        uri = uri.replace("http://dbpedia.org/resource/", "");
        
        if(pageRankMap.containsKey(uri)){
            return pageRankMap.get(uri);
        }
        
        return 0;
    }
}
