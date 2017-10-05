/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.template;

import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.variable.URIVariable;
import de.citec.sc.variable.State;
import factors.FactorScope;
import java.util.Objects;
import templates.AbstractTemplate;

/**
 *
 * @author sherzod
 * @param <HiddenVariable>
 */
public class SingleNodeFactorScope<HiddenVariable> extends FactorScope {

    private HiddenVariable var;
    private AnnotatedDocument doc;

    public SingleNodeFactorScope(AbstractTemplate<?, ?, ?> template, HiddenVariable var, AnnotatedDocument doc) {
        super(template, var, doc);
        this.doc = doc;
        this.var = var;
    }

    public HiddenVariable getVar() {
        return var;
    }

    public AnnotatedDocument getDoc() {
        return doc;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.var);
        hash = 41 * hash + Objects.hashCode(this.doc);
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
        final SingleNodeFactorScope<?> other = (SingleNodeFactorScope<?>) obj;
        if (!Objects.equals(this.var, other.var)) {
            return false;
        }
        if (!Objects.equals(this.doc, other.doc)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SingleNodeFactorScope{" + "var=" + var + ", doc=" + doc + '}';
    }

}
