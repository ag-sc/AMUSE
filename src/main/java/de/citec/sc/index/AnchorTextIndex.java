/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.index;

import de.citec.sc.query.CandidateRetriever;
import de.citec.sc.utils.FileFactory;
import de.citec.sc.utils.StringPreprocessor;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URLDecoder;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author sherzod
 */
public class AnchorTextIndex {

    private static Map<String, String> interlanguageLinksMap;
    private static Map<String, String> redirectsMap;

    public static void main(String[] args) throws FileNotFoundException {

        interlanguageLinksMap = getInterlanguageLinks("dbpediaData/interlanguage_links_en.ttl.bz2");

        if (args == null || args.length == 0) {
            args = new String[1];
            args[0] = "Wikipedia";
        }

        File outerFolder = new File(args[0]);
        File[] listOfLanguageFiles = outerFolder.listFiles();

        System.out.println("Detected " + listOfLanguageFiles.length + " language dirs.");

        for (File langFile : listOfLanguageFiles) {

            if (langFile.isDirectory()) {

                File[] listOfFolders = langFile.listFiles();

                String language = langFile.getName().replace(" ", "").replace("-json", "").trim();

                CandidateRetriever.Language l = CandidateRetriever.Language.valueOf(language.toUpperCase());

                redirectsMap = getRedirects("dbpediaData/redirects_" + language.toLowerCase() + ".ttl.bz2", l);

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
                                Map<String, Integer> partialIndexMap = getSurfaceForms(file, l);

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
                if (!dir.exists()) {
                    dir.mkdir();
                }
                writeIndex(indexMap, directory + "/" + fileName);
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

    private static Map<String, Integer> getSurfaceForms(File file, CandidateRetriever.Language lang) {

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
                        uri = uri.trim();

                        if ((uri.contains("Category:") || uri.contains("Kategorie:") || uri.contains("Categoría:") || uri.contains("(disambiguation)")) || uri.contains ("Lista_") || uri.contains("Liste_") || uri.contains("List_")) {
                            continue;
                        }

                        if (uri.length() < 2) {
                            continue;
                        }

                        
                        if (redirectsMap.containsKey(uri)) {
                            uri = redirectsMap.get(uri);
                            
                            if (interlanguageLinksMap.containsKey(uri)) {
                                uri = interlanguageLinksMap.get(uri);
                            }
                        }

                        if (interlanguageLinksMap.containsKey(uri)) {
                            uri = interlanguageLinksMap.get(uri);
                            
                            if (redirectsMap.containsKey(uri)) {
                                uri = redirectsMap.get(uri);
                            }
                        }

                        surfaceForm = StringEscapeUtils.unescapeJava(surfaceForm);
                        surfaceForm = URLDecoder.decode(surfaceForm, "UTF-8");

                        surfaceForm = StringPreprocessor.preprocess(surfaceForm, lang);

                        if (surfaceForm.length() < 2) {
                            continue;
                        }

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

    private static BufferedReader getBufferedReaderForCompressedFile(String fileIn) throws CompressorException, FileNotFoundException {
        FileInputStream fin = new FileInputStream(fileIn);
        BufferedInputStream bis = new BufferedInputStream(fin);
        CompressorInputStream input = new CompressorStreamFactory().createCompressorInputStream(bis);
        BufferedReader br2 = new BufferedReader(new InputStreamReader(input));
        return br2;
    }

    private static Map<String, String> getInterlanguageLinks(String filePath) {
        Map<String, String> content = new HashMap<>();

        try {
            System.out.println("Reading interlanguage links file ...");
            BufferedReader br = getBufferedReaderForCompressedFile(filePath);
            String line;

            String patternStringDE = "^(?!(#))<http://dbpedia.org/resource/(.*?)>.*<http://de.dbpedia.org/resource/(.*?)>";
            String patternStringES = "^(?!(#))<http://dbpedia.org/resource/(.*?)>.*<http://es.dbpedia.org/resource/(.*?)>";
            Pattern patternDE = Pattern.compile(patternStringDE);
            Pattern patternES = Pattern.compile(patternStringES);

            while ((line = br.readLine()) != null) {

                Matcher m1 = patternDE.matcher(line);
                while (m1.find()) {
                    String uri = m1.group(2);
                    String langURI = m1.group(3);

                    uri = uri.replace("http://dbpedia.org/resource/", "");
                    langURI = langURI.replace("http://de.dbpedia.org/resource/", "");

                    try {
                        langURI = URLDecoder.decode(langURI, "UTF-8");
                        langURI = StringEscapeUtils.unescapeJava(langURI);
                        uri = StringEscapeUtils.unescapeJava(uri);
                        uri = URLDecoder.decode(uri, "UTF-8");

                        content.put(langURI, uri);

                    } catch (Exception e) {
                    }
                }

                Matcher m2 = patternES.matcher(line);
                while (m2.find()) {
                    String uri = m2.group(2);
                    String langURI = m2.group(3);

                    uri = uri.replace("http://dbpedia.org/resource/", "");
                    langURI = langURI.replace("http://es.dbpedia.org/resource/", "");

                    try {
                        langURI = URLDecoder.decode(langURI, "UTF-8");
                        langURI = StringEscapeUtils.unescapeJava(langURI);
                        uri = StringEscapeUtils.unescapeJava(uri);
                        uri = URLDecoder.decode(uri, "UTF-8");

                        content.put(langURI, uri);

                    } catch (Exception e) {
                    }
                }
            }
            br.close();
        } catch (Exception e) {
            System.err.println("Error reading the file: " + filePath + "\n" + e.getMessage());
        }

        return content;
    }

    private static Map<String, String> getRedirects(String filePath, CandidateRetriever.Language lang) {
        Map<String, String> content = new HashMap<>();

        try {
            System.out.println("Reading redirects file ...");
            String output = "";
            BufferedReader br = getBufferedReaderForCompressedFile(filePath);
            String line;
            String patternString = "^(?!(#))<http://dbpedia.org/resource/(.*?)>.*<http://dbpedia.org/resource/(.*?)>";

            switch (lang) {
                case DE:
                    patternString = "^(?!(#))<http://de.dbpedia.org/resource/(.*?)>.*<http://de.dbpedia.org/resource/(.*?)>";
                    break;
                case ES:
                    patternString = "^(?!(#))<http://es.dbpedia.org/resource/(.*?)>.*<http://es.dbpedia.org/resource/(.*?)>";
                    break;
            }

            Pattern pattern = Pattern.compile(patternString);
            while ((line = br.readLine()) != null) {

                Matcher m = pattern.matcher(line);
                while (m.find()) {
                    String r = m.group(2);
                    String o = m.group(3);

                    o = o.replace("http://dbpedia.org/resource/", "");
                    r = r.replace("http://dbpedia.org/resource/", "");

                    try {
                        o = URLDecoder.decode(o, "UTF-8");
                        o = StringEscapeUtils.unescapeJava(o);
                        r = StringEscapeUtils.unescapeJava(r);
                        r = URLDecoder.decode(r, "UTF-8");

                        if (interlanguageLinksMap.containsKey(o)) {
                            o = interlanguageLinksMap.get(o);
                        }

                        content.put(r, o);

                    } catch (Exception e) {
                    }
                }
            }
            br.close();
        } catch (Exception e) {
            System.err.println("Error reading the file: " + filePath + "\n" + e.getMessage());
        }

        return content;
    }
}
