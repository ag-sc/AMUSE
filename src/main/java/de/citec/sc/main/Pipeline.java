package de.citec.sc.main;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.corpus.SampledMultipleInstance;
import de.citec.sc.learning.FeatureMapData;
import de.citec.sc.learning.LibSVMRegressionScorer;
import de.citec.sc.learning.NELObjectiveFunction;
import de.citec.sc.learning.NELHybridSamplingStrategyCallback;
import de.citec.sc.learning.NELTrainer;
import de.citec.sc.learning.NELTrainer.InstanceCallback;
import de.citec.sc.learning.QAHybridSamplingStrategyCallback;
import de.citec.sc.learning.QAObjectiveFunction;
import de.citec.sc.learning.QATrainer;
import de.citec.sc.learning.QueryTypeObjectiveFunction;
import de.citec.sc.sampling.EntityBasedSingleNodeExplorer;
import de.citec.sc.sampling.L2KBEdgeExplorer;
import de.citec.sc.sampling.MyBeamSearchSampler;
import de.citec.sc.sampling.QABeamSearchSampler;
import de.citec.sc.sampling.SamplingStrategies;
import de.citec.sc.sampling.QCEdgeExplorer;
import de.citec.sc.sampling.QueryTypeExplorer;
import de.citec.sc.sampling.SingleNodeExplorer;
import de.citec.sc.sampling.StateInitializer;
import de.citec.sc.template.NELEdgeTemplate;
import de.citec.sc.template.NELLexicalTemplate;
import de.citec.sc.template.NELNodeTemplate;
import de.citec.sc.template.QAEdgeAdvTemplate;
import de.citec.sc.template.QAEdgeTemplate;
import de.citec.sc.template.QATemplateFactory;
import de.citec.sc.template.QueryTypeTemplate;
import de.citec.sc.utils.Performance;
import de.citec.sc.utils.ProjectConfiguration;
import de.citec.sc.variable.State;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import learning.AdvancedLearner;
import learning.Learner;
import learning.Model;
import learning.ObjectiveFunction;
import learning.optimizer.SGD;
import learning.scorer.DefaultScorer;
import learning.scorer.Scorer;
import sampling.Explorer;
import sampling.samplingstrategies.AcceptStrategies;
import sampling.samplingstrategies.BeamSearchSamplingStrategies;
import sampling.stoppingcriterion.BeamSearchStoppingCriterion;
import templates.AbstractTemplate;
import variables.AbstractState;

/**
 *
 * @author sjebbara
 */
public class Pipeline {

    private static int NUMBER_OF_SAMPLING_STEPS = 15;
    private static int NUMBER_OF_EPOCHS = 6;
    private static int BEAM_SIZE_NEL_TRAINING = 20;
    private static int BEAM_SIZE_QA_TRAINING = 5;
    private static int BEAM_SIZE_QA_TEST = 5;
    private static int BEAM_SIZE_NEL_TEST = 20;
    private static Logger log = LogManager.getFormatterLogger();

    private static Set<String> linkingValidPOSTags;
    private static Set<String> qaValidPOSTags;
    private static Map<Integer, String> semanticTypes;
    private static Map<Integer, String> specialSemanticTypes;
    private static Set<String> validEdges;
    public static List<AbstractTemplate<AnnotatedDocument, State, ?>> nelTemplates;
    public static List<AbstractTemplate<AnnotatedDocument, State, ?>> qaTemplates;
    public static List<AbstractTemplate<AnnotatedDocument, State, ?>> queryTypeTemplates;
    public static Scorer scorer;
    private static Explorer nelExplorer;
    private static Explorer qaExplorer;
    private static final FeatureMapData featureMapData = new FeatureMapData();

    public static void initialize(Set<String> v1, Set<String> v2, Map<Integer, String> s, Map<Integer, String> st, Set<String> edges) {
        linkingValidPOSTags = v1;
        qaValidPOSTags = v2;
        semanticTypes = s;
        specialSemanticTypes = st;
        validEdges = edges;

        NUMBER_OF_SAMPLING_STEPS = ProjectConfiguration.getNumberOfSamplingSteps();
        NUMBER_OF_EPOCHS = ProjectConfiguration.getNumberOfEpochs();
        BEAM_SIZE_NEL_TRAINING = ProjectConfiguration.getNELTrainingBeamSize();
        BEAM_SIZE_QA_TRAINING = ProjectConfiguration.getQATrainingBeamSize();
        BEAM_SIZE_QA_TEST = ProjectConfiguration.getQATrainingBeamSize();
        BEAM_SIZE_NEL_TEST = ProjectConfiguration.getNELTrainingBeamSize();

//        scorer = new DefaultScorer();
        scorer = new LibSVMRegressionScorer();

        nelTemplates = new ArrayList<>();
        nelTemplates.add(new NELEdgeTemplate(linkingValidPOSTags, validEdges, semanticTypes));

        qaTemplates = new ArrayList<>();
        qaTemplates.add(new QAEdgeAdvTemplate(qaValidPOSTags, validEdges, semanticTypes, specialSemanticTypes));

        queryTypeTemplates = new ArrayList<>();
        queryTypeTemplates.add(new QueryTypeTemplate(qaValidPOSTags, validEdges, semanticTypes, specialSemanticTypes));

        nelExplorer = new EntityBasedSingleNodeExplorer(semanticTypes, linkingValidPOSTags);
//        nelExplorer = new L2KBEdgeExplorer(semanticTypes, linkingValidPOSTags, validEdges);
        qaExplorer = new QCEdgeExplorer(semanticTypes, specialSemanticTypes, qaValidPOSTags, validEdges);

        QATemplateFactory.initialize(linkingValidPOSTags, qaValidPOSTags, validEdges, semanticTypes, specialSemanticTypes);
    }

