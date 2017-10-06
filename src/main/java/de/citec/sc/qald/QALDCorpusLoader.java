/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.qald;

import de.citec.sc.corpus.QALDCorpus;
import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.main.Main;
import de.citec.sc.nel.AnnotationExtractor;
import de.citec.sc.nel.EntityAnnotation;
import de.citec.sc.parser.DependencyParse;
import de.citec.sc.parser.StanfordParser;
import de.citec.sc.parser.UDPipe;
import de.citec.sc.query.CandidateRetriever;
import de.citec.sc.query.CandidateRetriever.Language;
import de.citec.sc.query.Search;
import de.citec.sc.utils.Lemmatizer;
import de.citec.sc.utils.ProjectConfiguration;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author sherzod
 */
public class QALDCorpusLoader {
    
    private static String outputDirectoryQALD = QALDCorpusLoader.class.getClassLoader().getResource("qald").getPath();
    private static String outputDirectoryWebQuestions = QALDCorpusLoader.class.getClassLoader().getResource("webquestions").getPath();

    private static final String qald4FileTrain = outputDirectoryQALD+"/qald-4_multilingual_train_withanswers.xml";
    private static final String qald4FileTest = outputDirectoryQALD+"/qald-4_multilingual_test_withanswers.xml";
    private static final String qald5FileTrain = outputDirectoryQALD+"/qald-5_train.xml";
    private static final String qald6FileTrain = outputDirectoryQALD+"/qald-6-train-multilingual.json";
    private static final String qald7FileTrain = outputDirectoryQALD+"/qald-7-train-multilingual.json";
    private static final String qald6FileTest = outputDirectoryQALD+"/qald-6-test-multilingual.json";
    private static final String qald5FileTest = outputDirectoryQALD+"/qald-5_test.xml";
    private static final String qaldSubset = outputDirectoryQALD+"/qald_test.xml";
    private static final String webQuestionsTrain = outputDirectoryWebQuestions+"/WebQuestions.DBpedia.train.json";
    private static final String webQuestionsTest = outputDirectoryWebQuestions+"/WebQuestions.DBpedia.test.json";
    private static final String webQuestionsSubset = outputDirectoryWebQuestions+"/WebQuestions.DBpedia.subset.json";

    public enum Dataset {

        qald4Test, qald4Train, qald5Test, qald5Train, qaldSubset, qald6Train, qald6Test, qald7Train, webQuestionsTrain, webQuestionsTest, webQuestionsSubset
    }

