/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.gerbil;

import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.corpus.QALDCorpus;
import de.citec.sc.gerbil.instance.Convert;
import de.citec.sc.gerbil.instance.QALDInstance;
import de.citec.sc.main.Main;
import de.citec.sc.qald.CorpusLoader;
import de.citec.sc.qald.Question;
import de.citec.sc.query.CandidateRetriever;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author sherzod
 */
public class Process {

    private static Map<String, Question> map;

    public static void load() {
        map = new HashMap<>();

        boolean includeYAGO = true;
        boolean includeAggregation = true;
        boolean includeUNION = true;
        boolean onlyDBO = true;
        boolean isHybrid = true;

        CandidateRetriever.Language lang = CandidateRetriever.Language.EN;

        QALDCorpus corpus = CorpusLoader.load(CorpusLoader.Dataset.qald6Test, includeYAGO, includeAggregation, includeUNION, onlyDBO, isHybrid);

        for (AnnotatedDocument d : corpus.getDocuments()) {

            map.put(d.getQuestionString(), d.getQaldInstance());
        }
    }

    public static QALDInstance process(String input) {

        if (map.containsKey(input)) {
            Question q = map.get(input);

            String quesionString = input;
            String queryString = queryString = q.getQueryText();
            String languageString = "en";

            quesionString = input;

            QALDInstance qaldInstance = Convert.convert(quesionString, queryString, languageString);
            
            return qaldInstance;
        }

        QALDInstance qaldInstance = new QALDInstance();

        return qaldInstance;
    }
}
