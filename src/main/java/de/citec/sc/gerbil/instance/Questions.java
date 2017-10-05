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
public class Questions {
    private String id;

    private Query query;

    private String answertype;

    private Answers[] answers;

    private Question[] question;

    public String getId ()
    {
        return id;
    }

    public void setId (String id)
    {
        this.id = id;
    }

    public Query getQuery ()
    {
        return query;
    }

    public void setQuery (Query query)
    {
        this.query = query;
    }

    public String getAnswertype ()
    {
        return answertype;
    }

    public void setAnswertype (String answertype)
    {
        this.answertype = answertype;
    }

    public Answers[] getAnswers ()
    {
        return answers;
    }

    public void setAnswers (Answers[] answers)
    {
        this.answers = answers;
    }

    public Question[] getQuestion ()
    {
        return question;
    }

    public void setQuestion (Question[] question)
    {
        this.question = question;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [id = "+id+", query = "+query+", answertype = "+answertype+", answers = "+answers+", question = "+question+"]";
    }
}