    /**
     * reads QALD corpus {QALDSubset, QALD4Train, QALD4Test, QALD5Train,
     * QALD5Test} and returns corpus with documents
     *
     * @param dataset
     * @param file
     * @param includeAggregation
     * @param includeUNION
     * @param onlyDBO
     * @param isHybrid
     * @param includeYAGO
     * @return corpus
     */
    public static QALDCorpus load(Dataset dataset, boolean includeYAGO, boolean includeAggregation, boolean includeUNION, boolean onlyDBO, boolean isHybrid) {

        String filePath = "";

        List<Question> questions = new ArrayList<>();

        switch (dataset.name()) {
            case "qald4Train":
                filePath = qald4FileTrain;
                questions = QALD.getQuestions(filePath);
                break;
            case "qald4Test":
                filePath = qald4FileTest;
                questions = QALD.getQuestions(filePath);
                break;
            case "qald5Train":
                filePath = qald5FileTrain;
                questions = QALD.getQuestions(filePath);
                break;
            case "qald5Test":
                filePath = qald5FileTest;
                questions = QALD.getQuestions(filePath);
                break;
            case "qaldSubset":
                filePath = qaldSubset;
                questions = readJSONFile(filePath);
                break;
            case "qald6Train":
                filePath = qald6FileTrain;
                questions = readJSONFile(filePath);
                break;
            case "qald6Test":
                filePath = qald6FileTest;
                questions = readJSONFile(filePath);
                break;
            case "qald7Train":
                filePath = qald7FileTrain;
                questions = readJSONFile(filePath);
                break;
            case "webQuestionsTrain":
                filePath = webQuestionsTrain;
                questions = readWebQuestionsJSONFile(filePath);
                break;
            case "webQuestionsTest":
                filePath = webQuestionsTest;
                questions = readWebQuestionsJSONFile(filePath);
                break;
            case "webQuestionsSubset":
                filePath = webQuestionsSubset;
                questions = readWebQuestionsJSONFile(filePath);
                break;
            default:
                System.err.println("Corpus not found!");
                System.exit(0);
        }

        QALDCorpus corpus = new QALDCorpus();
        corpus.setCorpusName(dataset.name());

        List<AnnotatedDocument> documents = new ArrayList<>();

        System.out.println("Parsing sentences ... \n");

        for (Question q : questions) {

            String text = q.getQuestionText().get(CandidateRetriever.Language.valueOf(ProjectConfiguration.getLanguage()));

            String replacedText = text;

            DependencyParse parse = null;
            //do only for webquestions datasets
            if (dataset.name().toLowerCase().contains("webquestions")) {
                //extract entities with 2 pass over the input
                List<EntityAnnotation> annotations = AnnotationExtractor.getAnnotations(text, 4, 0.90, 0.65, Search.getRetriever());
                HashMap<String, String> replacedMap = new HashMap<>();

                //replace each found entity with Barack_Obama_N_index
                
                for (EntityAnnotation e1 : annotations) {
                    String entityText = e1.getSpannedText(text)+"";

                    replacedText = replacedText.replace(entityText, "Barack_Obama_N"+annotations.indexOf(e1));

                    replacedMap.put("Barack_Obama_N"+annotations.indexOf(e1), entityText+"");
                }

                parse = UDPipe.parse(replacedText, CandidateRetriever.Language.valueOf(ProjectConfiguration.getLanguage()));

                //replace the Barack_Obama_N_index with it's real value, add also replace the POSTAG with PROPN
                if (parse != null) {
                    HashMap<Integer, String> nodes = parse.getNodes();
                    HashMap<Integer, String> changedNodes = new HashMap<>();

                    for (Integer key : nodes.keySet()) {
                        String node = nodes.get(key);

                        if(replacedMap.containsKey(node)){
                            changedNodes.put(key, replacedMap.get(node)+"");
                            
                            parse.setPOSTag(key, "PROPN");
                        }
                        else{
                            changedNodes.put(key, node+"");
                            
                        }
                    }
                    parse.setNodes(changedNodes);
                }
            }
            else{
                parse = UDPipe.parse(replacedText, CandidateRetriever.Language.valueOf(ProjectConfiguration.getLanguage()));
            }
            

            AnnotatedDocument document = new AnnotatedDocument(parse, q);
            

            documents.add(document);

        }

        for (AnnotatedDocument doc : documents) {
            if (!includeAggregation) {
                if (doc.getQaldInstance().getAggregation().equals("true")) {
                    continue;
                }
            }
            if (!includeYAGO) {
                if (doc.getQaldInstance().getQueryText().contains("http://dbpedia.org/class/yago/")) {
                    continue;
                }
            }
            if (!includeUNION) {
                if (doc.getQaldInstance().getQueryText().contains("UNION")) {
                    continue;
                }
            }
            if (!isHybrid) {
                if (doc.getQaldInstance().getHybrid().equals("true")) {
                    continue;
                }
            }
            if (onlyDBO) {
                if (doc.getQaldInstance().getOnlyDBO().equals("false")) {
                    continue;
                }
            }

            corpus.addDocument(doc);
        }

        return corpus;
    }

