/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.utils;

import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.corpus.SampledMultipleInstance;
import de.citec.sc.learning.NELObjectiveFunction;
import de.citec.sc.learning.QueryConstructor;
import de.citec.sc.variable.HiddenVariable;
import de.citec.sc.variable.State;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import learning.ObjectiveFunction;
import static net.ricecode.similarity.StringSimilarityMeasures.score;

/**
 *
 * @author sherzod
 */
public class Performance {

    public static HashMap<String, String> parsedQuestions = new HashMap<>();
    public static HashMap<String, String> unParsedQuestions = new HashMap<>();

    public static void logNELTrain() {

        String fileName = "NEL_Language_"+ProjectConfiguration.getLanguage()+"_Manual_" + ProjectConfiguration.useManualLexicon() + "_Matoll_" + ProjectConfiguration.useMatoll() + "_Dataset_" + ProjectConfiguration.getTrainingDatasetName() + "_Epoch_" + ProjectConfiguration.getNumberOfEpochs() + "_Word_" + ProjectConfiguration.getTrainMaxWordCount()+"_Language_"+ProjectConfiguration.getLanguage() + "_GROUP_"+ProjectConfiguration.getFeatureGroup();

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

        unParsedQuestions.clear();
        parsedQuestions.clear();
    }

    public static void logQATrain() {

        String fileName = "QA_Language_"+ProjectConfiguration.getLanguage()+"_Manual_" + ProjectConfiguration.useManualLexicon() + "_Matoll_" + ProjectConfiguration.useMatoll() + "_Dataset_" + ProjectConfiguration.getTrainingDatasetName() + "_Epoch_" + ProjectConfiguration.getNumberOfEpochs() + "_Word_" + ProjectConfiguration.getTrainMaxWordCount()+"_Language_"+ProjectConfiguration.getLanguage()+"_Group_"+ProjectConfiguration.getFeatureGroup();

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

        unParsedQuestions.clear();
        parsedQuestions.clear();
    }

    public static void logNELTest(List<SampledMultipleInstance<AnnotatedDocument, String, State>> testResults, ObjectiveFunction function) {

        String fileName = "NEL_Manual_" + ProjectConfiguration.useManualLexicon() + "_Matoll_" + ProjectConfiguration.useMatoll() + "_Dataset_" + ProjectConfiguration.getTestDatasetName() + "_Epoch_" + ProjectConfiguration.getNumberOfEpochs() + "_Word_" + ProjectConfiguration.getTestMaxWordCount()+"_Language_"+ProjectConfiguration.getLanguage()+"_Group_"+ProjectConfiguration.getFeatureGroup();

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

                double s = function.score(state, triple.getGoldResult());

                if (maxState == null) {
                    maxState = state;
                }

                if (s > maxScore) {
                    maxScore = s;
                    maxState = state;
                }

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
        
        String result = "Test results with Top-k: " + ProjectConfiguration.getNELTestBeamSize() + "\n\nCorrect predictions: " + c + "/" + testResults.size() + " = " + correct;
        result +="\nIncorrect predictions: " + (testResults.size() - c) + "/" + testResults.size() + " = " + inCorrect;
        result += "\nMACRO F1: " + MACROF1;
        
        FileFactory.writeListToFile(outputDir + "/result_" + fileName + ".txt", result, false);
        
        System.out.println("NEL-TEST:\n"+result);
    }

    public static void logQATest(List<SampledMultipleInstance<AnnotatedDocument, String, State>> testResults, ObjectiveFunction function) {

        String fileName = "QA_Manual_" + ProjectConfiguration.useManualLexicon() + "_Matoll_" + ProjectConfiguration.useMatoll() + "_Dataset_" + ProjectConfiguration.getTestDatasetName() + "_Epoch_" + ProjectConfiguration.getNumberOfEpochs() + "_Word_" + ProjectConfiguration.getTestMaxWordCount()+"_Language_"+ProjectConfiguration.getLanguage()+"_Group_"+ProjectConfiguration.getFeatureGroup();

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

                double s = function.score(state, triple.getGoldResult());
                maxScore = s;
                maxState = state;
                break;
            }

            overAllScore += maxScore;

            String query = QueryConstructor.getSPARQLQuery(maxState);

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
        result+="\nMACRO F1: " + MACROF1 + "\n\n";

        int topKStateNumber = ProjectConfiguration.getQATestBeamSize();

        for (int i = 1; i <= topKStateNumber; i++) {
            int z = getCorrectInstanceNumber(testResults, function, i);

            double correct = z / (double) testResults.size();
            double inCorrect = (testResults.size() - z) / (double) testResults.size();

            result +="\nTop " + i + " states";
            result+="\nCorrect predictions: " + z + "/" + testResults.size() + " = " + correct;
            result+="\nIncorrect predictions: " + (testResults.size() - z) + "/" + testResults.size() + " = " + inCorrect+"\n";
        }
        
        //result
        FileFactory.writeListToFile(outputDir + "/result_" + fileName + ".txt", result, false);
        System.out.println("QA-TEST:\n"+result);

    }

    private static int getCorrectInstanceNumber(List<SampledMultipleInstance<AnnotatedDocument, String, State>> testResults, ObjectiveFunction function, int topK) {

        int c = 0;
        for (SampledMultipleInstance<AnnotatedDocument, String, State> triple : testResults) {

            List<State> states = triple.getStates();

            states = states.subList(0, Math.min(states.size(), topK));

            for (State state : states) {

                double s = function.score(state, triple.getGoldResult());
                if (s == 1.0) {
                    c++;
                    break;
                }
            }
        }

        return c;
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
}
