package com.physion.ovation.gui.ebuilder.expression;

import java.util.List;
import java.util.Date;

import com.physion.ovation.gui.ebuilder.datatypes.CollectionOperator;
import com.physion.ovation.gui.ebuilder.datatypes.Attribute;
import com.physion.ovation.gui.ebuilder.datatypes.Cardinality;
import com.physion.ovation.gui.ebuilder.datatypes.Type;
import com.physion.ovation.gui.ebuilder.datatypes.Operator;
import com.physion.ovation.gui.ebuilder.datamodel.RowData;
import com.physion.ovation.gui.ebuilder.datamodel.DataModel;


/**
 * TODO:  Put keywords such as "not", "or", "and", "as", "value",
 * "parameter", etc. into constants.
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
     * This method should only be called if the passed in
     * RowData object is the rootRow of an expression tree.
     */
    public static Expression createExpressionTree(RowData rootRow) {

        if (rootRow.isRootRow() == false) {
            System.err.println("ERROR:  ExpressionImp.createExpression() "+
                "was passed a rootRow parameter that is not really the "+
                "root of an expression tree.");
            return(null);
        }

        /**
         * This is the root Expression object that will be returned
         * to the caller of this method.
         */
        OperatorExpressionImp rootExpression;

        /**
         * In most cases, this will be set to point to the
         * same OperatorExpressionImp object as rootExpression.
         * But, in the case of the None collection operator,
         * this will point to a different OperatorExpressionImp
         * object.
         */
        OperatorExpressionImp lastExpression;

        /**
         * A root row that uses the None collection
         * operator is a special case.  It must be
         * turned into TWO operators:  the "not" operator
         * with the "or" operator as its only operand.
         */
        if (rootRow.getCollectionOperator() == CollectionOperator.NONE) {
            rootExpression = new OperatorExpressionImp("not");
            lastExpression = new OperatorExpressionImp("or");
            rootExpression.addOperand(lastExpression);
        }
        else {
            rootExpression = new OperatorExpressionImp(rootRow);
            lastExpression = rootExpression;
        }

        /**
         * At this point, lastExpression is a reference to
         * the OperatorExpressionImp that will have the
         * list of operands added to.
         */
        for (RowData childRow : rootRow.getChildRows()) {
            lastExpression.addOperand(createExpression(childRow));
        }

        return(rootExpression);
    }


    /**
     * Create an Expression from the passed in RowData object.
     * This method is NOT meant to handle the root RowData object.
     */
    private static Expression createExpression(RowData rowData) {

        OperatorExpressionImp expression = null;

        Attribute childmostAttribute = rowData.getChildmostAttribute();

        if (childmostAttribute.getType() == Type.PARAMETERS_MAP) {
            expression = new OperatorExpressionImp(rowData);

            OperatorExpressionImp dotOperand;
            Expression valueOperand;

            dotOperand = new OperatorExpressionImp(".");
            valueOperand = createLiteralValueExpression(rowData.getPropType(),
                                                        rowData);
            expression.addOperand(dotOperand);
            expression.addOperand(valueOperand);

            dotOperand.addOperand(createExpression(rowData.getAttributePath(),
                                  rowData));
            dotOperand.addOperand(new AttributeExpressionImp("value"));
        }
        else if (rowData.getAttributeOperator() != null) {
            expression = new OperatorExpressionImp(rowData);
            expression.addOperand(createExpression(rowData.getAttributePath(),
                                  rowData));
            expression.addOperand(createLiteralValueExpression(rowData));
        }
        else if (rowData.getCollectionOperator() != null) {
            /**
             * A row that uses the None collection
             * operator is a special case.  It must be
             * turned into TWO operators:  the "not" operator
             * with the "or" operator as its only operand.
             */
            OperatorExpressionImp lastExpression;
            if (rowData.getCollectionOperator() == CollectionOperator.NONE) {
                expression = new OperatorExpressionImp("not");
                lastExpression = new OperatorExpressionImp("or");
                expression.addOperand(lastExpression);
            }
            else {
                expression = new OperatorExpressionImp(rowData);
                lastExpression = expression;
            }

            lastExpression.addOperand(
                createExpression(rowData.getAttributePath(), rowData));
            for (RowData childRow : rowData.getChildRows()) {
                lastExpression.addOperand(createExpression(childRow));
            }
        }

        return(expression);
    }


    private static LiteralValueExpression createLiteralValueExpression(
        RowData rowData) {

        return(createLiteralValueExpression(rowData.getChildmostAttribute().
                                            getType(), rowData));
    }
    
    private static LiteralValueExpression createLiteralValueExpression(
        Type type, RowData rowData) {

        LiteralValueExpression literalValueExpression = null;

        Attribute attribute = rowData.getChildmostAttribute();
        switch (type) {

            case BOOLEAN:
                return(new BooleanLiteralValueExpressionImp(
                       ((Boolean)rowData.getAttributeValue()).booleanValue()));

            case UTF_8_STRING:
                return(new StringLiteralValueExpressionImp(
                       rowData.getAttributeValue().toString()));

            case INT_32:
                return(new Int32LiteralValueExpressionImp(
                       ((Integer)rowData.getAttributeValue()).intValue()));

            case FLOAT_64:
                return(new Float64LiteralValueExpressionImp(
                       ((Double)rowData.getAttributeValue()).doubleValue()));

            case DATE_TIME:
                return(new TimeLiteralValueExpressionImp(
                       ((Date)rowData.getAttributeValue())));

            case REFERENCE:
                return(new ClassLiteralValueExpressionImp(
                       attribute.getQueryName()));

            default:
                System.err.println("ERROR:  ExpressionImp."+
                    "createLiteralValueExpression().  Unhandled type.\n"+
                    "Type = "+type+"\n"+
                    "rowData:\n"+rowData.getRowString());
                (new Exception("Unhandled type")).printStackTrace();
                return(null);
        }
    }


    private static ClassLiteralValueExpression
        createClassLiteralValueExpression(Type type) {

        switch (type) {

            case BOOLEAN:
                return(new ClassLiteralValueExpressionImp("BooleanValue"));

            case UTF_8_STRING:
                return(new ClassLiteralValueExpressionImp("StringValue"));

            case INT_16:
                return(new ClassLiteralValueExpressionImp("IntegerValue"));

            case INT_32:
                return(new ClassLiteralValueExpressionImp("IntegerValue"));

            case FLOAT_64:
                return(new ClassLiteralValueExpressionImp(
                    "FloatingPointValue"));

            case DATE_TIME:
                return(new ClassLiteralValueExpressionImp("DateValue"));

            default:
                System.err.println("ERROR:  ExpressionImp."+
                    "createClassLiteralValueExpression().  Unhandled type.\n"+
                    "Type = "+type);
                return(null);
        }
    }


    /**
     * Create an Expression for the passed in attribute, (which is
     * of type PARAMETERS_MAP).
     *
     * For example:
     *
     *      protocolParamters.key(int) == 1
     *
     * becomes:
     *
     *      OperatorExpression(.)
     *        OperatorExpression(as)
     *          OperatorExpression(parameter)
     *            StringLiteralValueExpression(protocolParameters)
     *            StringLiteralValueExpression(key)
     *          OperatorExpression(IntValue)
     *        AttributeExpression(value)
     *
     * This method only returns the "as" sub-tree above.
     */
    private static Expression createExpressionParametersMap(
        Attribute attribute, RowData rowData) {

        //System.out.println("Enter createExpressionParametersMap()");

        /**
         * The comments below assume the passed in attribute and
         * rowData values are like the example described in this
         * method's header comment.
         */

        /**
         * Create the "as" operator and the AttributeExpression(value),
         * and add them as the left and right operands of the "." operator.
         */
        OperatorExpressionImp asOperator = new OperatorExpressionImp("as");

        /**
         * Create the "parameter" operator and the
         * ClassLiteralValueExpression(IntValue), and add them as the left
         * and right operands of the "as" operator.
         */
        OperatorExpressionImp parameterOperator = new OperatorExpressionImp(
            "parameter");
        asOperator.addOperand(parameterOperator);
        asOperator.addOperand(createClassLiteralValueExpression(
                              rowData.getPropType()));

        /**
         * Create the StringLiteralValueExpression operands
         * of "protocolParameters" and "key", and add them
         * as the left and right operands to the parameterOperator.
         */
        parameterOperator.addOperand(new StringLiteralValueExpressionImp(
            attribute.getQueryName()));
        parameterOperator.addOperand(new StringLiteralValueExpressionImp(
                                     rowData.getPropName()));

        return(asOperator);
    }


    /**
     * Create an expression based on the passed in attributePath and
     * RowData object.  This method calls itself recursively to build
     * up the Expression that represents the passed in attributePath.
     * We sometimes use the passed in rowData parameter to know the
     * property type and property name of the last attribute, if the
     * last attribute is of a type where those values are relevant.
     * For example, a PARAMETERS_MAP attribute type.
     *
     * Please note, although the passed in attributePath is "from"
     * the passed in RowData object, it is possibly a sub-list of
     * the RowData's full attributePath.  (This method calls itself
     * recursively, chopping off the last attribute from the
     * attributePath before calling itself.)  So, don't start
     * calling RowData.getAttributePath() unless you really are
     * sure that is what you want.
     */
    private static Expression createExpression(List<Attribute> attributePath,
                                               RowData rowData) {

        //System.out.println("Enter createExpression(List<Attribute>)");
        //System.out.println("attributePath.size() = "+attributePath.size());

        /*
        if (attributePath.size() < 1) {
            return(null);
        }
        */

        /**
         * If the attributePath is, (or has been whittled down to),
         * one attribute long, create the Expression for that one
         * attribute.
         */
        if (attributePath.size() == 1) {

            Attribute attribute = attributePath.get(0);
            if (attribute.getType() == Type.PARAMETERS_MAP) {
                return(createExpressionParametersMap(attribute, rowData));
            }
            else {
                return(new AttributeExpressionImp(attribute.getQueryName()));
            }
        }

        /**
         * If we get here, the attributePath is longer than one
         * attribute.  So, we will use the "." operator to concatenate
         * attributes on the attributePath.
         */
        OperatorExpressionImp expression = new OperatorExpressionImp(".");

        /**
         * Create and add the left operand.  Note, the left operand
         * is the sub-list of all attributes to the left of the the
         * rightmost attribute.  For example, if the attribute
         * path is:
         *
         *      epochGroup.source.label
         *
         * the left operand is made of "epochGroup.source" and the
         * right operand is "label".
         */

        Expression operand;
        List<Attribute> subList;
        subList = attributePath.subList(0, attributePath.size()-1);
        operand = createExpression(subList, rowData);

        //System.out.println("Add left operand.");
        expression.addOperand(operand);

        /**
         * Create and add the right operand.  The right operand
         * is the rightmost attribute in the passed in attributePath.
         */

        subList = attributePath.subList(attributePath.size()-1,
                                        attributePath.size());
        operand = createExpression(subList, rowData);

        //System.out.println("Add right operand: ");
        expression.addOperand(operand);

        return(expression);
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
        RowData rowData2;
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
         * Test an attribute path with two levels.
         * Also test using None collection operator which
         * gets turned into two operators:  "not" with
         * "or" as its only operand.
         *
         *      Epoch None
         *          epochGroup.label == "Test 27"
         *
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
/* Not working yet.
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
*/
        /**
         * Test a compound row:
         *
         *      Epoch | All
         *        Epoch | responses All
         *          Response | uuid == "xyz"
         */
/*
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

        rowData2 = new RowData();
        attribute = new Attribute("uuid", Type.UTF_8_STRING);
        rowData2.addAttribute(attribute);
        rowData2.setAttributeOperator(Operator.EQUALS);
        rowData2.setAttributeValue("xyz");
        rowData.addChildRow(rowData2);

        System.out.println("\nRowData:\n"+rootRow);
        expression = ExpressionImp.createExpressionTree(rootRow);
        System.out.println("\nExpression:\n"+expression);

        /**
         * Test a compound row:
         *
         *      Epoch | None
         *        Epoch | responses None
         *          Response | uuid == "xyz"
         */
/*
        rootRow = new RowData();
        rootRow.setClassUnderQualification(
            DataModel.getClassDescription("Epoch"));
        rootRow.setCollectionOperator(CollectionOperator.NONE);

        rowData = new RowData();
        attribute = new Attribute("responses", Type.REFERENCE,
                                  DataModel.getClassDescription("Response"),
                                  Cardinality.TO_MANY);
        rowData.addAttribute(attribute);
        rowData.setCollectionOperator(CollectionOperator.NONE);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        attribute = new Attribute("uuid", Type.UTF_8_STRING);
        rowData2.addAttribute(attribute);
        rowData2.setAttributeOperator(Operator.EQUALS);
        rowData2.setAttributeValue("xyz");
        rowData.addChildRow(rowData2);

        System.out.println("\nRowData:\n"+rootRow);
        expression = ExpressionImp.createExpressionTree(rootRow);
        System.out.println("\nExpression:\n"+expression);

        /**
         * Test a "Parameters Map" row of type time.
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(
            DataModel.getClassDescription("Epoch"));
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        attribute = new Attribute("protocolParameters", Type.PARAMETERS_MAP,
                                  null, Cardinality.N_A);
        rowData.addAttribute(attribute);
        rowData.setPropName("key");
        rowData.setPropType(Type.DATE_TIME);
        rowData.setAttributeOperator(Operator.EQUALS);
        rowData.setAttributeValue(new Date());
        rootRow.addChildRow(rowData);

        System.out.println("\nRowData:\n"+rootRow);
        expression = ExpressionImp.createExpressionTree(rootRow);
        System.out.println("\nExpression:\n"+expression);

        /**
         * Test a "Parameters Map" row of type float, that
         * has an attributePath that is more than one level deep.
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(
            DataModel.getClassDescription("Epoch"));
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        attribute = new Attribute("nextEpoch", Type.REFERENCE,
                                  DataModel.getClassDescription("Epoch"),
                                  Cardinality.TO_ONE);
        rowData.addAttribute(attribute);
        attribute = new Attribute("nextEpoch", Type.REFERENCE,
                                  DataModel.getClassDescription("Epoch"),
                                  Cardinality.TO_ONE);
        rowData.addAttribute(attribute);
        attribute = new Attribute("prevEpoch", Type.REFERENCE,
                                  DataModel.getClassDescription("Epoch"),
                                  Cardinality.TO_ONE);
        rowData.addAttribute(attribute);
        attribute = new Attribute("protocolParameters", Type.PARAMETERS_MAP,
                                  null, Cardinality.N_A);
        rowData.addAttribute(attribute);
        rowData.setPropName("key");
        rowData.setPropType(Type.FLOAT_64);
        rowData.setAttributeOperator(Operator.EQUALS);
        rowData.setAttributeValue(new Double(12.3));
        rootRow.addChildRow(rowData);

        System.out.println("\nRowData:\n"+rootRow);
        expression = ExpressionImp.createExpressionTree(rootRow);
        System.out.println("\nExpression:\n"+expression);

        System.out.println("\nExpressionImp test is ending.");
    }
}
