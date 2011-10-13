package com.physion.ovation.gui.ebuilder.expression;

import java.util.ArrayList;
import java.util.List;

import com.physion.ovation.gui.ebuilder.datatypes.CollectionOperator;
import com.physion.ovation.gui.ebuilder.datatypes.Attribute;
import com.physion.ovation.gui.ebuilder.datatypes.Cardinality;
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
        else if (rowData.getCollectionOperator() != null) {
            OperatorExpressionImp operatorExpression = 
                new OperatorExpressionImp(rowData);
            operatorExpression.addOperand(
                createExpression(rowData.getAttributePath()));
            for (RowData childRow : rowData.getChildRows()) {
                operatorExpression.addOperand(createExpression(childRow));
            }

            expression = operatorExpression;
        }

        return(expression);
    }


    private static LiteralValueExpression createLiteralValueExpression(
        RowData rowData) {

        LiteralValueExpression literalValueExpression = null;

        Attribute attribute = rowData.getChildmostAttribute();
        switch (attribute.getType()) {

            case BOOLEAN:
                return(new BooleanLiteralValueExpressionImp(
                       ((Boolean)rowData.getAttributeValue()).booleanValue()));

            case UTF_8_STRING:
                return(new StringLiteralValueExpressionImp(
                       rowData.getAttributeValue().toString()));

            case REFERENCE:
                return(new ClassLiteralValueExpressionImp(
                       attribute.getQueryName()));

            default:
                System.err.println("ERROR:  ExpressionImp."+
                    "createLiteralValueExpression().  Unhandled type.\n"+
                    "Type = "+attribute.getType()+"\n"+
                    "rowData:\n"+rowData.getRowString());
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

        Attribute attribute;
        RowData rowData;
        RowData rootRow;
        Expression expression;

        System.out.println("ExpressionImp test is starting...");

        /**
         * Test the Any collection operator, (which becomes "or"),
         * and the String type and a couple attribute operators.
         */
/*
        rootRow = new RowData();
        rootRow.setClassUnderQualification(
            DataModel.getClassDescription("Epoch"));
        rootRow.setCollectionOperator(CollectionOperator.ANY);

        rowData = new RowData();
        attribute = new Attribute("protocolID", Type.UTF_8_STRING);
        rowData.addAttribute(attribute);
        rowData.setAttributeOperator(Operator.EQUALS);
        rowData.setAttributeValue("abc");
        rootRow.addChildRow(rowData);

        rowData = new RowData();
        attribute = new Attribute("protocolID", Type.UTF_8_STRING);
        rowData.addAttribute(attribute);
        rowData.setAttributeOperator(Operator.MATCHES_CASE_INSENSITIVE);
        rowData.setAttributeValue("xyz");
        rootRow.addChildRow(rowData);
        
        /**
         * Now convert the RowData object we created above
         * into an Expression object.
         */
/*
        expression = ExpressionImp.createExpressionTree(rootRow);
        System.out.println("\nRowData:\n"+rootRow);
        System.out.println("\nExpression:\n"+expression);

        /**
         * Test the All collection operator, (which becomes "and"),
         * and the Boolean type.
         */
/*
        rootRow = new RowData();
        rootRow.setClassUnderQualification(
            DataModel.getClassDescription("Epoch"));
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        attribute = new Attribute("incomplete", Type.BOOLEAN);
        rowData.addAttribute(attribute);
        rowData.setAttributeOperator(Operator.EQUALS);
        rowData.setAttributeValue(new Boolean(true));
        rootRow.addChildRow(rowData);

        expression = ExpressionImp.createExpressionTree(rootRow);
        System.out.println("\nRowData:\n"+rootRow);
        System.out.println("\nExpression:\n"+expression);

        /**
         * Test an attribute path with two levels:
         *
         *      epochGroup.label == "Test 27"
         */
/*
        rootRow = new RowData();
        rootRow.setClassUnderQualification(
            DataModel.getClassDescription("Epoch"));
        rootRow.setCollectionOperator(CollectionOperator.NONE);

        rowData = new RowData();
        attribute = new Attribute("epochGroup", Type.REFERENCE,
                                  DataModel.getClassDescription("EpochGroup"),
                                  Cardinality.TO_ONE);
        rowData.addAttribute(attribute);
        attribute = new Attribute("label", Type.UTF_8_STRING);
        rowData.addAttribute(attribute);
        rowData.setAttributeOperator(Operator.EQUALS);
        rowData.setAttributeValue("Test 27");
        rootRow.addChildRow(rowData);

        expression = ExpressionImp.createExpressionTree(rootRow);
        System.out.println("\nRowData:\n"+rootRow);
        System.out.println("\nExpression:\n"+expression);

        /**
         * Test an attribute path with three levels:
         *
         *      epochGroup.source.label == "Test 27"
         */
/*
        rootRow = new RowData();
        rootRow.setClassUnderQualification(
            DataModel.getClassDescription("Epoch"));
        rootRow.setCollectionOperator(CollectionOperator.NONE);

        rowData = new RowData();
        attribute = new Attribute("epochGroup", Type.REFERENCE,
                                  DataModel.getClassDescription("EpochGroup"),
                                  Cardinality.TO_ONE);
        rowData.addAttribute(attribute);
        attribute = new Attribute("source", Type.REFERENCE,
                                  DataModel.getClassDescription("Source"),
                                  Cardinality.TO_ONE);
        rowData.addAttribute(attribute);
        attribute = new Attribute("label", Type.UTF_8_STRING);
        rowData.addAttribute(attribute);
        rowData.setAttributeOperator(Operator.EQUALS);
        rowData.setAttributeValue("Test 27");
        rootRow.addChildRow(rowData);

        expression = ExpressionImp.createExpressionTree(rootRow);
        System.out.println("\nRowData:\n"+rootRow);
        System.out.println("\nExpression:\n"+expression);

        /**
         * Test a reference value.
         *
         *      Epoch | All
         *        Epoch | owner is null
         */
/*
        rootRow = new RowData();
        rootRow.setClassUnderQualification(
            DataModel.getClassDescription("Epoch"));
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        attribute = new Attribute("owner", Type.REFERENCE,
                                  DataModel.getClassDescription("User"),
                                  Cardinality.TO_ONE);
        rowData.addAttribute(attribute);
        rowData.setAttributeOperator(Operator.IS_NULL);
        rootRow.addChildRow(rowData);

        System.out.println("\nRowData:\n"+rootRow);
        expression = ExpressionImp.createExpressionTree(rootRow);
        System.out.println("\nExpression:\n"+expression);

        /**
         * Test a compound row:
         *
         *      Epoch | All
         *        Epoch | responses All
         *          Response | uuid == "xyz"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(
            DataModel.getClassDescription("Epoch"));
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        attribute = new Attribute("responses", Type.REFERENCE,
                                  DataModel.getClassDescription("Response"),
                                  Cardinality.TO_MANY);
        rowData.addAttribute(attribute);
        rowData.setCollectionOperator(CollectionOperator.ALL);
        rootRow.addChildRow(rowData);

        RowData rowData2 = new RowData();
        attribute = new Attribute("uuid", Type.UTF_8_STRING);
        rowData2.addAttribute(attribute);
        rowData2.setAttributeOperator(Operator.EQUALS);
        rowData2.setAttributeValue("xyz");
        rowData.addChildRow(rowData2);

        System.out.println("\nRowData:\n"+rootRow);
        expression = ExpressionImp.createExpressionTree(rootRow);
        System.out.println("\nExpression:\n"+expression);

        System.out.println("\nExpressionImp test is ending.");
    }
}
