/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.template;

import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.main.Main;
import de.citec.sc.query.CandidateRetriever;
import de.citec.sc.utils.ProjectConfiguration;
import de.citec.sc.variable.URIVariable;

import de.citec.sc.variable.State;
import factors.Factor;
import factors.FactorScope;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import learning.Vector;
import net.ricecode.similarity.SimilarityStrategy;
import net.ricecode.similarity.StringSimilarityMeasures;
import templates.AbstractTemplate;

/**
 *
 * @author sherzod
 */
public class NodeSimilarityTemplate extends AbstractTemplate<AnnotatedDocument, State, SingleNodeFactorScope<URIVariable>> {

    private SimilarityStrategy stringSim;

    public NodeSimilarityTemplate() {
        this.stringSim = new StringSimilarityMeasures();
    }

    private Set<String> getRestrictionClassFeatures(String node, String pos, URIVariable u, int binNumber) {
        Set<String> featureNames = new HashSet<>();

        String featurePrefix = "SIMILARITY FEATURE : RESTRICTION_CLASS: ";

        String property = u.getCandidate().getUri().substring(0, u.getCandidate().getUri().indexOf("###"));
        String resource = u.getCandidate().getUri().substring(u.getCandidate().getUri().indexOf("###") + 2);

        final double weightedEditSimilarity = getSimilarityScore(node, resource);

        if (pos.startsWith("NN") && weightedEditSimilarity == 1.0) {
            featureNames.add(featurePrefix + " DIRECT MATCH FOUND");
        }

        String conjunctionFeatureName = featurePrefix + " LEVENSTEIN : ";
        binNumber = 10;

        Set<String> binFeatures = getBinFeatures(conjunctionFeatureName, weightedEditSimilarity, binNumber);

        for (String f : binFeatures) {
            featureNames.add(f);
        }

//        featureNames.add(featurePrefix + " " + node + " " + uri);
        return featureNames;
    }

    private Set<String> getClassFeatures(String node, String pos, URIVariable u, int binNumber) {
        Set<String> featureNames = new HashSet<>();

        String featurePrefix = "SIMILARITY FEATURE : CLASS: ";

        //compute levenstein edit distance similarity and normalize if they are Resources
        final double weightedEditSimilarity = getSimilarityScore(node, u.getCandidate().getUri());

        if (pos.startsWith("NN") && weightedEditSimilarity == 1.0) {
            featureNames.add(featurePrefix + " DIRECT MATCH FOUND");
        }

        String conjunctionFeatureName = featurePrefix + " LEVENSTEIN : ";
        binNumber = 10;

        Set<String> binFeatures = getBinFeatures(conjunctionFeatureName, weightedEditSimilarity, binNumber);

        for (String f : binFeatures) {
            featureNames.add(f);
        }

        return featureNames;
    }

    private Set<String> getResourceFeatures(String node, String pos, URIVariable u, int binNumber) {
        Set<String> featureNames = new HashSet<>();

        String featurePrefix = "SIMILARITY FEATURE : RESOURCE: ";

        //remove _() from resource uris
        String uri = u.getCandidate().getUri();
        if (uri.contains("_(")) {
            uri = uri.substring(0, uri.indexOf("_("));
        }

        //compute levenstein edit distance similarity and normalize if they are Resources
        final double weightedEditSimilarity = getSimilarityScore(node, uri);

        if (pos.startsWith("NN") && weightedEditSimilarity == 1.0) {
            featureNames.add(featurePrefix + " DIRECT MATCH FOUND");
        }

        String conjunctionFeatureName = featurePrefix + " DBpedia & LEVENSTEIN : ";
        binNumber = 10;

        Set<String> binFeatures = getConjunctionBinFeatures(conjunctionFeatureName, weightedEditSimilarity, u.getCandidate().getDbpediaScore(), binNumber);

        for (String f : binFeatures) {
            featureNames.add(f);
        }

        //add abbreviation feature
        if (weightedEditSimilarity < 0.5) {
            if (isAbbreviation(node, uri)) {
                featureNames.add(featurePrefix + "ABBREVIATION (URI, TOKEN)");
            }
        }

        return featureNames;
    }

