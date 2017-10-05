package de.citec.sc.sampling;

import de.citec.sc.learning.QueryConstructor;
import de.citec.sc.utils.DBpediaEndpoint;
import de.citec.sc.utils.FileFactory;
import de.citec.sc.variable.State;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sampling.samplingstrategies.BeamSearchSamplingStrategy;
import sampling.samplingstrategies.BeamSearchSamplingStrategy.StatePair;

import variables.AbstractState;

public class SamplingStrategies {

    private static Logger log = LogManager.getFormatterLogger();

    public static <StateT extends AbstractState<?>> BeamSearchSamplingStrategy<StateT> greedyBeamSearchSamplingStrategyByModel(
            int k, Function<StateT, Double> getScore) {
        return new BeamSearchSamplingStrategy<StateT>() {

            @Override
            public List<StatePair<StateT>> sampleCandidate(List<StatePair<StateT>> candidates) {
                candidates.sort((s1, s2) -> -Double.compare(getScore.apply(s1.getCandidateState()),
                        getScore.apply(s2.getCandidateState())));

                List<StatePair<StateT>> validPairs = new ArrayList<>();

                String s = "";
                int c = 0;
                for (StatePair<StateT> pair : candidates) {
                    
                    s += pair.getCandidateState() + "\n\n" +"======================================================================\n";
                    c++;

                    if (candidates.size() > 1000 && c == 200) {
                        break;
                    }
                }

                FileFactory.writeListToFile("states.txt", s, false);
                return candidates.subList(0, Math.min(k, candidates.size()));
            }

            @Override
            public boolean usesModel() {
                return true;
            }

            @Override
            public boolean usesObjective() {
                return false;
            }
        };
    }

    public static <StateT extends AbstractState<?>> BeamSearchSamplingStrategy<StateT> greedyBeamSearchSamplingStrategyByObjective(
            int k, Function<StateT, Double> getScore) {
        return new BeamSearchSamplingStrategy<StateT>() {

            @Override
            public List<BeamSearchSamplingStrategy.StatePair<StateT>> sampleCandidate(List<BeamSearchSamplingStrategy.StatePair<StateT>> candidates) {
                candidates.sort((s1, s2) -> -Double.compare(getScore.apply(s1.getCandidateState()),
                        getScore.apply(s2.getCandidateState())));

                String s = "";
                int c = 0;
                for (StatePair<StateT> pair : candidates) {
                    s += pair.getCandidateState() + "\n\n" + "======================================================================\n";
                    c++;

                    if (candidates.size() > 1000 && c == 200) {
                        break;
                    }
                }

                FileFactory.writeListToFile("states.txt", s, false);
                return candidates.subList(0, Math.min(k, candidates.size()));
            }

            @Override
            public boolean usesModel() {
                return false;
            }

            @Override
            public boolean usesObjective() {
                return true;
            }
        };
    }

    public static <StateT extends AbstractState<?>> BeamSearchSamplingStrategy<StateT> beamSearchSamplingStrategy(int k,
            Function<StateT, Double> getScore) {
        return new BeamSearchSamplingStrategy<StateT>() {

            @Override
            public List<StatePair<StateT>> sampleCandidate(List<StatePair<StateT>> candidates) {
                List<StatePair<StateT>> sampledState = new ArrayList<>();

                return sampledState;
            }

            @Override
            public boolean usesModel() {
                return true;
            }

            @Override
            public boolean usesObjective() {
                return false;
            }
        };
    }
}
