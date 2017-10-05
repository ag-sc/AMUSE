/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.learning;

import de.citec.sc.learning.FeatureMapData.FeatureDataPoint;
import de.citec.sc.variable.State;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import learning.scorer.Scorer;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;
import utility.Utils;
import variables.AbstractState;

/**
 *
 * @author sherzod
 */
public class LibSVMRegressionScorer implements Scorer{

    private static Logger log = LogManager.getFormatterLogger(LibSVMRegressionScorer.class.getName());

    private FeatureMapData trainingData;

    public LibSVMRegressionScorer() {
    }

    private svm_model model = null;

    public void svmTrain(FeatureMapData trainingData) {
        this.trainingData = trainingData;

        svm_problem prob = new svm_problem();
        final int dataCount = trainingData.getDataPoints().size();
        final int totalFeatureCount = trainingData.numberOfTotalFeatures();

        log.info("Training with " + dataCount);
        log.info("Number of features = " + totalFeatureCount);
        prob.y = new double[dataCount];
        prob.l = dataCount;
        prob.x = new svm_node[dataCount][];
        int dataPointIndex = 0;
        for (FeatureDataPoint tdp : trainingData.getDataPoints()) {
            prob.x[dataPointIndex] = toLibSVMNodeArray(tdp.features);
            prob.y[dataPointIndex] = tdp.score;
            dataPointIndex++;
        }

        svm_parameter param = new svm_parameter();
        param.eps = 0.01;
        param.p = 0.1;
        param.C = 0.0001;
        param.svm_type = svm_parameter.EPSILON_SVR;
        param.kernel_type = svm_parameter.LINEAR;
        param.cache_size = 12000;
        param.gamma = 0.0001;

        log.info("Start training SVR...");
        this.model = svm.svm_train(prob, param);
        log.info("done");
    }

    public double evaluate(FeatureDataPoint dp) {
        Map<Integer, Double> features = dp.features;// trainingData.toSparseFeatureIndexMap(dp);

        svm_node[] nodes = toLibSVMNodeArray(features);

        double v1 = svm.svm_predict(model, nodes);

        // System.out.println("(Actual:" + dp.score + " Prediction:" + v1 +
        // ")");
        return v1;
    }

    private svm_node[] toLibSVMNodeArray(Map<Integer, Double> features) {
        svm_node[] nodes = new svm_node[features.size()];

        int nonZeroFeatureIndex = 0;
        for (Entry<Integer, Double> feature : features.entrySet()) {
            svm_node node = new svm_node();
            node.index = feature.getKey();
            node.value = feature.getValue();

            nodes[nonZeroFeatureIndex] = node;
            nonZeroFeatureIndex++;
        }
        return nodes;
    }

    /**
     * Computes a score for each passed state given the individual factors.
     * Scoring is done is done in parallel if flag is set and scorer
     * implementation does not override this method.
     *
     * @param states
     * @param multiThreaded
     */
    public void score(List<? extends AbstractState<?>> states, boolean multiThreaded) {
        Stream<? extends AbstractState<?>> stream = Utils.getStream(states, multiThreaded);
        stream.forEach(s -> {
            scoreSingleState(s);
        });
    }

    /**
     * Computes the score of this state according to the trained model. The
     * computed score is returned but also updated in the state objects
     * <i>score</i> field.
     *
     * @param state
     * @return
     */
    protected void scoreSingleState(AbstractState<?> state) {

        if (model == null) {
            return;
        }

        if (state instanceof State) {
            state.setModelScore(evaluate(((State) state).toTrainingPoint(trainingData, false)));
        } else {
            throw new IllegalArgumentException("Unknown State: " + state.getClass().getSimpleName());
        }
    }

}
