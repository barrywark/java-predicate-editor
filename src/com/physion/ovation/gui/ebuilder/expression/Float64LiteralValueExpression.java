package com.physion.ovation.gui.ebuilder.expression;


/**
 */
public class Float64LiteralValueExpression
    extends Expression
    implements IFloat64LiteralValueExpression {

    private double value;


    @Override
    public Object getValue() {
        return(new Double(value));
    }


    Float64LiteralValueExpression(double value) {
        this.value = value;
    }


    public String toString(String indent) {
        return(indent+"Float64LiteralValueExpression("+value+")");
    }
}
