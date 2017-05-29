/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.evaluator;

import com.google.common.collect.Sets;
import corpus.SampledInstance;
import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.learning.NELObjectiveFunction;
import de.citec.sc.variable.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import learning.ObjectiveFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author sherzod
 */
public class ResultEvaluator {

    private static final Logger log = LogManager.getFormatterLogger();

    public static Map<String, Double> add(Map<String, Double> r1, Map<String, Double> r2) {
        Map<String, Double> result = new LinkedHashMap<>();
        Set<String> keys = Sets.union(r1.keySet(), r2.keySet());
        for (String key : keys) {
            result.put(key, r1.getOrDefault(key, 0.0) + r2.getOrDefault(key, 0.0));
        }
        return result;
    }

    public static Map<String, Double> evaluateAllByObjective(List<SampledInstance<AnnotatedDocument, String, State>> testResults, ObjectiveFunction<State, String> f) {
        Map<String, Double> map = new HashMap<>();

        NELObjectiveFunction function = (NELObjectiveFunction) f;
        double score = 0.0;

        log.info("========================================================================");
        for (SampledInstance instance : testResults) {

            State state = (State) instance.getState();
            double similarity = function.computeValue(state, state.getDocument().getGoldQueryString());

            String p = "State : \n" + state.toString();
            p += "\nGold Query : \n" + state.getDocument().getGoldQueryString();
            p += "\nScore: " + similarity;
            p += "\n========================================================================\n";

//            log.info(p);
//            if(similarity == 1.0)
            score += similarity;
        }

        score = score / (double) testResults.size();

        log.info("\n\nMacro F1 : " + score);

        map.put("Macro F1", score);

        return map;
    }
}
