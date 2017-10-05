/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author sherzod
 */
public class FileFactory {

    public static Set<String> readFile(File file) {
        Set<String> content = new LinkedHashSet<>();
        try {
            FileInputStream fstream = new FileInputStream(file);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String strLine;

            while ((strLine = br.readLine()) != null) {
                content.add(strLine);
            }
            fstream.close();
            in.close();
            br.close();
        } catch (Exception e) {
            System.err.println("Error reading the file: " + file.getPath() + "\n" + e.getMessage());
        }

        return content;
    }

    public static Set<String> readFile(String path) {

        return readFile(new File(path));

//        
//        Set<String> content = new HashSet<>();
//        try {
//            File file = new File(path);
//            FileInputStream fstream = new FileInputStream(file);
//            DataInputStream in = new DataInputStream(fstream);
//            BufferedReader br = new BufferedReader(new InputStreamReader(in));
//            String strLine;
//
//            while ((strLine = br.readLine()) != null) {
//                if (content == null) {
//                    content = new ArraySet<>();
//                }
//                content.add(strLine);
//            }
//            fstream.close();
//            in.close();
//            br.close();
//        } catch (Exception e) {
//            System.err.println("Error reading the file: " + path);
//        }
//
//        return content;
    }

    public static void writeListToFile(String fileName, String content, boolean append) {
        try {
            File file = new File(fileName);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file, append);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);
            pw.print(content);

            pw.close();
            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeListToFile(String fileName, Set<String> content, boolean append) {
        try {
            File file = new File(fileName);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file, append);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);
            String c = "";

            for (String s : content) {
                c += s + "\n";
            }
            pw.print(c);

            pw.close();
            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
