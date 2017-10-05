package de.citec.sc.nel;

import java.util.Objects;
import java.util.Set;


public class EntityAnnotation extends Annotation {

    private String type;
    private Set<String> values;

    public EntityAnnotation() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Set<String> getValues() {
        return values;
    }

    public void setValues(Set<String> values) {
        this.values = values;
    }

    public void addValue(String value) {
        this.values.add(value);
    } 

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.type);
        hash = 23 * hash + Objects.hashCode(this.values);
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
        final EntityAnnotation other = (EntityAnnotation) obj;
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        if (!Objects.equals(this.values, other.values)) {
            return false;
        }
        return true;
    }

    
   
    @Override
    public String toString() {
        
        String str = span.getChar_start()+".."+span.getChar_end();
        str += " >> "; 
        for (String v : values) {
            if (str.endsWith(" >> ")) {
                str += v;
            } else {
                str += " | " + v;
            }
        }
        str += " : " + type + " [" + provenance.getConfidence() + "]";
        str += " }";
        return str;
    }
    public String print(String text) {
        
        String str = getSpannedText(text);
        
        str += " >> "; 
        for (String v : values) {
            if (str.endsWith(" >> ")) {
                str += v;
            } else {
                str += " | " + v;
            }
        }
        str += " : " + type + " [" + provenance.getConfidence() + "]";
        
        return str;
    }
}
