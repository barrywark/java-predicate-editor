package com.physion.ovation.gui.ebuilder.expression;

import java.io.Serializable;


/**
 */
public class AttributeExpression
    extends Expression
    implements IAttributeExpression, Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String attributeName;


    /**
     * Get the name of this attribute.
     */
    @Override
    public String getAttributeName() {
        return(attributeName);
    }


    public AttributeExpression(String attributeName) {
        this.attributeName = attributeName;
    }


    public String toString(String indent) {
        return(indent+"AttributeExpression("+attributeName+")");
    }
}