    private Set<String> getPropertyFeatures(String node, String inputQuestion, String pos, URIVariable u, int binNumber) {
        Set<String> featureNames = new HashSet<>();

        String featurePrefix = "SIMILARITY FEATURE : ";

        if (u.getCandidate().getUri().startsWith("http://dbpedia.org/ontology/")) {
            featurePrefix += "ONTOLOGY PROPERTY : ";
        } else {
            featurePrefix += "RDF PROPERTY : ";
        }

        final double weightedEditSimilarity = getSimilarityScore(node, u.getCandidate().getUri());

        if (pos.startsWith("NN") && weightedEditSimilarity == 1.0) {
            featureNames.add(featurePrefix + " DIRECT MATCH FOUND");
        }

        String conjunctionFeatureName = featurePrefix + " MATOLL & LEVENSTEIN : ";
        binNumber = 10;

        Set<String> binFeatures = getConjunctionBinFeatures(conjunctionFeatureName, weightedEditSimilarity, u.getCandidate().getMatollScore(), binNumber);

        for (String f : binFeatures) {
            featureNames.add(f);
        }

        //add this feature if the uri given has a preposition from MATOLL
        if (!u.getCandidate().getMatollPreposition().equals("")) {
            boolean textContainsMatollPreposition = false;

            for (String token : inputQuestion.split(" ")) {
                if (token.equalsIgnoreCase(u.getCandidate().getMatollPreposition())) {
                    textContainsMatollPreposition = true;
                    break;
                }
            }

            if (textContainsMatollPreposition) {
                String prepositionFeature = featurePrefix + " TEXT CONTAINS MATOLL PREPOSITION";
                featureNames.add(prepositionFeature);
            }
        }

        //get remaining parts
        List<String> remains = getNotMatchingParts(node, u.getCandidate().getUri());

        if (remains.isEmpty()) {

        } else {
            String remainingNode = remains.get(0);
            String remainingURI = remains.get(1);

            String featureName = featurePrefix + " MATCHED UNTIL : " + remainingNode + "/" + remainingURI;
            featureNames.add(featureName);
        }

//        double normalizedValue = u.getPriorScore();
//
//        if (normalizedValue >= 0.1) {
//            featureNames.add(featurePrefix + "PriorScore >= 0.1");
//        }
//        if (normalizedValue >= 0.07) {
//            featureNames.add(featurePrefix + "PriorScore >= 0.07");
//        }
//        if (normalizedValue >= 0.2) {
//            featureNames.add(featurePrefix + "PriorScore >= 0.2");
//        }
        return featureNames;
    }

    private Set<String> getConjunctionBinFeatures(String featureName, double value1, double value2, int numberOfBins) {
        Set<String> features = new HashSet<>();

        //value to increase whilst adding bins
        double constant = (double) 1 / numberOfBins;

        DecimalFormat formatter = new DecimalFormat("#0.00");

        double maxValue = Math.max(value1, value2);
        double minValue = Math.min(value1, value2);

        for (double i = 0.1; i <= 1.0; i = i + constant) {

            boolean isAdded = false;

            if (value1 >= i && value2 >= i) {
                features.add(featureName + " BOTH VALUES >= " + formatter.format(i));
                isAdded = true;
            }

            if (!isAdded) {
                if (maxValue >= i) {
                    features.add(featureName + " MAX VALUE >= " + formatter.format(i));
                    isAdded = true;
                }
            }

//            if (minValue >= i) {
//                features.add(featureName + " MIN VALUE >= " + formatter.format(i));
//            }
        }

        return features;
    }

    private Set<String> getBinFeatures(String featureName, int value, int interval, int maxValue) {
        Set<String> features = new HashSet<>();

        //value to increase whilst adding bins
        for (int i = 0; i <= maxValue; i = i + interval) {

            if (value >= i) {
                features.add(featureName + " >= " + i);
            }
        }

        return features;
    }

    private Set<String> getBinFeatures(String featureName, double value, int numberOfBins) {
        Set<String> features = new HashSet<>();

        //value to increase whilst adding bins
        double constant = (double) 1 / numberOfBins;

        DecimalFormat formatter = new DecimalFormat("#0.00");

        for (double i = 0; i <= 1.0; i = i + constant) {

            if (value >= i) {
                features.add(featureName + " >= " + formatter.format(i));
            }
        }

        return features;
    }

    /**
     * levenstein sim
     */
    private double getSimilarityScore(String node, String uri) {

        uri = uri.replace("http://dbpedia.org/resource/", "");
        uri = uri.replace("http://dbpedia.org/property/", "");
        uri = uri.replace("http://dbpedia.org/ontology/", "");
        uri = uri.replace("http://www.w3.org/1999/02/22-rdf-syntax-ns#type###", "");

        uri = uri.replaceAll("@en", "");
        uri = uri.replaceAll("\"", "");
        uri = uri.replaceAll("_", " ");

        //replace capital letters with space
        //to tokenize compount classes e.g. ProgrammingLanguage => Programming Language
        String temp = "";
        for (int i = 0; i < uri.length(); i++) {
            String c = uri.charAt(i) + "";
            if (c.equals(c.toUpperCase())) {
                temp += " ";
            }
            temp += c;
        }

        uri = temp.trim().toLowerCase();

        //compute levenstein edit distance similarity and normalize
        final double weightedEditSimilarity = StringSimilarityMeasures.score(uri, node);

        return weightedEditSimilarity;
    }

