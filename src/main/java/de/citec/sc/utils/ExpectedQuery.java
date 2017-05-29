/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.utils;

import de.citec.sc.variable.State;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sherzod
 */
public class ExpectedQuery {

    private static List<String> postagsForASK;
    private static List<String> beginTokensForASK;

    private static void load() {

        postagsForASK = new ArrayList<>();
        postagsForASK.add("VBP");
        postagsForASK.add("VBZ");
        postagsForASK.add("VBD");
        postagsForASK.add("VB");

        beginTokensForASK = new ArrayList<>();
        beginTokensForASK.add("did");
        beginTokensForASK.add("do");
        beginTokensForASK.add("are");
        beginTokensForASK.add("was");
        beginTokensForASK.add("were");
        beginTokensForASK.add("does");
        beginTokensForASK.add("is");

    }

    //checks if given state is of type ASK query
    public static boolean isASK(State state) {
        if (postagsForASK == null || beginTokensForASK == null) {
            load();
        }

        String firstToken = state.getDocument().getParse().getToken(1).toLowerCase();
        String firstPOS = state.getDocument().getParse().getPOSTag(1);

        if (postagsForASK.contains(firstPOS) && beginTokensForASK.contains(firstToken)) {
            return true;
        }

        return false;
    }
}
