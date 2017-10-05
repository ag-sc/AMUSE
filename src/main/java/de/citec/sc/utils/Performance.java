/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.utils;

import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.corpus.SampledMultipleInstance;
import de.citec.sc.evaluator.AnswerEvaluator;
import de.citec.sc.learning.NELObjectiveFunction;
import de.citec.sc.learning.QAObjectiveFunction;
import de.citec.sc.learning.QueryConstructor;
import de.citec.sc.variable.State;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import learning.ObjectiveFunction;

/**
 *
 * @author sherzod
 */
public class Performance {

    public static HashMap<String, String> parsedQuestions = new HashMap<>();
    public static HashMap<String, String> unParsedQuestions = new HashMap<>();

    public static void logNELTrain() {

        String fileName = "NEL_Language_" + ProjectConfiguration.getLanguage() + "_Manual_" + ProjectConfiguration.useManualLexicon() + "_Matoll_" + ProjectConfiguration.useMatoll() + "_Dataset_" + ProjectConfiguration.getTrainingDatasetName() + "_Epoch_" + ProjectConfiguration.getNumberOfEpochs() + "_Word_" + ProjectConfiguration.getTrainMaxWordCount() + "_Language_" + ProjectConfiguration.getLanguage() + "_GROUP_" + ProjectConfiguration.getFeatureGroup() + "_SAMPLING_LEVEL_" + ProjectConfiguration.getLinkingSamplingLevel();

        //log based on word count
        String p = parsedQuestions.size() + "/" + (parsedQuestions.size() + unParsedQuestions.size()) + "\n\n";
        String u = unParsedQuestions.size() + "/" + (parsedQuestions.size() + unParsedQuestions.size()) + "\n\n";

        for (String p1 : parsedQuestions.keySet()) {
            p += p1 + "\n" + parsedQuestions.get(p1) + "\n\n";
        }
        for (String p1 : unParsedQuestions.keySet()) {
            u += unParsedQuestions.get(p1) + "\n\n";
        }

        String parsedOutputsDirectory = "trainResult";

        File theDir = new File(parsedOutputsDirectory);

        if (!theDir.exists()) {

            try {
                theDir.mkdir();
            } catch (SecurityException se) {
                //handle it
            }
        }

        FileFactory.writeListToFile(parsedOutputsDirectory + "/unParsedInstances_" + fileName + ".txt", u, false);
        FileFactory.writeListToFile(parsedOutputsDirectory + "/parsedInstances_" + fileName + ".txt", p, false);

        //general file
        Set<String> oldLogs = FileFactory.readFile(parsedOutputsDirectory + "/logs_NEL_" + ProjectConfiguration.getLanguage() + ".txt");

        Map<String, Double> map = new HashMap<>();
        for (String s : oldLogs) {
            String name = s.split("\t")[0];
            double score = Double.parseDouble(s.split("\t")[1]);

            map.put(name, score);
        }

        String n = "Manual_" + ProjectConfiguration.useManualLexicon() + "_Matoll_" + ProjectConfiguration.useMatoll() + "_W2V_" + ProjectConfiguration.useEmbeddingLexicon() + "_Dataset_" + ProjectConfiguration.getTrainingDatasetName() + "_Epoch_" + ProjectConfiguration.getNumberOfEpochs() + "_SAMPLING_LEVEL_" + ProjectConfiguration.getLinkingSamplingLevel();
        double s = parsedQuestions.size() / (double) (parsedQuestions.size() + unParsedQuestions.size());
        s = round(s, 2);

        map.put(n, s);

        map = SortUtils.sortByDoubleValue((HashMap<String, Double>) map);

        String content = "";
        for (String k : map.keySet()) {
            content += k + "\t" + map.get(k) + "\n";
        }
        content = content.trim();

        FileFactory.writeListToFile(parsedOutputsDirectory + "/logs_NEL_" + ProjectConfiguration.getLanguage() + ".txt", content, false);

        unParsedQuestions.clear();
        parsedQuestions.clear();
    }