    public static List<Model<AnnotatedDocument, State>> train(List<AnnotatedDocument> trainingDocuments) {

        Map<Model<AnnotatedDocument, State>, List<SampledMultipleInstance<AnnotatedDocument, String, State>>> nelPair = trainNEL(trainingDocuments);

        Model<AnnotatedDocument, State> nelModel = null;
        List<SampledMultipleInstance<AnnotatedDocument, String, State>> nelStates = new ArrayList<>();

        Model<AnnotatedDocument, State> qaModel = null;
        List<SampledMultipleInstance<AnnotatedDocument, String, State>> qaStates = new ArrayList<>();

        Model<AnnotatedDocument, State> queryTypeModel = null;
        List<SampledMultipleInstance<AnnotatedDocument, String, State>> queryTypeStates = new ArrayList<>();

        for (Model<AnnotatedDocument, State> m : nelPair.keySet()) {
            nelModel = m;
            nelStates = nelPair.get(m);

//            for (SampledMultipleInstance<AnnotatedDocument, String, State> triple : nelStates) {
//                for (State s1 : triple.getStates()) {
//                    System.out.println(s1 + "\n=================================================\n");
//                }
//            }
        }

        Map<Model<AnnotatedDocument, State>, List<SampledMultipleInstance<AnnotatedDocument, String, State>>> qaPair = trainQA(nelStates);

        for (Model<AnnotatedDocument, State> m : qaPair.keySet()) {
            qaModel = m;
            qaStates = qaPair.get(m);
        }

//        Map<Model<AnnotatedDocument, State>, List<SampledMultipleInstance<AnnotatedDocument, String, State>>> queryTypePair = trainQueryType(qaStates);
//
//        for (Model<AnnotatedDocument, State> m : queryTypePair.keySet()) {
//            queryTypeModel = m;
//            queryTypeStates = queryTypePair.get(m);
//        }
        List<Model<AnnotatedDocument, State>> models = new ArrayList<>();
        models.add(nelModel);
        models.add(qaModel);
//        models.add(queryTypeModel);

        return models;
    }

    public static void test(List<Model<AnnotatedDocument, State>> models, List<AnnotatedDocument> testDocuments) {

        Model<AnnotatedDocument, State> nelModel = models.get(0);
        Model<AnnotatedDocument, State> qaModel = models.get(1);
//        Model<AnnotatedDocument, State> queryTypeModel = models.get(2);

        System.out.println("NEL Model: \n" + nelModel.toDetailedString());
        System.out.println("QA Model: \n" + qaModel.toDetailedString());
//        System.out.println("Query Type Model: \n" + queryTypeModel.toDetailedString());

        NELObjectiveFunction nelObjectiveFunction = new NELObjectiveFunction();

        long startTime = System.currentTimeMillis();
        List<SampledMultipleInstance<AnnotatedDocument, String, State>> nelInstances = testNEL(nelModel, testDocuments);

        List<SampledMultipleInstance<AnnotatedDocument, String, State>> qaInstances = testQA(qaModel, nelInstances);

//        List<SampledMultipleInstance<AnnotatedDocument, String, State>> queryTypeInstances = testQueryType(queryTypeModel, qaInstances);
//      
        long endTime = System.currentTimeMillis();

        QAObjectiveFunction qaObjectiveFunction = new QAObjectiveFunction();
        qaObjectiveFunction.setUseQueryEvaluator(false);

        //test results for linking task
        System.out.println("NEL task : \n\n");
        Performance.logNELTest(nelInstances, nelObjectiveFunction);
        //test results for qa task
        System.out.println("QA task : \n\n");
        Performance.logQATest(qaInstances, qaObjectiveFunction);

//        System.out.println("Query Type task : \n\n");
//        Performance.logQueryTypeTest(queryTypeInstances, qaObjectiveFunction);
        long avgTime = (endTime - startTime) / testDocuments.size();
        System.out.println(avgTime + " ms per test instance.");

    }

