/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.utils;

import de.citec.sc.main.Main;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author sherzod
 */
public class ProjectConfiguration {

    public static void loadConfigurations(String[] args) {

        //read parameters
        readParamsFromCommandLine(args);
    }

    private static final Map<String, String> PARAMETERS = new HashMap<>();

    private static final String PARAMETER_PREFIX = "-";
    private static final String PARAM_SETTING_TRAIN_DATASET = "-trainDataset";
    private static final String PARAM_SETTING_TEST_DATASET = "-testDataset";
    private static final String PARAM_SETTING_MANUAL_LEXICON = "-includeManualLexicon";
    private static final String PARAM_SETTING_MATOLL = "-includeMatollLexicon";
    private static final String PARAM_SETTING_EPOCHS = "-epochs";
    private static final String PARAM_SETTING_SAMPLING_STEPS = "-samplingSteps";
    private static final String PARAM_SETTING_BEAMSIZE_TRAINING_NEL = "-nelBeamSize";
    private static final String PARAM_SETTING_BEAMSIZE_TRAINING_QA = "-qaBeamSize";
    private static final String PARAM_SETTING_INDEX = "-indexType";
    private static final String PARAM_SETTING_TRAIN_MAX_WORD_COUNT = "-maxWordCountTrain";
    private static final String PARAM_SETTING_TEST_MAX_WORD_COUNT = "-maxWordCountTrain";
    private static final String PARAM_SETTING_LANGUAGE = "-language";
    private static final String PARAM_SETTING_FEATURE_GROUP = "-featureLevel";
    private static final String PARAM_SETTING_WORD_EMBEDDING = "-includeEmbeddingLexicon";
    private static final String PARAM_SETTING_USE_DBPEDIA_ENDPOINT = "-useDBpedia";
    private static final String PARAM_SETTING_DBPEDIA_ENDPOINT_SERVER = "-dbpediaEndpoint";
    private static final String PARAM_SETTING_API = "-runAsAPI";
    private static final String PARAM_SETTING_LINKING_SAMPLING_LEVEL = "-linkingSamplingLevel";
    private static final String PARAM_SETTING_QA_SAMPLING_LEVEL = "-qaSamplingLevel";
    
    public static String getLinkingSamplingLevel() {

        if(PARAMETERS.isEmpty()){
            Main.initializeProjectConfiguration();
        }
        
        return PARAMETERS.get(PARAM_SETTING_LINKING_SAMPLING_LEVEL);
    }
    public static String getQASamplingLevel() {
        if(PARAMETERS.isEmpty()){
            Main.initializeProjectConfiguration();
        }
        

        return PARAMETERS.get(PARAM_SETTING_QA_SAMPLING_LEVEL);
    }

    public static boolean useRemoteDBpediaEndpoint() {
        if(PARAMETERS.isEmpty()){
            Main.initializeProjectConfiguration();
        }
        

        boolean useDBpediaEndpoint = "remote".equals(PARAMETERS.get(PARAM_SETTING_DBPEDIA_ENDPOINT_SERVER));

        return useDBpediaEndpoint;
    }
    public static boolean startAPI(){
        if(PARAMETERS.isEmpty()){
            Main.initializeProjectConfiguration();
        }
        

        boolean startAPI = "true".equals(PARAMETERS.get(PARAM_SETTING_API));

        return startAPI;
    }
    
    public static boolean useDBpediaEndpoint() {
        if(PARAMETERS.isEmpty()){
            Main.initializeProjectConfiguration();
        }
        

        boolean useDBpediaEndpoint = "true".equals(PARAMETERS.get(PARAM_SETTING_USE_DBPEDIA_ENDPOINT));

        return useDBpediaEndpoint;
    }
    
    public static boolean useEmbeddingLexicon() {
        if(PARAMETERS.isEmpty()){
            Main.initializeProjectConfiguration();
        }
        

        boolean useEmbeddingLexicon = "true".equals(PARAMETERS.get(PARAM_SETTING_WORD_EMBEDDING));

        return useEmbeddingLexicon;
    }
    
