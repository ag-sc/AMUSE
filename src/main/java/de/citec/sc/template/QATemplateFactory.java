/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.template;

import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.variable.State;
import exceptions.UnkownTemplateRequestedException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import templates.AbstractTemplate;
import templates.TemplateFactory;

/**
 *
 * @author sherzod
 */
public class QATemplateFactory implements TemplateFactory<AnnotatedDocument, State> {

    private static Set<String> validPOSTags;
    private static Set<String> validEdges;
    private static Map<Integer, String> semanticTypes;
    private static Map<Integer, String> specialSemanticTypes;

    public static void initialize(Set<String> v, Set<String> f, Map<Integer, String> s, Map<Integer, String> sp) {
        validPOSTags = v;
        semanticTypes = s;
        specialSemanticTypes = sp;
        validEdges = f;
    }

    @Override
    public AbstractTemplate<AnnotatedDocument, State, ?> newInstance(String templateName) throws UnkownTemplateRequestedException, Exception {

        switch (templateName) {
            case "NELLexicalTemplate":
                return new NELLexicalTemplate(validPOSTags, validEdges, semanticTypes);
            case "NELEdgeTemplate":
                return new NELEdgeTemplate(validPOSTags, validEdges, semanticTypes);
            case "NELNodeTemplate":
                return new NELNodeTemplate(validPOSTags, validEdges, semanticTypes);
            case "QAEdgeTemplate":
                return new QAEdgeTemplate(validPOSTags, validEdges, specialSemanticTypes);
            case "QAEdgeAdvTemplate":
                return new QAEdgeAdvTemplate(validPOSTags, validEdges,semanticTypes, specialSemanticTypes);

        }

        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
