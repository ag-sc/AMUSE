package de.citec.sc.nel;

/**
 *
 * @author christina
 */
public class Span implements Comparable<Span> {
    
    private int char_start;
    private int char_end;

    public Span() {
    }
    
    public Span(int start, int end) {
        
        this.char_start = start;
        this.char_end = end;
    }

    public int getChar_start() {
        return char_start;
    }

    public int getChar_end() {
        return char_end;
    }

    public void setChar_start(int char_start) {
        this.char_start = char_start;
    }

    public void setChar_end(int char_end) {
        this.char_end = char_end;
    }
    
    /* Compares spans based on their length */
    @Override
    public int compareTo(Span other) {
        return Integer.compare(char_start - char_end, other.char_start - other.char_end);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 19 * hash + this.char_start;
        hash = 19 * hash + this.char_end;
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
        final Span other = (Span) obj;
        if (this.char_start != other.char_start) {
            return false;
        }
        if (this.char_end != other.char_end) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Span{" + "char_start=" + char_start + ", char_end=" + char_end + '}';
    }
}
