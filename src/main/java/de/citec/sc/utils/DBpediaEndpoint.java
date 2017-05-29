/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.utils;

import de.citec.sc.learning.QueryConstructor;
import de.citec.sc.variable.State;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

/**
 *
 * @author sherzod
 */
public class DBpediaEndpoint {

    private static String endpointURL = "http://purpur-v11:8890/sparql";
    private static boolean isRemote = false;
    private static HashMap<String, List<String>> cacheOfResults = new HashMap<>();
    private static HashMap<String, List<String>> cacheOfCanonicalForms = new HashMap<>();
    private static HashMap<String, Boolean> cacheOfQueries = new HashMap<>();
    private static HashMap<String, Boolean> cacheOfDomains = new HashMap<>();
    private static HashMap<String, Boolean> cacheOfRanges = new HashMap<>();
    private static HashMap<String, Boolean> cacheOfDependentNodes = new HashMap<>();

    public static enum AnswerType {

        Date, String, URI
    };

    public static void setToRemote() {
        endpointURL = "http://dbpedia.org/sparql";
        isRemote = true;
    }

    public static String getCanonicalForm(String query) {

        query = query.replaceAll("[?][v][0-9]+", "?z");
        query = query.replace("?uri", "?z");

        //get body
        String body = query.substring(query.indexOf("WHERE"));
        String head = query.substring(0, query.indexOf("WHERE"));

//        query = query.replaceAll("\\s", "");
        body = body.replaceAll("\\s", "");
        query = head.trim() + " " + body;

        return query;
    }

    public static boolean isObjectTriple(String headURI, String depURI) {
        if (cacheOfDependentNodes.containsKey(headURI + "==" + depURI + "Object")) {
            return cacheOfDependentNodes.get(headURI + "==" + depURI + "Object");
        }

        String property = "";
        String resource = "";
        String classResourcePart = "";
        String classPropertyPart = "";

        String query = "";
        if (isProperty(depURI) && isProperty(headURI)) {
            property = "?x <" + headURI + "> ?y. ?z <" + depURI + "> ?y.";

            query = "SELECT DISTINCT ?y WHERE { " + property + " }";
        } else if (isProperty(depURI) && isResource(headURI)) {
            property = depURI;
            resource = headURI;

            query = "SELECT DISTINCT ?s WHERE { ?s <" + property + "> <" + resource + ">. }";
        } else if (isProperty(headURI) && isResource(depURI)) {
            property = headURI;
            resource = depURI;

            query = "SELECT DISTINCT ?s WHERE { ?s <" + property + "> <" + resource + ">. }";
        } else if (isClass(headURI) && isResource(depURI)) {
            classPropertyPart = headURI.substring(0, headURI.indexOf("###"));
            classResourcePart = headURI.replace(classPropertyPart + "###", "");

            resource = depURI;

            query = "SELECT DISTINCT ?s WHERE { ?s ?p <" + resource + ">. ?s <" + classPropertyPart + "> <" + classResourcePart + ">. }";
        } else if (isClass(depURI) && isResource(headURI)) {
            classPropertyPart = depURI.substring(0, depURI.indexOf("###"));
            classResourcePart = depURI.replace(classPropertyPart + "###", "");

            resource = headURI;

            query = "SELECT DISTINCT ?s WHERE { ?s ?p <" + resource + ">. ?s <" + classPropertyPart + "> <" + classResourcePart + ">. }";
        } else if (isClass(headURI) && isProperty(depURI)) {
            classPropertyPart = headURI.substring(0, headURI.indexOf("###"));
            classResourcePart = headURI.replace(classPropertyPart + "###", "");

            property = depURI;

            query = "SELECT DISTINCT ?s WHERE { ?s <" + property + "> ?o. ?o <" + classPropertyPart + "> <" + classResourcePart + ">. }";
        } else if (isClass(depURI) && isProperty(headURI)) {
            classPropertyPart = depURI.substring(0, depURI.indexOf("###"));
            classResourcePart = depURI.replace(classPropertyPart + "###", "");

            property = headURI;

            query = "SELECT DISTINCT ?s WHERE { ?s <" + property + "> ?o. ?o <" + classPropertyPart + "> <" + classResourcePart + ">. }";
        } else if (isResource(depURI) && isResource(headURI)) {

            query = "SELECT DISTINCT ?s WHERE { ?s1 ?p1 <" + depURI + ">. ?s2 ?p2 <" + headURI + ">. }";
        }

        boolean isObject = DBpediaEndpoint.isValidQuery(query, true);

        cacheOfDependentNodes.put(headURI + "==" + depURI + "Object", isObject);

        return isObject;
    }

