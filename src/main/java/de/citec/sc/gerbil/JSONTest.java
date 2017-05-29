/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.gerbil;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author sherzod
 */
public class JSONTest {

    public static void main(String[] args) {
        JSONObject obj = new JSONObject();

        JSONArray questions = new JSONArray();

        JSONObject question1 = new JSONObject();
        question1.put("id", "1");
        question1.put("answertype", "resource");

        JSONArray questionText = new JSONArray();

        JSONObject questionLanguage1 = new JSONObject();
        questionLanguage1.put("language", "en");
        questionLanguage1.put("string", "Who created Wikipedia?");

        questionText.add(questionLanguage1);

        question1.put("question", questionText);

        JSONObject sparql = new JSONObject();
        sparql.put("sparql", "SELECT ");

        question1.put("query", sparql);

        JSONArray answers = new JSONArray();

        JSONObject answer1 = new JSONObject();
        JSONObject head = new JSONObject();
        JSONObject results = new JSONObject();

        JSONObject vars = new JSONObject();
        JSONArray varsArray = new JSONArray();
        varsArray.add("uri");

        head.put("vars", vars);

        answer1.put("head", head);
        answer1.put("results", results);

        answers.add(answer1);

        question1.put("answers", answers);

        questions.add(question1);

        obj.put("questions", questions);

        System.out.println(obj.toJSONString());
    }
}
