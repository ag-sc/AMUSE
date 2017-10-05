/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.template;

import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.learning.QueryConstructor;
import de.citec.sc.main.Main;
import de.citec.sc.query.CandidateRetriever;
import de.citec.sc.query.DBpediaLabelRetriever;
import de.citec.sc.utils.DBpediaEndpoint;
import de.citec.sc.utils.ProjectConfiguration;
import de.citec.sc.utils.StringSimilarityUtils;
import de.citec.sc.variable.URIVariable;

import de.citec.sc.variable.State;
import factors.Factor;
import factors.FactorScope;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import learning.Vector;
import net.ricecode.similarity.SimilarityStrategy;
import net.ricecode.similarity.StringSimilarityMeasures;
import org.eclipse.jetty.util.ConcurrentHashSet;
import templates.AbstractTemplate;

/**
 *
 * @author sherzod
 */
public class QueryTypeTemplate extends AbstractTemplate<AnnotatedDocument, State, StateFactorScope<State>> {

    private Set<String> validPOSTags;
    private Set<String> validEdges;
    private Map<Integer, String> semanticTypes;
    private Map<Integer, String> specialSemanticTypes;

    public QueryTypeTemplate(Set<String> validPOSTags, Set<String> edges, Map<Integer, String> s, Map<Integer, String> sp) {

        semanticTypes = new ConcurrentHashMap<>();
        for (Integer key : s.keySet()) {
            semanticTypes.put(key, s.get(key));
        }

        specialSemanticTypes = new ConcurrentHashMap<>();
        for (Integer key : sp.keySet()) {
            specialSemanticTypes.put(key, sp.get(key));
        }

        this.validPOSTags = new ConcurrentHashSet<>();
        for (String v : validPOSTags) {
            this.validPOSTags.add(v);
        }

        this.validEdges = new ConcurrentHashSet<>();
        for (String v : edges) {
            this.validEdges.add(v);
        }
    }

    @Override
    public List<StateFactorScope<State>> generateFactorScopes(State state) {
        List<StateFactorScope<State>> factors = new ArrayList<>();

        for (Integer key : state.getDocument().getParse().getNodes().keySet()) {

            URIVariable a = state.getHiddenVariables().get(key);

            factors.add(new StateFactorScope<>(this, state));
        }

        return factors;
    }

    @Override
    public void computeFactor(Factor<StateFactorScope<State>> factor) {
        State state = factor.getFactorScope().getState();

        Vector featureVector = factor.getFeatureVector();

        Map<String, Double> queryTypeFeatures = getQueryTypeFeatures(state);

        for (String k : queryTypeFeatures.keySet()) {
            featureVector.addToValue(k, queryTypeFeatures.get(k));
        }
    }

    /**
     * returns features for query type
     */
    private Map<String, Double> getQueryTypeFeatures(State state) {
        Map<String, Double> features = new HashMap<>();
        
        String query = QueryConstructor.getSPARQLQuery(state);
        String constructedQueryType = "";
        if(query.contains("SELECT")){
            if(query.contains("COUNT")){
                constructedQueryType = "COUNT";
            }
            else{
                constructedQueryType = "SELECT";
            }
        }
        else{
            constructedQueryType = "ASK";
        }
        
        List<Integer> tokenIDs = new ArrayList<>(state.getDocument().getParse().getNodes().keySet());
        Collections.sort(tokenIDs);
        
        Integer firstTokenId = tokenIDs.get(0);
        String firstPOS = state.getDocument().getParse().getPOSTag(firstTokenId);
        String firstToken = state.getDocument().getParse().getToken(firstTokenId);
        
        String queryType = state.getQueryTypeVariable().toString();
        
        features.put("QUERY TYPE TOKEN:"+firstToken+" POS:"+firstPOS+" "+queryType +" CONSTRUCTED TYPE" +constructedQueryType, 1.0);
        
        return features;
    }

}