    private static Map<Model<AnnotatedDocument, State>, List<SampledMultipleInstance<AnnotatedDocument, String, State>>> trainNEL(List<AnnotatedDocument> trainingDocuments) {
        /*
         * Setup all necessary components for training and testing.
         */
 /*
         * Define an objective function that guides the training procedure.
         */
        ObjectiveFunction<State, String> objective = new NELObjectiveFunction();

        /*
         * Define templates that are responsible to generate factors/features to
         * score generated states.
         */
//        List<AbstractTemplate<AnnotatedDocument, State, ?>> templates = new ArrayList<>();
////        templates.add(new ResourceTemplate(validPOSTags, semanticTypes));
////        templates.add(new PropertyTemplate(validPOSTags, semanticTypes));
//        templates.add(new NELLexicalTemplate(validPOSTags, frequentWordsToExclude, semanticTypes));
//        templates.add(new NELEdgeTemplate(validPOSTags, frequentWordsToExclude, semanticTypes));
//        templates.add(new NELNodeTemplate(validPOSTags, frequentWordsToExclude, semanticTypes));

        /*
         * Create the scorer object that computes a score from the factors'
         * features and the templates' weight vectors.
         */
//        Scorer scorer = new DefaultScorer();
        /*
         * Define a model and provide it with the necessary templates.
         */
        Model<AnnotatedDocument, State> model = new Model<>(scorer, nelTemplates);
        model.setMultiThreaded(false);

        /*
         * Create an Initializer that is responsible for providing an initial
         * state for the sampling chain given a document.
         */
        StateInitializer initializer = new StateInitializer();

        /*
         * Define the explorers that will provide "neighboring" states given a
         * starting state. The sampler will select one of these states as a
         * successor state and, thus, perform the sampling procedure.
         */
        List<Explorer<State>> explorers = new ArrayList<>();
        explorers.add(nelExplorer);
//        explorers.add(new L2KBEdgeExplorer(semanticTypes, linkingValidPOSTags, validEdges));
        /*
         * Create a sampler that generates sampling chains with which it will
         * trigger weight updates during training.
         */

 /*
         * Stopping criterion for the sampling process. If you set this value
         * too small, the sampler can not reach the optimal solution. Large
         * values, however, increase computation time.
         */
        BeamSearchStoppingCriterion<State> scoreStoppingCriterion = new BeamSearchStoppingCriterion<State>() {

            @Override
            public boolean checkCondition(List<List<State>> chain, int step) {

                List<State> lastStates = chain.get(chain.size() - 1);
                //sort by objective
                lastStates = lastStates.stream().sorted((s1, s2) -> Double.compare(s1.getObjectiveScore(), s2.getObjectiveScore())).collect(Collectors.toList());

                State s = (State) lastStates.get(lastStates.size() - 1);

                double maxScore = s.getObjectiveScore();

                if (maxScore == 1.0) {
                    return true;
                }

                int count = 0;
                final int maxCount = 4;

                for (int i = chain.size() - 1; i >= 0; i--) {
                    List<State> chainStates = chain.get(i);
                    State maxState = (State) chainStates.get(chainStates.size() - 1);

                    if (maxState.getObjectiveScore() >= maxScore) {
                        count++;
                    }
                }
                return count >= maxCount || chain.size() >= NUMBER_OF_SAMPLING_STEPS;
            }
        };

        /*
         * 
         */
        MyBeamSearchSampler<AnnotatedDocument, State, String> nelSampler = new MyBeamSearchSampler<>(model, objective, explorers,
                scoreStoppingCriterion);
        nelSampler.setTrainSamplingStrategy(SamplingStrategies.greedyBeamSearchSamplingStrategyByObjective(BEAM_SIZE_NEL_TRAINING, s -> s.getObjectiveScore()));
        nelSampler.setTrainAcceptStrategy(AcceptStrategies.strictObjectiveAccept());

        nelSampler.addStepCallback(new MyBeamSearchSampler.StepCallback() {
            @Override
            public <InstanceT, StateT extends AbstractState<InstanceT>> void onEndStep(MyBeamSearchSampler<InstanceT, StateT, ?> sampler, int step, int e, int numberOfExplorers, List<StateT> initialStates, List<StateT> currentStates) {

                for (final StateT stateT : currentStates) {

                    featureMapData
                            .addFeatureDataPoint(((State) stateT).toTrainingPoint(featureMapData, true));
                }
            }

        });

//        MySampler<AnnotatedDocument, State, String> sampler = new MySampler<>(model, objective, explorers,
//                stoppingCriterion);
//        sampler.setTrainingSamplingStrategy(SamplingStrategies.greedyObjectiveStrategy());
//        sampler.setTrainingAcceptStrategy(AcceptStrategies.objectiveAccept());
        /*
         * Define a learning strategy. The learner will receive state pairs
         * which can be used to update the models parameters.
         */
        Learner<State> learner = new AdvancedLearner<>(model, new SGD());

        log.info("####################");
        log.info("Start training");

        /*
         * The trainer will loop over the data and invoke sampling and learning.
         * Additionally, it can invoke predictions on new data.
         */
        NELTrainer neltrainer = new NELTrainer();
        neltrainer.addInstanceCallback(new InstanceCallback() {

            @Override
            public <InstanceT, StateT extends AbstractState<InstanceT>> void onEndInstance(NELTrainer caller,
                    InstanceT instance, int indexOfInstance, StateT finalState, int numberOfInstances, int epoch,
                    int numberOfEpochs) {

                if (scorer instanceof LibSVMRegressionScorer) {
                    ((LibSVMRegressionScorer) scorer).svmTrain(featureMapData);
                }
            }
        });
        //hybrid training procedure, switches every epoch to another scoring method {objective or model}
        neltrainer.addEpochCallback(new NELHybridSamplingStrategyCallback(nelSampler, BEAM_SIZE_NEL_TRAINING));

        //train the model
        List<SampledMultipleInstance<AnnotatedDocument, String, State>> trainResults = neltrainer.train(nelSampler, initializer, learner, trainingDocuments, i -> i.getGoldQueryString(), NUMBER_OF_EPOCHS);

        System.out.println("\nNEL Model :\n" + model.toDetailedString());

        //log the parsing coverage
        Performance.logNELTrain();

        Map<Model<AnnotatedDocument, State>, List<SampledMultipleInstance<AnnotatedDocument, String, State>>> pair = new HashMap<>();
        pair.put(model, trainResults);

        return pair;
    }

