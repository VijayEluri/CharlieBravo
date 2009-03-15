/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.dmdirc.addons.calc;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author chris
 */
public class Lexer {

    final String input;

    public Lexer(String input) {
        this.input = input.replaceAll("\\s+", "");
    }

    public List<Token> tokenise() throws ParseException {
        final List<Token> res = new ArrayList<Token>();
        List<TokenType> possibles = Arrays.asList(TokenType.values());

        boolean cont = true;
        int i = 0;

        do {
            boolean found = false;

            for (TokenType type : possibles) {
                final int match = type.match(input, i);

                if (match > -1) {
                    res.add(new Token(type, input.substring(i, match)));

                    possibles = type.getFollowers();
                    i = match;
                    found = true;
                    cont = type != TokenType.END;

                    break;
                }
            }

            if (!found) {
                throw new ParseException("No legal token found at offset "
                        + i + ". Expecting one of: "
                        + Arrays.toString(possibles.toArray()), i);
            }
        } while (cont);

        return res;
    }

}
