
package com.dmdirc.addons.calc;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum TokenType {

    START(TokenTypeArity.HIDDEN, "^", 0, "NUMBER_*", "BRACKET_OPEN", "MOD_*"),
    END(TokenTypeArity.HIDDEN, "$", 0),
    
    BRACKET_OPEN(TokenTypeArity.NULLARY, "\\(", 0, "NUMBER_*", "MOD_*", "BRACKET_OPEN"),
    BRACKET_CLOSE(TokenTypeArity.NULLARY, "\\)", 50, "OP_*", "BRACKET_*", "END"),

    NUMBER_FLOAT(TokenTypeArity.NULLARY, "[0-9]+\\.[0-9]+", 1, "OP_*", "BRACKET_*", "END") {
        public Number evaluate(final TreeToken token) {
            return Float.valueOf(token.getToken().getContent());
        }
    },

    NUMBER_INT(TokenTypeArity.NULLARY, "[0-9]+", 1, "OP_*", "BRACKET_*", "END") {
        public Number evaluate(final TreeToken token) {
            return Float.valueOf(token.getToken().getContent());
        }
    },

    MOD_POSITIVE(TokenTypeArity.UNARY, "\\+", 100, "NUMBER_*") {
        public Number evaluate(final TreeToken token) {
            return token.getChildren().get(0).evaluate();
        }
    },

    MOD_NEGATIVE(TokenTypeArity.UNARY, "-", 100, "NUMBER_*") {
        public Number evaluate(final TreeToken token) {
            return -1 * token.getChildren().get(0).evaluate().floatValue();
        }
    },

    OP_PLUS(TokenTypeArity.BINARY, "\\+", 7, "NUMBER_*", "BRACKET_OPEN") {
        public Number evaluate(final TreeToken token) {
            return token.getChildren().get(0).evaluate().floatValue()
                    + token.getChildren().get(1).evaluate().floatValue();
        }
    },

    OP_MINUS(TokenTypeArity.BINARY, "-", 6, "NUMBER_*", "BRACKET_OPEN") {
        public Number evaluate(final TreeToken token) {
            return token.getChildren().get(0).evaluate().floatValue()
                    - token.getChildren().get(1).evaluate().floatValue();
        }
    },

    OP_MULT(TokenTypeArity.BINARY, "(?=\\()|\\*", 9, "NUMBER_*", "BRACKET_OPEN") {
        public Number evaluate(final TreeToken token) {
            return token.getChildren().get(0).evaluate().floatValue()
                    * token.getChildren().get(1).evaluate().floatValue();
        }
    },

    OP_DIVIDE(TokenTypeArity.BINARY, "/", 10, "NUMBER_*", "BRACKET_OPEN") {
        public Number evaluate(final TreeToken token) {
            return token.getChildren().get(0).evaluate().floatValue()
                    / token.getChildren().get(1).evaluate().floatValue();
        }
    },

    OP_MOD(TokenTypeArity.BINARY, "%", 8, "NUMBER_*", "BRACKET_OPEN") {
        public Number evaluate(final TreeToken token) {
            return token.getChildren().get(0).evaluate().floatValue()
                    % token.getChildren().get(1).evaluate().floatValue();
        }
    },

    OP_POWER(TokenTypeArity.BINARY, "\\^", 11, "NUMBER_*", "BRACKET_OPEN") {
        public Number evaluate(final TreeToken token) {
            return new Float(Math.pow(token.getChildren().get(0).evaluate().doubleValue(),
                    token.getChildren().get(1).evaluate().doubleValue()));
        }
    },
    ;

    private final String[] strfollows;
    private final int precedence;
    private List<TokenType> follows;
    private final Pattern regex;
    private final TokenTypeArity arity;

    TokenType(final TokenTypeArity arity, final String regex,
            final int precedence, final String ... follows) {
        this.arity = arity;
        this.strfollows = follows;
        this.precedence = precedence;
        this.regex = Pattern.compile(regex);
    }

    public synchronized  List<TokenType> getFollowers() {
        if (follows == null) {
            follows = new ArrayList<TokenType>();

            for (int i = 0; i < strfollows.length; i++) {
                follows.addAll(searchValueOf(strfollows[i]));
            }
        }

        return follows;
    }

    public TokenTypeArity getArity() {
        return arity;
    }

    public int getPrecedence() {
        return precedence;
    }

    public int match(final String input, final int offset) {
        final Matcher matcher = regex.matcher(input);
        matcher.useAnchoringBounds(false);
        matcher.useTransparentBounds(true);

        return matcher.find(offset) && matcher.start() == offset ? matcher.end() : -1;
    }

    public Number evaluate(final TreeToken token) {
        throw new AbstractMethodError("Can't evaluate this token type");
    }

    /**
     *
     * @param name
     * @return
     */
    protected static List<TokenType> searchValueOf(final String name) {
        final List<TokenType> res = new ArrayList<TokenType>();

        for (TokenType token : values()) {
            if ((name.endsWith("*")
                    && token.name().startsWith(name.substring(0, name.length() - 1)))
                    || name.equals(token.name())) {
                res.add(token);
            }
        }

        return res;
    }
}