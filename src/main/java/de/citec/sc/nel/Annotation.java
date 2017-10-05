package de.citec.sc.nel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author sherzod
 */
public class Annotation {

    Span span;
    Provenance provenance;

    public Span getSpan() {
        return span;
    }

    public void setSpan(Span span) {
        this.span = span;
    }

    public Provenance getProvenance() {
        return provenance;
    }

    public void setProvenance(Provenance provenance) {
        this.provenance = provenance;
    }

    public int length() {

        int length = (span.getChar_end() - span.getChar_start());

        return length;
    }

    public String getSpannedText(String text) {

        String spannedText = "";

        try {
            spannedText += " " + text.substring(span.getChar_start(), span.getChar_end()) + " ";
        } catch (StringIndexOutOfBoundsException e) {
            // Do something?
        }

        return spannedText.trim();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.span);
        hash = 17 * hash + Objects.hashCode(this.provenance);
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
        final Annotation other = (Annotation) obj;
        if (!Objects.equals(this.span, other.span)) {
            return false;
        }
        if (!Objects.equals(this.provenance, other.provenance)) {
            return false;
        }
        return true;
    }

}
