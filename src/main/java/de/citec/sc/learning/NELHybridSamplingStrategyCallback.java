package de.citec.sc.learning;

import de.citec.sc.learning.NELTrainer.EpochCallback;
import de.citec.sc.sampling.MyBeamSearchSampler;
import de.citec.sc.sampling.SamplingStrategies;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sampling.BeamSearchSampler;
import sampling.samplingstrategies.BeamSearchSamplingStrategies;
import variables.AbstractState;

public class NELHybridSamplingStrategyCallback<StateT extends AbstractState<?>> implements EpochCallback {

    private MyBeamSearchSampler<?, StateT, ?> sampler;
    private int beamSize = 10;

    private static Logger log = LogManager.getFormatterLogger();

    public NELHybridSamplingStrategyCallback(MyBeamSearchSampler<?, StateT, ?> sampler, int beamSize) {
        super();
        this.sampler = sampler;
        this.beamSize = beamSize;
    }

    @Override
    public void onStartEpoch(NELTrainer caller, int epoch, int numberOfEpochs, int numberOfInstances) {
        if ((epoch + 1) % 2 == 0) {
            sampler.setTrainSamplingStrategy(SamplingStrategies.greedyBeamSearchSamplingStrategyByModel(beamSize,
                    s -> s.getModelScore()));
            log.info("Switched to model score");
        } else {
            sampler.setTrainSamplingStrategy(
                    SamplingStrategies.greedyBeamSearchSamplingStrategyByObjective(beamSize, s -> s.getObjectiveScore()));
            log.info("Switched to objective score");
        }
    }

}
