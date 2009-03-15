/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.dmdirc.addons.calc;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author chris
 */
public class Parser {

    protected final Lexer lexer;
    protected static final List<TokenType> TOKENS_BY_PRECEDENCE;

    static {
        TOKENS_BY_PRECEDENCE = new ArrayList<TokenType>(Arrays.asList(TokenType.values()));
        Collections.sort(TOKENS_BY_PRECEDENCE, new TokenTypePrecedenceComparator());
    }

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public TreeToken parse() throws ParseException {
        final List<TreeToken> tokens = new ArrayList<TreeToken>();

        for (Token token : lexer.tokenise()) {
            tokens.add(new TreeToken(token));
        }

        return parse(tokens);
    }

    protected TreeToken parse(final List<TreeToken> tokens) throws ParseException {
        System.out.println("Parse: " + Arrays.toString(tokens.toArray()));

        while (tokens.size() > 1) {
            System.out.println(" --> " + Arrays.toString(tokens.toArray()));
            for (TokenType type : TOKENS_BY_PRECEDENCE) {
                final int offset = findTokenType(tokens, type);

                if (offset > -1) {
                    System.out.println(" --> Found token " + type);
                    switch (type.getArity()) {
                        case HIDDEN:
                            parseHiddenOperator(tokens, offset);
                            break;
                        case BINARY:
                            parseBinaryOperator(tokens, offset);
                            break;
                        case UNARY:
                            parseUnaryOperator(tokens, offset);
                            break;
                        case NULLARY:
                            parseNullaryOperator(tokens, offset);
                            break;
                    }

                    break;
                }
            }
        }

        return tokens.get(0);
    }

    protected void parseNullaryOperator(final List<TreeToken> tokens, final int offset)
            throws ParseException {
        if (tokens.get(offset).getToken().getType() == TokenType.BRACKET_CLOSE
                || tokens.get(offset).getToken().getType() == TokenType.BRACKET_OPEN) {
            parseBracket(tokens, offset);
        } else {
            parseNumber(tokens, offset);
        }
    }

    protected void parseBracket(final List<TreeToken> tokens, final int offset)
            throws ParseException {
        final List<TreeToken> stack = new ArrayList<TreeToken>();

        System.out.println("ParseBracket: " + offset + " " + Arrays.toString(tokens.toArray()));

        for (int i = offset - 1; i > 0; i--) {
            if (tokens.get(i).getToken().getType() == TokenType.BRACKET_OPEN
                    && !tokens.get(i).isProcessed()) {
                System.out.println("Found opening bracket at index " + i);
                tokens.add(i, parse(stack));
                tokens.get(i).setProcessed();
                tokens.remove(i + 1);
                tokens.remove(i + 1);
                return;
            } else {
                System.out.println("Skipping " + tokens.get(i));
                stack.add(0, tokens.get(i));
                tokens.remove(i);
            }
        }

        throw new ParseException("Couldn't find matching opening bracket", offset);
    }

    protected void parseBinaryOperator(final List<TreeToken> tokens, final int offset) {
        tokens.get(offset).addChild(tokens.get(offset - 1));
        tokens.get(offset).addChild(tokens.get(offset + 1));
        tokens.get(offset).setProcessed();

        tokens.remove(offset + 1);
        tokens.remove(offset - 1);
    }

    protected void parseUnaryOperator(final List<TreeToken> tokens, final int offset) {
        tokens.get(offset).addChild(tokens.get(offset + 1));
        tokens.get(offset).setProcessed();
        tokens.remove(offset + 1);
    }

    protected void parseHiddenOperator(final List<TreeToken> tokens, final int offset) {
        tokens.remove(offset);
    }

    protected void parseNumber(final List<TreeToken> tokens, final int offset) {
        tokens.get(offset).setProcessed();
    }

    protected static int findTokenType(final List<TreeToken> tokens, final TokenType type) {
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).getToken().getType() == type && !tokens.get(i).isProcessed()) {
                return i;
            }
        }

        return -1;
    }

    protected static class TokenTypePrecedenceComparator implements Comparator<TokenType> {

        @Override
        public int compare(TokenType o1, TokenType o2) {
            return o2.getPrecedence() - o1.getPrecedence();
        }

    }

}
