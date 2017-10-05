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
public class Head {
    private String[] vars;

    public String[] getVars ()
    {
        return vars;
    }

    public void setVars (String[] vars)
    {
        this.vars = vars;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [vars = "+vars+"]";
    }
}
