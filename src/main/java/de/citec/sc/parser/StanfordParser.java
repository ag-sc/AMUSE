/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.parser;

//import de.citec.sc.main.Main;
//import de.citec.sc.query.CandidateRetriever.Language;
//import edu.stanford.nlp.ie.AbstractSequenceClassifier;
//import edu.stanford.nlp.ie.crf.CRFClassifier;
//import edu.stanford.nlp.ling.CoreAnnotations;
//import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
//import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
//import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
//import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
//import edu.stanford.nlp.ling.CoreLabel;
//import edu.stanford.nlp.ling.IndexedWord;
//import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
//import edu.stanford.nlp.pipeline.Annotation;
//import edu.stanford.nlp.pipeline.StanfordCoreNLP;
//import edu.stanford.nlp.process.CoreLabelTokenFactory;
//import edu.stanford.nlp.process.PTBTokenizer;
//import edu.stanford.nlp.process.Tokenizer;
//import edu.stanford.nlp.process.TokenizerFactory;
//import edu.stanford.nlp.semgraph.SemanticGraph;
//import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
//import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
//import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.EnhancedDependenciesAnnotation;
//import edu.stanford.nlp.semgraph.SemanticGraphEdge;
//import edu.stanford.nlp.trees.GrammaticalStructure;
//import edu.stanford.nlp.trees.GrammaticalStructureFactory;
//import edu.stanford.nlp.trees.Tree;
//import edu.stanford.nlp.trees.TreeCoreAnnotations;
//import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
//import edu.stanford.nlp.trees.TreebankLanguagePack;
//import edu.stanford.nlp.trees.TypedDependency;
//import edu.stanford.nlp.util.CoreMap;
//import edu.stanford.nlp.util.PropertiesUtils;
//import edu.stanford.nlp.util.StringUtils;
//import java.io.IOException;
//import java.io.StringReader;
//import java.util.LinkedHashMap;
//import java.util.LinkedHashSet;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map.Entry;
//import java.util.Properties;
//import java.util.Set;

/**
 *
 * @author sherzod
 */