    public static void logQATrain() {

        String fileName = "QA_Language_" + ProjectConfiguration.getLanguage() + "_Manual_" + ProjectConfiguration.useManualLexicon() + "_Matoll_" + ProjectConfiguration.useMatoll() + "_Dataset_" + ProjectConfiguration.getTrainingDatasetName() + "_Epoch_" + ProjectConfiguration.getNumberOfEpochs() + "_Word_" + ProjectConfiguration.getTrainMaxWordCount() + "_Language_" + ProjectConfiguration.getLanguage() + "_Group_" + ProjectConfiguration.getFeatureGroup() + "_SAMPLING_LEVEL_" + ProjectConfiguration.getLinkingSamplingLevel();

        //log based on word count
        String p = parsedQuestions.size() + "/" + (parsedQuestions.size() + unParsedQuestions.size()) + "\n\n";
        String u = unParsedQuestions.size() + "/" + (parsedQuestions.size() + unParsedQuestions.size()) + "\n\n";

        for (String p1 : parsedQuestions.keySet()) {
            p += p1 + "\n" + parsedQuestions.get(p1) + "\n\n";
        }
        for (String p1 : unParsedQuestions.keySet()) {
            u += unParsedQuestions.get(p1) + "\n\n";
        }

        String parsedOutputsDirectory = "trainResult";

        File theDir = new File(parsedOutputsDirectory);

        if (!theDir.exists()) {

            try {
                theDir.mkdir();
            } catch (SecurityException se) {
                //handle it
            }
        }

        FileFactory.writeListToFile(parsedOutputsDirectory + "/unParsedInstances_" + fileName + ".txt", u, false);
        FileFactory.writeListToFile(parsedOutputsDirectory + "/parsedInstances_" + fileName + ".txt", p, false);

        //general file
        Set<String> oldLogs = FileFactory.readFile(parsedOutputsDirectory + "/logs_QA_" + ProjectConfiguration.getLanguage() + ".txt");

        Map<String, Double> map = new HashMap<>();
        for (String s : oldLogs) {
            String name = s.split("\t")[0];
            double score = Double.parseDouble(s.split("\t")[1]);

            map.put(name, score);
        }

        String n = "Manual_" + ProjectConfiguration.useManualLexicon() + "_Matoll_" + ProjectConfiguration.useMatoll() + "_W2V_" + ProjectConfiguration.useEmbeddingLexicon() + "_Dataset_" + ProjectConfiguration.getTrainingDatasetName() + "_Epoch_" + ProjectConfiguration.getNumberOfEpochs() + "_SAMPLING_LEVEL_" + ProjectConfiguration.getLinkingSamplingLevel();
        double s = parsedQuestions.size() / (double) (parsedQuestions.size() + unParsedQuestions.size());
        s = round(s, 2);

        map.put(n, s);

        map = SortUtils.sortByDoubleValue((HashMap<String, Double>) map);

        String content = "";
        for (String k : map.keySet()) {
            content += k + "\t" + map.get(k) + "\n";
        }
        content = content.trim();

        FileFactory.writeListToFile(parsedOutputsDirectory + "/logs_QA_" + ProjectConfiguration.getLanguage() + ".txt", content, false);

        unParsedQuestions.clear();
        parsedQuestions.clear();
    }

