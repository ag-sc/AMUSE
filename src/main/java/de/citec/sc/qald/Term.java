/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.qald;

/**
 *
 * @author sherzod
 */
public interface Term {

    public Term clone();

    public boolean isVariable();
}
