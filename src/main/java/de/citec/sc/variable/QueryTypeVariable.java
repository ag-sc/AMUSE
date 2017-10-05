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
public class QueryTypeVariable {

    private int type;

    public QueryTypeVariable(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        String s = "Type: ";
        switch(type){
            case 1:
                s += "SELECT";
                break;
            case 2:
                s += "COUNT";
                break;
            case 3:
                s += "ASK";
                break;
        }
        return s;
    }

    public QueryTypeVariable clone() {
        return new QueryTypeVariable(type);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + this.type;
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
        final QueryTypeVariable other = (QueryTypeVariable) obj;
        if (this.type != other.type) {
            return false;
        }
        return true;
    }
}
