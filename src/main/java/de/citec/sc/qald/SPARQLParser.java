/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.qald;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;

/**
 *
 * @author sherzod
 */
public class SPARQLParser {

    public static List<String> extractURIsFromQuery(String queryString) {

        List<String> uris = new ArrayList<>();

        List<Triple> triples = extractTriplesFromQuery(queryString);
        for (Triple t : triples) {

            if (!t.IsReturnVariable()) {

                if (!t.getPredicate().IsVariable()) {
                    uris.add(t.getPredicate().getPredicateName());
                }

                if (t.getSubject() instanceof Constant) {
                    Constant c = (Constant) t.getSubject();
                    uris.add(c.getUri());
                }

                if (t.getObject() instanceof Constant) {
                    Constant c = (Constant) t.getObject();
                    uris.add(c.getUri());
                }
            }
        }

//        try {
//            String[] data = queryString.split(" ");
//
//            for (String s : data) {
//                if (s.startsWith("<http:")) {
//                    s = s.replace("<", "");
//                    s = s.replace(">", "");
//
//                    uris.add(s);
//                }
//            }
//
//        } catch (Exception e) {
//        }
        return uris;
    }

    public static String getQuery(List<Triple> triples) {
        String query = "";

        if (triples.isEmpty()) {
            return "";
        }
        String returnVariables = "";

        for (Triple t : triples) {
            if (t.IsReturnVariable()) {
                returnVariables += t.toString();
            } else {
                query += t.toString() + "\n";
            }
        }

        if (!returnVariables.equals("")) {
            query = "SELECT DISTINCT " + returnVariables + " WHERE {\n " + query + " }";
        } else {
            query = "ASK WHERE { " + query + " }";
        }

        return query;
    }

    public static List<Triple> extractTriplesFromQuery(String queryString) {

        try {
            //replaces SELECT COUNT ?x with SELECT (COUNT ?x AS ?count) => because jena can't parse the first one
            //adds all namespaces
            String p = preprocessQuery(queryString);

            Query query = QueryFactory.create(p);

            //select count ?s .... query
            if (query.isSelectType() && query.hasAggregators()) {
                return countQuery(query);
            } else {
                if (query.hasOrderBy() || query.hasLimit() || query.hasOffset()) {
                    return standardQuery(query);
                } else if (!query.hasLimit() && !query.hasOffset()) {
                    return standardQuery(query);
                }
            }
        } catch (Exception e) {
        }

        return new ArrayList<>();
    }