    private static Map<Model<AnnotatedDocument, State>, List<SampledMultipleInstance<AnnotatedDocument, String, State>>> trainQA(List<SampledMultipleInstance<AnnotatedDocument, String, State>> nelInstances) {
        /*
         * Setup all necessary components for training and testing.
         */
 /*
         * Define an objective function that guides the training procedure.
         */
        ObjectiveFunction<State, String> objective = new QAObjectiveFunction();

        /*
         * Define templates that are responsible to generate factors/features to
         * score generated states.
         */
//        List<AbstractTemplate<AnnotatedDocument, State, ?>> templates = new ArrayList<>();
////        templates.add(new ResourceTemplate(validPOSTags, semanticTypes));
////        templates.add(new PropertyTemplate(validPOSTags, semanticTypes));
////        templates.add(new QALexicalTemplate(validPOSTags, frequentWordsToExclude, semanticTypes, specialSemanticTypes));
//        templates.add(new QAEdgeTemplate(validPOSTags, frequentWordsToExclude, specialSemanticTypes));

        /*
         * Create the scorer object that computes a score from the factors'
         * features and the templates' weight vectors.
         */
//        Scorer scorer = new DefaultScorer();
        /*
         * Define a model and provide it with the necessary templates.
         */
        Model<AnnotatedDocument, State> model = new Model<>(scorer, qaTemplates);
        model.setMultiThreaded(false);


        /*
         * Define the explorers that will provide "neighboring" states given a
         * starting state. The sampler will select one of these states as a
         * successor state and, thus, perform the sampling procedure.
         */
        List<Explorer<State>> explorers = new ArrayList<>();
//        explorers.add(new SingleNodeExplorer(semanticTypes, frequentWordsToExclude, validPOSTags));
        explorers.add(qaExplorer);
//        explorers.add(new EntityBasedSingleNodeExplorer(semanticTypes, qaValidPOSTags));
        /*
         * Create a sampler that generates sampling chains with which it will
         * trigger weight updates during training.
         */

 /*
         * Stopping criterion for the sampling process. If you set this value
         * too small, the sampler can not reach the optimal solution. Large
         * values, however, increase computation time.
         */
        BeamSearchStoppingCriterion<State> scoreStoppingCriterion = new BeamSearchStoppingCriterion<State>() {

            @Override
            public boolean checkCondition(List<List<State>> chain, int step) {

                List<State> lastStates = chain.get(chain.size() - 1);
                //sort by model
                lastStates = lastStates.stream().sorted((s1, s2) -> Double.compare(s1.getObjectiveScore(), s2.getObjectiveScore())).collect(Collectors.toList());
                State s = (State) lastStates.get(lastStates.size() - 1);

                double maxScore = s.getObjectiveScore();

                if (maxScore == 1.0) {
                    return true;
                }

                int count = 0;
                final int maxCount = 4;

                for (int i = chain.size() - 1; i >= 0; i--) {
                    List<State> chainStates = chain.get(i);
                    State maxState = (State) chainStates.get(chainStates.size() - 1);

                    if (maxState.getObjectiveScore() >= maxScore) {
                        count++;
                    }
                }
                return count >= maxCount || chain.size() >= NUMBER_OF_SAMPLING_STEPS;
            }
        };

        /*
         * 
         */
        QABeamSearchSampler<AnnotatedDocument, State, String> sampler = new QABeamSearchSampler<>(model, objective, explorers,
                scoreStoppingCriterion);
//        sampler.setTrainSamplingStrategy(BeamSearchSamplingStrategies.greedyBeamSearchSamplingStrategyByObjective(BEAM_SIZE_QA_TRAINING, s -> s.getObjectiveScore()));
        sampler.setTrainSamplingStrategy(SamplingStrategies.greedyBeamSearchSamplingStrategyByObjective(BEAM_SIZE_QA_TRAINING, s -> s.getObjectiveScore()));
        sampler.setTrainAcceptStrategy(AcceptStrategies.strictObjectiveAccept());

        sampler.addStepCallback(new QABeamSearchSampler.StepCallback() {
            @Override
            public <InstanceT, StateT extends AbstractState<InstanceT>> void onEndStep(QABeamSearchSampler<InstanceT, StateT, ?> sampler, int step, int e, int numberOfExplorers, List<StateT> initialStates, List<StateT> currentStates) {

                for (final StateT stateT : currentStates) {

                    featureMapData
                            .addFeatureDataPoint(((State) stateT).toTrainingPoint(featureMapData, true));
                }
            }

        });

//        MySampler<AnnotatedDocument, State, String> sampler = new MySampler<>(model, objective, explorers,
//                stoppingCriterion);
//        sampler.setTrainingSamplingStrategy(SamplingStrategies.greedyObjectiveStrategy());
//        sampler.setTrainingAcceptStrategy(AcceptStrategies.objectiveAccept());
        /*
         * Define a learning strategy. The learner will receive state pairs
         * which can be used to update the models parameters.
         */
        Learner<State> learner = new AdvancedLearner<>(model, new SGD());

        log.info("####################");
        log.info("Start training");

        /*
         * The trainer will loop over the data and invoke sampling and learning.
         * Additionally, it can invoke predictions on new data.
         */
        QATrainer trainer = new QATrainer();
        //hybrid training procedure, switches every epoch to another scoring method {objective or model}
        trainer.addEpochCallback(new QAHybridSamplingStrategyCallback(sampler, BEAM_SIZE_QA_TRAINING));

        trainer.addInstanceCallback(new QATrainer.InstanceCallback() {
            @Override
            public <InstanceT, StateT extends AbstractState<InstanceT>> void onEndInstance(QATrainer caller, InstanceT instance, int indexOfInstance, StateT finalState, int numberOfInstances, int epoch, int numberOfEpochs) {
                if (scorer instanceof LibSVMRegressionScorer) {
                    ((LibSVMRegressionScorer) scorer).svmTrain(featureMapData);
                }
            }
        });

        //set objective scores to 0
        for (SampledMultipleInstance<AnnotatedDocument, String, State> triple : nelInstances) {

            for (State state : triple.getStates()) {

                state.setObjectiveScore(0);
                state.setModelScore(1.0);
            }
        }

        //train the model
        List<SampledMultipleInstance<AnnotatedDocument, String, State>> finalStates = trainer.train(sampler, nelInstances, learner, NUMBER_OF_EPOCHS);

        System.out.println("\nQA Model :\n" + model.toDetailedString());

        //log the parsing coverage
        Performance.logQATrain();

        Map<Model<AnnotatedDocument, State>, List<SampledMultipleInstance<AnnotatedDocument, String, State>>> pair = new HashMap<>();
        pair.put(model, finalStates);

        return pair;

    }

