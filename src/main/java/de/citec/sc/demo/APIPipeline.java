package de.citec.sc.demo;

import de.citec.sc.main.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.corpus.SampledMultipleInstance;
import de.citec.sc.gerbil.instance.QALDInstance;
import de.citec.sc.learning.NELObjectiveFunction;
import de.citec.sc.learning.NELHybridSamplingStrategyCallback;
import de.citec.sc.learning.NELTrainer;
import de.citec.sc.learning.QAHybridSamplingStrategyCallback;
import de.citec.sc.learning.QAObjectiveFunction;
import de.citec.sc.learning.QATrainer;
import de.citec.sc.learning.QueryConstructor;
import static de.citec.sc.main.Main.lang;
import de.citec.sc.parser.DependencyParse;
import de.citec.sc.parser.UDPipe;
import de.citec.sc.qald.Question;
import de.citec.sc.query.CandidateRetriever;
import de.citec.sc.query.CandidateRetrieverOnLucene;
import de.citec.sc.query.CandidateRetrieverOnMemory;
import de.citec.sc.query.DBpediaLabelRetriever;
import de.citec.sc.query.ManualLexicon;
import de.citec.sc.query.Search;
import de.citec.sc.sampling.L2KBEdgeExplorer;
import de.citec.sc.sampling.MyBeamSearchSampler;
import de.citec.sc.sampling.QABeamSearchSampler;
import de.citec.sc.sampling.SamplingStrategies;
import de.citec.sc.sampling.QCEdgeExplorer;
import de.citec.sc.sampling.StateInitializer;
import de.citec.sc.template.NELEdgeTemplate;
import de.citec.sc.template.NELLexicalTemplate;
import de.citec.sc.template.NELNodeTemplate;
import de.citec.sc.template.QAEdgeAdvTemplate;
import de.citec.sc.template.QAEdgeTemplate;
import de.citec.sc.template.QATemplateFactory;
import de.citec.sc.utils.DBpediaEndpoint;
import de.citec.sc.utils.LanguageDetector;
import de.citec.sc.utils.Performance;
import de.citec.sc.utils.ProjectConfiguration;
import de.citec.sc.variable.State;
import de.citec.sc.wordNet.WordNetAnalyzer;
import exceptions.UnkownTemplateRequestedException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
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

/**
 *
 * @author sjebbara
 */
public class APIPipeline {

    private static int NUMBER_OF_SAMPLING_STEPS = 15;
    private static int BEAM_SIZE_QA_TEST = 5;
    private static int BEAM_SIZE_NEL_TEST = 20;
    private static Logger log = LogManager.getFormatterLogger();

    private static Set<String> linkingValidPOSTags;
    private static Set<String> qaValidPOSTags;
    private static Map<Integer, String> semanticTypes;
    private static Map<Integer, String> specialSemanticTypes;
    private static Set<String> validEdges;
    private static List<AbstractTemplate<AnnotatedDocument, State, ?>> nelTemplates;
    private static List<AbstractTemplate<AnnotatedDocument, State, ?>> qaTemplates;
    private static Scorer scorer;
    private static Map<String, List<Model<AnnotatedDocument, State>>> trainedModels;
    private static boolean initialized = false;

    public static void initialize() {

        System.out.println("Initialization process has started ....");

        CandidateRetriever retriever = new CandidateRetrieverOnLucene(false, "luceneIndex");

        WordNetAnalyzer wordNet = new WordNetAnalyzer("src/main/resources/WordNet-3.0/dict");

        Search.load(retriever, wordNet);
        Search.useMatoll(ProjectConfiguration.useMatoll());

        ManualLexicon.useManualLexicon(ProjectConfiguration.useManualLexicon());

        System.out.println("Testing index: " + retriever.getAllResources("john f. kennedy", 10, CandidateRetriever.Language.EN));
        System.out.println("Testing index: " + retriever.getAllResources("goofy", 10, CandidateRetriever.Language.DE));
        System.out.println("Testing index: " + retriever.getAllPredicates("erfunden", 10, CandidateRetriever.Language.DE));
        System.out.println("Testing manual: " + ManualLexicon.getProperties("erfunden", lang));

        //semantic types to sample from
        semanticTypes = new LinkedHashMap<>();
        semanticTypes.put(1, "Property");
        semanticTypes.put(2, "Individual");
        semanticTypes.put(3, "Class");
        semanticTypes.put(4, "RestrictionClass");
//        semanticTypes.put(5, "UnderSpecifiedClass");

        //semantic types with special meaning
        specialSemanticTypes = new LinkedHashMap<>();
        specialSemanticTypes.put(semanticTypes.size() + 1, "What");//it should be higher than semantic type size
        specialSemanticTypes.put(semanticTypes.size() + 2, "Which");//it should be higher than semantic type size
//        specialSemanticTypes.put(semanticTypes.size() + 3, "When");//it should be higher than semantic type size
//        specialSemanticTypes.put(semanticTypes.size() + 4, "Who");//it should be higher than semantic type size
        specialSemanticTypes.put(semanticTypes.size() + 5, "HowMany");//it should be higher than semantic type size

        linkingValidPOSTags = new HashSet<>();
        linkingValidPOSTags.add("PROPN");
        linkingValidPOSTags.add("VERB");
        linkingValidPOSTags.add("NOUN");
        linkingValidPOSTags.add("ADJ");
        linkingValidPOSTags.add("ADV");
        linkingValidPOSTags.add("ADP");

        qaValidPOSTags = new HashSet<>();
        qaValidPOSTags.add("PRON");
        qaValidPOSTags.add("DET");
//        qaValidPOSTags.add("PROPN");
        qaValidPOSTags.add("VERB");
        qaValidPOSTags.add("NOUN");
        qaValidPOSTags.add("ADJ");
//        qaValidPOSTags.add("ADV");
//        qaValidPOSTags.add("ADP");

        validEdges = new HashSet<>();
        validEdges.add("obj");
        validEdges.add("obl");
        validEdges.add("flat");
        validEdges.add("compound");
        validEdges.add("nummod");
        validEdges.add("appos");
        validEdges.add("subj");
        validEdges.add("nsubj");
        validEdges.add("dobj");
        validEdges.add("iobj");
        validEdges.add("nsubjpass");
        validEdges.add("nsubj:pass");
        validEdges.add("acl:relcl");
        validEdges.add("csubj");
        validEdges.add("csubjpass");
        validEdges.add("csubj:pass");
        validEdges.add("nmod:poss");
        validEdges.add("ccomp");
        validEdges.add("nmod");
        validEdges.add("amod");
        validEdges.add("xcomp");
        validEdges.add("vocative");
        validEdges.add("discourse");
        validEdges.add("parataxis");
        validEdges.add("advmod");
        validEdges.add("flat");
        validEdges.add("name");
        validEdges.add("discourse");

        QueryConstructor.initialize(specialSemanticTypes, semanticTypes, linkingValidPOSTags, validEdges);

        scorer = new DefaultScorer();

        nelTemplates = new ArrayList<>();
        nelTemplates.add(new NELEdgeTemplate(linkingValidPOSTags, validEdges, semanticTypes));

        qaTemplates = new ArrayList<>();
        qaTemplates.add(new QAEdgeAdvTemplate(qaValidPOSTags, validEdges, semanticTypes, specialSemanticTypes));

        QATemplateFactory.initialize(linkingValidPOSTags, qaValidPOSTags, validEdges, semanticTypes, specialSemanticTypes);

        System.out.println("Loading trained models");
        loadModels();

        System.out.println("Finished initialization.");
    }