    public static boolean isSubjectTriple(String headURI, String depURI) {

        if (cacheOfDependentNodes.containsKey(headURI + "==" + depURI + "Subject")) {
            return cacheOfDependentNodes.get(headURI + "==" + depURI + "Subject");
        }

        String property = "";
        String resource = "";
        String classResourcePart = "";
        String classPropertyPart = "";

        String query = "";
        if (isProperty(headURI) && isProperty(depURI)) {
            property = "?x <" + headURI + "> ?y. ?x <" + depURI + "> ?t.";

            query = "SELECT DISTINCT ?x WHERE { " + property + " }";
        } else if (isProperty(depURI) && isResource(headURI)) {
            property = depURI;
            resource = headURI;

            query = "SELECT DISTINCT ?o WHERE { <" + resource + "> <" + property + "> ?o. }";
        } else if (isProperty(headURI) && isResource(depURI)) {
            property = headURI;
            resource = depURI;

            query = "SELECT DISTINCT ?o WHERE { <" + resource + "> <" + property + "> ?o. }";
        } else if (isClass(headURI) && isResource(depURI)) {
            classPropertyPart = headURI.substring(0, headURI.indexOf("###"));
            classResourcePart = headURI.replace(classPropertyPart + "###", "");

            resource = depURI;

            query = "SELECT DISTINCT ?o WHERE { <" + resource + "> ?p ?o. ?o <" + classPropertyPart + "> <" + classResourcePart + ">. }";

            query = "SELECT DISTINCT ?o WHERE { <" + resource + "> <" + classPropertyPart + "> <" + classResourcePart + ">. }";

        } else if (isClass(depURI) && isResource(headURI)) {
            classPropertyPart = depURI.substring(0, depURI.indexOf("###"));
            classResourcePart = depURI.replace(classPropertyPart + "###", "");

            resource = headURI;

            query = "SELECT DISTINCT ?o WHERE { <" + resource + "> ?p ?o. ?o <" + classPropertyPart + "> <" + classResourcePart + ">. }";
        } else if (isClass(headURI) && isProperty(depURI)) {
            classPropertyPart = headURI.substring(0, headURI.indexOf("###"));
            classResourcePart = headURI.replace(classPropertyPart + "###", "");

            property = depURI;

            query = "SELECT DISTINCT ?o WHERE { ?s <" + property + "> ?o. ?s <" + classPropertyPart + "> <" + classResourcePart + ">. }";
        } else if (isClass(depURI) && isProperty(headURI)) {
            classPropertyPart = depURI.substring(0, depURI.indexOf("###"));
            classResourcePart = depURI.replace(classPropertyPart + "###", "");

            property = headURI;

            query = "SELECT DISTINCT ?o WHERE { ?s <" + property + "> ?o. ?s <" + classPropertyPart + "> <" + classResourcePart + ">. }";
        } else if (isClass(depURI) && isClass(headURI)) {
            classPropertyPart = depURI.substring(0, depURI.indexOf("###"));
            classResourcePart = depURI.replace(classPropertyPart + "###", "");

            String classPropertyPart2 = headURI.substring(0, headURI.indexOf("###"));
            String classResourcePart2 = headURI.replace(classPropertyPart2 + "###", "");

            query = "SELECT DISTINCT ?s WHERE { ?s <" + classPropertyPart + "> <" + classResourcePart + ">. ?s <" + classPropertyPart2 + "> <" + classResourcePart2 + ">. }";
        } else if (isResource(depURI) && isResource(headURI)) {

            query = "SELECT DISTINCT ?s WHERE { <" + depURI + "> ?p1 ?o1. <" + headURI + "> ?p2 ?o2. }";
        }

        boolean isSubject = DBpediaEndpoint.isValidQuery(query, true);

        cacheOfDependentNodes.put(headURI + "==" + depURI + "Subject", isSubject);

        return isSubject;
    }

    private static boolean isClass(String uri) {
        if (uri.contains("###")) {
            return true;
        }
        return false;
    }

    private static boolean isProperty(String uri) {
        if ((uri.startsWith("http://dbpedia.org/ontology/") || uri.startsWith("http://dbpedia.org/property/")) && !uri.contains("###")) {
            return true;
        }
        return false;
    }

