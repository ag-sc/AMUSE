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
public class Bindings {
     private Uri uri;

    public Uri getUri ()
    {
        return uri;
    }

    public void setUri (Uri uri)
    {
        this.uri = uri;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [uri = "+uri+"]";
    }
}
