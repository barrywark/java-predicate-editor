package com.physion.ovation.gui.ebuilder.expression;

import  com.physion.ovation.gui.ebuilder.datamodel.RowData;


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


    /*
    AttributeExpressionImp(RowData rowData) {
        attributeName = rowData.getAttributeName();
    }
    */
}
