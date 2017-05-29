package de.citec.sc.sampling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import learning.Learner;
import learning.Model;
import learning.ObjectiveFunction;
import sampling.Explorer;
import sampling.Sampler;
import sampling.samplingstrategies.AcceptStrategies;
import sampling.samplingstrategies.AcceptStrategy;
import sampling.samplingstrategies.SamplingStrategies;
import sampling.samplingstrategies.SamplingStrategy;
import sampling.stoppingcriterion.StepLimitCriterion;
import sampling.stoppingcriterion.StoppingCriterion;
import utility.Utils;
import variables.AbstractState;

public class MySampler<InstanceT, StateT extends AbstractState<InstanceT>, ResultT>
        implements Sampler<StateT, ResultT> {

    public interface StepCallback {

        default <InstanceT, StateT extends AbstractState<InstanceT>> void onStartStep(Sampler<StateT, ?> sampler,
                int step, int e, int numberOfExplorers, StateT initialState) {
        }

        default <InstanceT, StateT extends AbstractState<InstanceT>> void onEndStep(Sampler<StateT, ?> sampler,
                int step, int e, int numberOfExplorers, StateT initialState, StateT currentState) {

        }
    }

    private static Logger log = LogManager.getFormatterLogger();
    protected Model<InstanceT, StateT> model;
    protected ObjectiveFunction<StateT, ResultT> objective;
    private List<Explorer<StateT>> explorers;
    private StoppingCriterion<StateT> stoppingCriterion;

    protected final boolean multiThreaded = false;
    /**
     * Defines the sampling strategy for the training phase. The test phase
     * currently always uses the greedy variant.
     */
    private SamplingStrategy<StateT> trainSamplingStrategy = SamplingStrategies.linearModelSamplingStrategy();

    private AcceptStrategy<StateT> trainAcceptStrategy = AcceptStrategies.strictModelAccept();

    /**
     * Greedy sampling strategy for test phase.
     */
    private SamplingStrategy<StateT> predictionSamplingStrategy = SamplingStrategies.greedyModelStrategy();

    /**
     * Strict accept strategy for test phase.
     */
    private AcceptStrategy<StateT> predictionAcceptStrategy = AcceptStrategies.strictModelAccept();

    private List<StepCallback> stepCallbacks = new ArrayList<>();

    public List<StepCallback> getStepCallbacks() {
        return stepCallbacks;
    }

    public void addStepCallbacks(List<StepCallback> stepCallbacks) {
        this.stepCallbacks.addAll(stepCallbacks);
    }

    public void addStepCallback(StepCallback stepCallback) {
        this.stepCallbacks.add(stepCallback);
    }

    /**
     * The DefaultSampler implements the Sampler interface. This sampler divides
     * the sampling procedure in the exploration of the search space (using
     * Explorers) and the actual sampling that happens in this class. It is
     * designed to be flexible in the actual sampling strategy and the stopping
     * criterion.
     *
     * @param model
     * @param scorer
     * @param objective
     * @param explorers
     * @param stoppingCriterion
     */
    public MySampler(Model<InstanceT, StateT> model, ObjectiveFunction<StateT, ResultT> objective,
            List<Explorer<StateT>> explorers, StoppingCriterion<StateT> stoppingCriterion) {
        super();
        this.model = model;
        this.objective = objective;
        this.explorers = explorers;
        this.stoppingCriterion = stoppingCriterion;
    }

    /**
     * The DefaultSampler implements the Sampler interface. This sampler divides
     * the sampling procedure in the exploration of the search space (using
     * Explorers) and the actual sampling that happens in this class. It is
     * designed to be flexible in the actual sampling strategy and the stopping
     * criterion. This constructor uses a simple step limit as the stopping
     * criterion.
     *
     * @param model
     * @param scorer
     * @param objective
     * @param explorers
     * @param samplingSteps
     */
    public MySampler(Model<InstanceT, StateT> model, ObjectiveFunction<StateT, ResultT> objective,
            List<Explorer<StateT>> explorers, int samplingSteps) {
        super();
        this.model = model;
        this.objective = objective;
        this.explorers = explorers;
        this.stoppingCriterion = new StepLimitCriterion<>(samplingSteps);
    }

    @Override
    public List<StateT> generateChain(StateT initialState, ResultT goldResult, Learner<StateT> learner) {
        List<StateT> generatedChain = new ArrayList<>();

        StateT currentState = initialState;
        int step = 0;

        do {
            log.info("---------------------------");
            int e = 0;
            for (Explorer<StateT> explorer : explorers) {
                log.info("...............");
                log.info("TRAINING Step: %s; Explorer: %s", step + 1, explorer.getClass().getSimpleName());
                for (StepCallback c : stepCallbacks) {
                    c.onStartStep(this, step, e, explorers.size(), initialState);
                }

                currentState = performTrainingStep(learner, explorer, goldResult, currentState);
                generatedChain.add(currentState);
                log.info("Sampled State:  %s", currentState);
                for (StepCallback c : stepCallbacks) {
                    c.onEndStep(this, step, e, explorers.size(), initialState, currentState);
                }
                e++;
            }
            step++;
        } while (!stoppingCriterion.checkCondition(generatedChain, step));

        log.info("Stop sampling after step %s", step);
        return generatedChain;
    }

    @Override
    public List<StateT> generateChain(StateT initialState) {
        List<StateT> generatedChain = new ArrayList<>();
        StateT currentState = initialState;

        int step = 0;
        do {
            log.info("---------------------------");
            int e = 0;
            for (Explorer<StateT> explorer : explorers) {
                log.info("...............");
                log.info("PREDICTION Step: %s; Explorer: %s", step + 1, explorer.getClass().getSimpleName());
                for (StepCallback c : stepCallbacks) {
                    c.onStartStep(this, step, e, explorers.size(), initialState);
                }
                currentState = performPredictionStep(explorer, currentState);
                generatedChain.add(currentState);
                log.info("Sampled State:  %s", currentState);
                for (StepCallback c : stepCallbacks) {
                    c.onEndStep(this, step, e, explorers.size(), initialState, currentState);
                }
                e++;
            }
            step++;
        } while (!stoppingCriterion.checkCondition(generatedChain, step));
        log.info("Stop sampling after step %s", step);

        return generatedChain;
    }

    /**
     * Generates states, computes features, scores states, and updates the
     * model. After that a successor state is selected.
     *
     * @param learner
     * @param explorer
     * @param goldResult
     * @param currentState
     * @return
     */
    protected StateT performTrainingStep(Learner<StateT> learner, Explorer<StateT> explorer, ResultT goldResult,
            StateT currentState) {
        log.debug("TRAINING Step:");
        log.debug("Current State:\n%s", currentState);
        /**
         * Generate possible successor states.
         */
        List<StateT> nextStates = explorer.getNextStates(currentState);
        List<StateT> allStates = new ArrayList<>(nextStates);
        if (nextStates.size() > 0) {
            allStates.add(currentState);
            /**
             * Score all states with Objective/Model only if sampling strategy
             * needs that. If not, score only selected candidate and current.
             */
            if (trainSamplingStrategy.usesObjective()) {
                /**
                 * Compute objective function scores
                 */
                scoreWithObjective(allStates, goldResult);
            }
            if (trainSamplingStrategy.usesModel()) {
                /**
                 * Apply templates to states and, thus generate factors and
                 * features
                 */
                model.score(allStates, currentState.getInstance());
            }
            /**
             * Sample one possible successor
             */
            StateT candidateState = trainSamplingStrategy.sampleCandidate(nextStates);

            /**
             * If states were not scored before score only selected candidate
             * and current state.
             */
            if (!trainSamplingStrategy.usesObjective()) {
                /**
                 * Compute objective function scores
                 */
                scoreWithObjective(Arrays.asList(currentState, candidateState), goldResult);
            }
            if (!trainSamplingStrategy.usesModel()) {
                /**
                 * Apply templates to current and candidate state only
                 */
                model.score(Arrays.asList(currentState, candidateState), currentState.getInstance());
            }
            /**
             * Update model with selected state
             */
            learner.update(currentState, candidateState);
            /**
             * Choose to accept or reject selected state
             */
            return trainAcceptStrategy.isAccepted(candidateState, currentState) ? candidateState : currentState;
        }
        return currentState;
    }

    /**
     * Generates states, computes features and scores states. After that a
     * successor state is selected.
     *
     * @param explorer
     * @param currentState
     * @return
     */
    protected StateT performPredictionStep(Explorer<StateT> explorer, StateT currentState) {
        log.debug("PREDICTION:");
        /**
         * Generate possible successor states.
         */
        List<StateT> nextStates = explorer.getNextStates(currentState);
        if (nextStates.size() > 0) {
            List<StateT> allStates = new ArrayList<>(nextStates);
            allStates.add(currentState);
            /**
             * Apply templates to states and thus generate factors and features
             */

            model.score(allStates, currentState.getInstance());
            /**
             * Select a candidate state from the list of possible successors.
             */
            StateT candidateState = predictionSamplingStrategy.sampleCandidate(nextStates);
            /**
             * Decide to accept or reject the selected state
             */

            currentState = predictionAcceptStrategy.isAccepted(candidateState, currentState) ? candidateState
                    : currentState;
            return currentState;
        } else {
            return currentState;
        }
    }

    /**
     * Computes the objective scores for each of the given states. The
     * <i>multiThreaded</i> flag determines if the computation is performed in
     * parallel or sequentially.
     *
     * @param goldResult
     * @param currentState
     * @param nextStates
     */
    protected void scoreWithObjective(List<StateT> allStates, ResultT goldResult) {
        log.debug("Score %s states according to objective...", allStates.size() + 1);
        Stream<StateT> stream = Utils.getStream(allStates, multiThreaded);
        stream.forEach(s -> objective.score(s, goldResult));
    }

    protected Model<?, StateT> getModel() {
        return model;
    }

    public StoppingCriterion<StateT> getStoppingCriterion() {
        return stoppingCriterion;
    }

    /**
     * Set the stopping criterion for the sampling chain. This function can be
     * used to change the stopping criterion for the test phase.
     *
     * @param stoppingCriterion
     */
    public void setStoppingCriterion(StoppingCriterion<StateT> stoppingCriterion) {
        this.stoppingCriterion = stoppingCriterion;
    }

    public void setStepLimit(int samplingLimit) {
        this.stoppingCriterion = new StepLimitCriterion<>(samplingLimit);
    }

    public SamplingStrategy<StateT> getTrainingSamplingStrategy() {
        return trainSamplingStrategy;
    }

    /**
     * Sets the sampling strategy for the training phase. The candidate state
     * that is used for training is selected from all possible successor states
     * using this strategy.
     *
     * @param samplingStrategy
     */
    public void setTrainingSamplingStrategy(SamplingStrategy<StateT> samplingStrategy) {
        this.trainSamplingStrategy = samplingStrategy;
    }

    public AcceptStrategy<StateT> getTrainingAcceptStrategy() {
        return trainAcceptStrategy;
    }

    /**
     * Sets the strategy for accepting a sampled candidate state as the next
     * state in the training phase.
     *
     * @return
     */
    public void setTrainingAcceptStrategy(AcceptStrategy<StateT> acceptStrategy) {
        this.trainAcceptStrategy = acceptStrategy;
    }

    public SamplingStrategy<StateT> getTestSamplingStrategy() {
        return predictionSamplingStrategy;
    }

    /**
     * Sets the sampling strategy for the training phase. The candidate state
     * that is used for training is selected from all possible successor states
     * using this strategy.
     *
     * @param samplingStrategy
     */
    public void setTestSamplingStrategy(SamplingStrategy<StateT> samplingStrategy) {
        this.predictionSamplingStrategy = samplingStrategy;
    }

    public AcceptStrategy<StateT> getTestAcceptStrategy() {
        return predictionAcceptStrategy;
    }

    /**
     * Sets the strategy for accepting a sampled candidate state as the next
     * state in the training phase.
     *
     * @return
     */
    public void setTestAcceptStrategy(AcceptStrategy<StateT> acceptStrategy) {
        this.predictionAcceptStrategy = acceptStrategy;
    }

    public List<Explorer<StateT>> getExplorers() {
        return explorers;
    }

    public void setExplorers(List<Explorer<StateT>> explorers) {
        this.explorers = explorers;
    }

}
