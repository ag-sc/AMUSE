/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.demo;

import java.util.Set;

/**
 *
 * @author sherzod
 */
public class Response {
    private String query;
    private Set<String> answers;

    public Response(String query, Set<String> answers) {
        this.query = query;
        this.answers = answers;
    }
    
    
}
