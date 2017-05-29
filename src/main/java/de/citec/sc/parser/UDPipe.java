/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.parser;

import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.corpus.QALDCorpus;
import de.citec.sc.main.Main;
import de.citec.sc.qald.QALDCorpusLoader;
import de.citec.sc.query.CandidateRetriever;
import de.citec.sc.query.CandidateRetriever.Language;
import de.citec.sc.query.CandidateRetrieverOnLucene;
import de.citec.sc.query.CandidateRetrieverOnMemory;
import de.citec.sc.query.ManualLexicon;
import de.citec.sc.query.Search;
import de.citec.sc.utils.FileFactory;
import de.citec.sc.utils.ProjectConfiguration;
import de.citec.sc.wordNet.WordNetAnalyzer;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author sherzod
 */
public class UDPipe {

    private static JSONParser jsonParser;
    private static Map<String, String> cache = new HashMap<>();

    private static String requestUDPipeServer(String text, CandidateRetriever.Language lang) {
        String address = "";
        try {
            address = "http://lindat.mff.cuni.cz/services/udpipe/api/process?tokenizer&tagger&parser&data=" + URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(UDPipe.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (lang.equals(CandidateRetriever.Language.EN)) {
            address = address + "&model=english-ud-2.0-conll17-170315";
        } else if (lang.equals(CandidateRetriever.Language.DE)) {
            address = address + "&model=german-ud-1.2-160523";
        } else if (lang.equals(CandidateRetriever.Language.ES)) {
            address = address + "&model=spanish-ud-2.0-conll17-170315";
        }

        DataOutputStream wr = null;

        try {

            URL url = new URL(address);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            // Send post request
            con.setDoOutput(true);
            wr = new DataOutputStream(con.getOutputStream());
            wr.flush();
            wr.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (con.getInputStream())));

            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }

            if (jsonParser == null) {
                jsonParser = new JSONParser();
            }
            JSONObject jObject = (JSONObject) jsonParser.parse(response.toString());
            String result = (String) jObject.get("result");
            return result;

        } catch (IOException ex) {
//            ex.printStackTrace();
        } catch (ParseException ex) {
//            ex.printStackTrace();
        } finally {
            try {
                wr.close();
            } catch (IOException ex) {
//                ex.printStackTrace();
            }
        }