    /**
     * returns remaining parts of string that don't match
     *
     */
    private List<String> getNotMatchingParts(String node, String uriWithNameSpace) {

        String uri = uriWithNameSpace;

        uri = uri.replace("http://dbpedia.org/resource/", "");
        uri = uri.replace("http://dbpedia.org/property/", "");
        uri = uri.replace("http://dbpedia.org/ontology/", "");
        uri = uri.replace("http://www.w3.org/1999/02/22-rdf-syntax-ns#type###", "");

        uri = uri.replaceAll("@en", "");
        uri = uri.replaceAll("\"", "");
        uri = uri.replaceAll("_", " ");

        //replace capital letters with space
        //to tokenize compount classes e.g. ProgrammingLanguage => Programming Language
        String temp = "";
        for (int i = 0; i < uri.length(); i++) {
            String c = uri.charAt(i) + "";
            if (c.equals(c.toUpperCase())) {
                temp += " ";
            }
            temp += c;
        }

        uri = temp.trim().toLowerCase();

        //compute levenstein edit distance similarity and normalize
        int lastIndex = -1;

        for (int i = 0; i < uri.length(); i++) {

            if (i >= node.length()) {
                lastIndex = i - 1;
                break;
            }

            if (node.charAt(i) != uri.charAt(i)) {
                lastIndex = i;
                break;
            }
        }

        List<String> remains = new ArrayList<>();

        //don't add when the index starts from 0, it means nothing has matched
        if (lastIndex > 0) {
            String u = uri.substring(lastIndex);
            String n = node.substring(lastIndex);

            remains.add(n);
            remains.add(u);
        }

        //if lastIndex == -1 means the similarity is %100
        return remains;
    }

    private boolean isAbbreviation(String node, String uri) {
        String abbr = node.length() > uri.length() ? uri : node;
        String word = node.length() > uri.length() ? node : uri;

        abbr = abbr.replace(".", "");

        String[] tokens = word.split(" ");

        if (tokens.length != abbr.length()) {
            return false;
        }

        int count = 0;
        for (int i = 0; i < abbr.length(); i++) {
            String c = abbr.charAt(i) + "";
            if (tokens[i].startsWith(c)) {
                count++;
            }
        }

        if (count == abbr.length()) {
            return true;
        }

        return false;
    }

    @Override
    public List<SingleNodeFactorScope<URIVariable>> generateFactorScopes(State state) {
        List<SingleNodeFactorScope<URIVariable>> factors = new ArrayList<>();

        for (Integer key : state.getDocument().getParse().getNodes().keySet()) {

            URIVariable a = state.getHiddenVariables().get(key);

            factors.add(new SingleNodeFactorScope<>(this, a, state.getDocument()));
        }

        return factors;
    }

    @Override
    public void computeFactor(Factor<SingleNodeFactorScope<URIVariable>> factor) {
        URIVariable u = factor.getFactorScope().getVar();
        AnnotatedDocument doc = factor.getFactorScope().getDoc();

        Vector featureVector = factor.getFeatureVector();

        String node = doc.getParse().getToken(u.getTokenId());
        String uri = u.getCandidate().getUri();
        String inputQuestion = doc.getQaldInstance().getQuestionText().get(CandidateRetriever.Language.valueOf(ProjectConfiguration.getLanguage()));

        String pos = doc.getParse().getPOSTag(u.getTokenId());

        int binNumber = 10;

        if (!uri.equals("EMPTY_STRING")) {

            //restriction class
            if (uri.contains("###http://dbpedia.org/resource/")) {

                Set<String> featureNames = getRestrictionClassFeatures(node, pos, u, binNumber);

                for (String f : featureNames) {
                    featureVector.set(f, 1.0);
                }

            } else {
                if (uri.startsWith("http://dbpedia.org/ontology/") || uri.startsWith("http://dbpedia.org/property/")) {
                    Set<String> featureNames = getPropertyFeatures(node, inputQuestion, pos, u, binNumber);

                    for (String f : featureNames) {
                        featureVector.set(f, 1.0);
                    }
                }
                if (uri.startsWith("http://dbpedia.org/resource/")) {
                    Set<String> featureNames = getResourceFeatures(node, pos, u, binNumber);

                    for (String f : featureNames) {
                        featureVector.set(f, 1.0);
                    }
                }
                if (uri.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
                    Set<String> featureNames = getClassFeatures(node, pos, u, binNumber);

                    for (String f : featureNames) {
                        featureVector.set(f, 1.0);
                    }
                }
            }

        }
    }

}
