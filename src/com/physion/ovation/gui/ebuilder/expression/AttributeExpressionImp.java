package com.physion.ovation.gui.ebuilder.expression;


/**
 */
public class AttributeExpressionImp
    extends ExpressionImp
    implements AttributeExpression {

    private String attributeName;


    /**
     * Get the name of this attribute.
     */
    @Override
    public String getAttributeName() {
        return(attributeName);
    }


    AttributeExpressionImp(String attributeName) {
        this.attributeName = attributeName;
    }


    public String toString(String indent) {
        return(indent+"AttributeExpression("+attributeName+")");
    }
}
