package com.physion.ovation.gui.ebuilder.expression;

import java.util.ArrayList;
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
 * "parameter", "my", "elementsOfType", etc. into constants.
 *
 * TODO:  In what class should the declarations of OE_NOT, OE_OR, etc. go?
 *
 * TODO:  Do I want to put all this code into the ExpressionTree class
 * and get rid of the class ExpressionTranslator?
 */
public class ExpressionTranslator {

    public static final String OE_NOT = "not";
    public static final String OE_OR = "or";
    public static final String OE_AND = "and";
    public static final String OE_COUNT = "count";

    public static final String OE_EQUALS = "==";
    public static final String OE_NOT_EQUALS = "!=";

    public static final String OE_IS_NULL = "isnull";


    public static RowData createRowData(ExpressionTree expressionTree) {

        /**
         * First create the root RowData object.
         * It simply contains the CUQ (Class Under Qualification)
         * and a CollectionOperator.
         *
         * Note that mapping an Expression collection operator into
         * a RowData CollectionOperator, is not always one-to-one.
         * These are one-to-one:
         *
         *      "or" -> CollectionOperator.ANY
         *      "and" -> CollectionOperator.ALL
         *
         * but the RowData CollectionOperator.NONE is represented by
         * the Expression tree having a "not" OperatorExpression with
         * an "or" OperatorExpression as its only operand:
         *
         *      "not(or)" -> CollectionOperator.NONE
         */
        RowData rootRow = RowData.createRootRow();
        rootRow.setClassUnderQualification(
            expressionTree.getClassUnderQualification());

        IOperatorExpression oe = (IOperatorExpression)expressionTree.
            getRootExpression();

        rootRow.setCollectionOperator(getCOForOE(oe));

        ArrayList<RowData> childRows = getChildRows(getFirstChildOE(oe));
        rootRow.addChildRows(childRows);

        return(rootRow);
    }


    /**
     * This returns the first operator of the passed in tree that
     * is NOT the operator associated with the root RowData object.
     * For example, if the Expression starts like this:
     *
     *      OperatorExpression("and")
     *        OperatorExpression("==")
     *          ...
     *
     * this method would return the OperatorExpression("==") object.
     * If the Expression starts like this:
     *
     *      OperatorExpression("not")
     *        OperatorExpression("or")
     *          OperatorExpression("==")
     *            ...
     *  
     * this method would return the OperatorExpression("==") object.
     * If the Expression starts like this:
     *
     *      OperatorExpression("and")
     *        OperatorExpression("not")
     *          OperatorExpression("isnull")
     *            ...
     *
     * this method would return the OperatorExpression("not") object.
     *
     * I.e. this method returns the OperatorExpression that
     * will be used to create the first child row of the the
     * GUI's root row.  (Note, the GUI's root row might use
     * one or two of the first operators in the Expression tree,
     * as demonstrated in the examples above.
     */
    private static IOperatorExpression getFirstChildOE(IOperatorExpression oe) {
        
        if (oe.getOperandList().size() < 1)
            return(null);

        if (OE_NOT.equals(oe.getOperatorName())) {
            oe = (IOperatorExpression)(oe.getOperandList().get(0));
            if (oe.getOperandList().size() < 1)
                return(null);
            return((IOperatorExpression)(oe.getOperandList().get(0)));
        }
        else {
            return((IOperatorExpression)(oe.getOperandList().get(0)));
        }
    }