    private static boolean isResource(String uri) {
        if (uri.startsWith("http://dbpedia.org/resource/")) {
            return true;
        }
        return false;
    }

    public static String getNormalizedQuery(String query) {

        //get body
        String body = query.substring(query.indexOf("WHERE"));
        String head = query.substring(0, query.indexOf("WHERE"));

        body = body.replaceAll("\\s+", " ");
        query = head.trim() + " " + body;

        return query;
    }

    public static AnswerType getAnswertype(String query) {
        List<String> result = runQuery(query);

        if (!result.isEmpty()) {
            return checkAnswerType(result.get(0));
        }

        return AnswerType.URI;
    }

    private static AnswerType checkAnswerType(String token) {
        List<String> datePatterns = new ArrayList<>();

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

        datePatterns.add(monthYearPattern);
        datePatterns.add(datePattern);

        for (String pattern : datePatterns) {
            if (token.matches(pattern)) {
                return AnswerType.Date;
            }
        }

        List<String> uriPatterns = new ArrayList<>();

        uriPatterns.add("^(http://|https://)+.+$");

        for (String pattern : uriPatterns) {
            if (token.matches(pattern)) {
                return AnswerType.URI;
            }
        }

        return AnswerType.String;
    }

    public static void loadCachedQueries() {

        //validCanonicalFormQueries
        Set<String> setValidCanonicalFormQueries = FileFactory.readFile("validCanonicalFormQueries.txt");

        for (String s : setValidCanonicalFormQueries) {

            String seperator = "\tQUERY:";

            String[] chunks = s.split(seperator);

            List<String> q = Arrays.asList(chunks);
            ArrayList<String> queries = new ArrayList<>(q);

            //the first element is canonicalForm
            String key = queries.get(0);
            queries.remove(key);

            cacheOfCanonicalForms.put(key, queries);

        }

        Set<String> setValidQueries = FileFactory.readFile("validQueries.txt");

        for (String s : setValidQueries) {

            String seperator = "\tVALID:";
            String key = s.substring(0, s.indexOf(seperator));
            String valueAsString = s.substring(s.indexOf(seperator) + seperator.length());

            boolean b = true ? valueAsString.equals("true") : false;

            cacheOfQueries.put(key, b);
        }

        Set<String> setvalidDomainQueries = FileFactory.readFile("validDomainQueries.txt");

        for (String s : setvalidDomainQueries) {

            String seperator = "\tVALID:";
            String key = s.substring(0, s.indexOf(seperator));
            String valueAsString = s.substring(s.indexOf(seperator) + seperator.length());

            boolean b = true ? valueAsString.equals("true") : false;

            cacheOfDomains.put(key, b);
        }

        Set<String> setvalidRangeQueries = FileFactory.readFile("validRangeQueries.txt");

        for (String s : setvalidRangeQueries) {

            String seperator = "\tVALID:";
            String key = s.substring(0, s.indexOf(seperator));
            String valueAsString = s.substring(s.indexOf(seperator) + seperator.length());

            boolean b = true ? valueAsString.equals("true") : false;

            cacheOfRanges.put(key, b);
        }

    }

    public static void saveCachedQueries() {

        //canonicalForms
        String validCanonicalFormQueries = "";
        for (String q : cacheOfCanonicalForms.keySet()) {

            validCanonicalFormQueries += q;

            List<String> queries = cacheOfCanonicalForms.get(q);

            for (String query : queries) {
                validCanonicalFormQueries += "\tQUERY:" + query;
            }

            validCanonicalFormQueries += "\n";
        }

        FileFactory.writeListToFile("validCanonicalFormQueries.txt", validCanonicalFormQueries, false);

        //
        String validQueries = "";
        for (String q : cacheOfQueries.keySet()) {
            validQueries += q + "\tVALID:" + cacheOfQueries.get(q) + "\n";
        }
        FileFactory.writeListToFile("validQueries.txt", validQueries, false);

        String validDomainQueries = "";
        for (String q : cacheOfDomains.keySet()) {
            validDomainQueries += q + "\tVALID:" + cacheOfDomains.get(q) + "\n";
        }
        FileFactory.writeListToFile("validDomainQueries.txt", validDomainQueries, false);

        String validRangeQueries = "";
        for (String q : cacheOfRanges.keySet()) {
            validRangeQueries += q + "\tVALID:" + cacheOfRanges.get(q) + "\n";
        }
        FileFactory.writeListToFile("validRangeQueries.txt", validRangeQueries, false);

    }

