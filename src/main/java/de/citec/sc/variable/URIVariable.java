/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.variable;

import de.citec.sc.query.Candidate;
import java.util.Objects;

/**
 *
 * @author sherzod
 */
public class URIVariable {

    private Integer tokenId;
    private Integer dudeId;
    private Candidate candidate;

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + Objects.hashCode(this.tokenId);
        hash = 83 * hash + Objects.hashCode(this.dudeId);
        hash = 83 * hash + Objects.hashCode(this.candidate);
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
        final URIVariable other = (URIVariable) obj;
        if (!Objects.equals(this.tokenId, other.tokenId)) {
            return false;
        }
        if (!Objects.equals(this.dudeId, other.dudeId)) {
            return false;
        }
        if (!Objects.equals(this.candidate, other.candidate)) {
            return false;
        }
        return true;
    }

    public Integer getTokenId() {
        return tokenId;
    }

    public void setTokenId(Integer tokenId) {
        this.tokenId = tokenId;
    }

    public Integer getDudeId() {
        return dudeId;
    }

    public void setDudeId(Integer dudeId) {
        this.dudeId = dudeId;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public URIVariable(Integer tokenId, Integer dudeId, Candidate candidate) {
        this.tokenId = tokenId;
        this.dudeId = dudeId;
        this.candidate = candidate;
    }

    @Override
    public String toString() {
        return "TokenID: " + tokenId + " DUDE: " + dudeId + " URI: " + candidate.getUri() + " DBpedia Score: " + candidate.getDbpediaScore() + " MATOLL Score:  " + candidate.getMatollScore() + " Prior Score: " + candidate.getPriorScore();
    }

    public URIVariable clone() {
        return new URIVariable(tokenId, dudeId, candidate.clone());
    }

}