    public static void logNELTest(List<SampledMultipleInstance<AnnotatedDocument, String, State>> testResults, ObjectiveFunction function) {

        String fileName = "NEL_Manual_" + ProjectConfiguration.useManualLexicon() + "_Matoll_" + ProjectConfiguration.useMatoll() + "_W2V_" + ProjectConfiguration.useEmbeddingLexicon() + "_Dataset_" + ProjectConfiguration.getTestDatasetName() + "_Epoch_" + ProjectConfiguration.getNumberOfEpochs() + "_Word_" + ProjectConfiguration.getTrainMaxWordCount() + "_Language_" + ProjectConfiguration.getLanguage() + "_Group_" + ProjectConfiguration.getFeatureGroup() + "_SAMPLING_LEVEL_" + ProjectConfiguration.getLinkingSamplingLevel();

        String allStatesAsString = "";

        for (SampledMultipleInstance<AnnotatedDocument, String, State> triple : testResults) {

            allStatesAsString += triple.getInstance().toString() + "\n Number of States : " + triple.getStates().size() + "\n";

            for (State state : triple.getStates()) {
                allStatesAsString += "State : " + triple.getStates().indexOf(state) + "\n" + state.toLessDetailedString() + "\n\n--------------------------------------------------------\n";
            }

            allStatesAsString += "========================================================\n";
        }

        String correctInstances = "";
        String inCorrectInstances = "";

        double overAllScore = 0;

        int c = 0;
        for (SampledMultipleInstance<AnnotatedDocument, String, State> triple : testResults) {

            double maxScore = 0;
            State maxState = null;
            for (State state : triple.getStates()) {

//                double s = function.score(state, triple.getGoldResult());
                if (maxState == null) {
                    maxState = state;
                    break;
                }

//                if (s > maxScore) {
//                    maxScore = s;
//                    maxState = state;
//                }
            }

            if (maxState != null) {
                maxScore = function.score(maxState, triple.getGoldResult());
            }

//            testPrint += maxState + "\nScore: " + maxScore + "\n========================================================================\n";
            overAllScore += maxScore;

            if (maxScore == 1.0) {
                correctInstances += maxState + "\nScore: " + maxScore + "\n========================================================================\n";
                c++;
            } else {
                inCorrectInstances += maxState + "\nScore: " + maxScore + "\n========================================================================\n";
            }
        }

        double MACROF1 = overAllScore / (double) testResults.size();
        correctInstances = c + "/" + testResults.size() + "\nMACRO F1:" + MACROF1 + "\n\n" + correctInstances;
        inCorrectInstances = (testResults.size() - c) + "/" + testResults.size() + "\nMACRO F1:" + MACROF1 + "\n\n" + inCorrectInstances;

        String outputDir = "testResult";

        File theDir = new File(outputDir);

        if (!theDir.exists()) {

            try {
                theDir.mkdir();
            } catch (SecurityException se) {
                //handle it
            }
        }

        double correct = c / (double) testResults.size();
        double inCorrect = (testResults.size() - c) / (double) testResults.size();

        FileFactory.writeListToFile(outputDir + "/parsedInstances_" + fileName + ".txt", correctInstances, false);
        FileFactory.writeListToFile(outputDir + "/unParsedInstances_" + fileName + ".txt", inCorrectInstances, false);

        FileFactory.writeListToFile(outputDir + "/states_" + fileName + ".txt", allStatesAsString, false);

        String result = "Test results with Top-k: " + ProjectConfiguration.getNELTestBeamSize()+"\n";// + "\n\nExact Match: " + c + "/" + testResults.size() + " = " + correct;
//        result += "\nIncorrect predictions: " + (testResults.size() - c) + "/" + testResults.size() + " = " + inCorrect;
//        result += "\nMACRO F1: " + MACROF1 + "\n\n";

        int topKStateNumber = ProjectConfiguration.getNELTestBeamSize();

        for (int i = 1; i <= topKStateNumber; i++) {
            String resultAsString = getCorrectInstanceNumber(testResults, "NEL", i);

//            double c1 = z / (double) testResults.size();
//            double ic1 = (testResults.size() - z) / (double) testResults.size();
            result += "\nTop " + i + " states";
            result += resultAsString + "\n";
//            result += "\nIncorrect predictions: " + (testResults.size() - z) + "/" + testResults.size() + " = " + ic1 + "\n";
        }

        FileFactory.writeListToFile(outputDir + "/output_" + fileName + ".txt", result, false);

        System.out.println("NEL-TEST:\n" + result);

        ///////
        //////////////////
        //////////////////////
        //general file
        Set<String> oldLogs = FileFactory.readFile(outputDir + "/results_NEL_" + ProjectConfiguration.getLanguage() + ".txt");

        Map<String, Double> map = new HashMap<>();
        for (String s : oldLogs) {
            String name = s.split("\t")[0];
            double score = Double.parseDouble(s.split("\t")[1]);

            map.put(name, score);
        }

        String n = "Manual_" + ProjectConfiguration.useManualLexicon() + "_Matoll_" + ProjectConfiguration.useMatoll() + "_W2V_" + ProjectConfiguration.useEmbeddingLexicon() + "_Dataset_" + ProjectConfiguration.getTestDatasetName() + "_Epoch_" + ProjectConfiguration.getNumberOfEpochs() + "_Word_" + ProjectConfiguration.getTrainMaxWordCount() + "_Language_" + ProjectConfiguration.getLanguage() + "_Group_" + ProjectConfiguration.getFeatureGroup() + "_SAMPLING_LEVEL_" + ProjectConfiguration.getLinkingSamplingLevel();
        double s = correct;
        s = round(s, 2);

        map.put(n, s);

        map = SortUtils.sortByDoubleValue((HashMap<String, Double>) map);

        String content = "";
        for (String k : map.keySet()) {
            content += k + "\t" + map.get(k) + "\n";
        }
        content = content.trim();

        FileFactory.writeListToFile(outputDir + "/results_NEL_" + ProjectConfiguration.getLanguage() + ".txt", content, false);
    }

