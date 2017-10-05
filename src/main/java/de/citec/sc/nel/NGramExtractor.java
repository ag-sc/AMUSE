package de.citec.sc.nel;

import de.citec.sc.query.CandidateRetriever;
import de.citec.sc.utils.Stopwords;
import de.citec.sc.utils.StringPreprocessor;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author sherzod
 */
public class NGramExtractor {

    /**
     * extracts ngrams from String text ngrams that are covered in
     * coveredAnnotations are skipped
     *
     * @return List<EntityAnnotation> annotations -> spans that are not part of
     * the given coveredAnnotations
     */
    public static List<EntityAnnotation> getNGrams(String text, int maxNgramSize, List<EntityAnnotation> coveredAnnotations) {

        List<EntityAnnotation> annotations = new ArrayList<>();

        String[] unigrams = text.split("\\s");

        for (int i = 0; i < unigrams.length; i++) {

            int beginPosition = 0;

            for (int l = 0; l < i; l++) {
                //get character length of previous unigrams
                beginPosition += unigrams[l].length();
                //plus 1 for space
                beginPosition += 1;
            }

            for (int n = maxNgramSize; n > 0; n--) {

                if (i + n <= unigrams.length) {

                    String ngram = "";

                    for (int k = i; k < i + n; k++) {
                        ngram += unigrams[k] + " ";
                    }

                    ngram = ngram.replaceAll("n't", " not")
                        .replaceAll("'re", " are")
                        .replaceAll("'s", " is")
                        .replaceAll("'m", " am");
                    
                    ngram = Normalizer.normalize(ngram, Normalizer.Form.NFD);

                    ngram = ngram.replaceAll("[!?,.;:'\"]", "");
//                    ngram = StringPreprocessor.preprocess(ngram, CandidateRetriever.Language.EN);
//
                    if (Stopwords.isStopWord(ngram)) {
                        continue;
                    }

                    int endPosition = beginPosition + ngram.length() - 1;

                    //check if this span has been covered before
                    //add new annotation only if it is new span
                    if (isNewSpan(coveredAnnotations, beginPosition, endPosition)) {

                        EntityAnnotation a = new EntityAnnotation();
                        a.setType("NGRAM");
                        a.setValues(new HashSet<>(Arrays.asList("NGRAM")));
                        a.setSpan(new Span(beginPosition, endPosition));
                        a.setProvenance(new Provenance("NO-SOURCE", 1f));
                        annotations.add(a);
                    }

                }
            }
        }

        annotations.addAll(coveredAnnotations);

        return annotations;
    }

    //checks whether the span is already in coveredAnnotations based on argument beginPos and endPos
    private static boolean isNewSpan(List<EntityAnnotation> coveredAnnotations, int beginPos, int endPos) {

        for (EntityAnnotation a : coveredAnnotations) {
            Span span = a.getSpan();
            if (span.getChar_start() >= beginPos && span.getChar_end() <= endPos) {
                return false;
            }

        }

        return true;
    }
}
