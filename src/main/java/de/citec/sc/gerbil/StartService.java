/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.gerbil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import org.json.simple.JSONArray;
import spark.Spark;

/**
 *
 * @author sherzod
 */
public class StartService {

    private static Gson gson = new GsonBuilder().create();

    public static void main(String[] args) throws Exception {

        int port = 8080;
        Spark.port(port);

        System.out.println("Starting the annotate with port :" + port);

        Spark.post("/", "application/json", (request, response) -> {
            //get input from client
            String input = request.queryParams("query");

            try {
                input = URLDecoder.decode(input, "UTF-8");
            } catch (UnsupportedEncodingException ex) {

            }

            JSONArray questions = new JSONArray();

            System.out.println("Input : " + input);
            return input;

        }, new JsonTransformer());
    }

}
