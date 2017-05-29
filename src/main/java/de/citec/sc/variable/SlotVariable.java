/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.variable;

/**
 *
 * @author sherzod
 */
public class SlotVariable {

    private int slotNumber;
    private int tokenID;
    private int parentTokenID;

    public SlotVariable(int slotNumber, int tokenID, int parentTokenID) {
        this.slotNumber = slotNumber;
        this.tokenID = tokenID;
        this.parentTokenID = parentTokenID;
    }

    @Override
    public String toString() {
        return "Argument: " + slotNumber + ", tokenID: " + tokenID + ", parentTokenID: " + parentTokenID;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + this.slotNumber;
        hash = 41 * hash + this.tokenID;
        hash = 41 * hash + this.parentTokenID;
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
        final SlotVariable other = (SlotVariable) obj;
        if (this.slotNumber != other.slotNumber) {
            return false;
        }
        if (this.tokenID != other.tokenID) {
            return false;
        }
        if (this.parentTokenID != other.parentTokenID) {
            return false;
        }
        return true;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(int slotNumber) {
        this.slotNumber = slotNumber;
    }

    public int getTokenID() {
        return tokenID;
    }

    public void setTokenID(int tokenID) {
        this.tokenID = tokenID;
    }

    public int getParentTokenID() {
        return parentTokenID;
    }

    public void setParentTokenID(int parentTokenID) {
        this.parentTokenID = parentTokenID;
    }

    public SlotVariable clone() {
        return new SlotVariable(slotNumber, tokenID, parentTokenID);
    }
}
