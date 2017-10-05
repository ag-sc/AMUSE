/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.learning;

import de.citec.sc.dudes.rdf.ExpressionFactory;
import de.citec.sc.dudes.rdf.RDFDUDES;
import de.citec.sc.qald.Predicate;
import de.citec.sc.qald.SPARQLParser;
import de.citec.sc.qald.Term;
import de.citec.sc.qald.Triple;
import de.citec.sc.qald.Variable;
import de.citec.sc.utils.FreshVariable;
import de.citec.sc.variable.State;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author sherzod
 */
public class QueryConstructor {

    private static Map<Integer, String> semanticTypes;
    private static Map<Integer, String> specialSemanticTypes;
    private static Set<String> validPOSTags;
    private static Set<String> validEdges;
    private static ExpressionFactory expressions;

    public static void initialize(Map<Integer, String> sp, Map<Integer, String> s, Set<String> v, Set<String> edges) {

        specialSemanticTypes = sp;

        semanticTypes = s;

        validPOSTags = v;

        validEdges = edges;

        expressions = new ExpressionFactory();
    }

    public static String getSPARQLQuery(State state) {

        if (specialSemanticTypes.isEmpty()) {
            try {
                throw new RuntimeException("DUDE, you didn't initialize QueryConstructor.initialize method !!! ");
            } catch (Exception ex) {
                throw new RuntimeException("DUDE, you didn't initialize QueryConstructor.initialize method !!! ");
            }
        }

        String query = "";

        try {
            //instantiate DUDES with URIs
            HashMap<Integer, RDFDUDES> instantiatedDUDES = instantiateDUDES(state);

            RDFDUDES headDUDE = SemanticComposition.compose(state, instantiatedDUDES, validPOSTags, semanticTypes, specialSemanticTypes);

            if (headDUDE == null) {
                return "";
            }

            headDUDE.postprocess();

            boolean hasCorrectQueryType = true;
            //1=SELECT, 2 = COUNT , 3 = ASK
            if (state.getQueryTypeVariable().getType() == 3) {
                hasCorrectQueryType = false;
            }

//            List<Integer> tokenIDs = new ArrayList<>(state.getDocument().getParse().getNodes().keySet());
//            Collections.sort(tokenIDs);
//        
//            Integer firstToken = tokenIDs.get(0);
//            String firstPOS = state.getDocument().getParse().getPOSTag(firstToken);
//
//            String questionString = state.getDocument().getQuestionString();
//            
//            if(firstPOS.equals("PRON")){
//                isSELECTQuery = false;
//            }
//
////            if (questionString.startsWith("Did") || questionString.startsWith("Does") || questionString.startsWith("Do") || questionString.startsWith("Is") || questionString.startsWith("Were") || questionString.startsWith("Was") || questionString.startsWith("Are")) {
////                isSELECTQuery = false;
////            }
//
            boolean hasReturnVariable = false;
            for (Integer i : state.getHiddenVariables().keySet()) {
                if (specialSemanticTypes.containsKey(state.getHiddenVariables().get(i).getDudeId())) {
                    //check if any slot has this dep node
//                    if (state.getSlotVariables().containsKey(i)) {
                    hasReturnVariable = true;
                    break;
//                    }
                }
            }

            boolean isSELECTQuery = false;
            if (hasReturnVariable && hasCorrectQueryType) {
                isSELECTQuery = true;
            }

            query = headDUDE.convertToSPARQL(isSELECTQuery).toString();

            //remove double dots from the query
            if (query.contains(" . . ")) {
                query = query.replace(" . . ", " . ");
            }

        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println(state.toString());

        }

        return query;
    }

