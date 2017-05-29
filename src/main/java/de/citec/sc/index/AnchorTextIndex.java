/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.index;

import de.citec.sc.utils.FileFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author sherzod
 */
public class AnchorTextIndex {

    public static void main(String[] args) throws FileNotFoundException {
        
        if (args == null || args.length == 0) {
            args = new String[1];
            args[0] = "Wikipedia";
        }

        File outerFolder = new File(args[0]);
        File[] listOfLanguageFiles = outerFolder.listFiles();
        
        System.out.println("Detected "+listOfLanguageFiles.length+" language dirs.");

        for (File langFile : listOfLanguageFiles) {

            if (langFile.isDirectory()) {

                File[] listOfFolders = langFile.listFiles();

                System.out.println("Loading " + langFile.getName() + " with " + listOfFolders.length + " files ... ");

                Map<String, Integer> indexMap = new HashMap<>();

                int c = 0;

                for (File folder : listOfFolders) {
                    c++;
                    if (c % 10 == 0) {
                        System.out.println(c + " files are done.");
                    }
                    if (folder.isDirectory()) {
                        File[] listOfFiles = folder.listFiles();
                        for (File file : listOfFiles) {
                            if (file.isFile()) {
                                Map<String, Integer> partialIndexMap = getSurfaceForms(file);

                                //add each surface form
                                for (String surfaceForm : partialIndexMap.keySet()) {

                                    if (indexMap.containsKey(surfaceForm)) {
                                        indexMap.put(surfaceForm, indexMap.get(surfaceForm) + partialIndexMap.get(surfaceForm));
                                    } else {
                                        indexMap.put(surfaceForm, partialIndexMap.get(surfaceForm));
                                    }
                                }
                            }
                        }
                    }
                }
                
                String fileName = langFile.getName().replace(" ", "").replace("-json", "").trim() + "_resource_anchorFile.txt";
                
                String directory = "indexData";
                
                File dir = new File(directory);
                if(!dir.exists()){
                    dir.mkdir();
                }
                writeIndex(indexMap, directory+"/"+fileName);
            }
        }
    }

    private static void writeIndex(Map<String, Integer> indexMap, String filePath) {
        System.out.println("Saving the file ... " + indexMap.size());

        try {
            PrintStream p = new PrintStream(new File(filePath));

            int counter = 0;
            for (String s : indexMap.keySet()) {

                String k = s + "\t" + indexMap.get(s);
                p.println(k);

                counter++;

                if (counter % 100000 == 0) {
                    System.out.println(counter + "/" + indexMap.size() + " " + (counter / (double) indexMap.size()) + " are saved.");
                }
            }

            System.out.println("\nFile saved.");

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

    }

    private static Map<String, Integer> getSurfaceForms(File file) {

        Map<String, Integer> indexMap = new HashMap<>();

        JSONParser parser = new JSONParser();

        try {

            Set<String> content = FileFactory.readFile(file);

            for (String c : content) {
                Object obj = parser.parse(c);

                JSONObject jsonObject = (JSONObject) obj;

                // loop array
                JSONArray annotations = (JSONArray) jsonObject.get("annotations");

                for (Object o : annotations) {
                    JSONObject surfaceFormObject = (JSONObject) o;

                    String uri = (String) surfaceFormObject.get("uri");
                    String surfaceForm = (String) surfaceFormObject.get("surface_form");
                    

                    try {
                        uri = StringEscapeUtils.unescapeJava(uri);
                        uri = URLDecoder.decode(uri, "UTF-8");

                        surfaceForm = StringEscapeUtils.unescapeJava(surfaceForm);
                        surfaceForm = URLDecoder.decode(surfaceForm, "UTF-8");
                        
                        surfaceForm = surfaceForm.toLowerCase().trim();
                        uri = uri.trim();

                        String key = surfaceForm + "\t" + uri;

                        indexMap.put(key, indexMap.getOrDefault(key, 1) + 1);
                    } catch (Exception e) {
                    }
                }
            }

        } catch (org.json.simple.parser.ParseException ex) {
            Logger.getLogger(AnchorTextIndex.class.getName()).log(Level.SEVERE, null, ex);
        }

        return indexMap;
    }
}
