/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.evaluator;

import de.citec.sc.qald.Constant;
import de.citec.sc.qald.Predicate;
import de.citec.sc.qald.SPARQLParser;
import de.citec.sc.qald.Term;
import de.citec.sc.qald.Triple;
import de.citec.sc.qald.Variable;
import de.citec.sc.variable.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author sherzod
 */
public class QueryEvaluator {

    public static double evaluate(String derived, String goldStandard) {

        List<Triple> constructedTriples = SPARQLParser.extractTriplesFromQuery(derived);

        if (constructedTriples.isEmpty()) {
            return 0;
        }

        List<Triple> goldStandardTriples = SPARQLParser.extractTriplesFromQuery(goldStandard);

        return similarity(constructedTriples, goldStandardTriples);
    }

    private static double similarity(List<Triple> derived, List<Triple> goldStandard) {
        double sim = 0;

        double p = score(derived, goldStandard);
        double r = score(goldStandard, derived);

        sim = (2 * p * r) / (p + r);

        if (Double.isNaN(sim)) {
            sim = 0;
        }

        return sim;
    }

    private static double score(List<Triple> derived, List<Triple> goldStandard) {
        double score = 0;

        //remove sameAsVariables
//        derived = removeSameAsVariables(derived);
//        goldStandard = removeSameAsVariables(goldStandard);
        List<Triple> goldSetWithReturnVar = new ArrayList<>();
        List<Triple> goldSetWithoutReturnVar = new ArrayList<>();
        List<Triple> triplesWithReturnVar = new ArrayList<>();
        List<Triple> triplesWithoutReturnVar = new ArrayList<>();

        //get triples with and without return variables
        for (Triple g : goldStandard) {
            if (g.IsReturnVariable()) {
                goldSetWithReturnVar.add(g);
            } else {
                goldSetWithoutReturnVar.add(g);
            }
        }

        for (Triple t : derived) {
            if (t.IsReturnVariable()) {
                triplesWithReturnVar.add(t);
            } else {
                triplesWithoutReturnVar.add(t);
            }
        }

        //get mappings
        List<HashMap<Term, Term>> mappings = MappingGenerator.generateMappings(triplesWithoutReturnVar, goldSetWithoutReturnVar);

        if (mappings.isEmpty()) {
            //add empty mapping to run the process
            mappings.add(new HashMap<>());
        }

        double max = 0;
        String mapping = "";

        //get the highest score based on mapping
        for (HashMap<Term, Term> map : mappings) {

            double body = 0;
            for (Triple t : triplesWithoutReturnVar) {
                body += scoreTripleToQuery(t, goldSetWithoutReturnVar, map);
            }

            body = body / triplesWithoutReturnVar.size();

            //compare the query type
            //ask query and select query doesn't have equal number of return vars
            double structure = structure(triplesWithReturnVar, goldSetWithReturnVar, map);

            double alpha = 0.95;

            score = alpha * body + (1 - alpha) * structure;

            if (score > max) {
                max = score;
                mapping = map.toString();
            }
        }

        score = max;

        return score;
    }

    /**
     *
     */
    private static double structure(List<Triple> triples1, List<Triple> triples2, HashMap<Term, Term> map) {
        double s = 0;

        int count = 0;

        try {
            for (Triple t1 : triples1) {
                Term var1 = t1.getSubject();

                //retrieve from mapping
                Variable mappedVar = (Variable) map.get(var1);
                if (mappedVar == null) {
                    mappedVar = (Variable) var1;
                }

                //check if triples2 contains such variable
                boolean contains = false;
                for (Triple t2 : triples2) {

                    Variable var2 = (Variable) t2.getSubject();

                    if (var2.equals(mappedVar)) {
                        contains = true;
                        break;
                    }
                }
                if (contains) {
                    count++;
                }
            }
        } catch (Exception e) {
        }

        s = count / (double) triples1.size();

        if (Double.isNaN(s)) {
            s = 0;
        }

        if (triples1.isEmpty() && triples2.isEmpty()) {
            s = 1.0;
        }

        return s;
    }

    private static double scoreTripleToQuery(Triple t, List<Triple> goldStandard, HashMap<Term, Term> map) {
        double max = 0;

        for (Triple g : goldStandard) {
            double s = scoreTripleToTriple(t, g, map);

            if (s > max) {
                max = s;
            }
        }

        return max;
    }

    private static double scoreTripleToTriple(Triple t, Triple g, HashMap<Term, Term> map) {
        double sim = 0, p = 0, s = 0, o = 0;

//        //check if any part of the query has URI and it matches
//        double p = comparePredicate(t.getPredicate(), g.getPredicate());
//        double s = compareConstant(t.getSubject(), g.getSubject());
//        double o = compareConstant(t.getObject(), g.getObject());
//
//        sim = p + s + o;
        //some parts have matched, then do the normal matching with variables and mappings, constants, predicates
//        if (sim > 0) {
        sim = 0;
        p = comparePredicate(t.getPredicate(), g.getPredicate(), map);
        s = compareTerm(t.getSubject(), g.getSubject(), map);
        o = compareTerm(t.getObject(), g.getObject(), map);

        sim = p + s + o;
//        }

        return sim;
    }

    private static double comparePredicate(Predicate p, Predicate g, HashMap<Term, Term> map) {
        double sim = 0;

        //don't evaluate rdf:type part of the triple
//        if(p.getPredicateName().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") && g.equals(p)){
//            return sim;
//        }
        if (p.equals(g)) {
            sim = 0.50;
        } else {

            //compare Predicates as variables in Mapping 
            if (p.IsVariable() && g.IsVariable()) {

                Variable v1 = new Variable(p.getPredicateName());
                Variable v2 = new Variable(g.getPredicateName());

                if (map.containsKey(v1)) {

                    if (map.get(v1).equals(v2)) {
                        sim = 0.50;
                    }
                }

            } else {
                //underspecified property
                if (p.IsVariable() && p.getPredicateName().startsWith("v")) {
                    sim = 0.1;
                }
            }

//            else if (g.IsVariable() && g.getPredicateName().equals("p")) {
//                sim = 0.1;
//            }
        }
        return sim;
    }

    private static double compareTerm(Term t, Term g, HashMap<Term, Term> map) {
        double sim = 0;

        if ((g instanceof Constant) && (t instanceof Constant)) {

            if (t.equals(g)) {
                sim = 0.25;
            }
        } else if ((g instanceof Variable) && (t instanceof Variable)) {

            if (map.containsKey(t)) {

                if (map.get(t).equals(g)) {
                    sim = 0.25;
                }
            }
        }

        return sim;
    }

    private static double compareConstant(Term t, Term g) {
        double sim = 0;

        if ((g instanceof Constant) && (t instanceof Constant)) {

            if (t.equals(g)) {
                sim = 0.25;
            }
        }

        return sim;
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
