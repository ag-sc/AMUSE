/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.qald;

import java.util.Objects;

/**
 *
 * @author sherzod
 */
public class Triple {

    public boolean IsReturnVariable() {
        return isReturnVariable;
    }

    public void setIsReturnVariable(boolean isReturnVariable) {
        this.isReturnVariable = isReturnVariable;
    }
    private Term subject;
    private Predicate predicate;
    private Term object;

    private boolean isReturnVariable = false;
    private boolean isFilter = false;
    private boolean isOptional = false;

    public boolean isFilter() {
        return isFilter;
    }

    public void setIsFilter(boolean isFilter) {
        this.isFilter = isFilter;
    }

    public Term getSubject() {
        return subject;
    }

    public void setSubject(Term subject) {
        this.subject = subject;
    }

    public Predicate getPredicate() {
        return predicate;
    }

    public void setPredicate(Predicate predicate) {
        this.predicate = predicate;
    }

    public Term getObject() {
        return object;
    }

    public void setObject(Term object) {
        this.object = object;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + Objects.hashCode(this.subject);
        hash = 11 * hash + Objects.hashCode(this.predicate);
        hash = 11 * hash + Objects.hashCode(this.object);
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
        final Triple other = (Triple) obj;
        if (!Objects.equals(this.subject, other.subject)) {
            return false;
        }
        if (!Objects.equals(this.predicate, other.predicate)) {
            return false;
        }
        if (!Objects.equals(this.object, other.object)) {
            return false;
        }
        return true;
    }

    public Triple() {
    }

    @Override
    public String toString() {
        return convertToSPARQL();
    }

    public String convertToSPARQL() {
        String s = "";
        if (IsReturnVariable()) {
            //return var *
            //don't add ? to the *
            if (object.toString().equals("COUNT")) {
                s+= "COUNT(DISTINCT ?"+ subject.toString()+")";
            } else {
                if (subject.toString().equals("*")) {
                    s += subject.toString();
                } else {
                    s += "?" + subject.toString() + " ";
                }
            }

        } else {
            if (isFilter) {
                s += "FILTER (";

                //year(?uri), xsd:double(?x)
                if (object == null) {
                    s += predicate.toString() + "(" + getTerm(subject) + ")";

                } else {
                    if (predicate.getPredicateName().equals("regex")) {

                        //regex(?data, "^1980")
                        s += predicate.toString() + "(" + getTerm(subject) + ", " + getTerm(object) + ")";
                    } else {
                        //?uri >= ?date
                        s += getTerm(subject) + " " + predicate.toString() + " " + getTerm(object);
                    }
                }
                s += ") .";

            } else {

                s += getTerm(subject) + "  ";

                if (predicate.IsVariable()) {
                    s += predicate.toString() + "  ";
                } else {
                    s += "<" + predicate.toString() + ">  ";
                }

                s += getTerm(object) + " ";

                if (isOptional) {
                    s = "OPTIONAL { " + s + " }";
                }

                s += ".";
            }
        }

        return s;
    }

    private String getTerm(Term t) {
        String s = "";

        if (t instanceof Constant) {

            if (isLiteral(t.toString())) {
                s = t.toString();
            } else if (isNumeric(t.toString())) {
                s = t.toString();
            } else if (isSpecialCaseConstant(t.toString())) {
                s = t.toString();
            } else {
                s = "<" + t.toString() + ">";
            }

        } else if (t instanceof UnaryFunction) {
            UnaryFunction f = (UnaryFunction) t;
            s = f.getFunctionName() + "(?" + f.getArgumentName() + ")";
        } else {
            s += "?" + t.toString();
        }

        return s;
    }

    private boolean isLiteral(String str) {
        //'Chess'@en
        if (str.contains("'@")) {
            return true;
        }

        return false;
    }

    private boolean isNumeric(String str) {
        //8.0E7
        if (str.matches("-?\\d+(\\.\\d+)?\\w\\d+")) {
            return true;
        }
        //-10.202, 8
        if (str.matches("-?\\d+(\\.\\d+)?")) {
            return true;
        }

        return false;
    }

    private boolean isSpecialCaseConstant(String str) {
        // "1980^^xsd:year"
        if (str.startsWith("\"")) {
            return true;
        }
        return false;
    }

    public boolean isOptional() {
        return isOptional;
    }

    public void setIsOptional(boolean isOptional) {
        this.isOptional = isOptional;
    }

    public Triple clone() {
        Triple t = new Triple();
        t.setIsFilter(isFilter);
        t.setIsOptional(isOptional);
        t.setIsReturnVariable(isReturnVariable);

        t.setPredicate(predicate.clone());
        t.setObject(object.clone());
        t.setSubject(subject.clone());

        return t;
    }

}
