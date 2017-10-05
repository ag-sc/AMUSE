package de.citec.sc.nel;

import java.util.Objects;


public class Provenance {

    private String source;
    private double confidence;

    public Provenance() {
    }

    public Provenance(String source, double confidence) {
        this.source = source;
        this.confidence = confidence;
    }

    public String getSource() {
        return source;
    }

    public double getConfidence() {
        return confidence;
    }
    
    public void setSource(String s) {
        source = s;
    }
    
    public void setConfidence(double d) {
        confidence = d;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 43 * hash + Objects.hashCode(this.source);
        hash = 43 * hash + (int) (Double.doubleToLongBits(this.confidence) ^ (Double.doubleToLongBits(this.confidence) >>> 32));
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
        final Provenance other = (Provenance) obj;
        if (Double.doubleToLongBits(this.confidence) != Double.doubleToLongBits(other.confidence)) {
            return false;
        }
        if (!Objects.equals(this.source, other.source)) {
            return false;
        }
        return true;
    }
    
    @Override 
    public String toString() {
        
        return source + " " + confidence;
    }
}