    /**
     * Get the list of child RowData objects that describe the passed
     * in expression tree.  This method calls itself recursively to
     * generate the sub-tree of RowData objects.
     *
     * This method should NOT be called with the OperatorExpression
     * that is the root operator (or operators) of the Expression tree.
     * That operator(s) must be handled a bit differently to generate
     * the root RowData object.  See getFirstChildOE() and the method
     * that calls it for more information about generating the root
     * RowData object in the GUI.
     *
     * This method never returns null, but might return an empty list.
     */
    private static ArrayList<RowData> getChildRows(IOperatorExpression oe) {

        ArrayList<RowData> childRows = new ArrayList<RowData>();

        /**
         * If the oe is null, return an empty list.
         */
        if (oe == null)
            return(childRows);

        ArrayList<IExpression> ol = oe.getOperandList();
        int olIndex = 0;

        RowData rowData = new RowData();

        CollectionOperator collectionOperator = getCOForOE(oe);
        Operator attributeOperator = getAOForOE(oe);

        if (collectionOperator != null) {
            rowData.setCollectionOperator(collectionOperator);
            if (attributeOperator != null) {
                rowData.setAttributeOperator(attributeOperator);
            }
        }
        else {
            if (attributeOperator != null) {
                rowData.setAttributeOperator(attributeOperator);
                IExpression ex = ol.get(olIndex++);

                setAttributePath(rowData, ex);

                if ((attributeOperator != Operator.IS_NULL) &&
                    (attributeOperator != Operator.IS_NOT_NULL) &&
                    (attributeOperator != Operator.IS_TRUE) &&
                    (attributeOperator != Operator.IS_FALSE)) {
                    
                    ex = ol.get(olIndex++);
                    Object attributeValue = getAttributeValue(ex);
                    rowData.setAttributeValue(attributeValue);
                }
            }
        }

        childRows.add(rowData);

        if (olIndex < ol.size()) {
        }

        return(childRows);
    }


    /**
     * TODO: Write this method.
     */
    private static void setAttributePath(RowData rowData, IExpression ex) {

        Attribute attribute;

        attribute = new Attribute("protocolID", Type.UTF_8_STRING);
        rowData.addAttribute(attribute);
    }


    /**
     * Convert the passed in IExpression into an attributeValue
     * that the RowData object expects.
     *
     * TODO:  Write this method.
     */
    private static Object getAttributeValue(IExpression ex) {
        return(new String("someValue"));
    }


    /*
    private static boolean isCollectionOperator(String operatorString) {

        if (OE_OR.equals(operatorString) ||
            OE_NOT.equals(operatorString) ||

    }
    */


    /**
     * Get the CollectionOperator that is equivalent to the passed
     * in OperatorExpression.
     *
     * If the passed in OperatorExpression cannot be mapped to a
     * CollectionOperator, this method returns null.
     */
    private static CollectionOperator getCOForOE(IOperatorExpression oe) {

        if (OE_OR.equals(oe.getOperatorName())) {
            return(CollectionOperator.ANY);
        }
        else if (OE_AND.equals(oe.getOperatorName())) {
            return(CollectionOperator.ALL);
        }
        else if (OE_NOT.equals(oe.getOperatorName())) {
            oe = (IOperatorExpression)(oe.getOperandList().get(0));
            if (OE_OR.equals(oe.getOperatorName())) {
                return(CollectionOperator.NONE);
            }
        }
        else if (OE_COUNT.equals(oe.getOperatorName())) {
            return(CollectionOperator.COUNT);
        }

        //System.err.println("ERROR:  ExpressionTranslator.getCOForOE()"+
        //    "\nCode must be updated to handle this type of expression.");
        return(null);
    }


    /**
     * Get the attribute Operator that is equivalent to the passed
     * in OperatorExpression.
     *
     * If the passed in OperatorExpression cannot be mapped to a
     * attribute Operator, this method returns null.
     */
    private static Operator getAOForOE(IOperatorExpression oe) {

        if (OE_EQUALS.equals(oe.getOperatorName())) {
            return(Operator.EQUALS);
        }
        else if (OE_NOT_EQUALS.equals(oe.getOperatorName())) {
            return(Operator.NOT_EQUALS);
        }
        else if (OE_IS_NULL.equals(oe.getOperatorName())) {
            return(Operator.IS_NULL);
        }
        else if (OE_NOT.equals(oe.getOperatorName())) {
            oe = (IOperatorExpression)(oe.getOperandList().get(0));
            if (OE_IS_NULL.equals(oe.getOperatorName())) {
                return(Operator.IS_NOT_NULL);
            }
            // What if we get here?
        }
        // Do more

        return(null);
    }


