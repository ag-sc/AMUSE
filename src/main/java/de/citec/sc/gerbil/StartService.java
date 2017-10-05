/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.gerbil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.citec.sc.gerbil.instance.Date;
import de.citec.sc.gerbil.instance.QALDInstance;
import de.citec.sc.utils.FileFactory;
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
    private static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        
        Process.load();

        int port = 8080;
        Spark.port(port);

        System.out.println("Starting the annotate with port :" + port);
        
        Spark.post("/", "application/json", (request, response) -> {
            //get input from client
            String input = request.queryParams("query");
            
            String[] values = request.queryParamsValues("query");

            try {
                input = URLDecoder.decode(input, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }
            
            System.out.println("Input: "+input);
            
            java.util.Date d = new java.util.Date();
            
            
            
            QALDInstance instance = Process.process(input);
             
//            String output = gson.toJson(instance);
            String output = mapper.writeValueAsString(instance);
            
            FileFactory.writeListToFile("gerbilInputs.txt", d.toString() + "  --  "+input+"\n", true);
            FileFactory.writeListToFile("gerbilOutputs.txt", d.toString() + "  --  "+input+ "   "+instance.getQuestions()[0].getQuery().getSparql()+ "\n", true);
            
            
            return output;

        }, new JsonTransformer());
    }

}