    private static Map<Model<AnnotatedDocument, State>, List<SampledMultipleInstance<AnnotatedDocument, String, State>>> trainQueryType(List<SampledMultipleInstance<AnnotatedDocument, String, State>> qaInstances) {
        /*
         * Setup all necessary components for training and testing.
         */
 /*
         * Define an objective function that guides the training procedure.
         */
        ObjectiveFunction<State, String> objective = new QueryTypeObjectiveFunction();

        /*
         * Define templates that are responsible to generate factors/features to
         * score generated states.
         */
//        List<AbstractTemplate<AnnotatedDocument, State, ?>> templates = new ArrayList<>();
////        templates.add(new ResourceTemplate(validPOSTags, semanticTypes));
////        templates.add(new PropertyTemplate(validPOSTags, semanticTypes));
////        templates.add(new QALexicalTemplate(validPOSTags, frequentWordsToExclude, semanticTypes, specialSemanticTypes));
//        templates.add(new QAEdgeTemplate(validPOSTags, frequentWordsToExclude, specialSemanticTypes));

        /*
         * Create the scorer object that computes a score from the factors'
         * features and the templates' weight vectors.
         */
//        Scorer scorer = new DefaultScorer();
        /*
         * Define a model and provide it with the necessary templates.
         */
        Model<AnnotatedDocument, State> model = new Model<>(scorer, queryTypeTemplates);
        model.setMultiThreaded(false);


        /*
         * Define the explorers that will provide "neighboring" states given a
         * starting state. The sampler will select one of these states as a
         * successor state and, thus, perform the sampling procedure.
         */
        List<Explorer<State>> explorers = new ArrayList<>();
//        explorers.add(new SingleNodeExplorer(semanticTypes, frequentWordsToExclude, validPOSTags));
        explorers.add(new QueryTypeExplorer(semanticTypes, specialSemanticTypes, qaValidPOSTags, validEdges));
//        explorers.add(new EntityBasedSingleNodeExplorer(semanticTypes, qaValidPOSTags));
        /*
         * Create a sampler that generates sampling chains with which it will
         * trigger weight updates during training.
         */

 /*
         * Stopping criterion for the sampling process. If you set this value
         * too small, the sampler can not reach the optimal solution. Large
         * values, however, increase computation time.
         */
        BeamSearchStoppingCriterion<State> scoreStoppingCriterion = new BeamSearchStoppingCriterion<State>() {

            @Override
            public boolean checkCondition(List<List<State>> chain, int step) {

                List<State> lastStates = chain.get(chain.size() - 1);
                //sort by model
                lastStates = lastStates.stream().sorted((s1, s2) -> Double.compare(s1.getObjectiveScore(), s2.getObjectiveScore())).collect(Collectors.toList());
                State s = (State) lastStates.get(lastStates.size() - 1);

                double maxScore = s.getObjectiveScore();

                if (maxScore == 1.0) {
                    return true;
                }

                int count = 0;
                final int maxCount = 4;

                for (int i = chain.size() - 1; i >= 0; i--) {
                    List<State> chainStates = chain.get(i);
                    State maxState = (State) chainStates.get(chainStates.size() - 1);

                    if (maxState.getObjectiveScore() >= maxScore) {
                        count++;
                    }
                }
                return count >= maxCount || chain.size() >= NUMBER_OF_SAMPLING_STEPS;
            }
        };

        /*
         * 
         */
        QABeamSearchSampler<AnnotatedDocument, State, String> sampler = new QABeamSearchSampler<>(model, objective, explorers,
                scoreStoppingCriterion);
//        sampler.setTrainSamplingStrategy(BeamSearchSamplingStrategies.greedyBeamSearchSamplingStrategyByObjective(BEAM_SIZE_QA_TRAINING, s -> s.getObjectiveScore()));
        sampler.setTrainSamplingStrategy(SamplingStrategies.greedyBeamSearchSamplingStrategyByObjective(BEAM_SIZE_QA_TRAINING, s -> s.getObjectiveScore()));
        sampler.setTrainAcceptStrategy(AcceptStrategies.strictObjectiveAccept());

//        MySampler<AnnotatedDocument, State, String> sampler = new MySampler<>(model, objective, explorers,
//                stoppingCriterion);
//        sampler.setTrainingSamplingStrategy(SamplingStrategies.greedyObjectiveStrategy());
//        sampler.setTrainingAcceptStrategy(AcceptStrategies.objectiveAccept());
        /*
         * Define a learning strategy. The learner will receive state pairs
         * which can be used to update the models parameters.
         */
        Learner<State> learner = new AdvancedLearner<>(model, new SGD());

        log.info("####################");
        log.info("Start training");

        /*
         * The trainer will loop over the data and invoke sampling and learning.
         * Additionally, it can invoke predictions on new data.
         */
        QATrainer trainer = new QATrainer();
        //hybrid training procedure, switches every epoch to another scoring method {objective or model}
        trainer.addEpochCallback(new QAHybridSamplingStrategyCallback(sampler, BEAM_SIZE_QA_TRAINING));

        //set objective scores to 0
        for (SampledMultipleInstance<AnnotatedDocument, String, State> triple : qaInstances) {

            for (State state : triple.getStates()) {

                state.setObjectiveScore(0);
                state.setModelScore(1.0);
            }
        }

        //train the model
        List<SampledMultipleInstance<AnnotatedDocument, String, State>> finalStates = trainer.train(sampler, qaInstances, learner, NUMBER_OF_EPOCHS);

        System.out.println("\nQuery Type Model :\n" + model.toDetailedString());

        //log the parsing coverage
        Performance.logQATrain();

        Map<Model<AnnotatedDocument, State>, List<SampledMultipleInstance<AnnotatedDocument, String, State>>> pair = new HashMap<>();
        pair.put(model, finalStates);

        return pair;

    }

