/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.utils;

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
    private static final String PARAM_SETTING_DATASET = "-d1";
    private static final String PARAM_SETTING_TEST_DATASET = "-d2";
    private static final String PARAM_SETTING_MANUAL_LEXICON = "-m1";
    private static final String PARAM_SETTING_MATOLL = "-m2";
    private static final String PARAM_SETTING_EPOCHS = "-e";
    private static final String PARAM_SETTING_SAMPLING_STEPS = "-s";
    private static final String PARAM_SETTING_BEAMSIZE_TRAINING_NEL = "-k1";
    private static final String PARAM_SETTING_BEAMSIZE_TRAINING_QA = "-k2";
    private static final String PARAM_SETTING_BEAMSIZE_TEST_NEL = "-l1";
    private static final String PARAM_SETTING_BEAMSIZE_TEST_QA = "-l2";
    private static final String PARAM_SETTING_INDEX = "-i";
    private static final String PARAM_SETTING_TRAIN_MAX_WORD_COUNT = "-w1";
    private static final String PARAM_SETTING_TEST_MAX_WORD_COUNT = "-w2";
    private static final String PARAM_SETTING_LANGUAGE = "-l";
    private static final String PARAM_SETTING_FEATURE_GROUP = "-f";

    public static String getIndex() {

        return PARAMETERS.get(PARAM_SETTING_INDEX);
    }
    public static String getFeatureGroup() {

        return PARAMETERS.get(PARAM_SETTING_FEATURE_GROUP);
    }
    public static String getLanguage() {

        return PARAMETERS.get(PARAM_SETTING_LANGUAGE);
    }

    public static String getTrainingDatasetName() {

        return PARAMETERS.get(PARAM_SETTING_DATASET);
    }

    public static String getTestDatasetName() {

        return PARAMETERS.get(PARAM_SETTING_TEST_DATASET);
    }

    public static boolean useManualLexicon() {

        boolean useManualLexicon = "true".equals(PARAMETERS.get(PARAM_SETTING_MANUAL_LEXICON));

        return useManualLexicon;
    }

    public static boolean useMatoll() {

        boolean useMatoll = "true".equals(PARAMETERS.get(PARAM_SETTING_MATOLL));

        return useMatoll;
    }

    public static int getNumberOfEpochs() {
        int numberOfEpochs = Integer.parseInt(PARAMETERS.get(PARAM_SETTING_EPOCHS));

        return numberOfEpochs;
    }

    public static int getTrainMaxWordCount() {
        int numberOfEpochs = Integer.parseInt(PARAMETERS.get(PARAM_SETTING_TRAIN_MAX_WORD_COUNT));

        return numberOfEpochs;
    }

    public static int getTestMaxWordCount() {
        int numberOfEpochs = Integer.parseInt(PARAMETERS.get(PARAM_SETTING_TEST_MAX_WORD_COUNT));

        return numberOfEpochs;
    }

    public static int getNumberOfSamplingSteps() {
        int numberOfSamplingSteps = Integer.parseInt(PARAMETERS.get(PARAM_SETTING_SAMPLING_STEPS));

        return numberOfSamplingSteps;
    }

    public static int getNELTrainingBeamSize() {
        int numberKSamples = Integer.parseInt(PARAMETERS.get(PARAM_SETTING_BEAMSIZE_TRAINING_NEL));

        return numberKSamples;
    }

    public static int getQATrainingBeamSize() {
        int numberKSamples = Integer.parseInt(PARAMETERS.get(PARAM_SETTING_BEAMSIZE_TRAINING_QA));

        return numberKSamples;
    }

    public static int getNELTestBeamSize() {
        int numberKSamples = Integer.parseInt(PARAMETERS.get(PARAM_SETTING_BEAMSIZE_TEST_NEL));

        return numberKSamples;
    }

    public static int getQATestBeamSize() {
        int numberKSamples = Integer.parseInt(PARAMETERS.get(PARAM_SETTING_BEAMSIZE_TEST_QA));

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