    /**
     * Create an Expression from the passed in root RowData.
     * This method should only be called if the passed in
     * RowData object is the rootRow of an expression tree.
     */
    //public static IExpression createExpressionTree(RowData rootRow) {
    public static ExpressionTree createExpressionTree(RowData rootRow) {

        if (rootRow == null) {
            //System.out.println("ExpressionTranslator.createExpression() "+
            //                   "was passed a null rootRow parameter.");
            return(null);
        }

        if (rootRow.isRootRow() == false) {
            System.err.println("ERROR:  "+
                "ExpressionTranslator.createExpression() "+
                "was passed a rootRow parameter that is not really the "+
                "root of an expression tree.");
            return(null);
        }

        /**
         * This is the root Expression object.  I.e. the "top" node of the
         * expression tree.
         */
        OperatorExpression rootExpression;

        /**
         * In most cases, this will be set to point to the
         * same OpertorExpression object as rootExpression.
         * But, in the case of the None collection operator,
         * this will point to a different OpertorExpression
         * object.
         */
        OperatorExpression lastExpression;

        /**
         * A root row that uses the None collection
         * operator is a special case.  It must be
         * turned into TWO operators:  the "not" operator
         * with the "or" operator as its only operand.
         */
        if (rootRow.getCollectionOperator() == CollectionOperator.NONE) {
            rootExpression = new OperatorExpression("not");
            lastExpression = new OperatorExpression("or");
            rootExpression.addOperand(lastExpression);
        }
        else {
            rootExpression = new OperatorExpression(
                getCollectionOperatorName(rootRow));
            lastExpression = rootExpression;
        }

        /**
         * At this point, lastExpression is a reference to
         * the OpertorExpression that will have the
         * list of operands added to.
         */
        for (RowData childRow : rootRow.getChildRows()) {
            lastExpression.addOperand(createExpression(childRow));
        }

        //return(rootExpression);
        ExpressionTree expressionTree = new ExpressionTree(
            rootRow.getClassUnderQualification(), rootExpression);
        return(expressionTree);
    }


    /**
     * Create the operator, or operators, for the passed in
     * RowData object.  This method should only be called on
     * a RowData object that is NOT the root of the tree.
     *
     * Most of the time only one operator needs to be created.
     * For example, the RowData attributeOperators "==", "any",
     * "is null", can all be represented by a single
     * OpertorExpression object.
     *
     * But, some operators need two OpertorExpression
     * objects to represent them.  For example, the "None"
     * CollectionOperator becomes the "not" operator with
     * the "or" operator as its only operand.
     * The "is not null" attribute operator becomes the
     * "not" operator with the "is null" operator as its
     * only operand.
     *
     * So, this method returns an OperatorExpression which
     * might have an operand that is another OperatorExpression.
     * The caller needs to check whether the returned
     * OperatorExpression has an operand.  If it does, then the
     * caller will want to hang the rest of the expression tree
     * of the returned OperatorExpression's first (and only)
     * operand returned from OperatorExpression.getOperandList().
     */
    private static OperatorExpression createOperators(RowData rowData) {

        OperatorExpression op1;
        OperatorExpression op2;

        Attribute childmostAttribute = rowData.getChildmostAttribute();

        if ((childmostAttribute != null) &&
            (childmostAttribute.getType() == Type.PER_USER_PARAMETERS_MAP)) {

            if (childmostAttribute.getIsMine())
                op1 = new OperatorExpression("my");
            else
                op1 = new OperatorExpression("any");
        }
        else if ((childmostAttribute != null) &&
                 (childmostAttribute.getType() == Type.PARAMETERS_MAP)) {

            /**
             * TODO:  Can this case be gotten rid of because it is
             * the same as the default case at the end of this long
             * if/else block?
             */
            op1 = new OperatorExpression(
                rowData.getAttributeOperator().toString());
        }
        else if (rowData.getAttributeOperator() == Operator.IS_NULL) {
            op1 = new OperatorExpression("isnull");
        }
        else if (rowData.getAttributeOperator() == Operator.IS_NOT_NULL) {
            op1 = new OperatorExpression("not");
            op2 = new OperatorExpression("isnull");
            op1.addOperand(op2);
        }
        else if (rowData.getCollectionOperator() != null) {

            if (rowData.getCollectionOperator() == CollectionOperator.COUNT) {
                op1 = new OperatorExpression(rowData.getAttributeOperator().
                                             toString());
                op2 = new OperatorExpression("count");
                op1.addOperand(op2);
            }
            else if (rowData.getCollectionOperator() ==
                     CollectionOperator.NONE) {
                /**
                 * A row that uses the None collection
                 * operator is a special case.  It must be
                 * turned into TWO operators:  the "not" operator
                 * with the "or" operator as its only operand.
                 */
                op1 = new OperatorExpression("not");
                op2 = new OperatorExpression("or");
                op1.addOperand(op2);
            }
            else {
                op1 = new OperatorExpression(
                    getCollectionOperatorName(rowData));
            }
        }
        else {
            op1 = new OperatorExpression(
                rowData.getAttributeOperator().toString());
        }

        return(op1);
    }


