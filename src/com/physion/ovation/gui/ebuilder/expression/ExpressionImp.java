package com.physion.ovation.gui.ebuilder.expression;

import java.util.ArrayList;
import java.util.List;

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
     * Required by Expression interface, but not needed for
     * current work.
     */
    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
    }


    /**
     * Create an Expression from the passed in root RowData.
     */
    private static Expression createExpressionTree(RowData rootRow) {

        OperatorExpressionImp expression = new OperatorExpressionImp(rootRow);

        for (RowData childRow : rootRow.getChildRows()) {
            expression.addOperand(createExpression(childRow));
        }

        return(expression);
    }


    private static Expression createExpression(RowData rowData) {

        OperatorExpressionImp expression = null;

        if (rowData.getAttributeOperator() != null) {
            OperatorExpressionImp operatorExpression = 
                new OperatorExpressionImp(rowData);
            operatorExpression.addOperand(
                createExpression(rowData.getAttributePath()));
            operatorExpression.addOperand(
                createLiteralValueExpression(rowData));

            expression = operatorExpression;
        }

        return(expression);
    }


    private static LiteralValueExpression createLiteralValueExpression(
        RowData rowData) {

        LiteralValueExpression literalValueExpression = null;

        Attribute attribute = rowData.getChildmostAttribute();
        switch (attribute.getType()) {

            /*
            case BOOLEAN:
                return(new BooleanLiteralValueExpresion(
                       rowData.getAttributeValue()));
            break;
            */

            case UTF_8_STRING:
                return(new StringLiteralValueExpressionImp(
                       rowData.getAttributeValue().toString()));

            default:
                System.err.println("ERROR:  Unhandled type.");
                return(null);
        }
    }


    private static Expression createExpression(List<Attribute>
                                               attributePath) {

        if (attributePath.size() < 1) {
            return(null);
        }

        if (attributePath.size() == 1) {
            return(new AttributeExpressionImp(attributePath.get(0).
                                              getQueryName()));
        }

        OperatorExpressionImp operatorExpression =
            new OperatorExpressionImp(".");

        /*
        int index = attributePath.size()-1;
        Attribute attribute = attributePath.get(index);
        AttributeExpression attributeExpression =
            new AttributeExpressionImp(attribute.getQueryName());
        */

        List<Attribute> subList = attributePath.subList(0,
                                            attributePath.size()-1);
        Expression expression = createExpression(subList);
        operatorExpression.addOperand(expression);

        Attribute attribute = (Attribute)attributePath.get(
            attributePath.size()-1);
        AttributeExpression attributeExpression =
            new AttributeExpressionImp(attribute.getQueryName());

        operatorExpression.addOperand(attributeExpression);

        return(operatorExpression);
    }


    /**
     * Get a string version of this class that can be used
     * for debugging purposes.
     */
/*
    public String toString() {
        return(toString(""));
    }
*/

    /**
     * Get a string version of this class that can be used
     * for debugging purposes.
     *
     * @param indent The lines of the returned string will
     * all be indented by (at least) this amount.
     */
    public String toString(String indent) {
        return("ERROR: You need to override this.");
    }

    /*
        String string = indent;
        string += "Expression("+getOperatorName()+")\n";

        for (Expression expression : getOperandList()) {
            string += indent+expression.toString();
        }
        return(string);
    }
    */


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

        rowData = new RowData();
        attribute = new Attribute("protocolID", Type.UTF_8_STRING);
        rowData.addAttribute(attribute);
        rowData.setAttributeOperator(Operator.EQUALS);
        rowData.setAttributeValue("xyz");
        rootRow.addChildRow(rowData);
        
        /**
         * Now convert the RowData object we created above
         * into an Expression object.
         */
        Expression expression = ExpressionImp.createExpressionTree(rootRow);

        System.out.println("\nexpression:\n"+expression);

        System.out.println("ExpressionImp test is ending.");
    }
}
