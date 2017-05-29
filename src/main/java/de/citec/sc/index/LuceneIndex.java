/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.index;

import de.citec.sc.query.CandidateRetriever;
import de.citec.sc.query.CandidateRetrieverOnLucene;
import de.citec.sc.query.CandidateRetrieverOnMemory;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sherzod
 */
public class LuceneIndex {

    public static void main(String[] args) {

//        createIndex();
        queryIndex();

    }

    private static void queryIndex() {
        CandidateRetriever r = new CandidateRetrieverOnLucene(false, "luceneIndex");
        System.out.println(r.getAllResources("john f. kennedy", 10, CandidateRetriever.Language.EN));
        System.out.println(r.getAllResources("obama", 10, CandidateRetriever.Language.DE));
        System.out.println(r.getAllResources("siesta", 10, CandidateRetriever.Language.ES));
        System.out.println(r.getAllPredicates("nahrung", 10, CandidateRetriever.Language.DE));
        System.out.println(r.getAllClasses("anfibio", 10, true, CandidateRetriever.Language.ES));
        System.out.println(r.getAllResources("obama", 10, CandidateRetriever.Language.EN));
        System.out.println(r.getAllResources("obama", 10, CandidateRetriever.Language.DE));
        System.out.println(r.getAllResources("obama", 10, CandidateRetriever.Language.ES));
        System.out.println(r.getAllClasses("anfibio", 10, true, CandidateRetriever.Language.ES));
    }

    private static void createIndex() {

        boolean removePrevFiles = false;
        String directory = "rawFiles";

        AnchorTextLoader a1 = new AnchorTextLoader();
        a1.load(removePrevFiles, "luceneIndex/en", directory + "/en/resourceFiles/");
        a1.load(removePrevFiles, "luceneIndex/de", directory + "/de/resourceFiles/");
        a1.load(removePrevFiles, "luceneIndex/es", directory + "/es/resourceFiles/");

        PredicateLoader p1 = new PredicateLoader();
        p1.load(removePrevFiles, "luceneIndex/en", directory + "/en/predicateFiles/");
        p1.load(removePrevFiles, "luceneIndex/de", directory + "/de/predicateFiles/");
        p1.load(removePrevFiles, "luceneIndex/es", directory + "/es/predicateFiles/");

        de.citec.sc.index.ClassLoader c1 = new de.citec.sc.index.ClassLoader();
        c1.load(removePrevFiles, "luceneIndex/en", directory + "/en/classFiles/");
        c1.load(removePrevFiles, "luceneIndex/de", directory + "/de/classFiles/");
        c1.load(removePrevFiles, "luceneIndex/es", directory + "/es/classFiles/");

        MATOLLTextLoader m1 = new MATOLLTextLoader();
        m1.load(removePrevFiles, "luceneIndex/en", directory + "/en/matollFiles/");
        m1.load(removePrevFiles, "luceneIndex/de", directory + "/de/matollFiles/");
        m1.load(removePrevFiles, "luceneIndex/es", directory + "/es/matollFiles/");
    }
}
