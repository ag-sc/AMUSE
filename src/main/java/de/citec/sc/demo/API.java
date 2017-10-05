/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.demo;

import com.google.gson.Gson;
import de.citec.sc.utils.DBpediaEndpoint;
import java.util.Map;
import java.util.Set;
import spark.Spark;
import static spark.Spark.get;
import static spark.Spark.port;

/**
 *
 * @author sherzod
 */
public class API {

//    private static String query = "SELECT (COUNT(DISTINCT ?uri) as ?c) WHERE {  <http://dbpedia.org/resource/Benjamin_Franklin> <http://dbpedia.org/ontology/child> ?uri . }";

    public static void main(String[] args) {

        startService();

    }

    public static void startService() {

        APIPipeline.initialize();

        port(2000);

        System.out.println("Server has started on port 2000.\n");

        get("/api/:question/:language", "application/json", (request, response) -> {

            String question = request.params(":question");
            String language = request.params(":language");

            String input = request.body();

            System.out.println("Question: " + question + " Lang: " + language);
            
            String constructedQuery = APIPipeline.run(question);

            Set<String> answers = DBpediaEndpoint.runQuery(constructedQuery, false);

            Response r = new Response(constructedQuery, answers);

            return r;
        }, new JsonTransformer());
    }

}
