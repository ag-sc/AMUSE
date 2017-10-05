/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.variable;

import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.learning.FeatureMapData;
import de.citec.sc.learning.FeatureMapData.FeatureDataPoint;
import de.citec.sc.learning.QueryConstructor;
import de.citec.sc.query.Candidate;
import exceptions.MissingFactorException;
import factors.Factor;
import factors.FactorScope;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import utility.StateID;
import variables.AbstractState;

/**
 *
 * @author sherzod
 */
public class State extends AbstractState<AnnotatedDocument> {

    private AnnotatedDocument document;

    private Map<Integer, URIVariable> hiddenVariables;
    private Map<Integer, SlotVariable> slotVariables;
    private QueryTypeVariable queryTypeVariable;

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.document);
        hash = 59 * hash + Objects.hashCode(this.hiddenVariables);
        hash = 59 * hash + Objects.hashCode(this.slotVariables);
        hash = 59 * hash + Objects.hashCode(this.queryTypeVariable);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final State other = (State) obj;
        if (!Objects.equals(this.document, other.document)) {
            return false;
        }
        if (!Objects.equals(this.hiddenVariables, other.hiddenVariables)) {
            return false;
        }
        if (!Objects.equals(this.slotVariables, other.slotVariables)) {
            return false;
        }
        if (!Objects.equals(this.queryTypeVariable, other.queryTypeVariable)) {
            return false;
        }
        return true;
    }

    public Map<Integer, SlotVariable> getSlotVariables() {
        return slotVariables;
    }

    public void setSlotVariables(Map<Integer, SlotVariable> slotVariables) {
        this.slotVariables = slotVariables;
    }

    public Map<Integer, URIVariable> getHiddenVariables() {
        return hiddenVariables;
    }

    public void setHiddenVariables(Map<Integer, URIVariable> hiddenVariables) {
        this.hiddenVariables = hiddenVariables;
    }

    public State(AnnotatedDocument instance) {
        super(instance);
        this.document = (AnnotatedDocument) instance;
        this.hiddenVariables = new TreeMap<>();
        this.slotVariables = new HashMap<>();
        this.queryTypeVariable = new QueryTypeVariable(1);

    }

    public State(State state) {
        super(state);

        this.setDocument(state.document);

        //clone dudes
        HashMap<Integer, URIVariable> h = new HashMap<>();
        for (Integer d : state.hiddenVariables.keySet()) {
            h.put(d, state.hiddenVariables.get(d).clone());
        }
        //clone slots
        HashMap<Integer, SlotVariable> s = new HashMap<>();
        for (Integer d : state.slotVariables.keySet()) {
            s.put(d, state.slotVariables.get(d).clone());
        }

        this.queryTypeVariable = state.getQueryTypeVariable().clone();

        this.hiddenVariables = h;
        this.slotVariables = s;
    }

    public void addHiddenVariable(Integer indexOfNode, Integer indexOfDUDE, Candidate c) {

        URIVariable v = new URIVariable(indexOfNode, indexOfDUDE, c);
        this.hiddenVariables.put(indexOfNode, v);

    }

    public void addSlotVariable(Integer tokenID, Integer parentTokenID, Integer slotNumber) {

        SlotVariable s = new SlotVariable(slotNumber, tokenID, parentTokenID);

        this.slotVariables.put(tokenID, s);
    }

    @Override
    public String toString() {
        String state = "Document: " + "\n" + document.toString() + "\n";

        state += "\nHiddenVariables:\n";

        for (Integer d : hiddenVariables.keySet()) {
            state += hiddenVariables.get(d).toString() + "\n";
        }

        state += "\nSlotVariables:\n";

        for (Integer d : slotVariables.keySet()) {
            state += slotVariables.get(d).toString() + "\n";
        }

        state += "\n" + queryTypeVariable.toString() + "\n";

        state += "\nObjectiveScore: " + getObjectiveScore();
        state += "\nModelScore: " + getModelScore() + "\n";

        String query = QueryConstructor.getSPARQLQuery(this);

        state += "\nConstructed Query: \n\n" + query + "\n";

        return state;
    }

    public String toLessDetailedString() {
        String state = "";

        state += "HiddenVariables:\n";

        for (Integer d : hiddenVariables.keySet()) {
            state += hiddenVariables.get(d).toString() + "\n";
        }

        state += "\nSlotVariables:\n";

        for (Integer d : slotVariables.keySet()) {
            state += slotVariables.get(d).toString() + "\n";
        }

        state += "\n" + queryTypeVariable.toString() + "\n";

        state += "\nObjectiveScore: " + getObjectiveScore();
        state += "\nModelScore: " + getModelScore() + "\n";

        return state;
    }

    public AnnotatedDocument getDocument() {
        return document;
    }

    @Override
    public StateID getID() {
        return id;
    }

    public void setDocument(AnnotatedDocument document) {
        this.document = document;
    }

    public List<Integer> getUsedSlots(Integer headNodeIndex) {
        List<Integer> slots = new ArrayList<>();

        for (Integer i : slotVariables.keySet()) {
            if (headNodeIndex == slotVariables.get(i).getParentTokenID()) {

                slots.add(slotVariables.get(i).getSlotNumber());
            }
        }

        return slots;
    }

    public String getSlot(Integer depNode, Integer headNode) {
        String slot = "";

        if (slotVariables.containsKey(depNode)) {
            SlotVariable var = slotVariables.get(depNode);

            if (var.getParentTokenID() == headNode) {
                slot = var.getSlotNumber() + "";

                return slot;
            }
        }

        return slot;
    }

    public QueryTypeVariable getQueryTypeVariable() {
        return queryTypeVariable;
    }

    public void setQueryTypeVariable(QueryTypeVariable queryTypeVariable) {
        this.queryTypeVariable = queryTypeVariable;
    }

    public FeatureDataPoint toTrainingPoint(FeatureMapData data, boolean training) {

        final Map<String, Double> features = new HashMap<>();
        try {
            for (Factor<? extends FactorScope> factor : getFactorGraph().getFactors()) {
                for (Entry<String, Double> f : factor.getFeatureVector().getFeatures().entrySet()) {
                    features.put(f.getKey(), features.getOrDefault(f.getKey(), 0d) + f.getValue());
                }
            }
        } catch (MissingFactorException e) {
            e.printStackTrace();
        }
        return new FeatureDataPoint(data, features, getObjectiveScore(), training);
    }
}
