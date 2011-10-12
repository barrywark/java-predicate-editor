package com.physion.ovation.gui.ebuilder.expression;

import java.util.ArrayList;

import com.physion.ovation.gui.ebuilder.datatypes.CollectionOperator;
import com.physion.ovation.gui.ebuilder.datatypes.Attribute;
import com.physion.ovation.gui.ebuilder.datatypes.Type;
import com.physion.ovation.gui.ebuilder.datatypes.Operator;
import com.physion.ovation.gui.ebuilder.datamodel.RowData;
import com.physion.ovation.gui.ebuilder.datamodel.DataModel;


/**
 */
public class ExpressionImp
    implements Expression {

    /**
     * 
     */
    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
    }


    private static Expression createExpression(RowData rowData) {

        Expression expression = null;

        if (rowData.getCollectionOperator() != null) {
            expression = new OperatorExpressionImp(rowData);
        }

        return(expression);
    }


    /**
     * This is a simple test program for this class.
     */
    public static void main(String[] args) {

        System.out.println("ExpressionImp test is starting...");

        RowData rootRow = new RowData();
        rootRow.setClassUnderQualification(
            DataModel.getClassDescription("Epoch"));
        rootRow.setCollectionOperator(CollectionOperator.ANY);

        Attribute attribute;
        RowData rowData;

        rowData = new RowData();
        attribute = new Attribute("protocolID", Type.UTF_8_STRING);
        rowData.addAttribute(attribute);
        rowData.setAttributeOperator(Operator.EQUALS);
        rowData.setAttributeValue("abc");
        rootRow.addChildRow(rowData);
        
        /**
         * Now convert the RowData object we created above
         * into an Expression object.
         */
        Expression expression = ExpressionImp.createExpression(rootRow);

        System.out.println("\nexpression:\n"+expression);

        System.out.println("ExpressionImp test is ending.");
    }
}
