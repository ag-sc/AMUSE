/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.evaluator;

import de.citec.sc.qald.Question;
import de.citec.sc.qald.SPARQLParser;
import de.citec.sc.qald.Term;
import de.citec.sc.qald.Triple;
import de.citec.sc.qald.Variable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sherzod
 */
public class MappingGenerator {

    public static List<HashMap<Term, Term>> generateMappings(List<Triple> triples1, List<Triple> triples2) {
        List<Term> vars1 = getVariables(triples1);
        List<Term> vars2 = getVariables(triples2);

        List<HashMap<Term, Term>> list = new ArrayList<>();

        return generateMappings(vars1, vars2, list);
    }

    private static List<HashMap<Term, Term>> generateMappings(List<Term> vars1, List<Term> vars2, List<HashMap<Term, Term>> mappingList) {

        List<Term> first = new ArrayList<>();
        List<Term> second = new ArrayList<>();

        if (vars1.size() >= vars2.size()) {
            first = vars1;
            second = vars2;
        } else {
            first = vars2;
            second = vars1;
        }

        int slotSize = (first.size() - 1) * (first.size()) * (second.size());
        //add empty maps
        for (int i = 1; i <= slotSize; i++) {
            HashMap<Term, Term> m = new LinkedHashMap<>();
            mappingList.add(m);
        }

        int c = 0;
        //fill first slots
        for (Term t1 : first) {

            for (int j = 0; j < (second.size()); j++) {
                for (int i = 1; i <= (first.size() - 1); i++) {
                    mappingList.get(c).put(t1, second.get(j));

                    c++;
                }
            }
        }
        //System.out.println(mappingList);

        //increments by (n-1)
        for (int i = 0; i < mappingList.size(); i = i + (first.size() - 1)) {

            for (Term t1 : first) {
                for (Term t2 : second) {
                    //fill n-1 slots 
                    for (HashMap<Term, Term> m : mappingList.subList(i, i + (first.size() - 1))) {
                        if (!m.containsKey(t1) && !m.containsValue(t2)) {
                            m.put(t1, t2);
                            break;
                        }
                    }
                }
            }
        }

        if (vars1.size() == 1 && vars2.size() == 1) {
            //perfect mapping
            HashMap<Term, Term> map = new HashMap<>();
            map.put(vars1.get(0), vars2.get(0));
            mappingList.add(map);
        }

        //System.out.println(mappingList);
        //to check if any map contains a slot
        //check if any mapping has duplicate
        List<HashMap<Term, Term>> checkedMappings = new ArrayList<>();

        for (HashMap<Term, Term> map : mappingList) {

            if (!checkedMappings.contains(map)) {
                checkedMappings.add(map);
            }
        }

        //System.out.println(checkedMappings);
        //if smaller then the mapping has been created from reverse order
        //switch key with value from checkedmappings
        if (vars1.size() < vars2.size()) {

            List<HashMap<Term, Term>> temp = new ArrayList<>();

            for (HashMap<Term, Term> map : checkedMappings) {
                HashMap<Term, Term> reversed = new HashMap<>();

                for (Term k : map.keySet()) {
                    //add key to value, value to key position
                    reversed.put(map.get(k), k);
                }

                temp.add(reversed);
            }

            //add temp
            checkedMappings.clear();
            for (HashMap<Term, Term> map : temp) {
                checkedMappings.add(map);
            }
        }

        //System.out.println(checkedMappings);
        return checkedMappings;
    }

    private static List<Term> getVariables(List<Triple> triples) {

        List<Term> variables = new ArrayList<>();

        for (Triple t : triples) {
            if (!t.IsReturnVariable()) {

                if (t.getSubject() instanceof Variable) {
                    if (!variables.contains(t.getSubject())) {
                        variables.add(t.getSubject());
                    }
                }

                if (t.getObject() instanceof Variable) {
                    if (!variables.contains(t.getObject())) {
                        variables.add(t.getObject());
                    }
                }

                if (t.getPredicate().IsVariable()) {

                    Variable v = new Variable(t.getPredicate().getPredicateName());
                    if (!variables.contains(v)) {
                        variables.add(v);
                    }
                }
            }
        }

        return variables;
    }

}
