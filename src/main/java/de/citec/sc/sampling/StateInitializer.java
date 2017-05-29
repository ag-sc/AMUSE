package de.citec.sc.sampling;

import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.query.Candidate;
import de.citec.sc.query.Instance;
import de.citec.sc.variable.State;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import sampling.Initializer;

public class StateInitializer implements Initializer<AnnotatedDocument, State> {

    public StateInitializer() {
        super();

    }

    @Override
    public State getInitialState(AnnotatedDocument document) {
        State s = new State(document);
        s.setDocument(document);

        for (Integer k : s.getDocument().getParse().getNodes().keySet()) {
            Candidate emptyInstance = new Candidate(new Instance("EMPTY_STRING", 0), 0, 0, 0);
            s.addHiddenVariable(k, -1, emptyInstance);

        }

        s.setModelScore(0.0);
        s.setObjectiveScore(0.0);

        return s;
    }

}