    public static void logQATest(List<SampledMultipleInstance<AnnotatedDocument, String, State>> testResults, ObjectiveFunction function) {

        String fileName = "QA_Manual_" + ProjectConfiguration.useManualLexicon() + "_Matoll_" + ProjectConfiguration.useMatoll() + "_W2V_" + ProjectConfiguration.useEmbeddingLexicon() + "_Dataset_" + ProjectConfiguration.getTestDatasetName() + "_Epoch_" + ProjectConfiguration.getNumberOfEpochs() + "_Word_" + ProjectConfiguration.getTrainMaxWordCount() + "_Language_" + ProjectConfiguration.getLanguage() + "_Group_" + ProjectConfiguration.getFeatureGroup() + "_SAMPLING_LEVEL_" + ProjectConfiguration.getQASamplingLevel();

        String allStatesAsString = "";

        for (SampledMultipleInstance<AnnotatedDocument, String, State> triple : testResults) {

            allStatesAsString += triple.getInstance().toString() + "\n Number of States : " + triple.getStates().size() + "\n\n";

            for (State state : triple.getStates()) {
                String query = QueryConstructor.getSPARQLQuery(state);
                allStatesAsString += "State : " + triple.getStates().indexOf(state) + "\n" + state.toLessDetailedString() + "\nQuery:" + query + "\n\n------------------------------------------------\n";
            }

            allStatesAsString += "========================================================\n";
        }

        String correctInstances = "";
        String inCorrectInstances = "";

        double overAllScore = 0;

        int c = 0;
        for (SampledMultipleInstance<AnnotatedDocument, String, State> triple : testResults) {

            double maxScore = 0;

            State maxState = null;
            for (State state : triple.getStates()) {

                boolean isValidState = isValidState(state);

                if (isValidState) {
                    maxState = state;
                    break;
                }
            }
            String query = "";
            if (maxState != null) {

                query = QueryConstructor.getSPARQLQuery(maxState);

                maxScore = QAObjectiveFunction.computeValue(query, triple.getGoldResult());

                overAllScore += maxScore;
            }

            if (maxScore == 1.0) {
                correctInstances += maxState + "\nScore: " + maxScore + "\nConstructed Query: \n" + query + "\n========================================================================\n";
                c++;
            } else {
                inCorrectInstances += maxState + "\nScore: " + maxScore + "\nConstructed Query: \n" + query + "\n========================================================================\n";
            }
        }

        double MACROF1 = overAllScore / (double) testResults.size();
        correctInstances = c + "/" + testResults.size() + "\nMACRO F1:" + MACROF1 + "\n\n" + correctInstances;
        inCorrectInstances = (testResults.size() - c) + "/" + testResults.size() + "\nMACRO F1:" + MACROF1 + "\n\n" + inCorrectInstances;

        String outputDir = "testResult";

        File theDir = new File(outputDir);

        if (!theDir.exists()) {

            try {
                theDir.mkdir();
            } catch (SecurityException se) {
                //handle it
            }
        }

        FileFactory.writeListToFile(outputDir + "/parsedInstances_" + fileName + ".txt", correctInstances, false);
        FileFactory.writeListToFile(outputDir + "/unParsedInstances_" + fileName + ".txt", inCorrectInstances, false);

        //states
        FileFactory.writeListToFile(outputDir + "/states_" + fileName + ".txt", allStatesAsString, false);

        String result = "Test results with Top-k: " + ProjectConfiguration.getQATestBeamSize() + "\n";
//        result += "\nMACRO F1: " + MACROF1 + "\n\n";

        int topKStateNumber = ProjectConfiguration.getQATestBeamSize();

        for (int i = 1; i <= topKStateNumber; i++) {
            String resultAsString = getCorrectInstanceNumber(testResults, "QA", i);

//            double c1 = z / (double) testResults.size();
//            double ic1 = (testResults.size() - z) / (double) testResults.size();
            result += "\nTop " + i + " states";
            result += resultAsString + "\n";
//            result += "\nIncorrect predictions: " + (testResults.size() - z) + "/" + testResults.size() + " = " + inCorrect + "\n";
        }

        //result
        FileFactory.writeListToFile(outputDir + "/output_" + fileName + ".txt", result, false);
        System.out.println("QA-TEST:\n" + result);

        ///////
        //////////////////
        //////////////////////
        //general file
        Set<String> oldLogs = FileFactory.readFile(outputDir + "/results_QA_" + ProjectConfiguration.getLanguage() + ".txt");

        Map<String, Double> map = new HashMap<>();
        for (String s : oldLogs) {
            String name = s.split("\t")[0];
            double score = Double.parseDouble(s.split("\t")[1]);

            map.put(name, score);
        }

        String n = "Manual_" + ProjectConfiguration.useManualLexicon() + "_Matoll_" + ProjectConfiguration.useMatoll() + "_W2V_" + ProjectConfiguration.useEmbeddingLexicon() + "_Dataset_" + ProjectConfiguration.getTestDatasetName() + "_Epoch_" + ProjectConfiguration.getNumberOfEpochs() + "_Word_" + ProjectConfiguration.getTrainMaxWordCount() + "_Language_" + ProjectConfiguration.getLanguage() + "_Group_" + ProjectConfiguration.getFeatureGroup() + "_SAMPLING_LEVEL_" + ProjectConfiguration.getLinkingSamplingLevel();
        double s = MACROF1;
        s = round(s, 2);

        map.put(n, s);

        map = SortUtils.sortByDoubleValue((HashMap<String, Double>) map);

        String content = "";
        for (String k : map.keySet()) {
            content += k + "\t" + map.get(k) + "\n";
        }
        content = content.trim();

        FileFactory.writeListToFile(outputDir + "/results_QA_" + ProjectConfiguration.getLanguage() + ".txt", content, false);

    }

