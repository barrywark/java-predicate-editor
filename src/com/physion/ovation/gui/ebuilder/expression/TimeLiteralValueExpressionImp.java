package com.physion.ovation.gui.ebuilder.expression;

import java.util.Date;


/**
 */
public class TimeLiteralValueExpressionImp
    extends ExpressionImp
    implements TimeLiteralValueExpression {

    private Date value;


    @Override
    public Object getValue() {
        return(value);
    }


    TimeLiteralValueExpressionImp(Date value) {
        this.value = value;
    }


    public String toString(String indent) {
        return(indent+"TimeLiteralValueExpression("+value+")");
    }
}
