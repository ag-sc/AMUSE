/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.query;

import java.util.Comparator;
import java.util.Objects;

/**
 *
 * @author sherzod
 */
public class Candidate {

    private String uri;
    private double dbpediaScore;
    private double matollScore;
    private double priorScore;
    private String matollPreposition;
    private String matollPos;
    private String matollFrame;
    private String matollSubj;
    private String matollObj;

    public Candidate(Instance i, double dbpediaScore, double matollScore, double priorScore) {
        this.dbpediaScore = dbpediaScore;
        this.matollScore = matollScore;
        this.priorScore = priorScore;
        this.uri = i.getUri();
        this.matollPreposition = i.getPreposition();

        this.matollFrame = "";
        this.matollPos = "";
        this.matollSubj = "";
        this.matollObj = "";

//        this.matollFrame = i.getFrame();
//        this.matollPos = i.getPos();
//        this.matollSubj = i.getSubj();
//        this.matollObj = i.getObj();
    }

    public Candidate(String uri, double dbpediaScore, double matollScore, double priorScore, String matollPreposition, String matollPos, String matollFrame, String matollSubj, String matollObj) {
        this.uri = uri;
        this.dbpediaScore = dbpediaScore;
        this.matollScore = matollScore;
        this.priorScore = priorScore;
        this.matollPreposition = matollPreposition;
        this.matollPos = matollPos;
        this.matollFrame = matollFrame;
        this.matollSubj = matollSubj;
        this.matollObj = matollObj;
    }

    @Override
    public String toString() {
        return uri + ",    dbpediaScore=" + dbpediaScore + ",    matollScore=" + matollScore + " prior=" + priorScore;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public double getDbpediaScore() {
        return dbpediaScore;
    }

    public void setDbpediaScore(double dbpediaScore) {
        this.dbpediaScore = dbpediaScore;
    }

    public double getMatollScore() {
        return matollScore;
    }

    public void setMatollScore(double matollScore) {
        this.matollScore = matollScore;
    }

    public double getPriorScore() {
        return priorScore;
    }

    public void setPriorScore(double priorScore) {
        this.priorScore = priorScore;
    }

    public String getMatollPreposition() {
        return matollPreposition;
    }

    public void setMatollPreposition(String matollPreposition) {
        this.matollPreposition = matollPreposition;
    }

    public String getMatollPos() {
        return matollPos;
    }

    public void setMatollPos(String matollPos) {
        this.matollPos = matollPos;
    }

    public String getMatollFrame() {
        return matollFrame;
    }

    public void setMatollFrame(String matollFrame) {
        this.matollFrame = matollFrame;
    }

    public String getMatollSubj() {
        return matollSubj;
    }

    public void setMatollSubj(String matollSubj) {
        this.matollSubj = matollSubj;
    }

    public String getMatollObj() {
        return matollObj;
    }

    public void setMatollObj(String matollObj) {
        this.matollObj = matollObj;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.uri);
        hash = 37 * hash + (int) (Double.doubleToLongBits(this.dbpediaScore) ^ (Double.doubleToLongBits(this.dbpediaScore) >>> 32));
        hash = 37 * hash + (int) (Double.doubleToLongBits(this.matollScore) ^ (Double.doubleToLongBits(this.matollScore) >>> 32));
        hash = 37 * hash + (int) (Double.doubleToLongBits(this.priorScore) ^ (Double.doubleToLongBits(this.priorScore) >>> 32));
        hash = 37 * hash + Objects.hashCode(this.matollPreposition);
        hash = 37 * hash + Objects.hashCode(this.matollPos);
        hash = 37 * hash + Objects.hashCode(this.matollFrame);
        hash = 37 * hash + Objects.hashCode(this.matollSubj);
        hash = 37 * hash + Objects.hashCode(this.matollObj);
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
        final Candidate other = (Candidate) obj;
        if (!Objects.equals(this.uri, other.uri)) {
            return false;
        }
        if (Double.doubleToLongBits(this.dbpediaScore) != Double.doubleToLongBits(other.dbpediaScore)) {
            return false;
        }
        if (Double.doubleToLongBits(this.matollScore) != Double.doubleToLongBits(other.matollScore)) {
            return false;
        }
        if (Double.doubleToLongBits(this.priorScore) != Double.doubleToLongBits(other.priorScore)) {
            return false;
        }
        if (!Objects.equals(this.matollPreposition, other.matollPreposition)) {
            return false;
        }
        if (!Objects.equals(this.matollPos, other.matollPos)) {
            return false;
        }
        if (!Objects.equals(this.matollFrame, other.matollFrame)) {
            return false;
        }
        if (!Objects.equals(this.matollSubj, other.matollSubj)) {
            return false;
        }
        if (!Objects.equals(this.matollObj, other.matollObj)) {
            return false;
        }
        return true;
    }

    public Candidate clone() {
        return new Candidate(uri, dbpediaScore, matollScore, priorScore, matollPreposition, matollPos, matollFrame, matollSubj, matollObj);
    }
}