    private static List<SampledMultipleInstance<AnnotatedDocument, String, State>> testNEL(Model<AnnotatedDocument, State> model, List<AnnotatedDocument> testDocuments) {
        /*
         * Setup all necessary components for training and testing.
         */
 /*
         * Define an objective function that guides the training procedure.
         */
        ObjectiveFunction<State, String> objective = new NELObjectiveFunction();

        /*
         * Define templates that are responsible to generate factors/features to
         * score generated states.
         */
//        List<AbstractTemplate<AnnotatedDocument, State, ?>> templates = new ArrayList<>();
////        templates.add(new ResourceTemplate(validPOSTags, semanticTypes));
////        templates.add(new PropertyTemplate(validPOSTags, semanticTypes));
//        templates.add(new NELLexicalTemplate(validPOSTags, frequentWordsToExclude, semanticTypes));
//        templates.add(new NELEdgeTemplate(validPOSTags, frequentWordsToExclude, semanticTypes));
//        templates.add(new NELNodeTemplate(validPOSTags, frequentWordsToExclude, semanticTypes));

        /*
         * initialize QATemplateFactory
         */
//        QATemplateFactory.initialize(validPOSTags, frequentWordsToExclude, semanticTypes, specialSemanticTypes);

        /*
         * Create an Initializer that is responsible for providing an initial
         * state for the sampling chain given a document.
         */
        StateInitializer initializer = new StateInitializer();


        /*
         * Define the explorers that will provide "neighboring" states given a
         * starting state. The sampler will select one of these states as a
         * successor state and, thus, perform the sampling procedure.
         */
        List<Explorer<State>> explorers = new ArrayList<>();
//        explorers.add(new SingleNodeExplorer(semanticTypes, frequentWordsToExclude, validPOSTags));
//        explorers.add(new L2KBEdgeExplorer(semanticTypes, linkingValidPOSTags, validEdges));
        explorers.add(nelExplorer);
        /*
         * Create a sampler that generates sampling chains with which it will
         * trigger weight updates during training.
         */

 /*
         * Stopping criterion for the sampling process. If you set this value
         * too small, the sampler can not reach the optimal solution. Large
         * values, however, increase computation time.
         */
        BeamSearchStoppingCriterion<State> scoreStoppingCriterion = new BeamSearchStoppingCriterion<State>() {

            @Override
            public boolean checkCondition(List<List<State>> chain, int step) {

                List<State> lastStates = chain.get(chain.size() - 1);
                State s = (State) lastStates.get(lastStates.size() - 1);

                double maxScore = s.getModelScore();

                int count = 0;
                final int maxCount = 4;

                for (int i = chain.size() - 1; i >= 0; i--) {
                    List<State> chainStates = chain.get(i);
                    State maxState = (State) chainStates.get(chainStates.size() - 1);

                    if (maxState.getModelScore() >= maxScore) {
                        count++;
                    }
                }
                return count >= maxCount || chain.size() >= NUMBER_OF_SAMPLING_STEPS;
            }
        };

        /*
         * 
         */
        MyBeamSearchSampler<AnnotatedDocument, State, String> sampler = new MyBeamSearchSampler<>(model, objective, explorers,
                scoreStoppingCriterion);
        sampler.setTestSamplingStrategy(SamplingStrategies.greedyBeamSearchSamplingStrategyByModel(BEAM_SIZE_NEL_TEST, s -> s.getModelScore()));
        sampler.setTestAcceptStrategy(AcceptStrategies.strictModelAccept());

        log.info("####################");
        log.info("Start testing");

        /*
         * The trainer will loop over the data and invoke sampling.
         */
        NELTrainer trainer = new NELTrainer();

        List<SampledMultipleInstance<AnnotatedDocument, String, State>> testResults = trainer.test(sampler, initializer, testDocuments, i -> i.getGoldQueryString());
        /*
         * Since the test function does not compute the objective score of its
         * predictions, we do that here, manually, before we print the results.
         */

//        Performance.logTest(testResults, objective);
        return testResults;
    }

