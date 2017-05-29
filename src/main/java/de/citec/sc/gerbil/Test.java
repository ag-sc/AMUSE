/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.gerbil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 *
 * @author sherzod
 */
public class Test {

    private static Gson gson = new GsonBuilder().create();

    public static void main(String[] args) throws Exception {

        sendPost();
    }

    private static void sendPost() throws Exception {

        String address = "http://localhost:8080/qa";

        URL url = new URL(address);

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");

        String urlParameters = "query=" + URLEncoder.encode("Who created Wikipedia?", "UTF-8");

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        BufferedReader br = new BufferedReader(new InputStreamReader(
                (con.getInputStream())));

        String m = gson.fromJson(br, String.class);

        System.out.println(m);

    }

    private static void sendGet() throws Exception {

        String address = "http://localhost:8080/qa?query=Input";

        URL url = new URL(address);

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.flush();
        wr.close();

        BufferedReader br = new BufferedReader(new InputStreamReader(
                (con.getInputStream())));

        String m = gson.fromJson(br, String.class);

        System.out.println(m);

    }
}
