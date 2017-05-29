/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.template;

import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.variable.HiddenVariable;
import de.citec.sc.variable.State;
import factors.FactorScope;
import java.util.Objects;
import templates.AbstractTemplate;

/**
 *
 * @author sherzod
 * @param <HiddenVariable>
 */
public class StateFactorScope<State> extends FactorScope {

    private State state;

    public StateFactorScope(AbstractTemplate<?, ?, ?> template, State state) {
        super(template, state);
        this.state = state;
    }

    public State getState() {
        return state;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.state);
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
        final StateFactorScope<?> other = (StateFactorScope<?>) obj;
        if (!Objects.equals(this.state, other.state)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SingleNodeFactorScope{" + "state=" + state + '}';
    }

}
