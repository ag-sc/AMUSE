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
public class QALDInstance {
    private Questions[] questions;

    public Questions[] getQuestions ()
    {
        return questions;
    }

    public void setQuestions (Questions[] questions)
    {
        this.questions = questions;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [questions = "+questions+"]";
    }
}