    /**
     * This method is only meant to be used on an OpertorExpression
     * object that has just been created by the createOperators() method.
     * It either returns the passed in OpertorExpression object or
     * it returns the one and only operand in its operandList.
     */
    private static OperatorExpression getLastOperator(OperatorExpression
        op) {

        if (op.getOperandList().size() < 1) {
            return(op);
        }
        else {
            return((OperatorExpression)op.getOperandList().get(0));
        }
    }


    /**
     * Convert the passed in RowData's CollectionOperator enum value
     * to the string value PQL expects.
     */
    /*
    private static String getOperatorName(RowData rowData) {

        if (rowData.getCollectionOperator() != null) {
            return(getCollectionOperatorName(rowData));
        }
        else {
            if (rowData.getAttributeOperator() != null) {
                return(rowData.getAttributeOperator().toString());
            }
        }

        return("ERROR");
    }
    */


    /**
     * Get the string that should be used for the CollectionOperator
     * of the passed in rowData.  The mapping of the string displayed
     * in the GUI, (e.g. "Any", "All"), to the string used in the
     * expression tree depends upon whether the passed in rowData
     * is the root row.  For the root row:
     *
     *      Any -> or
     *      All -> and
     *
     * For other rows:
     *
     *      Any -> any
     *      All -> all
     *
     * The None and Count CollectionOperators are not handled by
     * this method.  They are handled elsewhere.  None becomes
     * two operators (not/or), and Count also becomes two operators.
     */
    private static String getCollectionOperatorName(RowData rowData) {

        /**
         * If this is NOT the root row, then the collection
         * operator is simply the lower case version of the
         * collection operator's string value.
         * E.g. CollectionOperator.ALL becomes "all",
         * CollectionOperator.ANY becomes "any".
         */
        if (!rowData.isRootRow()) {
            return(rowData.getCollectionOperator().toString().toLowerCase());
        }

        /**
         * This IS the root row, so the collection operator
         * is handled differently.
         */
        switch (rowData.getCollectionOperator()) {

            case ANY:
                return("or");

            case ALL:
                return("and");

            //case NONE:
            //case COUNT:
            default:
                return("ERROR");  // Should never be called with NONE or COUNT.
        }
    }