    public static Set<Triple> getTriples(State state) {

        Set<Triple> triples = new HashSet<>();

        try {
            String query = getSPARQLQuery(state);

            triples = SPARQLParser.extractTriplesFromQuery(query);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return triples;
    }

    private static HashMap<Integer, RDFDUDES> instantiateDUDES(State state) {
        HashMap<Integer, RDFDUDES> instantiatedDudes = new HashMap<>();

        //instantiate dudes with retrieved URIs
        for (Integer nodeIndex : state.getDocument().getParse().getNodes().keySet()) {

            RDFDUDES dude = null;
            String dudeName = "NULL";
            String uri = state.getHiddenVariables().get(nodeIndex).getCandidate().getUri();

            int dudeID = state.getHiddenVariables().get(nodeIndex).getDudeId();

            //special semantic type
            if (specialSemanticTypes.containsKey(dudeID)) {
                dudeName = specialSemanticTypes.get(dudeID);

            } else if (semanticTypes.containsKey(dudeID)) {
                dudeName = semanticTypes.get(dudeID);
            }

            switch (dudeName) {
                case "NULL":
                    instantiatedDudes.put(nodeIndex, dude);
                    break;
                case "Individual":
                    RDFDUDES someIndividual = new RDFDUDES(RDFDUDES.Type.INDIVIDUAL);

                    someIndividual.instantiateIndividual(uri);
                    instantiatedDudes.put(nodeIndex, someIndividual);
                    break;
                case "Property":
                    RDFDUDES someProperty = new RDFDUDES(RDFDUDES.Type.PROPERTY, "1", "2");

                    someProperty.instantiateProperty(uri);
                    instantiatedDudes.put(nodeIndex, someProperty);
                    break;
                case "Class":
                    RDFDUDES someClass = new RDFDUDES(RDFDUDES.Type.CLASS, "1");
                    someClass.instantiateProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
                    //restriction class
                    if (uri.contains("###")) {
                        String resourceURI = uri.substring(uri.indexOf("###") + 3);

                        someClass.instantiateObject(resourceURI);
                    }
                    instantiatedDudes.put(nodeIndex, someClass);
                    break;
                case "RestrictionClass":
                    RDFDUDES someRestrictionClass = new RDFDUDES(RDFDUDES.Type.CLASS, "1");

                    if (uri.contains("###")) {
                        String classURI = uri.substring(0, uri.indexOf("###"));
                        String resourceURI = uri.substring(uri.indexOf("###") + 3);

                        someRestrictionClass.instantiateObject(resourceURI);
                        someRestrictionClass.instantiateProperty(classURI);
                    }
                    instantiatedDudes.put(nodeIndex, someRestrictionClass);
                    break;

                case "UnderSpecifiedClass":
                    RDFDUDES someUnderSpecifiedClass = new RDFDUDES(RDFDUDES.Type.CLASS, "1");

                    int nextNumber = FreshVariable.get();

                    someUnderSpecifiedClass.instantiateProperty(nextNumber);
                    someUnderSpecifiedClass.instantiateObject(uri);

                    instantiatedDudes.put(nodeIndex, someUnderSpecifiedClass);
                    break;
                case "What":
                    RDFDUDES what = expressions.what();
                    instantiatedDudes.put(nodeIndex, what);
                    break;
                case "Which":
                    RDFDUDES which = expressions.which("1");
                    instantiatedDudes.put(nodeIndex, which);
                    break;
                case "When":
                    RDFDUDES whenAsProperty = new RDFDUDES(RDFDUDES.Type.PROPERTY, "1", "2");

                    whenAsProperty.instantiateProperty("http://dbpedia.org/ontology/date");

                    RDFDUDES what2 = expressions.what();
                    //merge the return variable in the slot position 1
                    whenAsProperty = whenAsProperty.merge(what2, "2");

                    instantiatedDudes.put(nodeIndex, whenAsProperty);
                    break;
                case "Who":
                    RDFDUDES who = expressions.wh("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://dbpedia.org/ontology/Agent");

                    instantiatedDudes.put(nodeIndex, who);
                    break;
                case "HowMany":
                    RDFDUDES howmany = expressions.howmany("1");

                    instantiatedDudes.put(nodeIndex, howmany);
                    break;
                case "Where":
                    RDFDUDES where = expressions.wh("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://dbpedia.org/ontology/PopulatedPlace");

                    instantiatedDudes.put(nodeIndex, where);
                    break;

            }
        }

        return instantiatedDudes;
    }

    private static List<Triple> removeSameAsVariables(List<Triple> triples) {

        List<Triple> changeTriples = new ArrayList<>();
        HashMap<Term, Term> sameAsVariables = new HashMap<>();

        for (Triple t : triples) {
            if (t.getPredicate().getPredicateName().equals("http://www.w3.org/2002/07/owl#sameAs")) {
                if (t.getSubject() instanceof Variable) {

                    //if it was added before
                    if (sameAsVariables.containsKey(t.getSubject())) {
                        sameAsVariables.remove(t.getSubject());
                        changeTriples.clear();
                        sameAsVariables.clear();
                        break;
                    } else {
                        sameAsVariables.put(t.getSubject().clone(), t.getObject().clone());
                    }
                }
            } else {
                changeTriples.add(t.clone());
            }
        }

        //change variable into resources using sameAsVariables HashMap
        for (Triple t : changeTriples) {

            if (!t.IsReturnVariable()) {
                //check Subject, if matches change variable with resource
                if (t.getSubject() instanceof Variable) {
                    if (sameAsVariables.containsKey(t.getSubject())) {

                        Term temp = sameAsVariables.get(t.getSubject());

                        //remove changed Resource
                        sameAsVariables.remove(t.getSubject());
                        t.setSubject(temp.clone());
                    }
                }

                //check Object, if matches change variable with resource
                if (t.getObject() instanceof Variable) {
                    if (sameAsVariables.containsKey(t.getObject())) {

                        Term temp = sameAsVariables.get(t.getObject());

                        //remove changed Resource
                        sameAsVariables.remove(t.getObject());
                        t.setObject(temp.clone());
                    }
                }
            }

        }

        //add all remaining sameAsVariables to reduce the score
        //if empty then all variables are replaced with resources
        for (Term t : sameAsVariables.keySet()) {
            Triple triple = new Triple();
            triple.setPredicate(new Predicate("http://www.w3.org/2002/07/owl#sameAs", false));
            triple.setSubject(t);
            triple.setObject(sameAsVariables.get(t));

            changeTriples.add(triple);
        }

        return changeTriples;
    }
}