public class StanfordParser {

//    private static LexicalizedParser lp;
//    private static StanfordCoreNLP enPipeline;
//    private static StanfordCoreNLP dePipeline;
//    private static StanfordCoreNLP esPipeline;
//    private static boolean loaded = false;
//
//    private static void loadModels(Language lang) {
//
//        switch (lang) {
//            case EN:
//                System.out.println("Loading Stanford models for EN");
//
//                Properties enProps = StringUtils.argsToProperties(
//                        new String[]{"-props", "src/main/resources/dep-parse-properties/english.props"});
//
//                enPipeline = new StanfordCoreNLP(enProps);
//                loaded = true;
//                break;
//            case DE:
//                System.out.println("Loading Stanford models for DE");
//
//                Properties deProps = StringUtils.argsToProperties(
//                        new String[]{"-props", "src/main/resources/dep-parse-properties/german.props"});
//                dePipeline = new StanfordCoreNLP(deProps);
//                loaded = true;
//                break;
//            case ES:
//                System.out.println("Loading Stanford models for ES");
//
//                Properties esProps = StringUtils.argsToProperties(
//                        new String[]{"-props", "src/main/resources/dep-parse-properties/spanish.props"});
//                esPipeline = new StanfordCoreNLP(esProps);
//                loaded = true;
//                break;
//        }
//
////        String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
////        lp = LexicalizedParser.loadModel(parserModel);
//    }
//
//    private static List<TypedDependency> getDependencies(String sentence, Language lang) {
//
//        if (!loaded) {
//            loadModels(lang);
//        }
//
//        TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
//        Tokenizer<CoreLabel> tok = tokenizerFactory.getTokenizer(new StringReader(sentence));
//        List<CoreLabel> rawWords2 = tok.tokenize();
//        Tree parse = lp.apply(rawWords2);
////        parse.pennPrint();
////
////        System.out.println(parse.toString());
//
//        TreebankLanguagePack tlp = lp.treebankLanguagePack(); // PennTreebankLanguagePack for English
//        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
//        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
//        List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
//
//        return tdl;
//    }
//
//    public static DependencyParse parse(String text, Language lang) {
//
//        if (!loaded) {
//            loadModels(lang);
//        }
//
//        DependencyParse parse = new DependencyParse();
//
//        Annotation document = new Annotation(text);
//
//        if (lang.equals(Language.EN)) {
//            enPipeline.annotate(document);
//        } else if (lang.equals(Language.DE)) {
//            dePipeline.annotate(document);
//        } else if (lang.equals(Language.ES)) {
//            esPipeline.annotate(document);
//        }
//
//        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
//
//        for (CoreMap sentence : sentences) {
//
//            SemanticGraph dependencies = sentence.get(EnhancedDependenciesAnnotation.class);
//
//            IndexedWord root = dependencies.getFirstRoot();
//
//            parse.setHeadNode(root.index());
//
//            List<SemanticGraphEdge> edges = dependencies.edgeListSorted();
//
//            //System.out.println(edges);
//            for (SemanticGraphEdge t : edges) {
//
//                String dep = t.getDependent().originalText();
//                int depIndex = t.getDependent().index();
//                String depPOS = t.getDependent().tag();
//                int depStart = t.getDependent().beginPosition();
//                int depEnd = t.getDependent().endPosition();
//
//                String gov = t.getGovernor().originalText();
//                int govIndex = t.getGovernor().index();
//                String govPOS = t.getGovernor().tag();
//                int govStart = t.getGovernor().beginPosition();
//                int govEnd = t.getGovernor().endPosition();
//
//                parse.addNode(govIndex, gov, govPOS, govStart, govEnd);
//                parse.addNode(depIndex, dep, depPOS, depStart, depEnd);
//
//                parse.addEdge(depIndex, govIndex, t.getRelation().getShortName());
//
//            }
//        }
//
//        return parse;
//    }
//
//    public static List<String> lemmatizeDocument(String documentText, Language lang) {
//
//        if (enPipeline == null) {
//            loadModels(lang);
//        }
//
//        List<String> lemmas = new LinkedList<>();
//
//        // create an empty Annotation just with the given text
//        Annotation document = new Annotation(documentText);
//
//        // run all Annotators on this text
//        if (lang.equals(Language.EN)) {
//            enPipeline.annotate(document);
//        }
//        if (lang.equals(Language.DE)) {
//            dePipeline.annotate(document);
//        }
//        if (lang.equals(Language.ES)) {
//            esPipeline.annotate(document);
//        }
//
//        // Iterate over all of the sentences found
//        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
//        for (CoreMap sentence : sentences) {
//            // Iterate over all tokens in a sentence
//            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
//                // Retrieve and add the lemma for each word into the
//                // list of lemmas
//                lemmas.add(token.get(CoreAnnotations.LemmaAnnotation.class));
//            }
//        }
//
//        return lemmas;
//    }
//
//    /**
//     *
//     * @param t
//     * @return
//     */
//    public static String lemmatize(String t, Language lang) {
//
//        if (!loaded) {
//            loadModels(lang);
//        }
//
//        String lemma = "";
//
//        try {
//            // create an empty Annotation just with the given text
//            Annotation document = new Annotation(t);
//
//            // run all Annotators on this text
//            if (lang.equals(Language.EN)) {
//                enPipeline.annotate(document);
//            }
//            if (lang.equals(Language.DE)) {
//                dePipeline.annotate(document);
//            }
//            if (lang.equals(Language.ES)) {
//                esPipeline.annotate(document);
//            }
//
//            // Iterate over all of the sentences found
//            List<CoreMap> sentences = document.get(SentencesAnnotation.class);
//            for (CoreMap sentence : sentences) {
//                // Iterate over all tokens in a sentence
//                for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
//                    // Retrieve and add the lemma for each word into the
//                    // list of lemmas
//                    lemma += " " + token.get(CoreAnnotations.LemmaAnnotation.class);
//
//                }
//            }
//        } catch (Exception e) {
//            System.err.println("Stanford Lemmatizer error exception Word: " + t);
//        }
//
//        return lemma.trim();
//    }
//
//    private void test() throws IOException, ClassCastException, ClassNotFoundException {
//
//        String serializedClassifier = "edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz";
//
//        AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(serializedClassifier);
//
//        // read some text in the text variable
//        String text = "What is the timezone in San Pedro de Atacama?";
//
//        //String text = "He ate the apple";
//        List<List<CoreLabel>> out = classifier.classify(text);
//        for (List<CoreLabel> sentence : out) {
//            for (CoreLabel word : sentence) {
//                System.out.print(word.word() + '/' + word.get(CoreAnnotations.AnswerAnnotation.class) + ' ');
//            }
//            System.out.println();
//        }
//
//    }
//
//    private void testParseTree() {
//        try {
//            Properties props = new Properties();
//            props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
//            StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
//
//// read some text in the text variable
//            String text = "Give me a list of all bandleaders that play trumpet.";
//
//// create an empty Annotation just with the given text
//            Annotation document = new Annotation(text);
//
//// run all Annotators on this text
//            pipeline.annotate(document);
//
//            // these are all the sentences in this document
//// a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
//            List<CoreMap> sentences = document.get(SentencesAnnotation.class);
//
//            for (CoreMap sentence : sentences) {
//  // traversing the words in the current sentence
//                // a CoreLabel is a CoreMap with additional token-specific methods
//
//                // this is the parse tree of the current sentence
//                Tree tree = sentence.get(TreeAnnotation.class);
//
//                // this is the Stanford dependency graph of the current sentence
//                SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
//
//                Set<IndexedWord> vertices = dependencies.vertexSet();
//                List<SemanticGraphEdge> edges = dependencies.edgeListSorted();
//
//                for (SemanticGraphEdge e : edges) {
//
//                }
//
//                for (IndexedWord i : vertices) {
//                    System.out.println(i.toString());
//                }
//            }
//
//        } catch (Exception e) {
//
//        }
//    }
//
//    private LinkedHashMap<LinkedHashMap<Integer, String>, String> identifyNER(String text) {
//
//        LinkedHashMap<LinkedHashMap<Integer, String>, String> map = new LinkedHashMap<>();
//
//        String serializedClassifier = "edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz";
//
//        CRFClassifier<CoreLabel> classifier = CRFClassifier.getClassifierNoExceptions(serializedClassifier);
//        List<List<CoreLabel>> classify = classifier.classify(text);
//        for (List<CoreLabel> coreLabels : classify) {
//            for (CoreLabel coreLabel : coreLabels) {
//
//                String word = coreLabel.word();
//                int index = coreLabel.index();
//                String category = coreLabel.get(CoreAnnotations.AnswerAnnotation.class);
//                if (!"O".equals(category)) {
//
////                    for(Entry e1 : map.entrySet()){
////                        
////                        LinkedHashMap<Integer, String> entries = (LinkedHashMap<Integer, String>) e1;
////                        
////                        
////                    }
//                    System.out.println(word + ":" + category);
//                }
//
//            }
//
//        }
//        return map;
//    }

}
