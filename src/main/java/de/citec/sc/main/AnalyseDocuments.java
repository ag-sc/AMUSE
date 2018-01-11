package de.citec.sc.main;

import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.corpus.QALDCorpus;
import de.citec.sc.qald.CorpusLoader;
import de.citec.sc.qald.Question;
import de.citec.sc.query.CandidateRetriever;
import de.citec.sc.query.CandidateRetrieverOnLucene;
import de.citec.sc.query.ManualLexicon;
import de.citec.sc.query.Search;
import de.citec.sc.utils.DBpediaEndpoint;
import de.citec.sc.utils.FileFactory;
import de.citec.sc.utils.SortUtils;
import de.citec.sc.wordNet.WordNetAnalyzer;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class AnalyseDocuments {

    private static final Logger log = LogManager.getFormatterLogger();

    public static void main(String[] args) {
        analyseGeobase();

        analyseQALD6();

        analyseWebQuestions();

        analyseGraphQuestions();
    }

    private static void analyseQALD6() {

        HashMap<String, Integer> trainWords = new HashMap<>();
        HashMap<String, Integer> testWords = new HashMap<>();

        String trainFile = "src/main/resources/qald-6-train-multilingual.json";
        String testFile = "src/main/resources/qald-6-test-multilingual.json";

        try {

            JSONParser parser = new JSONParser();
            HashMap obj = (HashMap) parser.parse(new FileReader(trainFile));

            JSONArray questions = (JSONArray) obj.get("questions");
            for (int i = 0; i < questions.size(); i++) {
                HashMap o1 = (HashMap) questions.get(i);

                String questionText = "";

                JSONArray questionTexts = (JSONArray) o1.get("question");
                for (Object qObject : questionTexts) {

                    JSONObject englishQuestionText = (JSONObject) qObject;

                    if (englishQuestionText.get("language").equals("en")) {
                        questionText = (String) englishQuestionText.get("string");

                        questionText = questionText.replaceAll("[!?,.;:]", "");

                        String[] tokens = questionText.split(" ");
                        for (String token : tokens) {

                            token = token.toLowerCase();

                            trainWords.put(token, trainWords.getOrDefault(token, 1) + 1);
                        }

                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {

            JSONParser parser = new JSONParser();
            HashMap obj = (HashMap) parser.parse(new FileReader(testFile));

            JSONArray questions = (JSONArray) obj.get("questions");
            for (int i = 0; i < questions.size(); i++) {
                HashMap o1 = (HashMap) questions.get(i);

                String questionText = "";

                JSONArray questionTexts = (JSONArray) o1.get("question");
                for (Object qObject : questionTexts) {

                    JSONObject englishQuestionText = (JSONObject) qObject;

                    if (englishQuestionText.get("language").equals("en")) {
                        questionText = (String) englishQuestionText.get("string");

                        questionText = questionText.replaceAll("[!?,.;:]", "");

                        String[] tokens = questionText.split(" ");
                        for (String token : tokens) {

                            token = token.toLowerCase();

                            testWords.put(token, testWords.getOrDefault(token, 1) + 1);
                        }

                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        printResults("QALD-6", trainWords, testWords);
    }

    private static void printResults(String datasetName, HashMap<String, Integer> trainWords, HashMap<String, Integer> testWords) {

        trainWords = SortUtils.sortByValue(trainWords);
        testWords = SortUtils.sortByValue(testWords);

        int c = 0;

        List<String> commonWords = new ArrayList<>();

        for (String word : testWords.keySet()) {

            if (trainWords.containsKey(word)) {

                commonWords.add(word);
                c++;
            }
        }

        Integer totalNumberOfTokens = trainWords.size() + testWords.size();

        double d1 = c / (double) testWords.size();

        System.out.println(datasetName + " Number of  tokens in Train: " + trainWords.size());
        System.out.println(datasetName + " Number of  tokens in Test: " + testWords.size());
        System.out.println(datasetName + " Common words : " + c + "/" + testWords.size() + " = " + d1);
    }

    private static void analyseGeobase() {

        //parse([which,rivers,run,through,states,bordering,new,mexico,?], answer(A,(river(A),traverse(A,B),state(B),next_to(B,C),const(C,stateid('new mexico'))))).
        Set<String> geobase880 = FileFactory.readFile("src/main/resources/geobase/geobase880.txt");
        Set<String> geobase250 = FileFactory.readFile("src/main/resources/geobase/geobase250.txt");

        HashMap<String, Integer> trainWords = new HashMap<>();
        HashMap<String, Integer> testWords = new HashMap<>();

        for (String s : geobase880) {

            String question = s.substring(s.indexOf("[") + 1, s.indexOf("]"));

            String[] tokens = question.split(",");
            for (String token : tokens) {

                token = token.toLowerCase();

                trainWords.put(token, trainWords.getOrDefault(token, 1) + 1);
            }

        }
        for (String s : geobase250) {

            String question = s.substring(s.indexOf("[") + 1, s.indexOf("]"));

            String[] tokens = question.split(",");
            for (String token : tokens) {

                token = token.toLowerCase();

                testWords.put(token, testWords.getOrDefault(token, 1) + 1);
            }
        }

        printResults("Geobase880", trainWords, testWords);

    }

    private static void analyseWebQuestions() {
        String trainFile = "src/main/resources/webquestions/webquestions.examples.train.json";
        String testFile = "src/main/resources/webquestions/webquestions.examples.test.json";

        HashMap<String, Integer> trainWords = new HashMap<>();
        HashMap<String, Integer> testWords = new HashMap<>();

        JSONParser parser = new JSONParser();
        try {

            JSONArray trainQuestions = (JSONArray) parser.parse(new FileReader(trainFile));
            for (int i = 0; i < trainQuestions.size(); i++) {
                HashMap o1 = (HashMap) trainQuestions.get(i);

                String questionText = (String) o1.get("utterance");
                questionText = questionText.replaceAll("[!?,.;:]", "");

                String[] tokens = questionText.split(" ");
                for (String token : tokens) {

                    token = token.toLowerCase();

                    trainWords.put(token, trainWords.getOrDefault(token, 1) + 1);
                }
            }

            JSONArray testQuestions = (JSONArray) parser.parse(new FileReader(testFile));
            for (int i = 0; i < testQuestions.size(); i++) {
                HashMap o1 = (HashMap) testQuestions.get(i);

                String questionText = (String) o1.get("utterance");
                questionText = questionText.replaceAll("[!?,.;:]", "");

                String[] tokens = questionText.split(" ");
                for (String token : tokens) {

                    token = token.toLowerCase();

                    testWords.put(token, testWords.getOrDefault(token, 1) + 1);
                }
            }

        } catch (Exception e) {

        }

        printResults("WebQuestions", trainWords, testWords);

    }

    private static void analyseGraphQuestions() {
        String trainFile = "src/main/resources/graphquestions/graphquestions.training.json";
        String testFile = "src/main/resources/graphquestions/graphquestions.testing.json";

        HashMap<String, Integer> trainWords = new HashMap<>();
        HashMap<String, Integer> testWords = new HashMap<>();

        JSONParser parser = new JSONParser();
        try {

            JSONArray trainQuestions = (JSONArray) parser.parse(new FileReader(trainFile));
            for (int i = 0; i < trainQuestions.size(); i++) {
                HashMap o1 = (HashMap) trainQuestions.get(i);

                String questionText = (String) o1.get("question");
                questionText = questionText.replaceAll("[!?,.;:]", "");

                String[] tokens = questionText.split(" ");
                for (String token : tokens) {

                    token = token.toLowerCase();

                    trainWords.put(token, trainWords.getOrDefault(token, 1) + 1);
                }
            }

            JSONArray testQuestions = (JSONArray) parser.parse(new FileReader(testFile));
            for (int i = 0; i < testQuestions.size(); i++) {
                HashMap o1 = (HashMap) testQuestions.get(i);

                String questionText = (String) o1.get("question");
                questionText = questionText.replaceAll("[!?,.;:]", "");

                String[] tokens = questionText.split(" ");
                for (String token : tokens) {

                    token = token.toLowerCase();

                    testWords.put(token, testWords.getOrDefault(token, 1) + 1);
                }
            }

        } catch (Exception e) {

        }

        printResults("GraphQuestions", trainWords, testWords);

    }

}
