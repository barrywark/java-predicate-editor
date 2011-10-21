package com.physion.ovation.gui.ebuilder.expression;


import java.io.Serializable;


/**
 */
public class Int32LiteralValueExpression
    extends Expression
    implements IInt32LiteralValueExpression, Serializable {

    private int value;


    @Override
    public Object getValue() {
        return(new Integer(value));
    }


    Int32LiteralValueExpression(int value) {
        this.value = value;
    }


    public String toString(String indent) {
        return(indent+"Int32LiteralValueExpression("+value+")");
    }
}
