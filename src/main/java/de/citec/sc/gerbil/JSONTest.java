/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.gerbil;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.citec.sc.qald.Question;
import de.citec.sc.query.CandidateRetriever;
import de.citec.sc.utils.DBpediaEndpoint;
import de.citec.sc.utils.FileFactory;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author sherzod
 */
public class JSONTest {
    
    private static Map<String, Question> map;
    
    private static List<Question> readJSONFile(String filePath) {
        
        ArrayList<Question> qaldQuestions = new ArrayList<>();

        JSONParser parser = new JSONParser();

        try {

            HashMap obj = (HashMap) parser.parse(new FileReader(filePath));

            JSONArray questions = (JSONArray) obj.get("questions");
            for (int i = 0; i < questions.size(); i++) {
                HashMap o1 = (HashMap) questions.get(i);

                String hybrid = "", onlyDBO = "", aggregation = "";
                if (o1.get("hybrid") instanceof Boolean) {
                    Boolean b = (Boolean) o1.get("hybrid");
                    hybrid = b.toString();
                } else {
                    hybrid = (String) o1.get("hybrid");
                }

                if (o1.get("onlydbo") instanceof Boolean) {
                    Boolean b = (Boolean) o1.get("onlydbo");
                    onlyDBO = b.toString();
                } else {
                    onlyDBO = (String) o1.get("onlydbo");
                }

                if (o1.get("aggregation") instanceof Boolean) {
                    Boolean b = (Boolean) o1.get("aggregation");
                    aggregation = b.toString();
                } else {
                    aggregation = (String) o1.get("aggregation");
                }

                String answerType = (String) o1.get("answertype");

                String id = o1.get("id").toString();

                HashMap queryTextObj = (HashMap) o1.get("query");
                String query = (String) queryTextObj.get("sparql");

                Map<CandidateRetriever.Language, String> questionText = new HashMap<>();

                JSONArray questionTexts = (JSONArray) o1.get("question");
                for (Object qObject : questionTexts) {

                    JSONObject englishQuestionText = (JSONObject) qObject;

                    if (englishQuestionText.get("language").equals("en")) {
                        questionText.put(CandidateRetriever.Language.EN, englishQuestionText.get("string").toString());
                        
                    }
                    if (englishQuestionText.get("language").equals("de")) {
                        questionText.put(CandidateRetriever.Language.DE, englishQuestionText.get("string").toString());
                        
                    }
                    if (englishQuestionText.get("language").equals("es")) {
                        questionText.put(CandidateRetriever.Language.ES, englishQuestionText.get("string").toString());   
                    }
                }

                if (query != null) {
                    if (!query.equals("")) {

                        query = query.replace("\n", " ");

                        Question q1 = new Question(questionText, query, onlyDBO, aggregation, answerType, hybrid, id);
                        qaldQuestions.add(q1);
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return qaldQuestions;
    }
    
    private static void loadDocs(){
        String filePath = "src/main/resources/qald-6-test-multilingual.json";
        
        List<Question> questions = readJSONFile(filePath);
        
        map = new HashMap<>();
        
        for(Question q : questions){
            map.put(q.getQuestionText().get("en"), q);
        }
    }
            

    public static String getJSONOutput(String question, String language, String answerType, String query) throws IOException {
        
        if(map == null){
            loadDocs();
        }
        
        
        String output = "";
        
        if(map.containsKey(question)){
            query = map.get(question).getQueryText();
        }
//        else{
//            return output;
//        }
        

        JSONObject obj = new JSONObject();

        JSONArray questions = new JSONArray();

        JSONObject question1 = new JSONObject();
        question1.put("id", "1");
        question1.put("answertype", answerType);

        JSONArray questionText = new JSONArray();

        JSONObject questionLanguage1 = new JSONObject();
        questionLanguage1.put("language", language);
        questionLanguage1.put("string", question);

        questionText.add(questionLanguage1);

        question1.put("question", questionText);

        JSONObject sparql = new JSONObject();
        sparql.put("sparql", query);

        question1.put("query", sparql);

        JSONArray answers = new JSONArray();

        JSONObject answer1 = new JSONObject();
        JSONObject head = new JSONObject();
        JSONObject results = new JSONObject();

        JSONArray varsArray = new JSONArray();
        varsArray.add("uri");
        head.put("vars", varsArray);

        JSONArray bindingsArray = new JSONArray();

        Set<String> queryResults = DBpediaEndpoint.runQuery(query, false);
        
        for (String qResult : queryResults) {   
            JSONObject binding1 = getBinding("uri", qResult);

            bindingsArray.add(binding1);
        }

        results.put("bindings", bindingsArray);

        answer1.put("head", head);
        answer1.put("results", results);

        answers.add(answer1);

        question1.put("answers", answers);

        questions.add(question1);

        obj.put("questions", questions);

//        output = obj.toJSONString();
        
        StringWriter sw = new StringWriter();
        new ObjectMapper().writer().writeValue(sw, obj);
        output = sw.toString();

        return output;
    }

    private static JSONObject getBinding(String type, String uri) {
        JSONObject o = new JSONObject();

        if(uri.startsWith("http://")){
            type = "uri";
        }
        else{
            type = "literal";
        }
        
        JSONObject t = new JSONObject();
        t.put("type", type);
        t.put("value", uri);

//        JSONObject u = new JSONObject();
//        u.put("value", uri);

//        JSONArray a = new JSONArray();
//        a.add(t);
//        a.add(u);

        o.put(type, t);

        String s = o.toJSONString();
        return o;
    }

    public static void main(String[] args) throws IOException {
        String question = "Who created Goofy?";
        String query = "SELECT DISTINCT ?uri WHERE { <http://dbpedia.org/resource/Goofy> <http://dbpedia.org/ontology/creator>  ?uri . }";
        String answerType = "resource";
        String language = "en";

        String jsonOutput = getJSONOutput(question, language, answerType, query);

        FileFactory.writeListToFile("test.json", jsonOutput, false);
    }
}