    private static void loadModels() {

        List<String> languages = new ArrayList<>();
        languages.add("EN");
        languages.add("DE");
        languages.add("ES");

        trainedModels = new HashMap<>();

        for (String language : languages) {
            List<Model<AnnotatedDocument, State>> modelsForLanguage = new ArrayList<>();

            QATemplateFactory factory = new QATemplateFactory();

            Model<AnnotatedDocument, State> modelNEL = new Model<>(APIPipeline.scorer, APIPipeline.nelTemplates);
            Model<AnnotatedDocument, State> modelQA = new Model<>(APIPipeline.scorer, APIPipeline.qaTemplates);

            try {
                modelNEL.loadModelFromDir("models/model_nel_" + language, factory);
                modelQA.loadModelFromDir("models/model_qa_" + language, factory);

                modelsForLanguage.add(modelNEL);
                modelsForLanguage.add(modelQA);

                trainedModels.put(language, modelsForLanguage);

            } catch (ClassNotFoundException ex) {
                java.util.logging.Logger.getLogger(APIPipeline.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnkownTemplateRequestedException ex) {
                java.util.logging.Logger.getLogger(APIPipeline.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(APIPipeline.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static String run(String text) {

        //detect the language
        String language = LanguageDetector.detect(text);

        Main.lang = CandidateRetriever.Language.valueOf(language);

        List<AnnotatedDocument> documents = new ArrayList<>();

        //dependency parse 
        DependencyParse parseTree = UDPipe.parse(text, Main.lang);
        Question qaldInstance = new Question(Main.lang, text, "SELECT DISTINCT ?uri WHERE {  <http://dbpedia.org/resource/Goofy> <http://dbpedia.org/ontology/creator> ?uri . }");

        //created annotated document
        AnnotatedDocument doc = new AnnotatedDocument(parseTree, qaldInstance);

        documents.add(doc);

        //get the model for specific language
        List<Model<AnnotatedDocument, State>> models = trainedModels.get(language);

        Model<AnnotatedDocument, State> nelModel = models.get(0);
        Model<AnnotatedDocument, State> qaModel = models.get(1);

        long startTime = System.currentTimeMillis();
        //run the process
        List<SampledMultipleInstance<AnnotatedDocument, String, State>> nelInstances = testNEL(nelModel, documents);
        List<SampledMultipleInstance<AnnotatedDocument, String, State>> qaInstances = testQA(qaModel, nelInstances);

        String query = "";
        //loop over states and find the most probable state
        for (SampledMultipleInstance<AnnotatedDocument, String, State> triple : qaInstances) {

            State maxState = null;
            for (State state : triple.getStates()) {

                boolean isValidState = isValidState(state);

                if (isValidState) {
                    maxState = state;
                    break;
                }
            }

            if (maxState != null) {
                query = QueryConstructor.getSPARQLQuery(maxState);
                break;
            }
        }

        return query;
    }

    private static boolean isValidState(State state) {
        String s = state.toString();

        String query = QueryConstructor.getSPARQLQuery(state);

        List<Integer> tokenIDs = new ArrayList<>(state.getDocument().getParse().getNodes().keySet());
        Collections.sort(tokenIDs);

        Integer firstToken = tokenIDs.get(0);
        String firstPOS = state.getDocument().getParse().getPOSTag(firstToken);

        if (query.contains("ASK") && firstPOS.equals("PRON")) {
            return false;
        }

        Set<String> answers = DBpediaEndpoint.runQuery(query, false);
        if (answers.isEmpty()) {
            return false;
        }

        return true;
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
        explorers.add(new L2KBEdgeExplorer(semanticTypes, linkingValidPOSTags, validEdges));
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
        explorers.add(new QCEdgeExplorer(semanticTypes, specialSemanticTypes, qaValidPOSTags, validEdges));
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
}