    public static void logQueryTypeTest(List<SampledMultipleInstance<AnnotatedDocument, String, State>> testResults, ObjectiveFunction function) {

        String fileName = "QA_Manual_" + ProjectConfiguration.useManualLexicon() + "_Matoll_" + ProjectConfiguration.useMatoll() + "_W2V_" + ProjectConfiguration.useEmbeddingLexicon() + "_Dataset_" + ProjectConfiguration.getTestDatasetName() + "_Epoch_" + ProjectConfiguration.getNumberOfEpochs() + "_Word_" + ProjectConfiguration.getTrainMaxWordCount() + "_Language_" + ProjectConfiguration.getLanguage() + "_Group_" + ProjectConfiguration.getFeatureGroup() + "_SAMPLING_LEVEL_" + ProjectConfiguration.getQASamplingLevel();

        String allStatesAsString = "";

        for (SampledMultipleInstance<AnnotatedDocument, String, State> triple : testResults) {

            allStatesAsString += triple.getInstance().toString() + "\n Number of States : " + triple.getStates().size() + "\n\n";

            for (State state : triple.getStates()) {
                String query = QueryConstructor.getSPARQLQuery(state);
                allStatesAsString += "State : " + triple.getStates().indexOf(state) + "\n" + state.toLessDetailedString() + "\nQuery:" + query + "\n\n------------------------------------------------\n";
            }

            allStatesAsString += "========================================================\n";
        }

        String correctInstances = "";
        String inCorrectInstances = "";

        double overAllScore = 0;

        int c = 0;
        for (SampledMultipleInstance<AnnotatedDocument, String, State> triple : testResults) {

            double maxScore = 0;

            State maxState = null;
            for (State state : triple.getStates()) {

                boolean isValidState = isValidState(state);

                if (isValidState) {
                    maxState = state;
                    break;
                }
            }
            String query = "";
            if (maxState != null) {

                query = QueryConstructor.getSPARQLQuery(maxState);

                maxScore = QAObjectiveFunction.computeValue(query, triple.getGoldResult());

                overAllScore += maxScore;
            }

            if (maxScore == 1.0) {
                correctInstances += maxState + "\nScore: " + maxScore + "\nConstructed Query: \n" + query + "\n========================================================================\n";
                c++;
            } else {
                inCorrectInstances += maxState + "\nScore: " + maxScore + "\nConstructed Query: \n" + query + "\n========================================================================\n";
            }
        }

        double MACROF1 = overAllScore / (double) testResults.size();
        correctInstances = c + "/" + testResults.size() + "\nMACRO F1:" + MACROF1 + "\n\n" + correctInstances;
        inCorrectInstances = (testResults.size() - c) + "/" + testResults.size() + "\nMACRO F1:" + MACROF1 + "\n\n" + inCorrectInstances;

        String outputDir = "testResult";

        File theDir = new File(outputDir);

        if (!theDir.exists()) {

            try {
                theDir.mkdir();
            } catch (SecurityException se) {
                //handle it
            }
        }

        FileFactory.writeListToFile(outputDir + "/parsedInstances_" + fileName + ".txt", correctInstances, false);
        FileFactory.writeListToFile(outputDir + "/unParsedInstances_" + fileName + ".txt", inCorrectInstances, false);

        //states
        FileFactory.writeListToFile(outputDir + "/states_" + fileName + ".txt", allStatesAsString, false);

        String result = "Test results with Top-k: " + ProjectConfiguration.getQATestBeamSize() + "\n\n";
        result += "\nMACRO F1: " + MACROF1 + "\n\n";

        int topKStateNumber = ProjectConfiguration.getQATestBeamSize();

        for (int i = 1; i <= topKStateNumber; i++) {
                        String resultAsString = getCorrectInstanceNumber(testResults, "QA", i);

//            double c1 = z / (double) testResults.size();
//            double ic1 = (testResults.size() - z) / (double) testResults.size();

            result += "\nTop " + i + " states";
            result += resultAsString+"\n";
//            result += "\nIncorrect predictions: " + (testResults.size() - z) + "/" + testResults.size() + " = " + inCorrect + "\n";
        }

        //result
        FileFactory.writeListToFile(outputDir + "/output_" + fileName + ".txt", result, false);
        System.out.println("Query Type-TEST:\n" + result);

        ///////
        //////////////////
        //////////////////////
        //general file
        Set<String> oldLogs = FileFactory.readFile(outputDir + "/results_QueryType_" + ProjectConfiguration.getLanguage() + ".txt");

        Map<String, Double> map = new HashMap<>();
        for (String s : oldLogs) {
            String name = s.split("\t")[0];
            double score = Double.parseDouble(s.split("\t")[1]);

            map.put(name, score);
        }

        String n = "Manual_" + ProjectConfiguration.useManualLexicon() + "_Matoll_" + ProjectConfiguration.useMatoll() + "_W2V_" + ProjectConfiguration.useEmbeddingLexicon() + "_Dataset_" + ProjectConfiguration.getTestDatasetName() + "_Epoch_" + ProjectConfiguration.getNumberOfEpochs() + "_Word_" + ProjectConfiguration.getTrainMaxWordCount() + "_Language_" + ProjectConfiguration.getLanguage() + "_Group_" + ProjectConfiguration.getFeatureGroup() + "_SAMPLING_LEVEL_" + ProjectConfiguration.getLinkingSamplingLevel();
        double s = MACROF1;
        s = round(s, 2);

        map.put(n, s);

        map = SortUtils.sortByDoubleValue((HashMap<String, Double>) map);

        String content = "";
        for (String k : map.keySet()) {
            content += k + "\t" + map.get(k) + "\n";
        }
        content = content.trim();

        FileFactory.writeListToFile(outputDir + "/results_QueryType_" + ProjectConfiguration.getLanguage() + ".txt", content, false);

    }

