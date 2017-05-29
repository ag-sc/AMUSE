/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.utils;

/**
 *
 * @author sherzod
 */
public class FreshVariable {

    private static int variableCount = 100;

    public static int get() {
        variableCount++;
        return variableCount;
    }
}
