package de.citec.sc.sampling;

import de.citec.sc.utils.Performance;
import de.citec.sc.variable.HiddenVariable;
import de.citec.sc.variable.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import evaluation.TaggedTimer;
import learning.AdvancedLearner.TrainingTriple;
import learning.Learner;
import learning.Model;
import learning.ObjectiveFunction;
import learning.scorer.Scorer;
import sampling.Explorer;
import sampling.IBeamSearchSampler;
import sampling.samplingstrategies.AcceptStrategies;
import sampling.samplingstrategies.AcceptStrategy;
import sampling.samplingstrategies.BeamSearchSamplingStrategies;
import sampling.samplingstrategies.BeamSearchSamplingStrategy;
import sampling.samplingstrategies.BeamSearchSamplingStrategy.StatePair;
import sampling.stoppingcriterion.BeamSearchStoppingCriterion;
import utility.Utils;
import variables.AbstractState;

public class MyBeamSearchSampler<InstanceT, StateT extends AbstractState<InstanceT>, ResultT>
        implements IBeamSearchSampler<StateT, ResultT> {

    public interface StepCallback {

        default <InstanceT, StateT extends AbstractState<InstanceT>> void onStartStep(
                MyBeamSearchSampler<InstanceT, StateT, ?> sampler, int step, int e, int numberOfExplorers,
                List<StateT> initialStates) {
        }

        default <InstanceT, StateT extends AbstractState<InstanceT>> void onEndStep(
                MyBeamSearchSampler<InstanceT, StateT, ?> sampler, int step, int e, int numberOfExplorers,
                List<StateT> initialStates, List<StateT> currentState) {

        }
    }

    protected static Logger log = LogManager.getFormatterLogger();
    protected Model<InstanceT, StateT> model;
    protected Scorer scorer;
    protected ObjectiveFunction<StateT, ResultT> objective;
    protected List<Explorer<StateT>> explorers;
    protected BeamSearchStoppingCriterion<StateT> stoppingCriterion;
    protected static int DEFAULT_BEAM_SIZE = 10;
    protected final boolean multiThreaded = false;
    protected int stepsBetweenTraining = 1;
    /**
     * Defines the sampling strategy for the training phase. The test phase
     * currently always uses the greedy variant.
     */
    private BeamSearchSamplingStrategy<StateT> trainSamplingStrategy = SamplingStrategies
            .greedyBeamSearchSamplingStrategyByObjective(DEFAULT_BEAM_SIZE, s -> s.getObjectiveScore());

    private AcceptStrategy<StateT> trainAcceptStrategy = AcceptStrategies.objectiveAccept();

    /**
     * Greedy sampling strategy for test phase.
     */
    private BeamSearchSamplingStrategy<StateT> testSamplingStrategy = SamplingStrategies
            .greedyBeamSearchSamplingStrategyByModel(DEFAULT_BEAM_SIZE, s -> s.getModelScore());

    /**
     * Strict accept strategy for test phase.
     */
    private AcceptStrategy<StateT> testAcceptStrategy = AcceptStrategies.strictModelAccept();

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
    public MyBeamSearchSampler(Model<InstanceT, StateT> model, ObjectiveFunction<StateT, ResultT> objective,
            List<Explorer<StateT>> explorers, BeamSearchStoppingCriterion<StateT> stoppingCriterion) {
        super();
        this.model = model;
        this.objective = objective;
        this.explorers = explorers;
        this.stoppingCriterion = stoppingCriterion;
    }

    @Override
    public List<List<StateT>> generateChain(List<StateT> initialStates, ResultT goldResult, Learner<StateT> learner) {
        List<List<StateT>> generatedChain = new ArrayList<>();

        List<StateT> currentStates = new ArrayList<>();

        currentStates.addAll(initialStates);
        int step = 0;
        do {
            log.info("---------------------------");
            int e = 0;
            for (Explorer<StateT> explorer : explorers) {
                log.info("...............");
                log.info("TRAINING Step: %s; Explorer: %s", step + 1, explorer.getClass().getSimpleName());
                log.info("Current states : %s", currentStates.size());
                for (StepCallback c : stepCallbacks) {
                    c.onStartStep(this, step, e, explorers.size(), initialStates);
                }

                if (step % stepsBetweenTraining == 0) {
                    currentStates = performTrainingStep(learner, explorer, goldResult, currentStates);
                } else {
                    currentStates = performPredictionStep(explorer, currentStates);
                }

                generatedChain.add(currentStates);

                for (StepCallback c : stepCallbacks) {
                    c.onEndStep(this, step, e, explorers.size(), initialStates, currentStates);
                }
                e++;
            }
            step++;
        } while (!stoppingCriterion.checkCondition(generatedChain, step));

        log.info("\n\nStop sampling after step %s", step);

        //loop over generated chain and check if any state has 1.0 as objective score
        List<StateT> lastChain = generatedChain.get(generatedChain.size() - 1);
        //get the highest scoring state
        StateT finalState = lastChain.stream().max((s1, s2) -> Double.compare(s1.getObjectiveScore(), s2.getObjectiveScore())).get();
        State s = (State) finalState;

        if (s.getObjectiveScore() == 1.0) {
            Performance.addParsed(s.getDocument().getQuestionString(), s.getDocument().getGoldQueryString());
        } else {
            String uris = "";

            for (Integer nodeID : s.getHiddenVariables().keySet()) {
                uris += "Node: " + s.getDocument().getParse().getToken(nodeID) + "   URI : " + s.getHiddenVariables().get(nodeID).getCandidate().getUri() + "\n";
            }
            String q = s.toString() + "\n\nScore: " + s.getObjectiveScore() + "\n\nAssignments: " + uris + "\n"
                    + "================================================================================================================\n";

            Performance.addUnParsed(s.getDocument().getQuestionString(), q);
        }
        return generatedChain;
    }

    @Override
    public List<List<StateT>> generateChain(List<StateT> initialStates) {

        List<List<StateT>> generatedChain = new ArrayList<>();
        List<StateT> currentStates = new ArrayList<>();
        currentStates.addAll(initialStates);

        int step = 0;
        do {
            log.info("---------------------------");
            int e = 0;
            for (Explorer<StateT> explorer : explorers) {
                log.info("...............");
                log.info("PREDICTION Step: %s; Explorer: %s", step + 1, explorer.getClass().getSimpleName());
                for (StepCallback c : stepCallbacks) {
                    c.onStartStep(this, step, e, explorers.size(), initialStates);
                }
                currentStates = performPredictionStep(explorer, currentStates);

                log.info("States# " + currentStates.size());

                generatedChain.add(currentStates);

                for (StepCallback c : stepCallbacks) {
                    c.onEndStep(this, step, e, explorers.size(), initialStates, currentStates);
                }
                e++;
            }
            step++;
        } while (!stoppingCriterion.checkCondition(generatedChain, step));

        log.info("\n\nStop sampling after step %s", step);

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
    protected List<StateT> performTrainingStep(Learner<StateT> learner, Explorer<StateT> explorer, ResultT goldResult,
            List<StateT> currentStates) {
        log.debug("TRAINING Step:");

        Map<Integer, List<StateT>> mapStates = new HashMap<>();

        int c = 0;
        List<StateT> allStates = new ArrayList<>(currentStates);
        List<StatePair<StateT>> allNextStatePairs = new ArrayList<>();
        Map<StateT, List<StateT>> allStateWithParent = new HashMap<>();

        for (StateT currentState : currentStates) {
            /**
             * Generate possible successor states.
             */
            List<StateT> nextStates = explorer.getNextStates(currentState);

            mapStates.put(c, nextStates);
            allStateWithParent.put(currentState, nextStates);

            allStates.addAll(nextStates);
            allNextStatePairs.addAll(
                    nextStates.stream().map(s -> new StatePair<>(currentState, s)).collect(Collectors.toList()));
            c++;
        }

        /**
         * Score all states with Objective/Model only if sampling strategy needs
         * that. If not, score only selected candidate and current.
         */
        if (trainSamplingStrategy.usesObjective()) {
            /**
             * Compute objective function scores
             */
            scoreWithObjective(allStates, goldResult);
        }

        if (trainSamplingStrategy.usesModel()) {
            /**
             * Apply templates to states and, thus generate factors and features
             */
            for (StateT currentState : allStateWithParent.keySet()) {

                model.score(allStateWithParent.get(currentState), currentState.getInstance());
            }
//            model.score(allStates, currentStates.get(0).getInstance());
        }
        /**
         * Sample one possible successor
         */

        List<StatePair<StateT>> candidateStatePairs = trainSamplingStrategy
                .sampleCandidate(new ArrayList<>(allNextStatePairs));
        List<StateT> candidateStates = candidateStatePairs.stream().map(p -> p.getCandidateState())
                .collect(Collectors.toList());

        /**
         * If states were not scored before score only selected candidate and
         * current state.
         */
        if (!trainSamplingStrategy.usesObjective()) {
            /**
             * Compute objective function scores
             */
            scoreWithObjective(candidateStates, goldResult);
        }
        if (!trainSamplingStrategy.usesModel()) {
            /**
             * Apply templates to current and candidate state only
             */
            List<StateT> scoredStates = new ArrayList<>();
            scoredStates.addAll(currentStates);
            scoredStates.addAll(candidateStates);
            model.score(scoredStates, scoredStates.get(0).getInstance());
        }
        /**
         * Update model with selected state
         */
        // StatePair<StateT> best = candidateStatePairs.stream().max((s1, s2) ->
        // Double
        // .compare(s1.getCandidateState().getModelScore(),
        // s2.getCandidateState().getModelScore())).get();
        // learner.update(best.getParentState(), best.getCandidateState());

        List<TrainingTriple<StateT>> trainingTriples = candidateStatePairs.stream()
                .map(p -> new TrainingTriple<>(p.getParentState(), p.getCandidateState(), 1.0))
                .collect(Collectors.toList());
        // for (StatePair<StateT> candidateStatePair : candidateStatePairs) {
        // StateT candidateState = candidateStatePair.getCandidateState();
        // StateT currentState = candidateStatePair.getParentState();
        learner.update(trainingTriples);
        // }

        Set<StateT> acceptedStates = new HashSet<>();
        for (StatePair<StateT> candidateStatePair : candidateStatePairs) {
            StateT candidateState = candidateStatePair.getCandidateState();
            StateT currentState = candidateStatePair.getParentState();
            StateT acceptedState = trainAcceptStrategy.isAccepted(candidateState, currentState) ? candidateState
                    : currentState;
            acceptedStates.add(acceptedState);
        }

        if (acceptedStates.isEmpty()) {
            acceptedStates.add(currentStates.get(0));
        }

        return new ArrayList<>(acceptedStates);

    }

    /**
     * Generates states, computes features and scores states. After that a
     * successor state is selected.
     *
     * @param explorer
     * @param currentState
     * @return
     */
    protected List<StateT> performPredictionStep(Explorer<StateT> explorer, List<StateT> currentStates) {
        log.debug("PREDICTION:");
        Map<Integer, List<StateT>> mapStates = new HashMap<>();

        int c = 0;
        List<StateT> allStates = new ArrayList<>(currentStates);
        List<StatePair<StateT>> allNextStatePairs = new ArrayList<>();
        Map<StateT, List<StateT>> allStateWithParent = new HashMap<>();

        for (StateT currentState : currentStates) {
            /**
             * Generate possible successor states.
             */
            List<StateT> nextStates = explorer.getNextStates(currentState);
            mapStates.put(c, nextStates);
            allStateWithParent.put(currentState, nextStates);

            allStates.addAll(nextStates);
            allNextStatePairs.addAll(
                    nextStates.stream().map(s -> new StatePair<>(currentState, s)).collect(Collectors.toList()));
            c++;
        }

        /**
         * Score all states with Objective/Model only if sampling strategy needs
         * that. If not, score only selected candidate and current.
         */
        /**
         * Apply templates to states and, thus generate factors and features
         */
        for (StateT currentState : allStateWithParent.keySet()) {

            model.score(allStateWithParent.get(currentState), currentState.getInstance());
        }
//        model.score(allStates, currentStates.get(0).getInstance());

        /**
         * Sample one possible successor
         */
        List<StatePair<StateT>> candidateStatePairs = testSamplingStrategy
                .sampleCandidate(new ArrayList<>(allNextStatePairs));

        /**
         * Update model with selected state
         */
        Set<StateT> acceptedStates = new HashSet<>();
        for (StatePair<StateT> candidateStatePair : candidateStatePairs) {
            StateT candidateState = candidateStatePair.getCandidateState();
            StateT currentState = candidateStatePair.getParentState();
            StateT acceptedState = testAcceptStrategy.isAccepted(candidateState, currentState) ? candidateState
                    : currentState;
            acceptedStates.add(acceptedState);
        }
        if (acceptedStates.isEmpty()) {
            acceptedStates.addAll(currentStates);
        }
        return new ArrayList<>(acceptedStates);
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
        long scID = TaggedTimer.start("OBJ-SCORE");
        log.debug("Score %s states according to objective...", allStates.size() + 1);
        Stream<StateT> stream = Utils.getStream(allStates, multiThreaded);
        stream.forEach(s -> objective.score(s, goldResult));
        TaggedTimer.stop(scID);
    }

    protected Model<?, StateT> getModel() {
        return model;
    }

    protected Scorer getScorer() {
        return scorer;
    }

    public BeamSearchStoppingCriterion<StateT> getStoppingCriterion() {
        return stoppingCriterion;
    }

    /**
     * Set the stopping criterion for the sampling chain. This function can be
     * used to change the stopping criterion for the test phase.
     *
     * @param stoppingCriterion
     */
    public void setStoppingCriterion(BeamSearchStoppingCriterion<StateT> stoppingCriterion) {
        this.stoppingCriterion = stoppingCriterion;
    }

    /**
     * Sets the sampling strategy for the training phase. The candidate state
     * that is used for training is selected from all possible successor states
     * using this strategy.
     *
     * @param samplingStrategy
     */
    public void setTrainSamplingStrategy(BeamSearchSamplingStrategy<StateT> samplingStrategy) {
        this.trainSamplingStrategy = samplingStrategy;
    }

    public void setTestSamplingStrategy(BeamSearchSamplingStrategy<StateT> samplingStrategy) {
        this.testSamplingStrategy = samplingStrategy;
    }

    /**
     * Sets the strategy for accepting a sampled candidate state as the next
     * state in the training phase.
     *
     * @return
     */
    public void setTrainAcceptStrategy(AcceptStrategy<StateT> acceptStrategy) {
        this.trainAcceptStrategy = acceptStrategy;
    }

    public void setTestAcceptStrategy(AcceptStrategy<StateT> acceptStrategy) {
        this.testAcceptStrategy = acceptStrategy;
    }

    public List<Explorer<StateT>> getExplorers() {
        return explorers;
    }

    public void setExplorers(List<Explorer<StateT>> explorers) {
        this.explorers = explorers;
    }

}
