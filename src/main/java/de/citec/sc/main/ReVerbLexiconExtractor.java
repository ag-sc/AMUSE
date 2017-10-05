/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.main;

import de.citec.sc.utils.FileFactory;
import java.util.Set;

/**
 *
 * @author sherzod
 */
public class ReVerbLexiconExtractor {
    public static void main(String[] args) {
        Set<String> lines = FileFactory.readFile("reverb_clueweb_tuples-1.1.txt");
        
        System.out.println(lines.size());
    }
}
