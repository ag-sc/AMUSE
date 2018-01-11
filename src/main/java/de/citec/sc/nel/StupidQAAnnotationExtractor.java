package de.citec.sc.nel;

import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.parser.DependencyParse;
import de.citec.sc.query.CandidateRetriever;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author sherzod
 */
public class StupidQAAnnotationExtractor {

    /**
     * performs two pass over the text by extracting ngrams and querying over
     * the database for Product, IngredientSynonym, Recipe, RecipeCategory
     *
     * first Pass over the text -> extract ngrams, query those ngrams in the
     * database, choose ones that have similarity scire >= firstPassThreshold
     * second Pass over the text -> extract ngrams that are not covered from the
     * result of the first pass, query those ngrams in the database, choose ones
     * that similarity score >= secondPassThreshold
     *
     * @param document
     * @param text
     * @param maxNGramSize
     * @param firstPassThreshold
     * @param retriever
     * @param secondPassThreshold
     * @param String text
     * @param int maxNGramsize
     * @param double firstPassThreshold
     * @param double secondPassThreshold
     * @return entityAnnotations
     */
    public static List<EntityAnnotation> getAnnotations(AnnotatedDocument document, CandidateRetriever retriever, Set<String> linkingValidPOSTags) {

        List<EntityAnnotation> annotations = new ArrayList<>();

        Set<String> coveredTypes = new HashSet<>();

        //extract ngrams up to maxNGramSize argument
//        List<EntityAnnotation> firstPassNGrams = NGramExtractor.getNGrams(document.getParse(), maxNGramSize, annotations);
        //retrieve entities from database based on similarity threshold -> firstPassThreshold
        List<EntityAnnotation> firstPassAnnotations = StupidQAEntityExtractor.getAnnotations(document, retriever, linkingValidPOSTags);

        if (!firstPassAnnotations.isEmpty()) {
            //add retrieved
            annotations.addAll(firstPassAnnotations);
        }

//        //perform second pass over the text and extract ngrams if not covered previously
//        //(and only for those that didn't match yet)
//        //extract ngrams while not adding the spans that are covered in the first pass
//        List<EntityAnnotation> secondPassNGrams = NGramExtractor.getNGrams(text, maxNGramSize, annotations);
//        //retreive entities from database based on ngrams from the second pass
//        List<EntityAnnotation> secondPassAnnotations = EntityExtractor.getAnnotations(secondPassNGrams, text, secondPassThreshold, coveredTypes, retriever);
//
//        if (!secondPassAnnotations.isEmpty()) {
//            //add retrieved
//            annotations.addAll(secondPassAnnotations);
//        }
        annotations = filterSameInterpretation(annotations);
        annotations = filterSubSpans(annotations);
        
        //sort annotations

        Collections.sort(annotations, new Comparator<Annotation>() {
            @Override
            public int compare(final Annotation a, Annotation b) {
                if (a.getProvenance().getConfidence() > b.getProvenance().getConfidence()) {
                    return -1;
                }
                if (a.getProvenance().getConfidence() < b.getProvenance().getConfidence()) {
                    return 1;
                }
                return 0;
            }
        });

        return annotations;
    }

    private static List<EntityAnnotation> filterSubSpans(List<EntityAnnotation> annotations) {

        Set<EntityAnnotation> subSpannedAnnotations = new HashSet<>();
        while (true) {
            boolean contains = false;

            for (EntityAnnotation e1 : annotations) {
                for (EntityAnnotation e2 : annotations) {
                    if (e1.equals(e2)) {
                        continue;
                    }

                    if (e1.getSpan().getChar_end() <= e2.getSpan().getChar_end() && e1.getSpan().getChar_start() > e2.getSpan().getChar_start()) {
                        subSpannedAnnotations.add(e1);
                        contains = true;
                        break;
                    } else if (e1.getSpan().getChar_end() < e2.getSpan().getChar_end() && e1.getSpan().getChar_start() >= e2.getSpan().getChar_start()) {
                        subSpannedAnnotations.add(e1);
                        contains = true;
                        break;
                    } //walt disney - disney kids
                    else if (e1.getSpan().getChar_end() < e2.getSpan().getChar_end() && e1.getSpan().getChar_start() < e2.getSpan().getChar_start() && e1.getSpan().getChar_end() > e2.getSpan().getChar_start()) {

                        if (e1.getProvenance().getConfidence() > e2.getProvenance().getConfidence()) {
                            subSpannedAnnotations.add(e2);
                        } else {
                            subSpannedAnnotations.add(e1);
                        }

                        contains = true;
                        break;
                    }
                }

                if (contains) {
                    break;
                }
            }

            if (!contains) {
                break;
            }

            for (EntityAnnotation e1 : subSpannedAnnotations) {
                annotations.remove(e1);
            }
        }

        return annotations;
    }

    private static List<EntityAnnotation> filterSameInterpretation(List<EntityAnnotation> annotations) {
        Set<EntityAnnotation> filteredAnnotations = new HashSet<>();

        while (true) {

            int counter = 0;
            for (EntityAnnotation a1 : annotations) {

                boolean hasDuplicates = false;

                if (filteredAnnotations.contains(a1)) {
                    continue;
                }

                for (EntityAnnotation a2 : annotations) {

                    if (!a1.equals(a2)) {

                        if (a1.getType().equals(a2.getType())
                                && a1.getValues().equals(a2.getValues())) {

                            //if a2 has better score than other
                            if (a2.getProvenance().getConfidence() > a1.getProvenance().getConfidence()) {
                                filteredAnnotations.add(a2);
                                hasDuplicates = true;
                                break;
                            }

                            //if scores are equal and a2 has shorter text, then prefer shorter one
                            if (a2.getProvenance().getConfidence() == a1.getProvenance().getConfidence()) {

                                if (a2.length() < a1.length()) {
                                    filteredAnnotations.add(a2);
                                    hasDuplicates = true;
                                    break;
                                }
                            }
                        }
                    }
                }

                //if no duplicates, then add a1 to filteredAnnotations
                if (!hasDuplicates) {
                    filteredAnnotations.add(a1);
                }

                //if there are duplicates then count
                if (hasDuplicates) {
                    counter++;
                }
            }

            annotations.clear();

            for (EntityAnnotation a : filteredAnnotations) {
                annotations.add(a);
//                System.out.println(a.toString());
            }

//            System.out.println("==========================================");
            filteredAnnotations.clear();

            if (counter == 0) {
                break;
            }
        }

        return annotations;
    }

}
