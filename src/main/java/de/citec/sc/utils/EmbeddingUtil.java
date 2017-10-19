/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import javax.net.ssl.HttpsURLConnection;
import static org.eclipse.jetty.http.HttpHeader.USER_AGENT;

/**
 *
 * @author sherzod
 */
public class EmbeddingUtil {

    private static final String apiURL = "http://scdemo.techfak.uni-bielefeld.de/sim/similarity";
//    private static final String apiURL = "http://purpur-v10:8081/similarity";

    public static double similarity(String word1, String word2, String language) {

        double sim = 0;

        try {
            String url = apiURL + "?word1=" + URLEncoder.encode(word1, "UTF-8") + "&word2=" + URLEncoder.encode(word2, "UTF-8") + "&lang=" + language;

            URL myUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) myUrl.openConnection();

            InputStream is = conn.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            String inputLine;

            while ((inputLine = br.readLine()) != null) {
                sim = Double.parseDouble(inputLine);
            }

            br.close();

//            String r = response.toString();
//
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sim;

    }
}
