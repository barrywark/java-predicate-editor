package com.physion.ovation.gui.ebuilder.expression;


/**
 */
public class AttributeExpression
    extends Expression
    implements IAttributeExpression {

    private String attributeName;


    /**
     * Get the name of this attribute.
     */
    @Override
    public String getAttributeName() {
        return(attributeName);
    }


    AttributeExpression(String attributeName) {
        this.attributeName = attributeName;
    }


    public String toString(String indent) {
        return(indent+"AttributeExpression("+attributeName+")");
    }
}
