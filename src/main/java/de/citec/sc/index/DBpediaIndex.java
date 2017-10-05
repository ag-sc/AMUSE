/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.index;

import de.citec.sc.query.CandidateRetriever.Language;
import de.citec.sc.utils.FileFactory;
import de.citec.sc.utils.StringPreprocessor;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * @author sherzod
 */
public class DBpediaIndex {

    private static Map<String, Integer> indexMap;
    private static Map<String, String> redirectsMap;
    private static Map<String, String> interlanguageLinksMap;
    private static Set<String> propertySet;

    public static void main(String[] args) {

        interlanguageLinksMap = getInterlanguageLinks("dbpediaData/interlanguage_links_en.ttl.bz2");
        propertySet = FileFactory.readFile("dbpediaData/dbpediaProperties.txt");

        DBpediaIndex.indexTriples(Language.EN);
        DBpediaIndex.indexTriples(Language.DE);
        DBpediaIndex.indexTriples(Language.ES);

    }

    private static void indexOntology() {

    }

    public static void indexTriples(Language lang) {

        System.out.println("Indexing data for language: " + lang.name());
        String directory = "dbpediaData";

        indexMap = new HashMap<>();
        redirectsMap = getRedirects(directory + "/redirects_" + lang.name().toLowerCase() + ".ttl.bz2", lang);

        //get files
        File folder = new File(directory);
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            if (file.isFile()) {
                String fileExtension = file.getName().substring(file.getName().lastIndexOf(".") + 1);
                String filePath = file.getPath();

                if (fileExtension.equals("bz2") && filePath.contains("_" + lang.name().toLowerCase()) && !file.getName().startsWith("redirects") && !file.getName().startsWith("interlanguage")) {

                    //go over each file and aggregate
                    try {
                        System.out.println("Processing file : " + file.getName());
                        indexData(filePath, lang);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        //add each redirect page as label
        for (String redirectURI : redirectsMap.keySet()) {

            String label = convertURI2Label(redirectURI);

            label = StringPreprocessor.preprocess(label, lang);

            if (label.isEmpty() || label.length() < 3) {
                continue;
            }

            String uri = redirectsMap.get(redirectURI);

            if (interlanguageLinksMap.containsKey(uri)) {
                uri = interlanguageLinksMap.get(uri);
            }

            indexMap.put(label + "\t" + uri, indexMap.getOrDefault(label + "\t" + uri, 1) + 1);
        }

        String outputDirectory = "indexData";

        File dir = new File(outputDirectory);
        if (!dir.exists()) {
            dir.mkdir();
        }

        //save the file
        writeIndex(indexMap, outputDirectory + "/" + lang.name().toLowerCase() + "_resource_dbpediaFile.txt");

        indexMap.clear();
        redirectsMap.clear();
    }

    private static void indexData(String filePath, Language lang) throws CompressorException, FileNotFoundException, IOException {

        String patternString = "^(?!(#))<http://dbpedia.org/resource/(.*?)> <(.*?)> \"(.*?)\"@en .";;

        switch (lang) {
            case DE:
                patternString = "^(?!(#))<http://de.dbpedia.org/resource/(.*?)> <(.*?)> \"(.*?)\"@de .";
                break;
            case ES:
                patternString = "^(?!(#))<http://es.dbpedia.org/resource/(.*?)> <(.*?)> \"(.*?)\"@es .";
                break;
        }

        Pattern patternLabel = Pattern.compile(patternString);

        BufferedReader br = getBufferedReaderForCompressedFile(filePath);

        String line;
        while ((line = br.readLine()) != null) {
            Matcher m = patternLabel.matcher(line);

            while (m.find()) {
                String uri = m.group(2);
                String property = m.group(3);
                String label = m.group(4);

                if (!propertySet.contains(property)) {
                    continue;
                }

                if ((uri.contains("Category:") || uri.contains("Kategorie:") || uri.contains("Categoría:") || uri.contains("(disambiguation)")) || uri.contains ("Lista_") || uri.contains("Liste_") || uri.contains("List_")) {
                    continue;
                }

                label = StringPreprocessor.preprocess(label, lang);

                if (label.isEmpty() || label.length() < 3) {
                    continue;
                }

                try {
                    label = StringEscapeUtils.unescapeJava(label);
                    label = URLDecoder.decode(label, "UTF-8");
                    uri = StringEscapeUtils.unescapeJava(uri);
                    uri = URLDecoder.decode(uri, "UTF-8");

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

                    indexMap.put(label + "\t" + uri, indexMap.getOrDefault(label + "\t" + uri, 1) + 1);

                } catch (Exception e) {
                }

            }
        }
    }

    private static void writeIndex(Map<String, Integer> indexMap, String filePath) {
        System.out.println("Saving the file size: " + indexMap.size());

        try {
            PrintStream p = new PrintStream(new File(filePath));
            int counter = 0;
            for (String s : indexMap.keySet()) {

                String k = s + "\t" + indexMap.get(s);
                if (k.contains("hat is one small step 	Apollo_11	2")) {
                    int z = 1;
                }
                p.println(k);

                counter++;

                if (counter % 100000 == 0) {
                    System.out.println(counter + "/" + indexMap.size() + " " + (counter / (double) indexMap.size()) + " are saved.");
                }
            }

            System.out.println("\nFile saved.\n");
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

    }

    private static String convertURI2Label(String s) {
        String label = "";

        s = s.replace("_", " ");
        //replace each big character with space and the same character
        //indexing  accessible computing is better than AccessibleComputing
        for (int k = 0; k < s.length(); k++) {
            char c = s.charAt(k);
            if (Character.isUpperCase(c)) {

                if (k - 1 >= 0) {
                    String prev = s.charAt(k - 1) + "";
                    if (prev.equals(" ")) {
                        label += c + "";
                    } else {
                        //put space between characters
                        label += " " + c;
                    }
                } else {
                    label += c + "";
                }
            } else {
                label += c + "";
            }
        }
        
        s = label.trim().toLowerCase();
        
        s = s.replaceAll("\\s+", " ");

        s = s.trim();
        
        return s;
    }

    private static BufferedReader getBufferedReaderForCompressedFile(String fileIn) throws CompressorException, FileNotFoundException {
        FileInputStream fin = new FileInputStream(fileIn);
        BufferedInputStream bis = new BufferedInputStream(fin);
        CompressorInputStream input = new CompressorStreamFactory().createCompressorInputStream(bis);
        BufferedReader br2 = new BufferedReader(new InputStreamReader(input));
        return br2;
    }

    private static Map<String, String> getRedirects(String filePath, Language lang) {
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
}
