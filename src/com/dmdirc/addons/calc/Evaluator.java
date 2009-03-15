/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.dmdirc.addons.calc;

/**
 *
 * @author chris
 */
public class Evaluator {

    private final TreeToken node;

    public Evaluator(TreeToken node) {
        this.node = node;
    }

    public Number evaluate() {
        return node.evaluate();
    }

}