    /**
     * Create an Expression from the passed in RowData object.
     * This method is NOT meant to handle the root RowData object.
     */
    private static OperatorExpression createExpression(RowData rowData) {

        OperatorExpression expression;
        OperatorExpression lastOperator;

        Attribute childmostAttribute = rowData.getChildmostAttribute();

        /**
         * Create the operator that is the "top" node that this
         * method will return.  (The returned OpertorExpression
         * will be placed in the operandList of whatever is
         * at the very top of the expression tree.  For example,
         * the very top of the expression tree might be a single
         * collection operator or the "not" operator with a child
         * operand that is the collection operator.
         */
        expression = createOperators(rowData);
        lastOperator = getLastOperator(expression);

        /**
         * At this point, lastOperator is either equal to the
         * same value that is in the expression variable,
         * or it is set to the one and only operand in expression's
         * operandList.
         */

        if (childmostAttribute.getType() == Type.PER_USER_PARAMETERS_MAP) {

            /**
             * As of October 2011, there is only the "properties"
             * attribute that is of this PER_USER_PARAMETERS_MAP
             * type.
             */

            lastOperator.addOperand(createExpression(
                rowData.getAttributePath(), rowData));

            OperatorExpression rightOperator = createOperators(rowData);
            rightOperator.addOperand(new AttributeExpression("value"));
            rightOperator.addOperand(createLiteralValueExpression(
                rowData.getPropType(), rowData));
            lastOperator.addOperand(rightOperator);
        }
        else if (childmostAttribute.getType() == Type.PARAMETERS_MAP) {

            OperatorExpression dotOperand;
            IExpression valueOperand;

            dotOperand = new OperatorExpression(".");
            valueOperand = createLiteralValueExpression(rowData.getPropType(),
                                                        rowData);
            lastOperator.addOperand(dotOperand);
            lastOperator.addOperand(valueOperand);

            dotOperand.addOperand(createExpression(rowData.getAttributePath(),
                                  rowData));
            dotOperand.addOperand(new AttributeExpression("value"));
        }
        else if (rowData.getCollectionOperator() != null) {

            if (rowData.getCollectionOperator() == CollectionOperator.COUNT) {
                /**
                 * This is a row with a Count CollectionOperator like
                 * this:
                 *
                 *      responses Count == 27
                 */
                lastOperator.addOperand(
                    createExpression(rowData.getAttributePath(), rowData));
                expression.addOperand(createLiteralValueExpression(
                    Type.INT_32, rowData));
            }
            else {
                /**
                 * This is an Any/All/None row.
                 */
                lastOperator.addOperand(
                    createExpression(rowData.getAttributePath(), rowData));
                for (RowData childRow : rowData.getChildRows()) {
                    lastOperator.addOperand(createExpression(childRow));
                } 
            }
        }
        else if (rowData.getAttributeOperator() != null) {

            lastOperator.addOperand(createExpression(rowData.getAttributePath(),
                                    rowData));
            if ((rowData.getAttributeOperator() != Operator.IS_NULL) &&
                (rowData.getAttributeOperator() != Operator.IS_NOT_NULL)) {
                lastOperator.addOperand(createLiteralValueExpression(rowData));
            }
        }

        return(expression);
    }


    /**
     * Create a LiteralValueExpression of the appropriate subclass
     * based on the passed in rowData.
     */
    private static ILiteralValueExpression createLiteralValueExpression(
        RowData rowData) {

        return(createLiteralValueExpression(rowData.getChildmostAttribute().
                                            getType(), rowData));
    }
    

    /**
     * Create a LiteralValueExpression of the appropriate subclass
     * based on the passed in type.  The value of the LiteralValueExpression
     * comes from the passed in rowData.
     */
    private static ILiteralValueExpression createLiteralValueExpression(
        Type type, RowData rowData) {

        Attribute attribute = rowData.getChildmostAttribute();
        switch (type) {

            case BOOLEAN:
                return(new BooleanLiteralValueExpression(
                       ((Boolean)rowData.getAttributeValue()).booleanValue()));

            case UTF_8_STRING:
                return(new StringLiteralValueExpression(
                       rowData.getAttributeValue().toString()));

            case INT_16:
                /**
                 * Note that we change the INT_16 to an INT_32.
                 * As of October 15, 2011, there is no
                 * Int16LiteralValueExpression.
                 */
                return(new Int32LiteralValueExpression(
                       ((Integer)rowData.getAttributeValue()).intValue()));

            case INT_32:
                return(new Int32LiteralValueExpression(
                       ((Integer)rowData.getAttributeValue()).intValue()));

            case FLOAT_64:
                return(new Float64LiteralValueExpression(
                       ((Double)rowData.getAttributeValue()).doubleValue()));

            case DATE_TIME:
                return(new TimeLiteralValueExpression(
                       ((Date)rowData.getAttributeValue())));

            case REFERENCE:
                return(new ClassLiteralValueExpression(
                       attribute.getQueryName()));

            default:
                System.err.println("ERROR:  ExpressionTranslator."+
                    "createLiteralValueExpression().  Unhandled type.\n"+
                    "Type = "+type+"\n"+
                    "rowData:\n"+rowData.getRowString());
                (new Exception("Unhandled type")).printStackTrace();
                return(null);
        }
    }