        return "";
    }

    public static DependencyParse parse(String text, CandidateRetriever.Language lang) {

        if (cache.isEmpty()) {
            loadCache();
        }
        String result = "";
        if (cache.containsKey(text)) {
            result = cache.get(text);
        } else {
            //query the api 10 times max, sometimes it doesn't work.
            for (int i = 1; i <= 10; i++) {
                result = requestUDPipeServer(text, lang);
                if (!result.isEmpty()) {
                    break;
                }
            }
            cache.put(text, result);
            writeCache();
        }

        DependencyParse parse = new DependencyParse();

        if (result.isEmpty()) {
            return null;
        }

        String[] lines = result.split("\n");

        try {
            for (String l : lines) {
                if (!l.startsWith("#")) {

                    String[] data = l.split("\t");
                    Integer tokenID = Integer.parseInt(data[0]);
                    String label = data[1];
                    String pos = data[3];
                    int beginPosition = text.indexOf(label);
                    int endPosition = beginPosition + label.length();

                    int parentNode = Integer.parseInt(data[6]);
                    String depRelation = data[7];

                    parse.addNode(tokenID, label, pos, beginPosition, endPosition);

                    if (depRelation.equals("root")) {
                        parse.setHeadNode(tokenID);
                    } else {
                        parse.addEdge(tokenID, parentNode, depRelation);
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }

        return parse;
    }

    private static void loadCache() {
        Set<String> set = FileFactory.readFile("ud-cache.txt");

        if (!set.isEmpty()) {

            String c = "";
            for (String s : set) {
                c += s + "\n";
            }

            if (jsonParser == null) {
                jsonParser = new JSONParser();
            }

            JSONObject jObject;
            try {
                jObject = (JSONObject) jsonParser.parse(c);

                JSONArray array = (JSONArray) jObject.get("parseTrees");

                for (Object o : array) {
                    JSONObject parse = (JSONObject) o;

                    String key = parse.get("text").toString();
                    String value = parse.get("tree").toString();

                    cache.put(key, value);
                }
            } catch (ParseException ex) {
                Logger.getLogger(UDPipe.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static void writeCache() {
        JSONObject obj = new JSONObject();

        JSONArray parseTrees = new JSONArray();

        obj.put("parseTrees", parseTrees);

        for (String k : cache.keySet()) {
            JSONObject parseTree = new JSONObject();
            parseTree.put("text", k);
            parseTree.put("tree", cache.get(k));

            parseTrees.add(parseTree);
        }

        FileFactory.writeListToFile("ud-cache.txt", obj.toJSONString(), false);
    }

    public static void main(String[] args) throws UnsupportedEncodingException {

        CandidateRetriever retriever = new CandidateRetrieverOnLucene(false, "luceneIndex");

        WordNetAnalyzer wordNet = new WordNetAnalyzer("src/main/resources/WordNet-3.0/dict");

        Search.load(retriever, wordNet);
        Search.useMatoll(ProjectConfiguration.useMatoll());

        ManualLexicon.useManualLexicon(ProjectConfiguration.useManualLexicon());

        boolean includeYAGO = false;
        boolean includeAggregation = false;
        boolean includeUNION = false;
        boolean onlyDBO = true;
        boolean isHybrid = false;

        List<Language> languages = new ArrayList<>();
//        languages.add(Language.EN);
//        languages.add(Language.DE);
        languages.add(Language.ES);

        String content = "";
        for (Language l : languages) {
            Main.lang = l;

            QALDCorpus corpus = QALDCorpusLoader.load(QALDCorpusLoader.Dataset.qald6Train, includeYAGO, includeAggregation, includeUNION, onlyDBO, isHybrid);
            QALDCorpus corpus2 = QALDCorpusLoader.load(QALDCorpusLoader.Dataset.qald6Test, includeYAGO, includeAggregation, includeUNION, onlyDBO, isHybrid);
            
            corpus.getDocuments().addAll(corpus2.getDocuments());
            
            
            
            for (AnnotatedDocument d : corpus.getDocuments()) {
                if (d.getParse() == null) {
//                    System.out.println(d.getQuestionString());
                    
                } else {
                    String before = d.getParse().toString();
                    d.getParse().mergeEdges();
                    String after = d.getParse().toString();
                    if (!before.equals(after)) {
                        
                        content +=d.getQuestionString() + "\n"+d.getGoldQueryString() + "\n"+after+"\n=========================================================================\n";
                        System.out.println(d.getQuestionString() + "\n");
                        System.out.println(d.getGoldQueryString() + "\n");
                        System.out.println(after);
//                        System.out.println(before + "\n\n" + after);
                        System.out.println("\n=========================================================================\n");
                    }
                }
            }
            FileFactory.writeListToFile(l.name()+"_output.txt", content, false);
        }
        
        
//        DependencyParse p1 = UDPipe.parse("Wer hat Goofy erfunden?", Language.DE);
//        System.out.println(p1);
//
//        p1.mergeEdges();
//        System.out.println("After\n" + p1);

//        DependencyParse parse1 = UDPipe.parse("Does Breaking Bad have more episodes than Game of Thrones?", CandidateRetriever.Language.EN);
//        System.out.println(parse1);
////
//        DependencyParse parse2 = UDPipe.parse("Wer war der Vizepräsident unter Barack Obama?", CandidateRetriever.Language.DE);
//        System.out.println(parse2);
//
//        DependencyParse parse3 = UDPipe.parse("¿Quién fué el vicepresidente de Barack Obama?", CandidateRetriever.Language.ES);
//        System.out.println(parse3);
    }
}
