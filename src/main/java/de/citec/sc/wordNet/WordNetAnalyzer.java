package de.citec.sc.wordNet;

import edu.smu.tspell.wordnet.NounSynset;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase;
import edu.smu.tspell.wordnet.WordSense;
import edu.stanford.nlp.util.ArraySet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class WordNetAnalyzer {

    /**
     * creates wordnet analyzer
     *
     * @param wordnetpath path to wordnet, pointing to WordNet-X.X/dict
     * subdirectory
     */
    public WordNetAnalyzer(String wordnetpath) {
        try {
            System.setProperty("wordnet.database.dir", wordnetpath);
            wordNetDatabase = WordNetDatabase.getFileInstance();
        } catch (Exception e) {
            System.err.println("Problem with path for WordNetAnalyzer initiation!!!\nWordNet files should be as follows => docs/WordNet-3.0/dict");
        }

    }

    /**
     * retrieves synonyms from wordnet
     *
     * @param word word for which synonyms are retrieved
     * @param delimiter character to delimit synonyms in return value
     * @return string containing synonyms separated by delimiter
     */
    public Set<String> getSynonyms(String word) {
        Set<String> synStringSet = new HashSet<String>();
        Synset[] synsets = wordNetDatabase.getSynsets(word);
        for (Synset synset : synsets) {
            for (String s1 : synset.getWordForms()) {
                synStringSet.add(s1);
            }
        }
        return synStringSet;
    }

    /**
     * retrieves hypernyms from wordnet
     *
     * @param word word for which hypernyms are retrieved (nouns only)
     * @param delimiter character to delimit hypernyms in return value
     * @return string containing hypernyms, separated by delimiter
     */
    public String getHypernyms(String word, String delimiter) {
        HashSet<String> hypStringSet = new HashSet<String>();
        Synset[] synsets = wordNetDatabase.getSynsets(word, SynsetType.NOUN);
        for (Synset synset : synsets) {
            for (NounSynset hyp : ((NounSynset) synset).getHypernyms()) {
                Collections.addAll(hypStringSet, hyp.getWordForms());
            }
        }
        return String.join(delimiter, hypStringSet);
    }

    /**
     * retrieves synonyms including derivational word forms
     *
     * @param word word for which synonyms and relational word forms are
     * retrieved
     * @param delimiter character to delimit synonyms and derivational word
     * forms
     * @return list containing synonyms and derivational words, separated by
     * delimiter
     */
    public Set<String> getDerivationalWords(String word) {
        Set<String> derivStringSet = new LinkedHashSet<>();
        derivStringSet.add(word);

        Synset[] synsets = wordNetDatabase.getSynsets(word);
        for (Synset synset : synsets) {
            for (String s : synset.getWordForms()) {
                for (WordSense w : synset.getDerivationallyRelatedForms(s)) {
                    if (!derivStringSet.contains(w.getWordForm())) {
                        derivStringSet.add(w.getWordForm());
                    }

                }
            }
        }
        return derivStringSet;
        //return String.join(delimiter, derivStringSet);
    }

    private WordNetDatabase wordNetDatabase;
}