    private static IClassLiteralValueExpression
        createClassLiteralValueExpression(Type type) {

        String name = "ovation.";

        switch (type) {

            case BOOLEAN:
                name += "BooleanValue";
            break;

            case UTF_8_STRING:
                name += "StringValue";
            break;

            case INT_16:
                name += "IntegerValue";
            break;

            case INT_32:
                name += "IntegerValue";
            break;

            case FLOAT_64:
                name += "FloatingPointValue";
            break;

            case DATE_TIME:
                name += "DateValue";
            break;

            default:
                System.err.println("ERROR:  ExpressionTranslator."+
                    "createClassLiteralValueExpression().  Unhandled type.\n"+
                    "Type = "+type);
                return(null);
        }

        return(new ClassLiteralValueExpression(name));
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
    private static IExpression createExpressionParametersMap(
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
        OperatorExpression asOperator = new OperatorExpression("as");

        /**
         * Create the "parameter" operator and the
         * ClassLiteralValueExpression(IntValue), and add them as the left
         * and right operands of the "as" operator.
         */
        OperatorExpression parameterOperator = new OperatorExpression(
            "parameter");
        asOperator.addOperand(parameterOperator);
        asOperator.addOperand(createClassLiteralValueExpression(
                              rowData.getPropType()));

        /**
         * Create the StringLiteralValueExpression operands
         * of "protocolParameters" and "key", and add them
         * as the left and right operands to the parameterOperator.
         */
        //parameterOperator.addOperand(new StringLiteralValueExpression(
        parameterOperator.addOperand(new AttributeExpression(
            attribute.getQueryName()));
        parameterOperator.addOperand(new StringLiteralValueExpression(
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
    private static IExpression createExpression(List<Attribute> attributePath,
                                               RowData rowData) {

        //System.out.println("Enter createExpression(List<Attribute>)");
        //System.out.println("attributePath.size() = "+attributePath.size());

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
            else if (attribute.getType() == Type.PER_USER_PARAMETERS_MAP) {
                OperatorExpression leftOperator =
                    new OperatorExpression("elementsOfType");
                OperatorExpression nameOperator =
                    new OperatorExpression(attribute.getQueryName());
                nameOperator.addOperand(new StringLiteralValueExpression(
                    rowData.getPropName()));
                leftOperator.addOperand(nameOperator);
                leftOperator.addOperand(createClassLiteralValueExpression(
                                        rowData.getPropType()));
                //myAnyOperator.addOperand(leftOperator);
                return(leftOperator);
            }
            else if (attribute.getType() == Type.PER_USER) {

                String queryName = attribute.getQueryName();
                if (attribute.getIsMine() == true)
                    queryName = "my"+queryName;
                return(new AttributeExpression(queryName));
            }
            else {
                return(new AttributeExpression(attribute.getQueryName()));
            }
        }

        /**
         * If we get here, the attributePath is longer than one
         * attribute.  So, we will use the "." operator to concatenate
         * attributes on the attributePath.
         */
        OperatorExpression expression = new OperatorExpression(".");

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

        IExpression operand;
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
     * This is a simple test program for this class.
     */
    public static void main(String[] args) {

        Attribute attribute;
        RowData rowData;
        //RowData rowData2;
        RowData rootRow;
        //IExpression expression;
        ExpressionTree expression;

        System.out.println("ExpressionTranslator test is starting...");

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
        expression = ExpressionTranslator.createExpressionTree(rootRow);
        System.out.println("\nRowData:\n"+rootRow);
        System.out.println("\nExpression:\n"+expression);

        /**
         * Test the All collection operator, (which becomes "and"),
         * and the Boolean type.
         */
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

        expression = ExpressionTranslator.createExpressionTree(rootRow);
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

        expression = ExpressionTranslator.createExpressionTree(rootRow);
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

        expression = ExpressionTranslator.createExpressionTree(rootRow);
        System.out.println("\nRowData:\n"+rootRow);
        System.out.println("\nExpression:\n"+expression);

        /**
         * Test a reference value for null.
         *
         *      Epoch | All
         *        Epoch | owner is null
         */
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
        expression = ExpressionTranslator.createExpressionTree(rootRow);
        System.out.println("\nExpression:\n"+expression);

        /**
         * Test a reference value for not null.
         *
         *      Epoch | All
         *        Epoch | owner is not null
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(
            DataModel.getClassDescription("Epoch"));
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        attribute = new Attribute("owner", Type.REFERENCE,
                                  DataModel.getClassDescription("User"),
                                  Cardinality.TO_ONE);
        rowData.addAttribute(attribute);
        rowData.setAttributeOperator(Operator.IS_NOT_NULL);
        rootRow.addChildRow(rowData);

        System.out.println("\nRowData:\n"+rootRow);
        expression = ExpressionTranslator.createExpressionTree(rootRow);
        System.out.println("\nExpression:\n"+expression);

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
        expression = ExpressionTranslator.createExpressionTree(rootRow);
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
        expression = ExpressionTranslator.createExpressionTree(rootRow);
        System.out.println("\nExpression:\n"+expression);

        /**
         * Test a compound row:
         *
         *      Epoch | All
         *        Epoch | All keywords None
         *          KeywordTag | uuid == "xyz"
         */
/*
        rootRow = new RowData();
        rootRow.setClassUnderQualification(
            DataModel.getClassDescription("Epoch"));
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        attribute = new Attribute("keywords", Type.PER_USER,
                                  DataModel.getClassDescription("KeywordTag"),
                                  Cardinality.TO_MANY);
        attribute.setIsMine(false);
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
        expression = ExpressionTranslator.createExpressionTree(rootRow);
        System.out.println("\nExpression:\n"+expression);

        /**
         * Test a compound row:
         *
         *      Epoch | All
         *        Epoch | My keywords All
         *          KeywordTag | uuid == "xyz"
         */
/*
        rootRow = new RowData();
        rootRow.setClassUnderQualification(
            DataModel.getClassDescription("Epoch"));
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        attribute = new Attribute("nextEpoch", Type.REFERENCE,
                                  DataModel.getClassDescription("Epoch"),
                                  Cardinality.TO_ONE);
        rowData.addAttribute(attribute);
        attribute = new Attribute("keywords", Type.PER_USER,
                                  DataModel.getClassDescription("KeywordTag"),
                                  Cardinality.TO_MANY);
        attribute.setIsMine(true);
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
        expression = ExpressionTranslator.createExpressionTree(rootRow);
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
        expression = ExpressionTranslator.createExpressionTree(rootRow);
        System.out.println("\nExpression:\n"+expression);

        /**
         * Test a "Parameters Map" row of type float, that
         * has an attributePath that is more than one level deep.
         */
/*
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
        expression = ExpressionTranslator.createExpressionTree(rootRow);
        System.out.println("\nExpression:\n"+expression);

        /**
         * Test a "Per User Parameters Map" row.
         */
/*
        rootRow = new RowData();
        rootRow.setClassUnderQualification(
            DataModel.getClassDescription("Epoch"));
        rootRow.setCollectionOperator(CollectionOperator.ANY);

        rowData = new RowData();
        attribute = new Attribute("nextEpoch", Type.REFERENCE,
                                  DataModel.getClassDescription("Epoch"),
                                  Cardinality.TO_ONE);
        rowData.addAttribute(attribute);
        attribute = new Attribute("properties", Type.PER_USER_PARAMETERS_MAP,
                                  null, Cardinality.N_A);
        rowData.addAttribute(attribute);
        rowData.setPropName("someKey");
        rowData.setPropType(Type.INT_32);
        rowData.setAttributeOperator(Operator.NOT_EQUALS);
        rowData.setAttributeValue(new Integer(34));
        rootRow.addChildRow(rowData);

        System.out.println("\nRowData:\n"+rootRow);
        expression = ExpressionTranslator.createExpressionTree(rootRow);
        System.out.println("\nExpression:\n"+expression);
*/
        System.out.println("\nExpressionTranslator test is ending.");
    }
}
