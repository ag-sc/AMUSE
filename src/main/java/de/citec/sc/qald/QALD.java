/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.qald;

import de.citec.sc.query.CandidateRetriever.Language;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author sherzod
 */
public class QALD {
    

    public static ArrayList<Question> getQuestions(String fileName) {
        try {

            File fXmlFile = new File(fileName);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();

            ArrayList<Question> questions = new ArrayList<Question>();

            NodeList nList = doc.getElementsByTagName("question");

            System.out.println(nList.getLength());

            int count = 0;
            ArrayList<String> tempList = new ArrayList<>();

            for (int temp = 0; temp < nList.getLength(); temp++) {

                Node nNode = nList.item(temp);

                boolean isAdded = false;
                String onlyDBO = "", aggregation = "", answerType = "", hybrid = "";
                String id = "";

                if (nNode.getAttributes().getNamedItem("onlydbo") != null
                        && nNode.getAttributes().getNamedItem("aggregation") != null
                        && nNode.getAttributes().getNamedItem("answertype") != null
                        && nNode.getAttributes().getNamedItem("hybrid") != null) {

                    onlyDBO = nNode.getAttributes().getNamedItem("onlydbo").getTextContent();
                    aggregation = nNode.getAttributes().getNamedItem("aggregation").getTextContent();
                    answerType = nNode.getAttributes().getNamedItem("answertype").getTextContent();
                    hybrid = nNode.getAttributes().getNamedItem("hybrid").getTextContent();
                }

                if (nNode.getAttributes().getNamedItem("id") != null) {
                    String idd = nNode.getAttributes().getNamedItem("id").getTextContent();
                    id = idd;
                }

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;

                    String query = "", question = "", keywords = "";

                    try {
                        if (hybrid.equals("true")) {
                            question = getTagValue("string", eElement);
                            query = getTagValue("pseudoquery", eElement);
                        } else {
                            question = getTagValue("string", eElement);
                            keywords = getTagValue("keywords", eElement);
                            query = getTagValue("query", eElement);
                        }

                    } catch (Exception e) {
                        System.err.println("Error parsing: " + id);
                    }

                    if (!question.equals("") && !query.equals("")) {
                        if (!query.contains("OUT OF SCOPE")) {
                            tempList.add(question);
                            isAdded = true;
                        }

                    }

                    Map<Language, String> questionMap = new HashMap<>();
                    questionMap.put(Language.EN, question);
                    
                    Question q1 = new Question(questionMap, query, onlyDBO, aggregation, answerType, hybrid, id);

                    if (!query.contains("OUT OF SCOPE")) {
                        if (!question.equals("") && !query.equals("")) {
                            questions.add(q1);
                        }
                    }
                }
            }
            return questions;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getTagValue(String sTag, Element eElement) {

        NodeList temp = eElement.getElementsByTagName(sTag);

        if (temp.item(0) == null) {
            return "";

        } else {
            NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();

            Node nValue = (Node) nlList.item(0);

            return nValue.getNodeValue();
        }

    }
}