    private static List<SampledMultipleInstance<AnnotatedDocument, String, State>> testQA(Model<AnnotatedDocument, State> model, List<SampledMultipleInstance<AnnotatedDocument, String, State>> nelInstances) {
        /*
         * Setup all necessary components for training and testing.
         */
 /*
         * Define an objective function that guides the training procedure.
         */
        ObjectiveFunction<State, String> objective = new QAObjectiveFunction();

        /*
         * Define templates that are responsible to generate factors/features to
         * score generated states.
         */
//        List<AbstractTemplate<AnnotatedDocument, State, ?>> templates = new ArrayList<>();
////        templates.add(new ResourceTemplate(validPOSTags, semanticTypes));
////        templates.add(new PropertyTemplate(validPOSTags, semanticTypes));
////        templates.add(new QALexicalTemplate(validPOSTags, frequentWordsToExclude, semanticTypes, specialSemanticTypes));
//        templates.add(new QAEdgeTemplate(validPOSTags, frequentWordsToExclude, specialSemanticTypes));

        /*
         * initialize QATemplateFactory
         */
//        QATemplateFactory.initialize(validPOSTags, frequentWordsToExclude, semanticTypes, specialSemanticTypes);

        /*
         * Define the explorers that will provide "neighboring" states given a
         * starting state. The sampler will select one of these states as a
         * successor state and, thus, perform the sampling procedure.
         */
        List<Explorer<State>> explorers = new ArrayList<>();
//        explorers.add(new SingleNodeExplorer(semanticTypes, frequentWordsToExclude, validPOSTags));
        explorers.add(qaExplorer);
        /*
         * Create a sampler that generates sampling chains with which it will
         * trigger weight updates during training.
         */

 /*
         * Stopping criterion for the sampling process. If you set this value
         * too small, the sampler can not reach the optimal solution. Large
         * values, however, increase computation time.
         */
        BeamSearchStoppingCriterion<State> scoreStoppingCriterion = new BeamSearchStoppingCriterion<State>() {

            @Override
            public boolean checkCondition(List<List<State>> chain, int step) {

                List<State> lastStates = chain.get(chain.size() - 1);
                State s = (State) lastStates.get(lastStates.size() - 1);

                double maxScore = s.getModelScore();

                int count = 0;
                final int maxCount = 4;

                for (int i = chain.size() - 1; i >= 0; i--) {
                    List<State> chainStates = chain.get(i);
                    State maxState = (State) chainStates.get(chainStates.size() - 1);

                    if (maxState.getModelScore() >= maxScore) {
                        count++;
                    }
                }
                return count >= maxCount || chain.size() >= NUMBER_OF_SAMPLING_STEPS;
            }
        };

        /*
         * 
         */
        QABeamSearchSampler<AnnotatedDocument, State, String> sampler = new QABeamSearchSampler<>(model, objective, explorers,
                scoreStoppingCriterion);
//        sampler.setTestSamplingStrategy(BeamSearchSamplingStrategies.greedyBeamSearchSamplingStrategyByModel(BEAM_SIZE_QA_TEST, s -> s.getModelScore()));
        sampler.setTestSamplingStrategy(SamplingStrategies.greedyBeamSearchSamplingStrategyByModel(BEAM_SIZE_QA_TEST, s -> s.getModelScore()));
        sampler.setTestAcceptStrategy(AcceptStrategies.strictModelAccept());

        log.info("####################");
        log.info("Start testing");

        for (SampledMultipleInstance<AnnotatedDocument, String, State> triple : nelInstances) {

            for (State state : triple.getStates()) {

                state.setObjectiveScore(0);
                state.setModelScore(1.0);
            }
        }

        /*
         * The trainer will loop over the data and invoke sampling.
         */
        QATrainer trainer = new QATrainer();

        List<SampledMultipleInstance<AnnotatedDocument, String, State>> testResults = trainer.test(sampler, nelInstances);
        /*
         * Since the test function does not compute the objective score of its
         * predictions, we do that here, manually, before we print the results.
         */

        return testResults;
    }

