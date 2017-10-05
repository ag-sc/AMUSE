/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.gerbil.instance;

/**
 *
 * @author sherzod
 */
public class Query {
    private String sparql;

    public String getSparql ()
    {
        return sparql;
    }

    public void setSparql (String sparql)
    {
        this.sparql = sparql;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [sparql = "+sparql+"]";
    }
}
