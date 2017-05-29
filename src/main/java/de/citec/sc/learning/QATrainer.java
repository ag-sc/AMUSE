package de.citec.sc.learning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import corpus.LabeledInstance;
import corpus.SampledInstance;
import de.citec.sc.corpus.SampledMultipleInstance;
import java.util.stream.Collectors;
import learning.Learner;
import sampling.IBeamSearchSampler;
import sampling.Initializer;
import sampling.Sampler;
import variables.AbstractState;

public class QATrainer {

    public interface InstanceCallback {

        default public <InstanceT> void onStartInstance(QATrainer caller, InstanceT instance, int indexOfInstance,
                int numberOfInstances, int epoch, int numberOfEpochs) {

        }

        public <InstanceT, StateT extends AbstractState<InstanceT>> void onEndInstance(QATrainer caller,
                InstanceT instance, int indexOfInstance, StateT finalState, int numberOfInstances, int epoch,
                int numberOfEpochs);

    }

    public interface EpochCallback {

        default void onStartEpoch(QATrainer caller, int epoch, int numberOfEpochs, int numberOfInstances) {
        }

        default void onEndEpoch(QATrainer caller, int epoch, int numberOfEpochs, int numberOfInstances) {
        }
    }

    private static Logger log = LogManager.getFormatterLogger();

    /**
     * This object is a basically a helper that iterates over data instances and
     * triggers the generation of sampling chains for the provided documents.
     * </br>
     * The <b>train</b> function should be used for training while <b>test</b>
     * and <b>predict</b> can be used to evaluate the trained model.
     */
    public QATrainer() {
        super();
    }

    private List<InstanceCallback> instanceCallbacks = new ArrayList<>();
    private List<EpochCallback> epochCallbacks = new ArrayList<>();

    public List<InstanceCallback> getDocumentCallbacks() {
        return instanceCallbacks;
    }

    public void addInstanceCallbacks(List<InstanceCallback> instanceCallbacks) {
        this.instanceCallbacks.addAll(instanceCallbacks);
    }

    public void addInstanceCallback(InstanceCallback instanceCallback) {
        this.instanceCallbacks.add(instanceCallback);
    }

    public void removeInstanceCallback(InstanceCallback instanceCallback) {
        this.instanceCallbacks.remove(instanceCallback);
    }

    public List<EpochCallback> getEpochCallbacks() {
        return epochCallbacks;
    }

    public void addEpochCallbacks(List<EpochCallback> epochCallbacks) {
        this.epochCallbacks.addAll(epochCallbacks);
    }

    public void addEpochCallback(EpochCallback epochCallback) {
        this.epochCallbacks.add(epochCallback);
    }

    public void removeEpochCallback(EpochCallback epochCallback) {
        this.epochCallbacks.remove(epochCallback);
    }

    /**
     * This method iterates over the provided training instances and generates
     * for each such instance a sampling chain using the <i>sampler</i> object.
     * The chain is initialized by the <i>initializer</i> which creates an
     * initial state based on the training instance. After each step in the
     * sampling chain, the sampler notifies the <i>learner</i> to update the
     * model w.r.t. the generated next states. The overall training iterates
     * <i>numberOfEpochs</i> times over the training data. The final sampling
     * state for each document is returned.
     *
     * @param sampler
     * @param initializer
     * @param learner
     * @param instances
     * @param numberOfEpochs
     * @param steps
     * @return
     */
    public <InstanceT, ResultT, StateT extends AbstractState<InstanceT>> List<SampledMultipleInstance<InstanceT, ResultT, StateT>> train(
            IBeamSearchSampler<StateT, ResultT> sampler, List<SampledMultipleInstance<InstanceT, ResultT, StateT>> sampledInstances,
            Learner<StateT> learner, int numberOfEpochs) {
        Random random = new Random(100l);
        List<SampledMultipleInstance<InstanceT, ResultT, StateT>> finalStates = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        log.info("#Epochs=%s, #Instances=%s", numberOfEpochs, sampledInstances.size());
        for (int e = 0; e < numberOfEpochs; e++) {
            log.info("##############################");
            log.info("Epoch: %s/%s", e + 1, numberOfEpochs);
            log.info("##############################");
            for (EpochCallback c : epochCallbacks) {
                c.onStartEpoch(this, e, numberOfEpochs, sampledInstances.size());
            }
            Collections.shuffle(sampledInstances, random);

            for (int i = 0; i < sampledInstances.size(); i++) {
                InstanceT instance = sampledInstances.get(i).getInstance();
                ResultT goldResult = sampledInstances.get(i).getGoldResult();
                log.info("===========QA TRAIN===========");
                log.info("Epoch: %s/%s; Instance: %s/%s", e + 1, numberOfEpochs, i + 1, sampledInstances.size());
                log.info("Gold Result: %s", goldResult);
                log.info("Instance: %s", instance);
                log.info("===========================");
                for (InstanceCallback c : instanceCallbacks) {
                    c.onStartInstance(this, instance, i, sampledInstances.size(), e, numberOfEpochs);
                }

                List<List<StateT>> generatedChain = sampler.generateChain(sampledInstances.get(i).getStates(), goldResult, learner);
                List<StateT> lastStepStates = generatedChain.get(generatedChain.size() - 1);

                lastStepStates = lastStepStates.stream().sorted((s1, s2) -> -Double.compare(s1.getObjectiveScore(), s2.getObjectiveScore())).collect(Collectors.toList());

                /*
                 * Get the highest scoring state (by model score)
                 */
                StateT finalState = lastStepStates.get(0);

                long stopTime = System.currentTimeMillis();

                log.info("++++++++++++++++");
                log.info("Gold Result:   %s", goldResult);
                log.info("Final State:  %s", finalState);
                log.info("TrainingTime: %s (%s seconds)", (stopTime - startTime), (stopTime - startTime) / 1000);
                log.info("++++++++++++++++");

                /*
                 * Store the final predicted state for the current document if
                 * the current epoch is the final one.
                 */
                if (e == numberOfEpochs - 1) {
                    finalStates.add(new SampledMultipleInstance<InstanceT, ResultT, StateT>(instance, goldResult, lastStepStates));
                }
                log.info("===========================");
                for (InstanceCallback c : instanceCallbacks) {
                    c.onEndInstance(this, instance, i, finalState, sampledInstances.size(), e, numberOfEpochs);
                }
                finalState.resetFactorGraph();
            }
            log.info("##############################");
            for (EpochCallback c : epochCallbacks) {
                c.onEndEpoch(this, e, numberOfEpochs, sampledInstances.size());
            }
        }
        return finalStates;
    }

