package com.physion.ovation.gui.ebuilder.expression;

import java.io.Serializable;
import java.util.Date;


/**
 */
public class TimeLiteralValueExpression
    extends Expression
    implements ITimeLiteralValueExpression, Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Date value;


    @Override
    public Object getValue() {
        return(value);
    }


    public TimeLiteralValueExpression(Date value) {
        this.value = value;
    }


    public String toString(String indent) {
        return(indent+"TimeLiteralValueExpression("+value+")");
    }
}
