package com.physion.ovation.gui.ebuilder.expression;


/**
 */
public class Float64LiteralValueExpressionImp
    extends ExpressionImp
    implements Float64LiteralValueExpression {

    private double value;


    @Override
    public Object getValue() {
        return(new Double(value));
    }


    Float64LiteralValueExpressionImp(double value) {
        this.value = value;
    }


    public String toString(String indent) {
        return(indent+"Float64LiteralValueExpression("+value+")");
    }
}