    private static List<Question> readJSONFile(String filePath) {
        ArrayList<Question> qaldQuestions = new ArrayList<>();

        JSONParser parser = new JSONParser();

        try {

            HashMap obj = (HashMap) parser.parse(new FileReader(filePath));

            JSONArray questions = (JSONArray) obj.get("questions");
            for (int i = 0; i < questions.size(); i++) {
                HashMap o1 = (HashMap) questions.get(i);

                String hybrid = "", onlyDBO = "", aggregation = "";
                if (o1.get("hybrid") instanceof Boolean) {
                    Boolean b = (Boolean) o1.get("hybrid");
                    hybrid = b.toString();
                } else {
                    hybrid = (String) o1.get("hybrid");
                }

                if (o1.get("onlydbo") instanceof Boolean) {
                    Boolean b = (Boolean) o1.get("onlydbo");
                    onlyDBO = b.toString();
                } else {
                    onlyDBO = (String) o1.get("onlydbo");
                }

                if (o1.get("aggregation") instanceof Boolean) {
                    Boolean b = (Boolean) o1.get("aggregation");
                    aggregation = b.toString();
                } else {
                    aggregation = (String) o1.get("aggregation");
                }

                String answerType = (String) o1.get("answertype");

                String id = o1.get("id").toString();

                HashMap queryTextObj = (HashMap) o1.get("query");
                String query = (String) queryTextObj.get("sparql");

                Map<Language, String> questionText = new HashMap<>();

                JSONArray questionTexts = (JSONArray) o1.get("question");
                for (Object qObject : questionTexts) {

                    JSONObject englishQuestionText = (JSONObject) qObject;

                    if (englishQuestionText.get("language").equals("en")) {
                        questionText.put(Language.EN, englishQuestionText.get("string").toString());

                    }
                    if (englishQuestionText.get("language").equals("de")) {
                        questionText.put(Language.DE, englishQuestionText.get("string").toString());

                    }
                    if (englishQuestionText.get("language").equals("es")) {
                        questionText.put(Language.ES, englishQuestionText.get("string").toString());
                    }
                }

                if (query != null) {
                    if (!query.equals("")) {

                        if (query.contains("UNION")) {
                            query = removeUNION(query);
                        }

                        query = query.replace("\n", " ");

                        Question q1 = new Question(questionText, query, onlyDBO, aggregation, answerType, hybrid, id);
                        qaldQuestions.add(q1);
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return qaldQuestions;
    }

    private static List<Question> readWebQuestionsJSONFile(String filePath) {
        ArrayList<Question> qaldQuestions = new ArrayList<>();

        JSONParser parser = new JSONParser();

        try {

            HashMap obj = (HashMap) parser.parse(new FileReader(filePath));

            JSONArray questions = (JSONArray) obj.get("questions");
            for (int i = 0; i < questions.size(); i++) {
                HashMap o1 = (HashMap) questions.get(i);

                String hybrid = "false", onlyDBO = "true", aggregation = "false", answerType = "resource";

                String question = (String) o1.get("questionText");
                String query = (String) o1.get("query");

                Map<Language, String> questionText = new HashMap<>();
                questionText.put(Language.EN, question);

                String id = (i + 1) + "";

                List<String> answers = (List<String>) o1.get("answers");

                Question q1 = new Question(questionText, query, onlyDBO, aggregation, answerType, hybrid, id);
                q1.setAnswers(answers);

                qaldQuestions.add(q1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return qaldQuestions;
    }

    private static String removeUNION(String q) {

        while (q.contains("UNION")) {
            String s1 = q.substring(q.indexOf("UNION"));
            String s2 = s1.substring(0, s1.indexOf("}") + 1);

            q = q.replace(s2, "");
            String tail = q.substring(q.lastIndexOf("}") + 1);
            if (!tail.trim().isEmpty()) {
                q = q.replace(tail, " ");
            }

            q = q.replace("{", " ");
            q = q.replace("}", " ");
            q = q.replace("WHERE", "WHERE { ");
            q = q + "}" + tail;
        }

        return q;
    }
}