    private static boolean isEndPointAvailable() {

        try {
            String query = "PREFIX dbo: <http://dbpedia.org/ontology/>\n"
                    + "PREFIX res: <http://dbpedia.org/resource/>\n"
                    + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                    + "SELECT DISTINCT ?uri\n"
                    + "WHERE {\n"
                    + "        ?uri rdf:type dbo:Film.\n"
                    + "        ?uri dbo:starring res:Tom_Cruise .\n"
                    + "}";
            Query sparqlQuery = QueryFactory.create(query);

            //QueryExecution qq = QueryExecutionFactory.create(query);
            QueryExecution qexec = QueryExecutionFactory.sparqlService(endpointURL, sparqlQuery);

            // Set the DBpedia specific timeout.
            ((QueryEngineHTTP) qexec).addParam("timeout", "10000");

            // Execute.
            ResultSet rs = qexec.execSelect();

            if (rs.next() != null) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * returns true if the SPARQL query derived from the state returns an answer
     *
     * @param state State
     * @param boolean isTraining (if true, the query will be transformed into
     * SELECT {...} LIMIT 1, to speed up query time
     * @return boolean
     */
    public static boolean isValidState(State state) {

        String query = QueryConstructor.getSPARQLQuery(state);

        return isValidQuery(query, true);
    }

    /**
     * returns true if the SPARQL query returns an answer
     *
     * @param String query
     * @param boolean isTraining (if true, the query will be transformed into
     * SELECT {...} LIMIT 1, to speed up query time
     * @return boolean
     */
    public static boolean isValidQuery(String query, boolean isTraining) {

        if (query.equals("")) {
            return false;
        }

        if (cacheOfQueries.containsKey(query)) {
            return cacheOfQueries.get(query);
        }

        String canonicalForm = getCanonicalForm(query);

        //remove spaces from query
        query = getNormalizedQuery(query);

        if (isTraining) {
            if (query.contains("SELECT") && !query.contains("LIMIT")) {
                query += " LIMIT 1";
            }
        }

        boolean isValid = false;

        if (cacheOfCanonicalForms.containsKey(canonicalForm)) {
            //get query, which is value from canonical map
            //get value for the given query, key
            List<String> queries = cacheOfCanonicalForms.get(canonicalForm);

            if (queries.contains(query) && cacheOfQueries.containsKey(query)) {

                isValid = cacheOfQueries.get(query);

            } else {

                List<String> results = runQuery(query);

                if (!results.isEmpty()) {
                    cacheOfQueries.put(query, true);

                    isValid = true;

                } else {
                    cacheOfQueries.put(query, false);

                    isValid = false;
                }
                //update canonicalForm Map
                queries.add(query);
                cacheOfCanonicalForms.put(canonicalForm, queries);
            }

        } else {

            //add new entries to both Maps
            List<String> results = runQuery(query);

            if (!results.isEmpty()) {
                cacheOfQueries.put(query, true);

                isValid = true;
            } else {
                cacheOfQueries.put(query, false);

                isValid = false;
            }

            List<String> queries = new ArrayList<>();
            //update canonicalForm Map
            queries.add(query);
            cacheOfCanonicalForms.put(canonicalForm, queries);

        }

        return isValid;
    }

    /**
     * returns true if the domain of the property matches types of the given
     * resource
     *
     * @param property
     * @param resource
     *
     * @return true if there exists such property and resource with type and
     * domain restriction
     */
    public static boolean domainMatches(String property, String resource) {

        String query = "PREFIX dbo: <http://dbpedia.org/ontology/>\n"
                + "PREFIX res: <http://dbpedia.org/resource/>\n"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "ASK\n"
                + "WHERE {\n"
                + "        <" + property + "> rdfs:domain ?d . \n"
                + "        <" + resource + "> rdf:type ?d .\n"
                + "FILTER (?d != <http://dbpedia.org/ontology/Agent>) \n"
                + "}";

        if (cacheOfDomains.containsKey(property + "###" + resource)) {

            return cacheOfDomains.get(property + "###" + resource);
        }

        try {
            Query sparqlQuery = QueryFactory.create(query);

            //QueryExecution qq = QueryExecutionFactory.create(query);
            QueryExecution qexec = QueryExecutionFactory.sparqlService(endpointURL, sparqlQuery);

            // Set the DBpedia specific timeout.
            ((QueryEngineHTTP) qexec).addParam("timeout", "10000");

            boolean b = qexec.execAsk();

            if (b == true) {
                cacheOfDomains.put(property + "###" + resource, b);
            }

            qexec.close();

            return b;

        } catch (Exception e) {
//            e.printStackTrace();
        }

        return false;

    }

    /**
     * returns true if the range of the property matches types of the given
     * resource
     *
     * @param property
     * @param resource
     *
     * @return true if there exists such property and resource with type and
     * domain restriction
     */
    public static boolean rangeMatches(String property, String resource) {

        String query = "PREFIX dbo: <http://dbpedia.org/ontology/>\n"
                + "PREFIX res: <http://dbpedia.org/resource/>\n"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "ASK\n"
                + "WHERE {\n"
                + "        <" + property + "> rdfs:range ?d . \n"
                + "        <" + resource + "> rdf:type ?d .\n"
                + "FILTER (?d != <http://dbpedia.org/ontology/Agent>) \n"
                + "}";

        if (cacheOfRanges.containsKey(property + "###" + resource)) {
            return cacheOfRanges.get(property + "###" + resource);
        }

        try {
            Query sparqlQuery = QueryFactory.create(query);

            //QueryExecution qq = QueryExecutionFactory.create(query);
            QueryExecution qexec = QueryExecutionFactory.sparqlService(endpointURL, sparqlQuery);

            // Set the DBpedia specific timeout.
            ((QueryEngineHTTP) qexec).addParam("timeout", "10000");

            boolean b = qexec.execAsk();

            cacheOfRanges.put(property + "###" + resource, b);

            qexec.close();

            return b;
        } catch (Exception e) {

        }

        return false;

    }

    public static String getRange(String property) {

        String query = "PREFIX dbo: <http://dbpedia.org/ontology/>\n"
                + "PREFIX res: <http://dbpedia.org/resource/>\n"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "SELECT ?d \n"
                + "WHERE {\n"
                + "        <" + property + "> rdfs:range ?d . \n"
                //                + "FILTER (?d != <http://dbpedia.org/ontology/Agent>) \n"
                + "}";

        List<String> result = runQuery(query);

        if (result == null) {
            return "UNKNOWN";
        }

        if (!result.isEmpty()) {
            return result.get(0);
        }

        return "UNKNOWN";

    }

    public static String getDomain(String property) {

        String query = "PREFIX dbo: <http://dbpedia.org/ontology/>\n"
                + "PREFIX res: <http://dbpedia.org/resource/>\n"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "SELECT ?d \n"
                + "WHERE {\n"
                + "        <" + property + "> rdfs:domain ?d . \n"
                //                + "FILTER (?d != <http://dbpedia.org/ontology/Agent>) \n"
                + "}";

        List<String> result = runQuery(query);

        if (result == null) {
            return "UNKNOWN";
        }

        if (!result.isEmpty()) {
            return result.get(0);
        }

        return "UNKNOWN";

    }

    public static List<String> runQuery(String query) {

        List<String> results = new ArrayList<>();

        if (cacheOfResults.containsKey(query)) {
            return cacheOfResults.get(query);
        }

        try {
            Query sparqlQuery = QueryFactory.create(query);

            //QueryExecution qq = QueryExecutionFactory.create(query);
            QueryExecution qexec = QueryExecutionFactory.sparqlService(endpointURL, sparqlQuery);

            // Set the DBpedia specific timeout.
            ((QueryEngineHTTP) qexec).addParam("timeout", "10000");

            //if ASK Query
            if (sparqlQuery.isAskType()) {
                boolean b = qexec.execAsk();
                results.add(b + "");
                cacheOfResults.put(query, results);
                return results;
            }

            //SELECT Query
            // Execute.
            ResultSet rs = qexec.execSelect();

            //get return variables
            List<String> returnVars = sparqlQuery.getResultVars();

            //this condition occurs when SELECT * WHERE and the body doesn't have any variable, all triples contain only resources and properties
            //it means the query doesn't return an answer, return empty list
            if (returnVars.isEmpty() && sparqlQuery.isSelectType()) {
                cacheOfResults.put(query, results);

                qexec.close();

                return results;
            }

            while (rs.hasNext()) {

                QuerySolution s = rs.next();

                String r = "";
                for (String v : returnVars) {
                    RDFNode node = s.get(v);

                    r += node.toString() + "\t";
                }

                results.add(r.trim());
            }

            qexec.close();

        } catch (Exception e) {
//            System.out.println("Exception while querying endpoint : " + query + "\n\n");
//            e.printStackTrace();

        }

        cacheOfResults.put(query, results);

        return results;
    }

}
