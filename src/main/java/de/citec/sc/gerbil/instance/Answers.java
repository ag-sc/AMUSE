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
public class Answers {
    private Results results;

    private Head head;

    public Results getResults ()
    {
        return results;
    }

    public void setResults (Results results)
    {
        this.results = results;
    }

    public Head getHead ()
    {
        return head;
    }

    public void setHead (Head head)
    {
        this.head = head;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [results = "+results+", head = "+head+"]";
    }
}
