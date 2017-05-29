/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.variable;

import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.query.Candidate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private Map<Integer, HiddenVariable> hiddenVariables;
    private Map<Integer, SlotVariable> slotVariables;

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.document);
        hash = 29 * hash + Objects.hashCode(this.hiddenVariables);
        hash = 29 * hash + Objects.hashCode(this.slotVariables);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
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
        return true;
    }

    public Map<Integer, SlotVariable> getSlotVariables() {
        return slotVariables;
    }

    public void setSlotVariables(Map<Integer, SlotVariable> slotVariables) {
        this.slotVariables = slotVariables;
    }

    public Map<Integer, HiddenVariable> getHiddenVariables() {
        return hiddenVariables;
    }

    public void setHiddenVariables(Map<Integer, HiddenVariable> hiddenVariables) {
        this.hiddenVariables = hiddenVariables;
    }

    public State(AnnotatedDocument instance) {
        super(instance);
        this.document = (AnnotatedDocument) instance;
        this.hiddenVariables = new TreeMap<>();
        this.slotVariables = new HashMap<>();

    }

    public State(State state) {
        super(state);

        this.setDocument(state.document);

        //clone dudes
        HashMap<Integer, HiddenVariable> h = new HashMap<>();
        for (Integer d : state.hiddenVariables.keySet()) {
            h.put(d, state.hiddenVariables.get(d).clone());
        }
        //clone slots
        HashMap<Integer, SlotVariable> s = new HashMap<>();
        for (Integer d : state.slotVariables.keySet()) {
            s.put(d, state.slotVariables.get(d).clone());
        }

        this.hiddenVariables = h;
        this.slotVariables = s;
    }

    public void addHiddenVariable(Integer indexOfNode, Integer indexOfDUDE, Candidate c) {

        HiddenVariable v = new HiddenVariable(indexOfNode, indexOfDUDE, c);
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

        state += "\nObjectiveScore: " + getObjectiveScore();
        state += "\nModelScore: " + getModelScore() + "\n";

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

}