    private static boolean isValidState(State state) {
        String s = state.toString();

        String query = QueryConstructor.getSPARQLQuery(state);

        List<Integer> tokenIDs = new ArrayList<>(state.getDocument().getParse().getNodes().keySet());
        Collections.sort(tokenIDs);

        Integer firstToken = tokenIDs.get(0);
        String firstPOS = state.getDocument().getParse().getPOSTag(firstToken);

        if (query.contains("ASK") && firstPOS.equals("PRON")) {
            return false;
        }

        Set<String> answers = DBpediaEndpoint.runQuery(query, false);
        if (answers.isEmpty()) {
            return false;
        }

        return true;
    }

    private static String getCorrectInstanceNumber(List<SampledMultipleInstance<AnnotatedDocument, String, State>> testResults, String task, int topK) {

        int c = 0;
        double overAllScores = 0;
        for (SampledMultipleInstance<AnnotatedDocument, String, State> triple : testResults) {

            List<State> states = triple.getStates();

            states = states.subList(0, Math.min(states.size(), topK));

            double maxScore = -1;

            for (State state : states) {

                String query = QueryConstructor.getSPARQLQuery(state);

                double s = 0;
                if (task.equals("NEL")) {
                    s = NELObjectiveFunction.computeValue(query, triple.getGoldResult());
                } else {
                    
                    if(ProjectConfiguration.getTestDatasetName().toLowerCase().contains("webquestions")){
                        s = AnswerEvaluator.evaluate(query,state.getDocument().getQaldInstance().getAnswers(), true);
                    }
                    else{
                        s = QAObjectiveFunction.computeValue(query, triple.getGoldResult());
                    }
                }

                if (s > maxScore) {
                    maxScore = s;
                }
            }

            overAllScores += maxScore;

            if (maxScore == 1.0) {
                c++;

            }
        }

        double exactMatchScore = c / (double) testResults.size();
        double macroF1 = overAllScores / (double) testResults.size();

        String result = "\nExact match: " + exactMatchScore + "\nMacro F1: " + macroF1;
        return result;
    }

    public static void addParsed(String s, String q) {
        if (unParsedQuestions.containsKey(s)) {
            unParsedQuestions.remove(s);
        }

        parsedQuestions.put(s, q);
    }

    public static void addUnParsed(String s, String q) {

        //make it un parsed if the parsed map doesn't contain
        //if parsed map contains it means that at some point it parsed, keep it like this
        if (!parsedQuestions.containsKey(s)) {
            unParsedQuestions.put(s, q);
        }
    }

    private static double round(double value, int places) {

        if (Double.isNaN(value)) {
            return 0;
        }
        if (Double.isInfinite(value)) {
            return 0;
        }
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