    private static String preprocessQuery(String q) {

        String p1 = "^[S][E][L][E][C][T]\\s*\\w*\\s*[C][O][U][N][T]\\s*[(](.*)";

        //remove namespaces
        if (q.contains("SELECT")) {
            q = q.substring(q.indexOf("SELECT"));
        }
        if (q.contains("ASK")) {
            q = q.substring(q.indexOf("ASK"));
        }

        String p2 = "[C][O][U][N][T]\\s*[(](.*)([?]\\w+\\s*)*[)]";

        Pattern pattern = Pattern.compile(p1, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(q);
        boolean matchFound = matcher.matches();

        if (matchFound) {

            Pattern p = Pattern.compile(p2, Pattern.DOTALL);
            Matcher m = p.matcher(q);

            while (m.find()) {
                String part = m.group();

                q = q.replace(part, "(" + part + " AS ?count)");
            }
        }
//        if (q.contains("SELECT COUNT(DISTINCT ")) {
//            String prefix = q.substring(0, q.indexOf("SELECT COUNT(DISTINCT "));
//            String triples = q.substring(q.indexOf("WHERE"));
//            q = q.substring(q.indexOf("SELECT COUNT(DISTINCT "));
//
//            String b = "SELECT COUNT(DISTINCT ";
//            q = q.substring(0 + b.length() - 1, q.indexOf(")"));
//
//            q = "SELECT (COUNT(DISTINCT " + q.trim() + ") AS ?count)";
//
//            q = prefix + q + " \n" + triples;
//        }

        //add all namespaces
        q = getNamespaces() + "\n" + q;

        return q;
    }

    private static List<Triple> countQuery(Query query) {
        List<Triple> triples = new ArrayList<>();

        try {

            String returnVariable = "";

            Triple count = new Triple();

            count.setPredicate(new Predicate("type", false));
            count.setSubject(new Variable("?VAR_COUNT"));
            count.setObject(new Constant("COUNT"));

            count.setIsReturnVariable(true);

            triples.add(count);

            List<ExprAggregator> aggregators = query.getAggregators();
            for (ExprAggregator a : aggregators) {
                returnVariable = a.getAggVar().getVarName();
//                returnVariable = a.getAggregator()..getExpr().getVarName();

                Triple t = new Triple();

                t.setPredicate(new Predicate("type", false));
                t.setSubject(new Variable(returnVariable));
                t.setObject(new Constant("RETURN_VARIABLE"));

                t.setIsReturnVariable(true);

                triples.add(t);
            }

            List<Triple> predicates = new ArrayList<>();

            predicates = getPredicates(query);

            //if query is empty with only return variable, then no triple to return
            if (predicates.isEmpty()) {
                triples.clear();
            } else {
                triples.addAll(predicates);
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }

        return triples;
    }

    private static List<Triple> standardQuery(Query query) {
        List<Triple> triples = new ArrayList<>();

        try {

            List<String> returnVars = query.getResultVars();

            List<Var> VARS = query.getProjectVars();

            //query starts with SELECT * WHERE
            if (returnVars.isEmpty() && query.isSelectType()) {
                returnVars.add("*");
            }

            //add each return variable as Triple
            for (String s : returnVars) {
                Triple t = new Triple();

                t.setPredicate(new Predicate("type", false));
                t.setSubject(new Variable(s));
                t.setObject(new Constant("RETURN_VARIABLE"));

                t.setIsReturnVariable(true);

                triples.add(t);
            }

            List<Triple> predicates = new ArrayList<>();

            predicates = getPredicates(query);

            //if query is empty with only return variable, then no triple to return
            if (predicates.isEmpty()) {
                triples.clear();
            } else {
                triples.addAll(predicates);
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }

        return triples;
    }

    /*
     * @return all triples as Atoms
     * @param SPARQL query
     */
    private static List<Triple> getPredicates(Query query) {
        List<Triple> triples = new ArrayList<>();

        if (query.getQueryPattern() instanceof ElementGroup) {
            triples.addAll(getPredicatesFromElementGroup(query));
        }

        if (query.getQueryPattern() instanceof ElementTriplesBlock) {
            ElementTriplesBlock triplesBlock = (ElementTriplesBlock) query.getQueryPattern();

            Set<Triple> trips = getTripleFromElementTriplesBlock(triplesBlock);
            for (Triple t : trips) {
                triples.add(t);
            }
        }

        return triples;
    }

    private static List<Triple> getPredicatesFromElementGroup(Query query) {
        List<Triple> triples = new ArrayList<>();

        ElementGroup body = (ElementGroup) query.getQueryPattern();

        for (Element e : body.getElements()) {

            if (e instanceof ElementPathBlock) {
                ElementPathBlock block = (ElementPathBlock) e;

                Set<Triple> trips = getTripleFromElementPathBlock(block);
                for (Triple t : trips) {
                    triples.add(t);
                }
            }

            if (e instanceof ElementTriplesBlock) {
                ElementTriplesBlock triplesBlock = (ElementTriplesBlock) e;

                Set<Triple> trips = getTripleFromElementTriplesBlock(triplesBlock);
                for (Triple t : trips) {
                    triples.add(t);
                }
            }

            if (e instanceof ElementFilter) {
                ElementFilter filter = (ElementFilter) e;
                Expr expr = filter.getExpr();

                Set<Triple> trips = getTripleFromFilter(expr, 0);
                for (Triple t : trips) {
                    triples.add(t);
                }
            }

            if (e instanceof ElementOptional) {
                ElementOptional opt = (ElementOptional) e;
                Set<Triple> trips = getTripleFromOptional(opt);
                for (Triple t : trips) {
                    triples.add(t);
                }
            }
        }

        return triples;
    }

    private static Set<Triple> getTripleFromOptional(ElementOptional opt) {

        Set<Triple> triples = new LinkedHashSet<>();

        ElementGroup group = (ElementGroup) opt.getOptionalElement();

        for (Element e : group.getElements()) {

            if (e instanceof ElementPathBlock) {
                ElementPathBlock block = (ElementPathBlock) e;

                Set<Triple> trips = getTripleFromElementPathBlock(block);
                for (Triple t : trips) {
                    t.setIsOptional(true);
                    triples.add(t);
                }
            }

            if (e instanceof ElementFilter) {
                ElementFilter filter = (ElementFilter) e;
                Expr expr = filter.getExpr();

                Set<Triple> trips = getTripleFromFilter(expr, 0);
                for (Triple t : trips) {
                    t.setIsOptional(true);
                    triples.add(t);
                }
            }
        }

        return triples;
    }

    private static Set<Triple> getTripleFromElementTriplesBlock(ElementTriplesBlock block) {

        Set<Triple> triples = new LinkedHashSet<>();

        Iterator<org.apache.jena.graph.Triple> iterator = block.patternElts();

        while (iterator.hasNext()) {
            org.apache.jena.graph.Triple t = iterator.next();

            Triple triple = new Triple();

            //underspecified properties
//            if (t.getPredicate().toString().startsWith("v")) {
//                triple.setPredicate(new Predicate(t.getPredicate().getNameSpace(), true));
//            } else {
            if (t.getPredicate().isURI()) {

                triple.setPredicate(new Predicate(t.getPredicate().getURI(), false));
            }
            if (t.getPredicate().isVariable()) {
                triple.setPredicate(new Predicate(t.getPredicate().toString().replace("?", ""), true));
            }
//            }

            if (t.getSubject().isVariable()) {
                triple.setSubject(new Variable(t.getSubject().toString().replace("?", "")));
            }
            if (t.getSubject().isURI()) {
                triple.setSubject(new Constant(t.getSubject().getURI()));
            }
            if (t.getSubject().isLiteral()) {
                String l = t.getSubject().getLiteral().toString(true);
                triple.setSubject(new Constant(l));
            }

            if (t.getObject().isVariable()) {
                triple.setObject(new Variable(t.getObject().toString().replace("?", "")));
            }
            if (t.getObject().isURI()) {
                triple.setObject(new Constant(t.getObject().getURI()));
            }
            if (t.getObject().isLiteral()) {
                String l = t.getObject().getLiteral().toString(true);
                triple.setObject(new Constant(l));
            }

            triples.add(triple);

        }

        return triples;
    }

    private static Set<Triple> getTripleFromElementPathBlock(ElementPathBlock block) {

        Set<Triple> triples = new LinkedHashSet<>();

        Iterator<TriplePath> iterator = block.patternElts();

        while (iterator.hasNext()) {
            TriplePath t = iterator.next();

            Triple triple = new Triple();

            //underspecified properties
//            if (t.getPredicate().toString().startsWith("v")) {
//                triple.setPredicate(new Predicate(t.getPredicate().getNameSpace(), true));
//            } else {
            if (t.getPredicate().isURI()) {

                triple.setPredicate(new Predicate(t.getPredicate().getURI(), false));
            }
            if (t.getPredicate().isVariable()) {
                triple.setPredicate(new Predicate(t.getPredicate().toString().replace("?", ""), true));
            }
//            }

            if (t.getSubject().isVariable()) {
                triple.setSubject(new Variable(t.getSubject().toString().replace("?", "")));
            }
            if (t.getSubject().isURI()) {
                triple.setSubject(new Constant(t.getSubject().getURI()));
            }
            if (t.getSubject().isLiteral()) {
                String l = t.getSubject().getLiteral().toString(true);
                triple.setSubject(new Constant(l));
            }

            if (t.getObject().isVariable()) {
                triple.setObject(new Variable(t.getObject().toString().replace("?", "")));
            }
            if (t.getObject().isURI()) {
                triple.setObject(new Constant(t.getObject().getURI()));
            }
            if (t.getObject().isLiteral()) {
                String l = t.getObject().getLiteral().toString(true);
                triple.setObject(new Constant(l));
            }

            triples.add(triple);

        }

        return triples;
    }

    private static Set<Triple> getTripleFromFilter(Expr expr, int count) {
        Set<Triple> triples = new LinkedHashSet<>();

        String operation = expr.getClass().getName().replace("com.hp.hpl.jena.sparql.expr.E_", "");
        String op = expr.getFunction().getOpName();

        if (operation.equals("Function")) {
            operation = expr.getFunction().getFunctionIRI();
            op = operation;
            //replace the namespace
            if (operation.startsWith("http://www.w3.org/2001/XMLSchema#")) {
                operation = operation.replace("http://www.w3.org/2001/XMLSchema#", "xsd:");
                op = operation;
            }
        }

        if (operation.equals("Regex")) {
            op = operation.toLowerCase();
        }
        if (operation.equals("Bound")) {
            op = operation.toLowerCase();
        }
        if (operation.equals("DateTimeYear")) {
            op = "year";
        }

        String subject = "", object = "";
        Term s = null, o = null;

        for (int i = 0; i < expr.getFunction().getArgs().size(); i++) {
            Expr e = expr.getFunction().getArgs().get(i);

            //System.out.println(e);
            if (e.isFunction()) {
                Set<Triple> trips = getTripleFromFilter(e, count);
                count += trips.size();
                for (Triple t : trips) {

                    // t is equals to this patter: year(?uri), bound(?date), xsd:integer(?d)
                    if (t.getObject() == null) {
                        subject = t.getPredicate() + "(" + t.getSubject() + ")";
                        s = new UnaryFunction(t.getPredicate().getPredicateName(), t.getSubject().toString());
                    }

//                    if (operation.equals("LogicalNot")) {
//                        t.getPredicate().setPredicateName("! " + t.getPredicate().getPredicateName());
//                    }
//                    triples.add(t);
                }
            } else {
                if (e.isVariable()) {
                    if (i == 0) {
                        subject = e.toString().replace("?", "");
                        s = new Variable(subject);
                    }
                    if (i == 1) {
                        object = e.toString().replace("?", "");
                        o = new Variable(object);
                    }
                }
                if (e.isConstant()) {
                    if (i == 0) {
                        subject = e.toString().replace("<", "").replace(">", "");
                        s = new Constant(subject);
                    }
                    if (i == 1) {
                        object = e.toString().replace("<", "").replace(">", "");
                        o = new Constant(object);
                    }
                }
            }

        }

        if (s != null) {
            if (!op.equals("!")) {
                Triple t1 = t1 = new Triple();
                t1.setIsFilter(true);
                t1.setObject(o);
                t1.setSubject(s);
                t1.setPredicate(new Predicate(op, false));
                triples.add(t1);
            }

        }
        return triples;
    }

    private static String getNamespaces() {
        String n = "PREFIX dbo: <http://dbpedia.org/ontology/>\n"
                + "PREFIX res: <http://dbpedia.org/resource/>\n"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                + "PREFIX dbp: <http://dbpedia.org/property/>\n"
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX yago: <http://dbpedia.org/class/yago/> \n"
                + "";

        return n;
    }
}
