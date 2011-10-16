package com.physion.ovation.gui.ebuilder.expression;

import java.util.Date;


/**
 */
public class TimeLiteralValueExpression
    extends Expression
    implements ITimeLiteralValueExpression {

    private Date value;


    @Override
    public Object getValue() {
        return(value);
    }


    TimeLiteralValueExpression(Date value) {
        this.value = value;
    }


    public String toString(String indent) {
        return(indent+"TimeLiteralValueExpression("+value+")");
    }
}
