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
public interface Var {
    
    public String getValue ();

    public void setValue (String value);

    public String getType ();

    public void setType (String type);
}