    public <InstanceT, ResultT, StateT extends AbstractState<InstanceT>> List<SampledMultipleInstance<InstanceT, ResultT, StateT>> test(
            IBeamSearchSampler<StateT, ResultT> sampler, List<SampledMultipleInstance<InstanceT, ResultT, StateT>> sampledInstances) {
        List<SampledMultipleInstance<InstanceT, ResultT, StateT>> finalStates = new ArrayList<>();
        for (int i = 0; i < sampledInstances.size(); i++) {
            InstanceT instance = sampledInstances.get(i).getInstance();
            ResultT goldResult = sampledInstances.get(i).getGoldResult();
            log.info("===========QA TEST============");
            log.info("Document: %s/%s", i + 1, sampledInstances.size());
            log.info("Content   : %s", instance);
            log.info("Gold Result: %s", goldResult);
            log.info("===========================");
            for (InstanceCallback c : instanceCallbacks) {
                c.onStartInstance(this, instance, i, sampledInstances.size(), 1, 1);
            }

            List<List<StateT>> generatedChain = sampler.generateChain(sampledInstances.get(i).getStates());
            List<StateT> lastStepStates = generatedChain.get(generatedChain.size() - 1);

            lastStepStates = lastStepStates.stream().sorted((s1, s2) -> -Double.compare(s1.getModelScore(), s2.getModelScore())).collect(Collectors.toList());

            /*
             * Get the highest scoring state (by model score)
             */
            StateT finalState = lastStepStates.get(0);

            finalState.getFactorGraph().clear();
            finalState.getFactorGraph().getFactorPool().clear();
            finalStates.add(new SampledMultipleInstance<>(instance, goldResult, lastStepStates));

            log.info("++++++++++++++++");
            log.info("Gold Result:   %s", goldResult);
            log.info("Final State:  %s", finalState);
            log.info("++++++++++++++++");
            log.info("===========================");
            for (InstanceCallback c : instanceCallbacks) {
                c.onEndInstance(this, instance, i, finalState, sampledInstances.size(), 1, 1);
            }
        }
        return finalStates;
    }

    /**
     * This method iterates over the provided instances and generates for each
     * such instance a sampling chain using the <i>sampler</i> object. The chain
     * is initialized by the <i>initializer</i> which creates an initial state
     * based on the training instance. The final sampling state for each
     * document is returned.
     *
     * @param sampler
     * @param initializer
     * @param documents
     * @param steps
     * @return
     */
    public <InstanceT, StateT extends AbstractState<InstanceT>> List<StateT> predict(Sampler<StateT, ?> sampler,
            Initializer<InstanceT, StateT> initializer, List<InstanceT> documents) {
        List<StateT> finalStates = new ArrayList<>();
        for (int d = 0; d < documents.size(); d++) {
            InstanceT document = documents.get(d);
            log.info("===========================");
            log.info("Document: %s/%s", d + 1, documents.size());
            log.info("Content   : %s", document);
            log.info("===========================");
            StateT initialState = initializer.getInitialState(document);
            List<StateT> generatedChain = sampler.generateChain(initialState);
            StateT finalState = generatedChain.get(generatedChain.size() - 1);

            finalState.getFactorGraph().clear();
            finalState.getFactorGraph().getFactorPool().clear();
            finalStates.add(finalState);
            log.info("++++++++++++++++");
            log.info("Final State:  %s", finalState);
            log.info("++++++++++++++++");
        }
        return finalStates;
    }

}
