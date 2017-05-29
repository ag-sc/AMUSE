package de.citec.sc.corpus;

import java.util.List;
import variables.AbstractState;

/**
 * The SampledInstance represents a data point for which we can provide a result
 * e.g. a sentence with gold annotations.
 *
 * @author sjebbara
 *
 * @param <ResultT>
 */
public class SampledMultipleInstance<InstanceT, ResultT, StateT extends AbstractState<InstanceT>> {

    /**
     * The data instance.
     *
     * @return
     */
    private InstanceT instance;
    /**
     * The result for this data instance.
     *
     * @return
     */
    private ResultT result;

    private List<StateT> states;

    public SampledMultipleInstance(InstanceT instance, ResultT result, List<StateT> states) {
        super();
        this.instance = instance;
        this.result = result;
        this.states = states;
    }

    public InstanceT getInstance() {
        return instance;
    }

    public ResultT getGoldResult() {
        return result;
    }

    public List<StateT> getStates() {
        return states;
    }

}