    public static String getIndex() {
        if(PARAMETERS.isEmpty()){
            Main.initializeProjectConfiguration();
        }

        return PARAMETERS.get(PARAM_SETTING_INDEX);
    }
    public static String getFeatureGroup() {
        if(PARAMETERS.isEmpty()){
            Main.initializeProjectConfiguration();
        }

        return PARAMETERS.get(PARAM_SETTING_FEATURE_GROUP);
    }
    public static String getLanguage() {
        if(PARAMETERS.isEmpty()){
            Main.initializeProjectConfiguration();
        }

        return PARAMETERS.get(PARAM_SETTING_LANGUAGE);
    }

    public static String getTrainingDatasetName() {
        if(PARAMETERS.isEmpty()){
            Main.initializeProjectConfiguration();
        }

        return PARAMETERS.get(PARAM_SETTING_TRAIN_DATASET);
    }

    public static String getTestDatasetName() {
        if(PARAMETERS.isEmpty()){
            Main.initializeProjectConfiguration();
        }

        return PARAMETERS.get(PARAM_SETTING_TEST_DATASET);
    }

    public static boolean useManualLexicon() {
        if(PARAMETERS.isEmpty()){
            Main.initializeProjectConfiguration();
        }

        boolean useManualLexicon = "true".equals(PARAMETERS.get(PARAM_SETTING_MANUAL_LEXICON));

        return useManualLexicon;
    }

    public static boolean useMatoll() {
        if(PARAMETERS.isEmpty()){
            Main.initializeProjectConfiguration();
        }

        boolean useMatoll = "true".equals(PARAMETERS.get(PARAM_SETTING_MATOLL));

        return useMatoll;
    }

    public static int getNumberOfEpochs() {
        if(PARAMETERS.isEmpty()){
            Main.initializeProjectConfiguration();
        }
        
        int numberOfEpochs = Integer.parseInt(PARAMETERS.get(PARAM_SETTING_EPOCHS));

        return numberOfEpochs;
    }

    public static int getTrainMaxWordCount() {
        if(PARAMETERS.isEmpty()){
            Main.initializeProjectConfiguration();
        }
        int numberOfEpochs = Integer.parseInt(PARAMETERS.get(PARAM_SETTING_TRAIN_MAX_WORD_COUNT));

        return numberOfEpochs;
    }

    public static int getTestMaxWordCount() {
        if(PARAMETERS.isEmpty()){
            Main.initializeProjectConfiguration();
        }
        int numberOfEpochs = Integer.parseInt(PARAMETERS.get(PARAM_SETTING_TEST_MAX_WORD_COUNT));

        return numberOfEpochs;
    }

    public static int getNumberOfSamplingSteps() {
        if(PARAMETERS.isEmpty()){
            Main.initializeProjectConfiguration();
        }
        int numberOfSamplingSteps = Integer.parseInt(PARAMETERS.get(PARAM_SETTING_SAMPLING_STEPS));

        return numberOfSamplingSteps;
    }

    public static int getNELTrainingBeamSize() {
        if(PARAMETERS.isEmpty()){
            Main.initializeProjectConfiguration();
        }
        int numberKSamples = Integer.parseInt(PARAMETERS.get(PARAM_SETTING_BEAMSIZE_TRAINING_NEL));

        return numberKSamples;
    }

    public static int getQATrainingBeamSize() {
        if(PARAMETERS.isEmpty()){
            Main.initializeProjectConfiguration();
        }
        int numberKSamples = Integer.parseInt(PARAMETERS.get(PARAM_SETTING_BEAMSIZE_TRAINING_QA));

        return numberKSamples;
    }

    private static void readParamsFromCommandLine(String[] args) {
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].startsWith(PARAMETER_PREFIX)) {
                    PARAMETERS.put(args[i], args[i++ + 1]); // Skip value
                }
            }
        }
    }

    public static String getAllParameters() {
        String s = "";
        for (String k : PARAMETERS.keySet()) {
            s += k + "--" + PARAMETERS.get(k) + "\n";
        }
        return s;
    }
}
