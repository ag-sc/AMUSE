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
public class Results {
    private Bindings[] bindings;

    public Bindings[] getBindings ()
    {
        return bindings;
    }

    public void setBindings (Bindings[] bindings)
    {
        this.bindings = bindings;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [bindings = "+bindings+"]";
    }
}
