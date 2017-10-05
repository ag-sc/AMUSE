/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.gerbil.instance;

import de.citec.sc.utils.DBpediaEndpoint;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * @author sherzod
 */
public class Convert {

    public static QALDInstance convert(String questionString, String queryString, String languageString) {
        QALDInstance qaldInstance = new QALDInstance();
        qaldInstance.setQuestions(getQuestionsArray(questionString, queryString, languageString));

        return qaldInstance;
    }

    private static Questions[] getQuestionsArray(String questionString, String queryString, String languageString) {
        Questions[] questionsArray = new Questions[1];

        Questions questions = getQuestions(questionString, queryString, languageString);

        questionsArray[0] = questions;

        return questionsArray;
    }

    private static Questions getQuestions(String questionString, String queryString, String languageString) {
        Questions questions = new Questions();

        Set<String> queryResults = DBpediaEndpoint.runQuery(queryString, false);

        questions.setAnswers(getAnswersArray(questionString, queryResults, languageString));
        questions.setAnswertype(getAnswerType(queryResults));

        questions.setId("1");
        questions.setQuery(getQuery(queryString));
        questions.setQuestion(getQuestion(questionString, languageString));

        return questions;
    }

    private static Question[] getQuestion(String questionString, String languageString) {
        Question question = new Question();
        question.setLanguage(languageString);
        question.setString(questionString);

        Question[] q = new Question[1];
        q[0] = question;

        return q;

    }

    private static Query getQuery(String queryString) {
        try {
            queryString = StringEscapeUtils.unescapeJava(queryString);
            queryString = URLDecoder.decode(queryString, "UTF-8");
            queryString = queryString.replace("\n", " ");
            queryString = queryString.trim();
        } catch (Exception e) {

        }

        Query q = new Query();
        q.setSparql(queryString);

        return q;
    }

    private static Answers[] getAnswersArray(String questionString, Set<String> queryResults, String languageString) {
        Answers[] answersArray = new Answers[1];

        answersArray[0] = getAnswers(questionString, queryResults, languageString);

        return answersArray;
    }

    private static Answers getAnswers(String questionString, Set<String> queryResults, String languageString) {

        String var = getVarsName(queryResults);

        String type = getType(var);

        Answers answers = new Answers();
        String[] vars = new String[1];
        vars[0] = var;

        Head head = new Head();
        head.setVars(vars);
        answers.setHead(head);

        answers.setResults(getResults(var, type, queryResults));

        return answers;
    }

    private static Results getResults(String var, String type, Set<String> queryResults) {
        Results results = new Results();

        Bindings[] bindingsArray = new Bindings[queryResults.size()];

        int c = 0;
        for (String r : queryResults) {
            Bindings bindings = new Bindings();

            Uri uri = new Uri();
            uri.setType(type);

            try {
                r = StringEscapeUtils.unescapeJava(r);
                r = URLDecoder.decode(r, "UTF-8");
            } catch (Exception e) {

            }

            uri.setValue(r);

            bindings.setUri(uri);

            bindingsArray[c] = bindings;
            c++;
        }

        results.setBindings(bindingsArray);

        return results;
    }

    private static String getType(String var) {
        String type = "";

        switch (var) {
            case "c":
                type = "literal";
                break;
            case "uri":
                type = "uri";
                break;
            case "date":
                type = "literal";
                break;
            case "string":
                type = "literal";
                break;
        }

        return type;
    }

    private static String getVarsName(Set<String> results) {
        String var = "uri";

        for (String r : results) {
            if (r.startsWith("http://")) {
                var = "uri";

                return var;
            } else if (isTokenDateType(r)) {
                var = "date";

                return var;
            } else if (isTokenNumber(r)) {
                var = "c";

                return var;
            } else {
                var = "string";
            }
        }

        return var;
    }

    private static String getAnswerType(Set<String> results) {
        String var = "resource";

        for (String r : results) {
            if (r.startsWith("http://")) {
                var = "resource";

                return var;
            } else if (isTokenDateType(r)) {
                var = "date";

                return var;
            } else if (isTokenNumber(r)) {
                var = "number";

                return var;
            } else {
                var = "string";
            }

            break;
        }

        return var;
    }

    private static boolean isTokenDateType(String token) {
        List<String> patterns = new ArrayList<>();

        String monthYearPattern = "((January|Jan|February|Feb|March|Mar|April|Apr|May|June|Jun|July|Jul|August|Aug|September|Sep|October|Oct|November|Nov|December|Dec)\\s+((19|20)\\d\\d))";//March 2015, Aug 1999
        monthYearPattern += "|((0?[1-9]|1[012])/((19|20)\\d\\d))"; //mm/YYYY
        monthYearPattern += "|((0?[1-9]|1[012])-((19|20)\\d\\d))"; //mm-YYYY

        String datePattern = "((0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((19|20)\\d\\d))";//dd/MM/YYYY
        datePattern += "|((0?[1-9]|[12][0-9]|3[01])-(0?[1-9]|1[012])-((19|20)\\d\\d))"; //dd-MM-YYYY
        datePattern += "|((January|Jan|February|Feb|March|Mar|April|Apr|May|June|Jun|July|Jul|August|Aug|September|Sep|October|Oct|November|Nov|December|Dec)\\s+((1st)|(2nd)|(3rd)|(([1-9]|[12][0-9]|3[01])(th)))(,)?\\s+((19|20)\\d\\d))"; //Jan 1st 2015, August 14th 1999
        datePattern += "|((January|Jan|February|Feb|March|Mar|April|Apr|May|June|Jun|July|Jul|August|Aug|September|Sep|October|Oct|November|Nov|December|Dec)\\s+(0?[1-9]|[12][0-9]|3[01])(,)?\\s+((19|20)\\d\\d))"; //Jan 1st 2015, August 14th 1999
        datePattern += "|((0?[1-9]|[12][0-9]|3[01])\\s+(January|Jan|February|Feb|March|Mar|April|Apr|May|June|Jun|July|Jul|August|Aug|September|Sep|October|Oct|November|Nov|December|Dec)\\s+((19|20)\\d\\d))"; //Jan 1st 2015, August 14th 1999
        datePattern += "|((0?[1-9]|1[012])/(0?[1-9]|[12][0-9]|3[01])/((19|20)\\d\\d))"; //MM/dd/YYYY
        datePattern += "|((0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01])-((19|20)\\d\\d))"; //MM-dd-YYYY

        patterns.add(monthYearPattern);
        patterns.add(datePattern);

        for (String pattern : patterns) {
            if (token.matches(pattern)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isTokenNumber(String token) {
        List<String> patterns = new ArrayList<>();

        patterns.add("(^[-+]?\\d+)");
        patterns.add("(^[+]?\\d+)");
        patterns.add("(^[+]?\\d+)");
        patterns.add("(^[-+]?\\d+\\.\\d+)");
        patterns.add("(^[-+]?\\d+\\.\\d+)");
        patterns.add("\\d+");
        patterns.add("\\d{4}");

        for (String pattern : patterns) {
            if (token.matches(pattern)) {
                return true;
            }
        }

        return false;
    }

}