    private static List<SampledMultipleInstance<AnnotatedDocument, String, State>> testQueryType(Model<AnnotatedDocument, State> model, List<SampledMultipleInstance<AnnotatedDocument, String, State>> qaInstances) {
        /*
         * Setup all necessary components for training and testing.
         */
 /*
         * Define an objective function that guides the training procedure.
         */
        ObjectiveFunction<State, String> objective = new QueryTypeObjectiveFunction();

        /*
         * Define templates that are responsible to generate factors/features to
         * score generated states.
         */
//        List<AbstractTemplate<AnnotatedDocument, State, ?>> templates = new ArrayList<>();
////        templates.add(new ResourceTemplate(validPOSTags, semanticTypes));
////        templates.add(new PropertyTemplate(validPOSTags, semanticTypes));
////        templates.add(new QALexicalTemplate(validPOSTags, frequentWordsToExclude, semanticTypes, specialSemanticTypes));
//        templates.add(new QAEdgeTemplate(validPOSTags, frequentWordsToExclude, specialSemanticTypes));

        /*
         * initialize QATemplateFactory
         */
//        QATemplateFactory.initialize(validPOSTags, frequentWordsToExclude, semanticTypes, specialSemanticTypes);

        /*
         * Define the explorers that will provide "neighboring" states given a
         * starting state. The sampler will select one of these states as a
         * successor state and, thus, perform the sampling procedure.
         */
        List<Explorer<State>> explorers = new ArrayList<>();
//        explorers.add(new SingleNodeExplorer(semanticTypes, frequentWordsToExclude, validPOSTags));
        explorers.add(new QueryTypeExplorer(semanticTypes, specialSemanticTypes, qaValidPOSTags, validEdges));
        /*
         * Create a sampler that generates sampling chains with which it will
         * trigger weight updates during training.
         */

 /*
         * Stopping criterion for the sampling process. If you set this value
         * too small, the sampler can not reach the optimal solution. Large
         * values, however, increase computation time.
         */
        BeamSearchStoppingCriterion<State> scoreStoppingCriterion = new BeamSearchStoppingCriterion<State>() {

            @Override
            public boolean checkCondition(List<List<State>> chain, int step) {

                List<State> lastStates = chain.get(chain.size() - 1);
                State s = (State) lastStates.get(lastStates.size() - 1);

                double maxScore = s.getModelScore();

                int count = 0;
                final int maxCount = 4;

                for (int i = chain.size() - 1; i >= 0; i--) {
                    List<State> chainStates = chain.get(i);
                    State maxState = (State) chainStates.get(chainStates.size() - 1);

                    if (maxState.getModelScore() >= maxScore) {
                        count++;
                    }
                }
                return count >= maxCount || chain.size() >= NUMBER_OF_SAMPLING_STEPS;
            }
        };

        /*
         * 
         */
        QABeamSearchSampler<AnnotatedDocument, State, String> sampler = new QABeamSearchSampler<>(model, objective, explorers,
                scoreStoppingCriterion);
//        sampler.setTestSamplingStrategy(BeamSearchSamplingStrategies.greedyBeamSearchSamplingStrategyByModel(BEAM_SIZE_QA_TEST, s -> s.getModelScore()));
        sampler.setTestSamplingStrategy(SamplingStrategies.greedyBeamSearchSamplingStrategyByModel(BEAM_SIZE_QA_TEST, s -> s.getModelScore()));
        sampler.setTestAcceptStrategy(AcceptStrategies.strictModelAccept());

        log.info("####################");
        log.info("Start testing");

        for (SampledMultipleInstance<AnnotatedDocument, String, State> triple : qaInstances) {

            for (State state : triple.getStates()) {

                state.setObjectiveScore(0);
                state.setModelScore(1.0);
            }
        }

        /*
         * The trainer will loop over the data and invoke sampling.
         */
        QATrainer trainer = new QATrainer();

        List<SampledMultipleInstance<AnnotatedDocument, String, State>> testResults = trainer.test(sampler, qaInstances);
        /*
         * Since the test function does not compute the objective score of its
         * predictions, we do that here, manually, before we print the results.
         */

        return testResults;
    }
}
