package com.physion.ovation.gui.ebuilder.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import com.physion.ovation.gui.ebuilder.ExpressionBuilder;
import com.physion.ovation.gui.ebuilder.datatypes.CollectionOperator;
import com.physion.ovation.gui.ebuilder.datatypes.ClassDescription;
import com.physion.ovation.gui.ebuilder.datatypes.Attribute;
import com.physion.ovation.gui.ebuilder.datatypes.Cardinality;
import com.physion.ovation.gui.ebuilder.datatypes.Type;
import com.physion.ovation.gui.ebuilder.datatypes.Operator;
import com.physion.ovation.gui.ebuilder.datamodel.RowData;
import com.physion.ovation.gui.ebuilder.datamodel.DataModel;


/**
 * TODO:  Split the Expression to RowData translator
 * code into a separate class from the
 * RowData to Expression translator code.
 */
public class ExpressionTranslator {

    public static final String CLVE_BOOLEAN = "ovation.BooleanValue";
    public static final String CLVE_STRING = "ovation.StringValue";
    public static final String CLVE_INTEGER = "ovation.IntegerValue";
    public static final String CLVE_FLOAT = "ovation.FloatingPointValue";
    public static final String CLVE_DATE = "ovation.DateValue";

    public static final String AE_VALUE = "value";
    public static final String AE_THIS = "this";

    public static final String OE_NOT = "not";
    public static final String OE_OR = "or";
    public static final String OE_AND = "and";
    public static final String OE_ALL = "all";
    public static final String OE_COUNT = "count";
    public static final String OE_AS = "as";
    public static final String OE_PARAMETER = "parameter";
    //public static final String OE_MY = "my";
    public static final String OE_ANY = "any";
    public static final String OE_ELEMENTS_OF_TYPE = "elementsOfType";

    public static final String OE_EQUALS = "==";
    public static final String OE_NOT_EQUALS = "!=";
    public static final String OE_LESS_THAN = "<";
    public static final String OE_GREATER_THAN = ">";
    public static final String OE_LESS_THAN_EQUALS = "<=";
    public static final String OE_GREATER_THAN_EQUALS = ">=";
    public static final String OE_MATCHES_CASE_SENSITIVE = "=~";
    public static final String OE_MATCHES_CASE_INSENSITIVE = "=~~";
    public static final String OE_DOES_NOT_MATCH_CASE_SENSITIVE = "!~";
    public static final String OE_DOES_NOT_MATCH_CASE_INSENSITIVE = "!~~";

    public static final String OE_IS_NULL = "isnull";
    // Note there is no OE_IS_NOT_NULL value.
    public static final String OE_DOT = ".";


    /**
     * This method turns the passed in ExpressionTree into a
     * RowData object.
     *
     * To turn a a RowData object into an ExpressionTree, use
     * the createExpressionTree() method.
     */
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

        /**
         * Convert the oe into a list of child RowData objects and
         * add them to the rootRow.
         */
         /*
        ArrayList<RowData> childRows = createChildRows(getFirstChildOE(oe),
            expressionTree.getClassUnderQualification());
        rootRow.addChildRows(childRows);
        */
        //createAndAddChildRows(rootRow, getFirstChildOE(oe),
        //    expressionTree.getClassUnderQualification());

        if (OE_NOT.equals(oe.getOperatorName())) {
            if ((oe.getOperandList().size() < 1) ||
                (!(oe.getOperandList().get(0) instanceof
                   IOperatorExpression))) {

                String s = "IOperatorExpression(not) without an operand "+
                    "that is an IOperatorExpression.  A \"not\" operator "+
                    "at the root of an expression tree should have a single "+
                    "operand that is an IOperatorExpression of with an "+
                    "operator name of:  \"or\" or \"and\".";
                throw(new IllegalArgumentException(s));
            }

            oe = (IOperatorExpression)oe.getOperandList().get(0);
        }

        ArrayList<IExpression> operandList = oe.getOperandList();
        for (IExpression ex : operandList) {
            if (!(ex instanceof IOperatorExpression)) {
                String s = "Root IOperatorExpression("+oe.getOperatorName()+
                    ") had an operand that was not an IOperatorExpression().";
                throw(new IllegalArgumentException(s));
            }

            createAndAddChildRows(rootRow, (IOperatorExpression)ex,
                                  expressionTree.getClassUnderQualification());
        }

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
/*
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
*/

    /**
     * Create the list of child RowData objects that describe the passed
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
     *
     * @param classDescription The ClassDescription that is the "parent"
     * class for all the child rows that will be created.  So, if we
     * are creating child rows for the topmost row in the GUI, this
     * would be the Class Under Qualification.
     */
    private static ArrayList<RowData> createChildRows(IOperatorExpression oe,
        ClassDescription classDescription) {

        /*
        System.out.println("\nEnter createChildRows()");
        System.out.println("oe: "+(Expression)oe);
        System.out.println("classDescription: "+classDescription);
        */

        ArrayList<RowData> childRows = new ArrayList<RowData>();

        /**
         * If the oe is null, return an empty list.
         */
        if (oe == null)
            return(childRows);

        ArrayList<IExpression> ol = oe.getOperandList();

        RowData rowData = new RowData();

        CollectionOperator collectionOperator = getCOForOE(oe);
        //System.out.println("collectionOperator = "+collectionOperator);

        /**
         * Handle the special case of the Count collection operator.
         */
        if (collectionOperator == null) {
            if (oe.getOperandList().size() > 0) {
                IExpression ex = oe.getOperandList().get(0);
                if (ex instanceof IOperatorExpression) {
                    IOperatorExpression oe2 = (IOperatorExpression)ex;
                    if (OE_COUNT.equals(oe2.getOperatorName())) {
                        collectionOperator = getCOForOE(oe2);
                    }
                }
            }
        }

        if (collectionOperator == CollectionOperator.NONE) {
            /**
             * We need to "skip ahead" one operator because
             * the NONE collection operator is implemented in
             * the Expression syntax as two OperatorExpressions:
             *
             *      OperatorExpression(not)
             *        OperatorExpression(or)
             */
            ol = ((IOperatorExpression)ol.get(0)).getOperandList();
        }

        Operator attributeOperator = getAOForOE(oe);
        //System.out.println("attributeOperator = "+attributeOperator);

        IExpression tempEx = ol.get(0);
        if ((collectionOperator != null) &&
            isElementsOfTypeOperator(tempEx, classDescription)) {
            /**
             * We got a collection operator whose first operand
             * is IOperatorExpression(elementsOfType).  That means
             * it is the start of a PER_USER_PARAMETERS_MAP Attribute 
             * such as "My Property" (myproperties) or
             * "Any Property" (properties).
             *
             * For example, the RowData below:
             *
             *      Any Property.someKey(int) != "34"
             *
             * is equivalent to the Expression:
             *
             *      OperatorExpression(and)
             *        OperatorExpression(any)
             *          OperatorExpression(elementsOfType)
             *            OperatorExpression(properties)
             *              AttributeExpression(this)
             *              StringLiteralValueExpression(someKey)
             *            ClassLiteralValueExpression(ovation.IntegerValue)
             *          OperatorExpression(!=)
             *            AttributeExpression(value)
             *            Int32LiteralValueExpression(34)
             *
             * A more complicated RowData example with nesting:
             *
             *  nextEpoch.nextEpoch.prevEpoch.Any Property.someKey(int) != "34"
             *
             * is equivalent to the Expression:
             *
             *  OperatorExpression(and)
             *    OperatorExpression(any)
             *      OperatorExpression(elementsOfType)
             *        OperatorExpression(properties)
             *          OperatorExpression(.)
             *            OperatorExpression(.)
             *              AttributeExpression(nextEpoch)
             *              AttributeExpression(nextEpoch)
             *            AttributeExpression(prevEpoch)
             *          StringLiteralValueExpression(someKey)
             *        ClassLiteralValueExpression(ovation.IntegerValue)
             *      OperatorExpression(!=)
             *        AttributeExpression(value)
             *        Int32LiteralValueExpression(34)
             *
             * Note, as of October 2011, the collectionOperator will
             * always be "any".  It will have two operands.  The first
             * is IOperatorExpression(elementsOfType), which will have
             * the "properties" or "myproperties" IOperatorExpression
             * as its first operand and IClassLiteralValueExpression
             * as its second.
             *
             * The second operand for the "any" collectionOperator
             * will be an IOperatorExpression with AttributeExpression(value)
             * as its first operand and some LiteralValueExpression as its
             * second operand.
             */
            //System.out.println("This is a PER_USER_PARAMETERS_MAP exp.");

            /**
             * If we get here, tempEx is IOperatorExpression(elementsOfType).
             * It should have two operands.  The first is the Attribute (and
             * the path to the Attribute).  The second is a
             * IClassLiteralValueExpression(ovation.<someType>.
             * Where <someType> is a value like: ovation.DateValue,
             * ovation.FloatingPointValue, ovation.IntegerValue, etc.
             */

            /**
             * First, because it is simple and easy, handle
             * the IOperatorExpression(elementsOfType) node's second
             * operand which gives us the property type.
             */
            
            IOperatorExpression oeElementsOfType = (IOperatorExpression)tempEx;
            IExpression exTemp = oeElementsOfType.getOperandList().get(1);
            if (!(exTemp instanceof IClassLiteralValueExpression)) {
                String s = "IOperatorExpression(elementsOfType)'s second "+
                    "operand is not of type "+
                    "IClassLiteralValueExpression.";
                throw(new IllegalArgumentException(s));
            }

            IClassLiteralValueExpression clve =
                (IClassLiteralValueExpression)exTemp;
            Type type = getTypeForCLVE(clve);
            rowData.setPropType(type);

            /**
             * Now deal with the IOperatorExpression(elementsOfType) node's
             * first operand.
             */

            exTemp = oeElementsOfType.getOperandList().get(0);
            if (!(exTemp instanceof IOperatorExpression)) {
                String s = "IOperatorExpression(elementsOfType) does not have "+
                    "an IOperatorExpression as its first "+
                    "operand.";
                throw(new IllegalArgumentException(s));
            }

            IOperatorExpression oeAttributePath = (IOperatorExpression)exTemp;

            /**
             * At this point, oeAttributePath tells us the
             * name of the Attribute, the "path" to it, and
             * the key that it uses.
             * 
             * oeAttributePath contains the name of the Attribute.
             * Its first operand is the "path" to the attribute.
             *
             * Its second operand is the property "key".
             */

            if (oeAttributePath.getOperandList().size() != 2) {
                String s = "PER_USER_PARAMETERS_MAP IOperatorExpression("+
                    oeAttributePath.getOperatorName()+
                    ") does not have two operands.";
                throw(new IllegalArgumentException(s));
            }

            exTemp = oeAttributePath.getOperandList().get(0);
            appendToAttributePath(rowData, oeAttributePath, classDescription);

            /**
             * Now turn the second operand into the property name/key
             * for the row.
             */

            exTemp = oeAttributePath.getOperandList().get(1);
            if (!(exTemp instanceof IStringLiteralValueExpression)) {
                String s = "PER_USER_PARAMETERS_MAP "+
                    "IOperatorExpression("+
                    oeAttributePath.getOperatorName()+
                    ") is not of type IStringLiteralValueExpression.";
                throw(new IllegalArgumentException(s));
            }

            IStringLiteralValueExpression slve =
                (IStringLiteralValueExpression)exTemp;
            rowData.setPropName(slve.getValue().toString());

            /**
             * Set the attributeOperator and (possibly) the
             * attributeValue.
             */

            tempEx = ol.get(1);
            if (!(tempEx instanceof IOperatorExpression)) {
                String s = "Operand after the IOperatorExpression("+
                    "elementsOfType) operand is not of type "+
                    "IOperatorExpression. It is: "+tempEx;
                throw(new IllegalArgumentException(s));
            }

            oe = (IOperatorExpression)tempEx;
            attributeOperator = getAOForOE(oe);
            if ((attributeOperator != Operator.IS_NULL) &&
                (attributeOperator != Operator.IS_NOT_NULL) &&
                (attributeOperator != Operator.IS_TRUE) &&
                (attributeOperator != Operator.IS_FALSE)) {

                ArrayList<IExpression> operandList = oe.getOperandList();
                ILiteralValueExpression lve;
                lve = (ILiteralValueExpression)operandList.get(1);
                Attribute attribute = rowData.getChildmostAttribute();
                Object attributeValue = createAttributeValue(
                    lve, attribute.getType());
                rowData.setAttributeValue(attributeValue);
            }

            //System.out.println("Calling rowData.setAttributeOperator");
            rowData.setAttributeOperator(attributeOperator);
        }
        else if (collectionOperator != null) {

            rowData.setCollectionOperator(collectionOperator);

            if (attributeOperator != null) {
                setAttributeOperatorPathAndValue(rowData, ol, classDescription,
                                                 attributeOperator);
            }

            if (collectionOperator.isCompoundOperator()) {

                int olIndex = 0;
                ClassDescription childClass = classDescription;
                if (ol.size() > 1) {
                    /**
                     * Convert the first operand into a RowData
                     * attributePath.
                     */
                    IExpression firstOperand = ol.get(olIndex++);
                    setAttributePath(rowData, firstOperand, classDescription);
                    //System.out.println("rowData so far: "+
                    //    rowData.getRowString());
                    Attribute childmostAttribute =
                        rowData.getChildmostAttribute();
                    childClass = childmostAttribute.getClassDescription();
                }
                //System.out.println("childClass: "+childClass);

                /**
                 * When we get here, we know that the row is one
                 * of three kinds:
                 *
                 * Type 1)
                 *
                 *      The first operand was an Expression that told
                 *      us what attribute is being queried.  The operands
                 *      AFTER the first operand are the Expressions that 
                 *      are being tested.  TODO:  Add example.
                 *
                 * Type 2)
                 *
                 *      The first operand was an Expression that told
                 *      us what attribute is being queried.  ("responses"
                 *      in the example below.)  The second
                 *      operand is another collection operator that will
                 *      become the row's second collection operator.
                 *      (The not(or) operators in the example below.)
                 *      
                 *      OperatorExpression(not)
                 *        OperatorExpression(any)
                 *          AttributeExpression(responses)
                 *          OperatorExpression(not)
                 *            OperatorExpression(or)
                 *              OperatorExpression(==)
                 *                AttributeExpression(uuid)
                 *                StringLiteralValueExpression(xyz)
                 *
                 *      will become the RowData
                 *
                 *          responses None have None
                 *              uuid == xyz
                 *
                 * Type 3)
                 *
                 *      The row is a compound row that only has a
                 *      collection operator in it.
                 *      For example, the row says:  "Any of the following",
                 *      "All of the following", "None of the following".
                 *      So, there is a list of operands that are the
                 *      children of this Any/All/None collection operator.
                 */
                IExpression secondOperand = ol.get(olIndex);
                //System.out.println("Second operand = "+
                //    ((Expression)secondOperand).toString(""));

                if (!(secondOperand instanceof IOperatorExpression)) {
                    String s = "Second operand is "+secondOperand+
                        ".  It should be an IOperatorExpression.";
                    throw(new IllegalArgumentException(s));
                }

                IOperatorExpression oe2 = (IOperatorExpression)secondOperand;
                //System.out.println("oe2 = "+oe.getOperatorName());
                CollectionOperator collectionOperator2 = getCOForOE(oe2);
                //System.out.println("collectionOperator2 = "+
                //                   collectionOperator2);

                Attribute childmost = rowData.getChildmostAttribute();
                //System.out.println("childmost = "+childmost);

                if (collectionOperator2 == null) {
                    /**
                     * Type 1 described above.
                     */
                    //System.out.println("Type 1");
                }
                else if ((childmost != null) &&
                         ((childmost.getType() != Type.PARAMETERS_MAP) &&
                          (childmost.getType() != Type.PER_USER_PARAMETERS_MAP))) {
                    /**
                     * Type 2 described above.
                     */
                    //System.out.println("Type 2");
                    rowData.setCollectionOperator2(collectionOperator2);

                    if (OE_NOT.equals(oe2.getOperatorName())) {
                        if ((oe2.getOperandList().size() < 1) ||
                            (!(oe2.getOperandList().get(0) instanceof
                             IOperatorExpression))) {

                            String s = "An IOperatorExpression(not) does not "+
                                "have an IOperatorExpression of some type "+
                                "as its one and only operand.";
                            throw(new IllegalArgumentException(s));
                        }
                        IExpression ex = oe2.getOperandList().get(0);
                        oe2 = (IOperatorExpression)ex;
                    }
                    ol = oe2.getOperandList();
                    olIndex = 0;
                }
                else {
                    /**
                     * Type 3 described above.
                     */
                    //System.out.println("Type 3");
                }

                /**
                 * For all three types described above,
                 * process the operands.
                 * Note that the olIndex has been set above
                 * somewhere to either 0 or 1.
                 */
                for (; olIndex < ol.size(); olIndex++) {

                    IOperatorExpression operand =
                        (IOperatorExpression)ol.get(olIndex);
                    createAndAddChildRows(rowData, operand, childClass);
                }
            }
        }
        else {
            if (attributeOperator != null) {
                /**
                 * TODO: Perhaps put this "if (attributeOperator != null)"
                 * block outside of this else block.  The same code
                 * is executed above also.
                 */
                setAttributeOperatorPathAndValue(rowData, ol, classDescription,
                                                 attributeOperator);
                //rowData.setAttributeOperator(attributeOperator);
            }
        }

        childRows.add(rowData);

        return(childRows);
    }


    /**
     * This method will set the attributeOperator, attributePath,
     * and attributeValue of the passed in rowData based on the
     * passed in values.
     */
    private static void setAttributeOperatorPathAndValue(RowData rowData,
        ArrayList<IExpression> operandList, ClassDescription classDescription,
        Operator attributeOperator) {

        //System.out.println("Enter setAttributeOperatorPathAndValue");
        //System.out.println("attributeOperator: "+attributeOperator);

        /**
         * Convert the first (left) operand into a RowData
         * attributePath.
         */
        IExpression ex = operandList.get(0);
        if (ex instanceof IOperatorExpression) {

            String operatorName = ((IOperatorExpression)ex).getOperatorName();
            if (OE_IS_NULL.equals(operatorName)) {

                // TODO: Throw exception if no operand.
                ex = ((IOperatorExpression)ex).getOperandList().get(0);
            }
        }
        setAttributePath(rowData, ex, classDescription);

        /**
         * Now handle the second (right) operand.
         */
        if ((attributeOperator != Operator.IS_NULL) &&
            (attributeOperator != Operator.IS_NOT_NULL) &&
            (attributeOperator != Operator.IS_TRUE) &&
            (attributeOperator != Operator.IS_FALSE)) {
            
            ILiteralValueExpression lve;
            lve = (ILiteralValueExpression)operandList.get(1);
            Attribute attribute = rowData.getChildmostAttribute();
            Object attributeValue = createAttributeValue(
                lve, attribute.getType());
            rowData.setAttributeValue(attributeValue);
        }

        //System.out.println("Calling rowData.setAttributeOperator");
        rowData.setAttributeOperator(attributeOperator);
    }


    /**
     * @param rowData The row whose child rows we will create.
     * We also use this to get the "parent" class used to interpret
     * values in the oe's operand list.
     *
     * @param oe The IOperatorExpression whose list of operands will
     * define the RowData children we create.
     */
    private static void createAndAddChildRows(RowData rowData,
        IOperatorExpression oe, ClassDescription classDescription) {

        ArrayList<RowData> childRows = createChildRows(oe, classDescription);
        rowData.addChildRows(childRows);
    }


    /**
     * Set the value of the rowData's attributePath to be the
     * equivalent of the value in the IExpression.
     *
     * TODO: Get rid of this method.
     *
     * @param rowData The RowData object whose attributePath we will set.
     *
     * @param ex The IExpression that is the left operand of an operator.
     */
    private static void setAttributePath(RowData rowData, IExpression ex,
        ClassDescription classDescription) {

        /*
        System.out.println("Enter ExpressionTranslator.setAttributePath");
        System.out.println("rowData: "+rowData.getRowString());
        System.out.println("classDescription: "+classDescription);
        */

        /*
        ArrayList<Attribute> attributePath = new ArrayList<Attribute>();

        appendToAttributePath(rowData, attributePath, ex, classDescription);

        for (Attribute attribute : attributePath) {
            rowData.addAttribute(attribute);
        }
        */
        appendToAttributePath(rowData, ex, classDescription);
    }


    /**
     * This returns true if the passed in IExpression is
     * a PER_USER Attribute like "keywords", "mykeywords", etc.
     */
    private static boolean isPerUserOperator(IExpression ex,
                                             ClassDescription cd) {

        if (ex instanceof IOperatorExpression) {

            IOperatorExpression oe = (IOperatorExpression)ex;
            String name = oe.getOperatorName();

            Attribute attribute = cd.getAttribute(name);
            if ((attribute != null) &&
                (attribute.getType() == Type.PER_USER)) {
                return(true);
            }
        }

        return(false);
    }


    /**
     * This returns true if the passed in IExpression is
     * a PER_USER_PARAMETERS_MAP Attribute like "properties",
     * "myproperties", etc.
     */
    private static boolean isPerUserParametersMapOperator(IExpression ex,
                                                          ClassDescription cd) {

        if (ex instanceof IOperatorExpression) {

            IOperatorExpression oe = (IOperatorExpression)ex;
            String name = oe.getOperatorName();

            Attribute attribute = cd.getAttribute(name);
            if ((attribute != null) &&
                (attribute.getType() == Type.PER_USER_PARAMETERS_MAP)) {
                return(true);
            }
        }

        return(false);
    }


    /**
     * This returns true if the passed in IExpression is
     * the parent of a PER_USER_PARAMETERS_MAP Attribute like "properties"
     * or "myproperties".
     *
     * Note, we know that this IExpression tree is a
     * PER_USER_PARAMETERS_MAP tree if it is an
     * IOperatorExpression(elementsOfType).  If that is
     * the case, then the IOperatorExpression that is its
     * first operand, (e.g. IOperatorExpression(myproperties)),
     * is an Attribute of type PER_USER_PARAMETERS_MAP.
     * So, we don't actually check the child at this point.
     *
     * Note, as of October 2011, the only Attributes of
     * Type.PER_USER_PARAMETERS_MAP are "properties" and
     * "myproperties".
     */
    private static boolean isElementsOfTypeOperator(IExpression ex,
                                                    ClassDescription cd) {

        if (ex instanceof IOperatorExpression) {

            IOperatorExpression oe = (IOperatorExpression)ex;
            String name = oe.getOperatorName();

            if (OE_ELEMENTS_OF_TYPE.equals(name))
                return(true);
        }

        return(false);
    }


    /**
     * Append the passed in IExpression to the passed in
     * rowData's attributePath.
     * This method converts the passed in IExpression tree
     * into Attributes and adds them to the rowData's attributePath.
     *
     * Please note, this method calls itself recursively.
     * So, for each RowData object, (i.e. each row in the GUI),
     * this method is only called once by another function,
     * but after that initial call with the node that is
     * the "top" of the attribute path part of the expression
     * passed in as the "ex" parameter, this method calls itself
     * recursively to create the entire path.
     *
     * @param ex This is the subtree that defines the attribute path.
     *
     * @param classDescription This is the "parent" class that
     * is the class of the leftmost Attribute of the path.
     *
     * @return The ClassDescription of the leftmost Attribute
     * on which this method is currently working.  If the
     * Attribute is a primitive, (e.g. int, string), this returns null.
     */
    private static ClassDescription appendToAttributePath(RowData rowData,
        IExpression ex, ClassDescription classDescription) {

        /*
        System.out.println("Enter appendToAttributePath");
        System.out.println("rowData: "+rowData.getRowString());
        System.out.println("ex: "+((Expression)ex));
        System.out.println("classDescription: "+classDescription);
        */

        if ((ex instanceof IAttributeExpression) ||
            (isPerUserOperator(ex, classDescription)) ||
            (isPerUserParametersMapOperator(ex, classDescription))) {

            String name;
            if (ex instanceof IAttributeExpression) {
                IAttributeExpression ae = (IAttributeExpression)ex;
                name = ae.getAttributeName();
            }
            else {
                /**
                 * ex is a PER_USER "operator" such as "keywords"
                 * or "mykeywords", or it is a PER_USER_PARAMETERS_MAP
                 * "operator" such as "properties" or "myproperties.
                 *
                 * In either case, it will have an attribute
                 * path as a subtree.  If so, we need to parse it
                 * and prepend it to the attribute path.
                 * For example:
                 *
                 *      OperatorExpression(and)
                 *        OperatorExpression(all)
                 *          OperatorExpression(keywords)
                 *            OperatorExpression(.)
                 *              OperatorExpression(.)
                 *                AttributeExpression(nextEpoch)
                 *                AttributeExpression(nextEpoch)
                 *              AttributeExpression(prevEpoch)
                 *          OperatorExpression(or)
                 *            OperatorExpression(==)
                 *              AttributeExpression(uuid)
                 *              StringLiteralValueExpression(xyz)
                 *
                 * needs to become:
                 *
                 *      nextEpoch.nextEpoch.prevEpoch.All Keywords All have Any
                 *
                 * Note that if there is no "path", the subtree
                 * under OperatorExpression(keywords) will
                 * be just the AttributeExpression(this).
                 */
                IOperatorExpression oe = (IOperatorExpression)ex;
                name = oe.getOperatorName();

                if (oe.getOperandList().size() < 1) {
                    String s = "A PER_USER IOperatorExpression("+name+") "+
                        "does not have any operands.  It should have at "+
                        "least one operand such as AttributeExpression(this).";
                    throw(new IllegalArgumentException(s));
                }

                IExpression ex2 = oe.getOperandList().get(0);

                /**
                 * Check whether the operand is the special
                 * AttributeExpression(this) value.
                 */
                if ((ex2 instanceof IAttributeExpression) &&
                    AE_THIS.equals(((IAttributeExpression)ex2).
                                    getAttributeName())) {
                    /**    
                     * The operand is AttributeExpresion(this).
                     * It is NOT added to the attribute path.
                     * It is something that exists in
                     * the Expression tree, but not in the GUI.
                     */
                }
                else {
                    /**
                     * Traverse the subtree that define the
                     * attribute path to the special PER_USER
                     * operator.  E.g. traverse the 
                     * nextEpoch.nextEpoch.prevEpoch of the
                     * example attribute path described above.
                     */
                    appendToAttributePath(rowData, ex2, classDescription);
                }
            }

            Attribute attribute = getAttribute(name, classDescription);

            if (attribute == null) {
                String s = "Attribute name \""+name+
                    "\" does not exist in class \""+classDescription.getName()+
                    "\"";
                throw(new IllegalArgumentException(s));
            }

            //System.out.println("Adding attribute \""+attribute+"\" to path.");
            rowData.addAttribute(attribute);
            return(attribute.getClassDescription());
        }
        else if (ex instanceof IOperatorExpression) {

            IOperatorExpression oe = (IOperatorExpression)ex;
            if (OE_DOT.equals(oe.getOperatorName())) {

                /**
                 * The operator is a ".", so this could be a
                 * "normal" attribute path or this could
                 * be a PARAMETERS_MAP attribute path.
                 */

                IExpression op = oe.getOperandList().get(0);

                if ((op instanceof IOperatorExpression) &&
                    OE_AS.equals(((IOperatorExpression)op).getOperatorName())) {

                    /**
                     * This is a PARAMETERS_MAP attribute path.
                     *
                     * Here is an example that is not "nested" after
                     * other attributes:
                     *
                     * protocolParameters.someTimeKey(time) == "Fri Jan 01 2010"
                     *
                     * OperatorExpression(and)
                     *   OperatorExpression(==)
                     *     OperatorExpression(.)
                     *       OperatorExpression(as)
                     *         OperatorExpression(parameter)
                     *           AttributeExpression(protocolParameters)
                     *           StringLiteralValueExpression(someTimeKey)
                     *         ClassLiteralValueExpression(ovation.DateValue)
                     *       AttributeExpression(value)
                     *     TimeLiteralValueExpression(Fri Jan 01 2010)
                     *
                     * Here is an example that is "nested":
                     *
                     * nextEpoch.nextEpoch.prevEpoch.protocolParameters.key(float) == "12.3"
                     *
                     * OperatorExpression(and)
                     *   OperatorExpression(==)
                     *     OperatorExpression(.)
                     *       OperatorExpression(as)
                     *         OperatorExpression(parameter)
                     *           OperatorExpression(.)
                     *             OperatorExpression(.)
                     *               OperatorExpression(.)
                     *                 AttributeExpression(nextEpoch)
                     *                 AttributeExpression(nextEpoch)
                     *               AttributeExpression(prevEpoch)
                     *             AttributeExpression(protocolParameters)
                     *           StringLiteralValueExpression(key)
                     *         ClassLiteralValueExpression(ovation.FloatingPointValue)
                     *       AttributeExpression(value)
                     *     Float64LiteralValueExpression(12.3)
                     *
                     * The other operand should be AttributeExpression(value).
                     * TODO:  Do we need to check that that is the case?
                     *
                     * The OperatorExpression(as) node should have two
                     * operands:  OperatorExpression(parameter) and
                     * ClassLiteralValueExpression(ovation.<someType>).
                     * Where <someType> is a value like: ovation.DateValue,
                     * ovation.FloatingPointValue, ovation.IntegerValue, etc.
                     */
                    IOperatorExpression oeAs = (IOperatorExpression)op;
                    if (oeAs.getOperandList().size() != 2) {
                        String s = "IOperatorExpression(as) does not have "+
                            "two operands.";
                        throw(new IllegalArgumentException(s));
                    }

                    /**
                     * First, because it is simple and easy, handle
                     * the IOperatorExpression(as) node's second
                     * operand which gives us the property type.
                     */
                    
                    IExpression exTemp = oeAs.getOperandList().get(1);
                    if (!(exTemp instanceof IClassLiteralValueExpression)) {
                        String s = "IOperatorExpression(as)'s second "+
                            "operand is not of type "+
                            "IClassLiteralValueExpression.";
                        throw(new IllegalArgumentException(s));
                    }

                    IClassLiteralValueExpression clve =
                        (IClassLiteralValueExpression)exTemp;
                    Type type = getTypeForCLVE(clve);
                    rowData.setPropType(type);

                    /**
                     * Now deal with the IOperatorExpression(as) node's
                     * first operand.
                     */

                    exTemp = oeAs.getOperandList().get(0);
                    if ((!(exTemp instanceof IOperatorExpression)) ||
                        !OE_PARAMETER.equals(((IOperatorExpression)exTemp).
                                             getOperatorName())) {
                        String s = "IOperatorExpression(as) does not have "+
                            "IOperatorExpression(parameter) as its first "+
                            "operand.";
                        throw(new IllegalArgumentException(s));
                    }

                    IOperatorExpression oeParameter =
                        (IOperatorExpression)exTemp;

                    /**
                     * The OperatorExpression(parameter) node should have
                     * two operands.  The first is an IExpression
                     * node/tree that is the path to the attribute
                     * name.  For example, protocolParameters, or
                     * nextEpoch.nextEpoch.prevEpoch.protocolParameter.
                     * The second operand is a StringLiteralValueExpression
                     * that gives key.  The string "someTimeKey" or "key"
                     * in the examples in the comments above.
                     *
                     * Turn the first operand into an attribute path.
                     * Use the second operand to set the property name
                     * field in the RowData.
                     */
                    
                    if (oeParameter.getOperandList().size() != 2) {
                        String s = "IOperatorExpression(parameter) does not "+
                            "have two operands.";
                        throw(new IllegalArgumentException(s));
                    }

                    exTemp = oeParameter.getOperandList().get(0);
                    appendToAttributePath(rowData, exTemp, classDescription);

                    exTemp = oeParameter.getOperandList().get(1);
                    if (!(exTemp instanceof IStringLiteralValueExpression)) {
                        String s = "IOperatorExpression(parameter)'s second "+
                            "operand is not of type "+
                            "IStringLiteralValueExpression.";
                        throw(new IllegalArgumentException(s));
                    }

                    IStringLiteralValueExpression slve =
                        (IStringLiteralValueExpression)exTemp;
                    rowData.setPropName(slve.getValue().toString());
                }
                else {

                    ClassDescription childClass;
                    childClass = appendToAttributePath(rowData, op,
                                                       classDescription);

                    if (oe.getOperandList().size() > 1) {
                        op = oe.getOperandList().get(1);
                        return(appendToAttributePath(rowData, op, childClass));
                    }
                    else {
                        /**
                         * This should never happen.  A dot operator must
                         * always have two operands.
                         */
                    }
                }
            }
            else if (OE_COUNT.equals(oe.getOperatorName())) {
                IExpression op = oe.getOperandList().get(0);
                return(appendToAttributePath(rowData, op, classDescription));
            }
            else if (OE_IS_NULL.equals(oe.getOperatorName())) {
                /**
                 * Do nothing because the later call to
                 * RowData.setAttributeOperator() calls
                 * addAttribute() with the correct
                 * Attribute.IS_NULL or Attribute.IS_NOT_NULL
                 * value.
                 */
            }
            /*
            else if (OE_ANY.equals(oe.getOperatorName())) {
            }
            else if (OE_OR.equals(oe.getOperatorName())) {
            }
            else if (OE_ALL.equals(oe.getOperatorName())) {
            }
            */
            else {
                String s = "Unhandled IOperatorExpression: "+
                    oe.getOperatorName();
                (new Exception(s)).printStackTrace();
            }
        }
        else {
            (new Exception("Unhandled IExpression")).printStackTrace();
        }
        return(null);
    }


    private static Attribute getAttribute(String attributeName,
        ClassDescription classDescription) {

        Attribute attribute = classDescription.getAttribute(attributeName);
        
        return(attribute);
    }


    /**
     * Convert the passed in ILiteralValueExpression into an attributeValue
     * that the RowData object expects.
     *
     * In the case of an IInt32LiteralValueExpression we also need
     * the passed in Type to know what type of object to create.
     * This is because the Expression structure does not have a
     * IInt16LiteralValueExpression class.  (All integer values are
     * the same.)
     */
    private static Object createAttributeValue(ILiteralValueExpression lve,
                                               Type type) {
    
        Object attributeValue = null;

        /**
         * Some values in the Expression tree are already the
         * correct object type for the RowData object.
         *
         * But, Expression IInt32LiteralValueExpression values
         * need to compared to the DataModel to figure out
         * whether the attributeValue should be an Integer or a
         * Short object.  (INT_16 or INT_32)
         */
        if ((lve instanceof ITimeLiteralValueExpression) ||
            (lve instanceof IStringLiteralValueExpression) ||
            (lve instanceof IFloat64LiteralValueExpression)) {
            return(lve.getValue());
        }
        else if (lve instanceof IBooleanLiteralValueExpression) {
            /**
             * We should not be passed an IBooleanLiteralValueExpression
             * to turn into an attributeValue, because booleans are
             * handled via the special Operator.IS_TRUE
             * The caller should have figured that out and not called us.
             */
            (new Exception("Unhandled ILiteralValueExpression subclass")).
                printStackTrace();
            return(null);
        }
        else if (lve instanceof IInt32LiteralValueExpression) {
            /**
             * Look at the DataModel to figure out whether this
             * should be a Short or an Integer.
             */
            if (type == Type.INT_16) {
                int value = (Integer)lve.getValue();
                return(new Short((short)value));
            }
            else {
                /**
                 * The value is already an Integer object.
                 * (Or at least it should be!)
                 */
                return(lve.getValue());
            }
        }
        else {
            (new Exception("Unhandled ILiteralValueExpression subclass")).
                printStackTrace();
            return(new String("ERROR"));
        }
    }


    /*
    private static boolean isCollectionOperator(String operatorString) {

        if (OE_OR.equals(operatorString) ||
            OE_NOT.equals(operatorString) ||

    }
    */

    /**
     * Get the OperatorExpression that corresponds to the passed
     * in CollectionOperator.
     *
     * Please note, for CollectionOperator.NONE, this method returns
     * a reference to OperatorExpression(not).  The OperatorExpression(not)
     * has as its only child operand another OperatorExpression() that
     * is either "or" or "any".  For example, this method returns a
     * tree like this:
     *
     *      OperatorExprssion(not)
     *        OperatorExpression(or)
     *
     * or a tree like this:
     *
     *      OperatorExprssion(not)
     *        OperatorExpression(any)
     *
     * Use the getLastOperator() method to get the child operator
     * if it exists.
     */
    private static OperatorExpression getOEForCO(CollectionOperator co,
                                                 boolean isCompoundOperator) {

        if (isCompoundOperator) {
            switch (co) {
                case ANY:
                    return(new OperatorExpression(OE_OR));

                case ALL:
                    return(new OperatorExpression(OE_AND));

                case NONE:
                    OperatorExpression notOE = new OperatorExpression(OE_NOT);
                    notOE.addOperand(new OperatorExpression(OE_OR));
                    return(notOE);

                default:
                    String s = "Illegal collectionOperator in rowData?";
                    (new Exception(s)).printStackTrace();
                    return(null);
            }
        }
        else {
            switch (co) {
                case ANY:
                    return(new OperatorExpression(OE_ANY));

                case ALL:
                    return(new OperatorExpression(OE_ALL));

                case NONE:
                    OperatorExpression notOE = new OperatorExpression(OE_NOT);
                    notOE.addOperand(new OperatorExpression(OE_ANY));
                    return(notOE);

                case COUNT:
                    return(new OperatorExpression(OE_COUNT));

                default:
                    String s = "Illegal collectionOperator in rowData?";
                    (new Exception(s)).printStackTrace();
                    return(null);
            }
        }
    }


    /**
     * Get the CollectionOperator that is equivalent to the passed
     * in OperatorExpression.
     *
     * If the passed in OperatorExpression cannot be mapped to a
     * CollectionOperator, this method returns null.
     */
    private static CollectionOperator getCOForOE(IOperatorExpression oe) {

        if ((OE_OR.equals(oe.getOperatorName())) ||
            (OE_ANY.equals(oe.getOperatorName()))) {
            return(CollectionOperator.ANY);
        }
        else if ((OE_AND.equals(oe.getOperatorName())) ||
                 (OE_ALL.equals(oe.getOperatorName()))) {
            return(CollectionOperator.ALL);
        }
        else if (OE_NOT.equals(oe.getOperatorName())) {
            oe = (IOperatorExpression)(oe.getOperandList().get(0));
            if (OE_OR.equals(oe.getOperatorName()) ||
                OE_ANY.equals(oe.getOperatorName())) {
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
     * in IOperatorExpression.
     *
     * If the passed in IOperatorExpression cannot be mapped to a
     * attribute Operator, this method returns null.
     * Note, that is not necessarily and error.
     */
    private static Operator getAOForOE(IOperatorExpression oe) {

        /**
         * Note there is no OE_IS_NOT_NULL, OE_IS_TRUE, OE_IS_FALSE.
         * Those values are handled differently in an Expression and
         * in a RowData.
         */

        if (OE_EQUALS.equals(oe.getOperatorName())) {

            IExpression ex = null;
            if (oe.getOperandList().size() > 1)
                ex = oe.getOperandList().get(1);

            if (ex instanceof IBooleanLiteralValueExpression) {

                IBooleanLiteralValueExpression blve;
                blve = (IBooleanLiteralValueExpression)ex;
                Boolean value = (Boolean)blve.getValue();
                if (value.booleanValue() == true)
                    return(Operator.IS_TRUE);
                else
                    return(Operator.IS_FALSE);
            }
            else {
                return(Operator.EQUALS);
            }
        }
        else if (OE_NOT_EQUALS.equals(oe.getOperatorName())) {
            return(Operator.NOT_EQUALS);
        }
        else if (OE_LESS_THAN.equals(oe.getOperatorName())) {
            return(Operator.LESS_THAN);
        }
        else if (OE_GREATER_THAN.equals(oe.getOperatorName())) {
            return(Operator.GREATER_THAN);
        }
        else if (OE_LESS_THAN_EQUALS.equals(oe.getOperatorName())) {
            return(Operator.LESS_THAN_EQUALS);
        }
        else if (OE_GREATER_THAN_EQUALS.equals(oe.getOperatorName())) {
            return(Operator.GREATER_THAN_EQUALS);
        }
        else if (OE_MATCHES_CASE_SENSITIVE.equals(oe.getOperatorName())) {
            return(Operator.MATCHES_CASE_SENSITIVE);
        }
        else if (OE_MATCHES_CASE_INSENSITIVE.equals(oe.getOperatorName())) {
            return(Operator.MATCHES_CASE_INSENSITIVE);
        }
        else if (OE_DOES_NOT_MATCH_CASE_SENSITIVE.equals(
                 oe.getOperatorName())) {
            return(Operator.DOES_NOT_MATCH_CASE_SENSITIVE);
        }
        else if (OE_DOES_NOT_MATCH_CASE_INSENSITIVE.equals(
                 oe.getOperatorName())) {
            return(Operator.DOES_NOT_MATCH_CASE_INSENSITIVE);
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
        /**
         * This is wrong.
         * TODO: Ask Barry how this should be handled.
         */
        else if ("is true".equals(oe.getOperatorName())) {
            return(Operator.IS_TRUE);
        }
        else if ("is false".equals(oe.getOperatorName())) {
            return(Operator.IS_FALSE);
        }

        /**
         * The passed in IOperatorExpression cannot be mapped to an
         * attribute Operator.
         */
        return(null);
    }


    /**
     * Create an Expression from the passed in root RowData.
     * This method should only be called if the passed in
     * RowData object is the rootRow of an expression tree.
     */
    public static ExpressionTree createExpressionTree(RowData rootRow) {

        if (rootRow == null) {
            //System.out.println("ExpressionTranslator.createExpressionTree() "+
            //                   "was passed a null rootRow parameter.");
            return(null);
        }

        if (rootRow.isRootRow() == false) {
            System.err.println("ERROR:  "+
                "ExpressionTranslator.createExpressionTree() "+
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
         * Create the root expression.  If the root
         * row uses the None CollectionOperator, that
         * might actually create two operators.  I.e. not(or)
         */
        rootExpression = getOEForCO(rootRow.getCollectionOperator(), true);
        lastExpression = getLastOperator(rootExpression);

        /**
         * At this point, lastExpression is a reference to
         * the OperatorExpression that will have the
         * list of operands added to.
         *
         * Now add one IExpression operand to lastExpression for each RowData
         * that is a child of the rootRow.
         */
        for (RowData childRow : rootRow.getChildRows()) {
            lastExpression.addOperand(createExpression(childRow));
        }

        ExpressionTree expressionTree = new ExpressionTree(
            rootRow.getClassUnderQualification().getName(), rootExpression);
        return(expressionTree);
    }


    /**
     * Create the operator, or operators, for the passed in
     * RowData object.
     *
     * Most of the time, only one operator needs to be created.
     * For example, the RowData attributeOperators "==", "any",
     * "is null", can all be represented by a single
     * OpertorExpression object.
     *
     * But, some operators need two OpertorExpression
     * objects to represent them.  For example, the "None"
     * CollectionOperator becomes the "not" operator with
     * the "or" operator as its only operand if the rowData
     * is the root row.  I.e. not(or())  It becomes not(any())
     * for rows other than the root row.
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
        CollectionOperator co = rowData.getCollectionOperator();
        Operator ao = rowData.getAttributeOperator();

        if (childmostAttribute == null) {
            return(getOEForCO(co, true));
        }
        else {
            if (childmostAttribute.getType() == Type.PER_USER_PARAMETERS_MAP) {
                /**
                 * Handle the special case of the PER_USER_PARAMETERS_MAP
                 * type that always has the "any" operator.
                 * Note, in a future release of the GUI, this might change.
                 */
                return(new OperatorExpression(OE_ANY));
            }
            else if (childmostAttribute.getType() == Type.PARAMETERS_MAP) {
                return(new OperatorExpression(ao.toString()));
            }
        }

        /**
         * If we get here, rowData is not a Compound Row, but
         * it still could have a CollectionOperator.
         */

        if (ao == Operator.IS_TRUE) {
            /**
             * Note that the Operator.IS_TRUE and Operator.IS_FALSE
             * are not operators in the Expression tree.  In the
             * Expression tree we use the "==" operator and
             * a BooleanLiteralValueExpression.
             */
            op1 = new OperatorExpression(OE_EQUALS);
        }
        else if (ao == Operator.IS_FALSE) {
            op1 = new OperatorExpression(OE_EQUALS);
        }
        else if (ao == Operator.IS_NULL) {
            op1 = new OperatorExpression(OE_IS_NULL);
        }
        else if (ao == Operator.IS_NOT_NULL) {
            op1 = new OperatorExpression(OE_NOT);
            op2 = new OperatorExpression(OE_IS_NULL);
            op1.addOperand(op2);
        }
        else if (co != null) {

            if (co == CollectionOperator.COUNT) {
                op1 = new OperatorExpression(ao.toString());
                op2 = getOEForCO(co, false);
                op1.addOperand(op2);
            }
            else {
                op1 = getOEForCO(co, false);
            }
        }
        else {
            op1 = new OperatorExpression(ao.toString());
        }

        return(op1);
    }


    /**
     * This method is meant to be used on an OperatorExpression
     * object that has just been created by the createOperators() method.
     * It either returns the passed in OpertorExpression object or
     * it returns the one and only operand in its operandList.
     *
     * For example, if you pass it a tree like this:
     *
     *      OperatorExprssion(not)
     *        OperatorExpression(or)
     *
     * this method returns a reference to the OperatorExpression(or) node.
     *
     * If you pass it a "tree" like this:
     *
     *      OperatorExpression(any)
     *
     * this method returns a reference to the same OperatorExpression(any)
     * node you passed as the op parameter.
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
     * Create an Expression from the passed in RowData object.
     * This method is NOT meant to handle the root RowData object.
     * The method createExpressionTree() handles the root row and
     * starts the whole process of translating everything below
     * the root row, which calls this method for every child RowData
     * object.
     *
     * Please note, this method calls itself recursively.
     */
    private static OperatorExpression createExpression(RowData rowData) {

        OperatorExpression expression;
        OperatorExpression lastOperator;

        Attribute childmostAttribute = rowData.getChildmostAttribute();

        /**
         * Create the operator that is the "top" node that this
         * method will return.  The returned OperatorExpression
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

        if ((childmostAttribute != null) &&
            (childmostAttribute.getType() == Type.PER_USER_PARAMETERS_MAP)) {

            /**
             * As of October 2011, there is only the "properties"
             * and "myproperties" attributes that are of this
             * PER_USER_PARAMETERS_MAP type.
             */
            createAndAddPerUserParametersMap(lastOperator, rowData);
        }
        else if ((childmostAttribute != null) &&
                 (childmostAttribute.getType() == Type.PARAMETERS_MAP)) {

            createAndAddParametersMap(lastOperator, rowData);
        }
        else if ((rowData.getCollectionOperator() != null) &&
                 (rowData.getCollectionOperator2() != null)) {

                IExpression ex = createExpressionPath(
                    rowData.getAttributePath(), rowData);
                lastOperator.addOperand(ex);

                OperatorExpression op = getOEForCO(
                    rowData.getCollectionOperator2(), true);
                lastOperator.addOperand(op);

                lastOperator = getLastOperator(op);

                for (RowData childRow : rowData.getChildRows()) {
                    //System.out.println("Add an operand");
                    lastOperator.addOperand(createExpression(childRow));
                } 
        }
        else if (rowData.getCollectionOperator() != null) {

            if (rowData.getCollectionOperator() == CollectionOperator.COUNT) {
                /**
                 * This is a row with a CollectionOperator.COUNT like
                 * this:
                 *
                 *      responses Count == 27
                 */
                lastOperator.addOperand(
                    createExpressionPath(rowData.getAttributePath(), rowData));
                expression.addOperand(createLiteralValueExpression(
                    Type.INT_32, rowData));
            }
            else {
                /**
                 * This is an Any/All/None row.
                 */
                if (rowData.getAttributeCount() > 0) {
                    lastOperator.addOperand(
                        createExpressionPath(rowData.getAttributePath(),
                            rowData));
                }
                for (RowData childRow : rowData.getChildRows()) {
                    //System.out.println("Add an operand");
                    lastOperator.addOperand(createExpression(childRow));
                } 
            }
        }
        else if (rowData.getAttributeOperator() != null) {

            lastOperator.addOperand(createExpressionPath(
                rowData.getAttributePath(), rowData));
            if ((rowData.getAttributeOperator() != Operator.IS_NULL) &&
                (rowData.getAttributeOperator() != Operator.IS_NOT_NULL)) {
                lastOperator.addOperand(createLiteralValueExpression(rowData));
            }
        }

        if (expression == null) {
            String s = "expression == null, which means code is not finished.";
            (new Exception(s)).printStackTrace();
        }

        return(expression);
    }


    /**
     * Create the operands for a PER_USER expression.
     *
     * The comments use these example rowData values:
     *
     *      nextEpoch.nextEpoch.prevEpoch.My Keywords None
     *          uuid == "xyz"
     */
    private static OperatorExpression createPerUserExpression(RowData rowData) {

        Attribute childmostAttribute = rowData.getChildmostAttribute();
        OperatorExpression oe;
        oe = new OperatorExpression(childmostAttribute.getQueryName());

        if (rowData.getAttributeCount() < 2) {
            oe.addOperand(new AttributeExpression(AE_THIS));
        }
        else {
            /**
             * Add the expression that represents the
             * "nextEpoch.nextEpoch.prevEpoch" path.
             */
            createAndAddDotPath(oe, rowData);
        }
        return(oe);
    }


    private static void createAndAddParametersMap(
        OperatorExpression lastOperator, RowData rowData) {

        OperatorExpression dotOperand;
        IExpression valueOperand;

        dotOperand = new OperatorExpression(".");
        valueOperand = createLiteralValueExpression(rowData.getPropType(),
                                                    rowData);
        lastOperator.addOperand(dotOperand);
        lastOperator.addOperand(valueOperand);

        dotOperand.addOperand(createExpressionPath(rowData.getAttributePath(),
                              rowData));
        dotOperand.addOperand(new AttributeExpression(AE_VALUE));
    }


    /**
     * Create and add the Expressions for the PER_USER_PARAMETERS_MAP
     * type.  As of October 2011, only the "properties" and "myproperties"
     * Attributes are of this type.
     *
     * By the time this method is called, lastOperator has been set
     * to the OperatorExpression("any").
     * (This is always the case for an Attribute of type
     * PER_USER_PARAMETERS_MAP as far as I understand.)
     *
     * An example tree might look like this:
     *
     */
    private static void createAndAddPerUserParametersMap(
        OperatorExpression lastOperator, RowData rowData) {

        OperatorExpression op2;
        OperatorExpression elementsOfTypeOperator;
        IExpression valueOperand;
        OperatorExpression pupmOperator;  // Per User Properties Map Operator

        elementsOfTypeOperator = new OperatorExpression(OE_ELEMENTS_OF_TYPE);
        lastOperator.addOperand(elementsOfTypeOperator);

        op2 = new OperatorExpression(
            rowData.getAttributeOperator().toString());
        lastOperator.addOperand(op2);

        op2.addOperand(new AttributeExpression(AE_VALUE));
        valueOperand = createLiteralValueExpression(rowData.getPropType(),
                                                    rowData);
        op2.addOperand(valueOperand);

        Attribute attribute = rowData.getChildmostAttribute();
        pupmOperator = new OperatorExpression(attribute.getQueryName());
        elementsOfTypeOperator.addOperand(pupmOperator);
        elementsOfTypeOperator.addOperand(createCLVEForType(rowData.getPropType()));

        /**
         * Create the two operands for the pupmOperator.
         * (Per User Properties Map Operator)
         * As of October 2011, only the "properties" and "myproperties"
         * attributes were of this type.
         *
         * The first operand is the qualifying path to the attribute,
         * if any.  We use "this" if no path exists.
         * The second operand is the property key the user entered.
         */

        if (rowData.getAttributeCount() < 2) {
            pupmOperator.addOperand(new AttributeExpression(AE_THIS));
        }
        else {
            createAndAddDotPath(pupmOperator, rowData);
        }

        pupmOperator.addOperand(new StringLiteralValueExpression(
            rowData.getPropName()));
    }


    /**
     * Look at the passed in rowData object and create a
     * "dot path" of all the Attributes in the rowData's
     * attributePath except for the last Attribute.
     * I.e. the attributePath qualifies what the last Attribute is.
     * Then add that Expression to the passed in parent Expression.
     *
     * For example, if the rowData's attributePath is:
     *
     *      nextEpoch.nextEpoch.prevEpoch.My Keywords
     *
     * and the parent is:
     *
     *    OperatorExpression(mykeywords)
     *
     * this method will add the Expression tree below to parent:
     *
     *      OperatorExpression(.)
     *        OperatorExpression(.)
     *          AttributeExpression(nextEpoch)
     *          AttributeExpression(nextEpoch)
     *        AttributeExpression(prevEpoch)
     *
     * to create:
     *
     *    OperatorExpression(mykeywords)
     *      OperatorExpression(.)
     *        OperatorExpression(.)
     *          AttributeExpression(nextEpoch)
     *          AttributeExpression(nextEpoch)
     *        AttributeExpression(prevEpoch)
     */
    private static final void createAndAddDotPath(OperatorExpression parent,
        RowData rowData) {

        List<Attribute> attributePath = rowData.getAttributePath();

        /**
         * We ignore the last Attribute in the path.
         */
        attributePath = attributePath.subList(0, attributePath.size()-1);
        if (attributePath.size() < 1)
            return;

        parent.addOperand(createExpressionPath(attributePath, rowData));
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
                boolean value;
                value = (rowData.getAttributeOperator() == Operator.IS_TRUE);
                return(new BooleanLiteralValueExpression(value));

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


    private static Type getTypeForCLVE(IClassLiteralValueExpression clve) {

        if ((clve == null) || (clve.getValue() == null))
            return(null);

        String name = clve.getValue().toString();

        /**
         * At this point, name should be something like:
         *
         *      ovation.DateValue
         */

        if (CLVE_BOOLEAN.equals(name))
            return(Type.BOOLEAN);
        else if (CLVE_STRING.equals(name))
            return(Type.UTF_8_STRING);
        else if (CLVE_INTEGER.equals(name))
            return(Type.INT_32);
        else if (CLVE_FLOAT.equals(name))
            return(Type.FLOAT_64);
        else if (CLVE_DATE.equals(name))
            return(Type.DATE_TIME);
        else {
            String s = "Bad IClassLiteralValue parameter clve: \""+name+"\"";
            throw(new IllegalArgumentException(s));
        }
    }


    /**
     * Create and return a ClassLiteralValueExpression for the passed
     * in Type.
     *
     * TODO:  Perhaps create a set of static final ClassLiteralValueExpression
     * objects instead of the set of strings I currently have?  Then we
     * would not be creating new objects all the time.
     */
    private static ClassLiteralValueExpression createCLVEForType(Type type) {

        switch (type) {

            case BOOLEAN:
                return(new ClassLiteralValueExpression(CLVE_BOOLEAN));

            case UTF_8_STRING:
                return(new ClassLiteralValueExpression(CLVE_STRING));

            case INT_16:
                return(new ClassLiteralValueExpression(CLVE_INTEGER));

            case INT_32:
                return(new ClassLiteralValueExpression(CLVE_INTEGER));

            case FLOAT_64:
                return(new ClassLiteralValueExpression(CLVE_FLOAT));

            case DATE_TIME:
                return(new ClassLiteralValueExpression(CLVE_DATE));

            default:
                String s = "Unhandled type parameter: "+type;
                throw(new IllegalArgumentException(s));
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
     *            AttributeExpression(protocolParameters)
     *            StringLiteralValueExpression(key)
     *          OperatorExpression(ovation.IntegerValue)
     *        AttributeExpression(value)
     *
     * This method only returns the "as" sub-tree above.
     *
     * Please note, if the passed in attributePath is more than
     * just the one "protocolParameters" entry in the example above,
     * this method handles the nesting of the parent Attributes
     * differently than for other Attribute types.  For example:
     *
     * nextEpoch.nextEpoch.prevEpoch.protocolParameters.key(float) == "12.3"
     *
     * becomes:
     * 
     * Expression:
     * CUQ: Epoch
     * rootExpression:
     * OperatorExpression(and)
     *   OperatorExpression(==)
     *     OperatorExpression(.)
     *       OperatorExpression(as)
     *         OperatorExpression(parameter)
     *           OperatorExpression(.)
     *             OperatorExpression(.)
     *               OperatorExpression(.)
     *                 AttributeExpression(nextEpoch)
     *                 AttributeExpression(nextEpoch)
     *               AttributeExpression(prevEpoch)
     *             AttributeExpression(protocolParameters)
     *           StringLiteralValueExpression(key)
     *         ClassLiteralValueExpression(ovation.FloatingPointValue)
     *       AttributeExpression(value)
     *     Float64LiteralValueExpression(12.3)
     *
     * @return The "as" sub-tree described in the example above.
     * I.e. it returns an OperatorExpression(as) that has operands
     * similar to those described above.
     */
    private static IExpression createExpressionParametersMap(
        List<Attribute> attributePath, RowData rowData) {

        /**
         * Create the "as" operator and the AttributeExpression(value),
         * and add them as the left and right operands of the "." operator.
         */
        OperatorExpression asOperator = new OperatorExpression(OE_AS);

        /**
         * Create the "parameter" operator and the
         * ClassLiteralValueExpression(IntValue), and add them as the left
         * and right operands of the "as" operator.
         */
        OperatorExpression parameterOperator = new OperatorExpression(
            OE_PARAMETER);
        asOperator.addOperand(parameterOperator);
        asOperator.addOperand(createCLVEForType(rowData.getPropType()));

        /**
         * Create the left (i.e. first) operand for the 
         * OperatorExpression(parameter) operator.
         */

        Attribute lastAttribute = attributePath.get(attributePath.size()-1);
        IExpression aeQueryName = new AttributeExpression(
            lastAttribute.getQueryName());
        IExpression parameterLeftOperand;
        if (attributePath.size() > 1) {
            List<Attribute> allButLastAttribute = attributePath.subList(0,
                attributePath.size()-1);

            IExpression ex = createExpressionPath(allButLastAttribute, rowData);
            OperatorExpression oeDot = new OperatorExpression(OE_DOT);
            oeDot.addOperand(ex);
            oeDot.addOperand(aeQueryName);
            parameterLeftOperand = oeDot;
        }
        else {
            parameterLeftOperand = aeQueryName;
        }

        /**
         * Now add the left operand we created above.
         */
        parameterOperator.addOperand(parameterLeftOperand);

        /**
         * Create and add the right (i.e. second) operand for the
         * OperatorExpression(parameter) operator.  This is the
         * "key" name that the user entered.
         */
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
     *
     * Note, the syntax of the Expression tree is NOT consistent
     * for all data types.  For "generic" attributes, we create
     * a tree of nested OpertorExpressions.  For example:
     *
     * rootRow:
     * Epoch | Any
     *   Epoch | nextEpoch.nextEpoch.prevEpoch.protocolID == "Test 27"
     * 
     * Expression:
     * CUQ: Epoch
     * rootExpression:
     * OperatorExpression(or)
     *   OperatorExpression(==)
     *     OperatorExpression(.)
     *       OperatorExpression(.)
     *         OperatorExpression(.)
     *           AttributeExpression(nextEpoch)
     *           AttributeExpression(nextEpoch)
     *         AttributeExpression(prevEpoch)
     *       AttributeExpression(protocolID)
     *     StringLiteralValueExpression(Test 27)
     *
     * But for a PARAMETERS_MAP type, things are different.
     * Please see the createExpressionParametersMap() method for more info.
     *
     * @return The IExpression returned is, in the example above,
     * the first operand of the "==" operator subtree shown above.
     * I.e. the subtree that starts with the first OperatorExpression(.)
     */
    private static IExpression createExpressionPath(
        List<Attribute> attributePath, RowData rowData) {

        //System.out.println("Enter createExpressionPath(List<Attribute>)");
        //System.out.println("attributePath.size() = "+attributePath.size());

        if (attributePath.size() < 1) {

            /**
             * This should never happen.
             */
            String s = "attributePath.size() < 1";
            (new Exception(s)).printStackTrace();
            return(null);
        }

        /**
         * Throw away special Attributes suchs a "is null" and "is not null".
         */
        Attribute lastAttribute = attributePath.get(attributePath.size()-1);
        if ((attributePath.size() > 1) &&
            ((lastAttribute == Attribute.IS_NULL) ||
             (lastAttribute == Attribute.IS_NOT_NULL))) {
            attributePath = attributePath.subList(0, attributePath.size()-1);
            lastAttribute = attributePath.get(attributePath.size()-1);
        }

        /**
         * Handle the special cases:  PARAMETERS_MAP, PER_USER
         *
         * They don't handle "nesting" in the normal way because
         * they cannot be an operand of the dot operator.
         */
        if (lastAttribute.getType() == Type.PARAMETERS_MAP) {
            return(createExpressionParametersMap(attributePath, rowData));
        }
        else if (lastAttribute.getType() == Type.PER_USER) {
            return(createPerUserExpression(rowData));
        }

        /**
         * If the attributePath is, (or has been whittled down to),
         * one attribute long, create the Expression for that one
         * attribute.
         */
        if (attributePath.size() == 1) {

            Attribute attribute = attributePath.get(0);

            /**
             * Quick sanity check during development.
             */
            if (attribute.getType() == Type.PARAMETERS_MAP) {
                /**
                 * We should never get here.
                 * This type should already have been handled
                 * above.
                 */
                String s = "Got an Attribute of Type.PARAMETERS_MAP "+
                    "unexpectedly.  There is a problem in the code.";
                (new Exception(s)).printStackTrace();
                return(null);
            }

            if (attribute.getType() == Type.PER_USER) {
                /**
                 * TODO:  I don't think we get here any more.
                 */
                return(new OperatorExpression(
                       attribute.getQueryName()));
            }
            else {
                return(new AttributeExpression(
                       attribute.getQueryName()));
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
        operand = createExpressionPath(subList, rowData);

        //System.out.println("Add left operand.");
        expression.addOperand(operand);

        /**
         * Create and add the right operand.  The right operand
         * is the rightmost attribute in the passed in attributePath.
         */

        subList = attributePath.subList(attributePath.size()-1,
                                        attributePath.size());
        operand = createExpressionPath(subList, rowData);

        //System.out.println("Add right operand: ");
        expression.addOperand(operand);

        return(expression);
    }


    /**
     * A long list of tests.
     */
    public static void runAllTests() {

        RowData rootRow;
        RowData rowData;
        RowData rowData2;
        RowData rowData3;
        RowData rowData4;
        ExpressionTree expression;

        ClassDescription epochCD = DataModel.getClassDescription("Epoch");
        ClassDescription epochGroupCD = DataModel.getClassDescription(
            "EpochGroup");
        ClassDescription sourceCD = DataModel.getClassDescription("Source");
        ClassDescription responseCD = DataModel.getClassDescription("Response");
        ClassDescription resourceCD = DataModel.getClassDescription("Resource");
        ClassDescription derivedResponseCD =
            DataModel.getClassDescription("DerivedResponse");
        ClassDescription externalDeviceCD = 
            DataModel.getClassDescription("ExternalDevice");

        /**
         * Test the Any collection operator, (which becomes "or"),
         * and the String type and a couple attribute operators.
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ANY);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("protocolID"));
        rowData.setAttributeOperator(Operator.EQUALS);
        rowData.setAttributeValue("abc");
        rootRow.addChildRow(rowData);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("protocolID"));
        rowData.setAttributeOperator(Operator.MATCHES_CASE_INSENSITIVE);
        rowData.setAttributeValue("xyz");
        rootRow.addChildRow(rowData);
        
        printResults("CollectionOperator.ANY With Two Operands", rootRow);

        /**
         * Test the All collection operator, (which becomes "and"),
         * and the Boolean type.
         *
         *      incomplete is true
         *
         * Note, the GUI does not accept:
         *
         *      incomplete == true
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("incomplete"));
        /**
         * This is the wrong way to handle booleans in the GUI.
         */
        //rowData.setAttributeOperator(Operator.EQUALS);
        //rowData.setAttributeValue(new Boolean(true));
        /**
         * This is the correct way.
         */
        rowData.setAttributeOperator(Operator.IS_TRUE);

        rootRow.addChildRow(rowData);

        printResults("Attribute Boolean Operator.IS_TRUE", rootRow);

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
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.NONE);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("epochGroup"));
        rowData.addAttribute(epochGroupCD.getAttribute("label"));
        rowData.setAttributeOperator(Operator.EQUALS);
        rowData.setAttributeValue("Test 27");
        rootRow.addChildRow(rowData);

        printResults("Attribute Path Nested Twice", rootRow);

        /**
         * Test an attribute path with three levels:
         *
         *      epochGroup.source.label == "Test 27"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.NONE);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("epochGroup"));
        rowData.addAttribute(epochGroupCD.getAttribute("source"));
        rowData.addAttribute(sourceCD.getAttribute("label"));
        rowData.setAttributeOperator(Operator.EQUALS);
        rowData.setAttributeValue("Test 27");
        rootRow.addChildRow(rowData);

        printResults("Attribute Path Nested Thrice", rootRow);

        /**
         * Test a reference value for null.
         *
         *      Epoch | All
         *        Epoch | owner is null
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("owner"));
        rowData.addAttribute(Attribute.IS_NULL);  // optional call
        rowData.setAttributeOperator(Operator.IS_NULL);
        rootRow.addChildRow(rowData);

        printResults("Reference Value Operator.IS_NULL", rootRow);

        /**
         * Test a reference value for not null.
         *
         *      Epoch | All
         *        Epoch | owner is not null
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("owner"));
        rowData.addAttribute(Attribute.IS_NOT_NULL);  // optional call
        rowData.setAttributeOperator(Operator.IS_NOT_NULL);
        rootRow.addChildRow(rowData);

        printResults("Reference Value Operator.IS_NOT_NULL", rootRow);

        /**
         * Test a compound row:
         *
         *      Epoch | All
         *        Epoch | responses All have Any
         *          Response | uuid == "xyz"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("responses"));
        rowData.setCollectionOperator(CollectionOperator.ALL);
        rowData.setCollectionOperator2(CollectionOperator.ANY);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.addAttribute(responseCD.getAttribute("uuid"));
        rowData2.setAttributeOperator(Operator.EQUALS);
        rowData2.setAttributeValue("xyz");
        rowData.addChildRow(rowData2);

        printResults("Compound Row", rootRow);

        /**
         * Test a compound row with lots of None collection operators:
         *
         *      Epoch | None
         *        Epoch | responses None have None
         *          Response | uuid == "xyz"
         *          Response | samplingRate != 1.23
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.NONE);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("responses"));
        rowData.setCollectionOperator(CollectionOperator.NONE);
        rowData.setCollectionOperator2(CollectionOperator.NONE);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.addAttribute(responseCD.getAttribute("uuid"));
        rowData2.setAttributeOperator(Operator.EQUALS);
        rowData2.setAttributeValue("xyz");
        rowData.addChildRow(rowData2);

        rowData2 = new RowData();
        rowData2.addAttribute(responseCD.getAttribute("samplingRate"));
        rowData2.setAttributeOperator(Operator.NOT_EQUALS);
        rowData2.setAttributeValue(new Double(1.23));
        rowData.addChildRow(rowData2);

        printResults("Compound Row With Lots Of None Collection Operators",
                     rootRow);

        /**
         * Test a compound row with lots of None collection operators:
         *
         *      Epoch | None
         *        Epoch | responses None have None
         *          Response | uuid == "xyz"
         *          Response | samplingRate != 1.23
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.NONE);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("prevEpoch"));
        rowData.addAttribute(epochCD.getAttribute("responses"));
        rowData.setCollectionOperator(CollectionOperator.NONE);
        rowData.setCollectionOperator2(CollectionOperator.NONE);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.addAttribute(responseCD.getAttribute("uuid"));
        rowData2.setAttributeOperator(Operator.EQUALS);
        rowData2.setAttributeValue("xyz");
        rowData.addChildRow(rowData2);

        rowData2 = new RowData();
        rowData2.addAttribute(responseCD.getAttribute("samplingRate"));
        rowData2.setAttributeOperator(Operator.NOT_EQUALS);
        rowData2.setAttributeValue(new Double(1.23));
        rowData.addChildRow(rowData2);

        printResults("Same As Above, But Nested", rootRow);

        /**
         * Test a compound row:
         *
         *      Epoch | All
         *        Epoch | responses All have Any
         *          Response | resources Any have Any
         *            Epoch | protocolID != "Test 27"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("responses"));
        rowData.setCollectionOperator(CollectionOperator.ALL);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.addAttribute(responseCD.getAttribute("resources"));
        rowData2.setCollectionOperator(CollectionOperator.ANY);
        rowData.addChildRow(rowData2);

        rowData3 = new RowData();
        rowData3.addAttribute(resourceCD.getAttribute("uuid"));
        rowData3.setAttributeOperator(Operator.NOT_EQUALS);
        rowData3.setAttributeValue("ID 27");
        rowData2.addChildRow(rowData3);

        printResults("Compound Row Nested Classes", rootRow);

        /**
         * Test a compound row that uses the Count collection operator:
         *
         *      Epoch | All
         *        Epoch | responses All have Any
         *          Response | resources Count <= 5
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("responses"));
        rowData.setCollectionOperator(CollectionOperator.ALL);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.addAttribute(responseCD.getAttribute("resources"));
        rowData2.setCollectionOperator(CollectionOperator.COUNT);
        rowData2.setAttributeOperator(Operator.LESS_THAN_EQUALS);
        rowData2.setAttributeValue(new Integer(5));
        rowData.addChildRow(rowData2);

        printResults("Compound Row ALL COUNT", rootRow);

        /**
         * Test a compound row:
         *
         *      Epoch | Any
         *        Epoch | responses Any have Any
         *          Response | uuid == "xyz"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ANY);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("responses"));
        rowData.setCollectionOperator(CollectionOperator.ANY);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.addAttribute(responseCD.getAttribute("uuid"));
        rowData2.setAttributeOperator(Operator.EQUALS);
        rowData2.setAttributeValue("xyz");
        rowData.addChildRow(rowData2);

        printResults("Compound Row ANY ANY", rootRow);

        /**
         * Test a compound row:
         *
         *      Epoch | None
         *        Epoch | responses None have Any
         *          Response | uuid == "xyz"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.NONE);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("responses"));
        rowData.setCollectionOperator(CollectionOperator.NONE);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.addAttribute(responseCD.getAttribute("uuid"));
        rowData2.setAttributeOperator(Operator.EQUALS);
        rowData2.setAttributeValue("xyz");
        rowData.addChildRow(rowData2);

        printResults("Compound Row NONE NONE", rootRow);

        /**
         * Test a PER_USER attribute type.
         *
         *      Epoch | All
         *        Epoch | My keywords None have Any
         *          KeywordTag | uuid == "xyz"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("mykeywords"));
        rowData.setCollectionOperator(CollectionOperator.NONE);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.addAttribute(epochCD.getAttribute("uuid"));
        rowData2.setAttributeOperator(Operator.EQUALS);
        rowData2.setAttributeValue("xyz");
        rowData.addChildRow(rowData2);

        printResults("PER_USER", rootRow);

        /**
         * Test a PER_USER attribute type with Count.
         *
         *      Epoch | All
         *        Epoch | My keywords Count == 5
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("mykeywords"));
        rowData.setCollectionOperator(CollectionOperator.COUNT);
        rowData.setAttributeOperator(Operator.EQUALS);
        rowData.setAttributeValue(new Integer(5));
        rootRow.addChildRow(rowData);

        printResults("PER_USER CollectionOperator.COUNT", rootRow);

        /**
         * Test a nested PER_USER attribute type.
         *
         *      Epoch | Any
         *        Epoch | nextEpoch.All keywords Any have Any
         *          KeywordTag | uuid == "xyz"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ANY);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("keywords"));
        rowData.setCollectionOperator(CollectionOperator.ANY);
        rowData.setCollectionOperator2(CollectionOperator.ANY);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.addAttribute(epochCD.getAttribute("uuid"));
        rowData2.setAttributeOperator(Operator.EQUALS);
        rowData2.setAttributeValue("xyz");
        rowData.addChildRow(rowData2);

        printResults("PER_USER Nested Once CollectionOperator.ANY", rootRow);

        /**
         * Test a nested PER_USER attribute type.
         *
         *      Epoch | None
         *        Epoch | nextEpoch.All keywords None have All
         *          KeywordTag | uuid == "xyz"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.NONE);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("keywords"));
        rowData.setCollectionOperator(CollectionOperator.NONE);
        rowData.setCollectionOperator2(CollectionOperator.ALL);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.addAttribute(epochCD.getAttribute("uuid"));
        rowData2.setAttributeOperator(Operator.EQUALS);
        rowData2.setAttributeValue("xyz");
        rowData.addChildRow(rowData2);

        printResults("PER_USER Nested Twice CollectionOperator.NONE", rootRow);

        /**
         * Test a nested PER_USER attribute type.
         *
         *     Epoch | All
         *       Epoch | nextEpoch.nextEpoch.prevEpoch.All keywords All have Any
         *         KeywordTag | uuid == "xyz"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("prevEpoch"));
        rowData.addAttribute(epochCD.getAttribute("keywords"));
        rowData.setCollectionOperator(CollectionOperator.ALL);
        rowData.setCollectionOperator2(CollectionOperator.ANY);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.addAttribute(epochCD.getAttribute("uuid"));
        rowData2.setAttributeOperator(Operator.EQUALS);
        rowData2.setAttributeValue("xyz");
        rowData.addChildRow(rowData2);

        printResults("PER_USER Nested Thrice CollectionOperator.ALL", rootRow);

        /**
         * Test a PARAMETERS_MAP row of type time.
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("protocolParameters"));
        rowData.setPropName("someTimeKey");
        rowData.setPropType(Type.DATE_TIME);
        rowData.setAttributeOperator(Operator.EQUALS);
        rowData.setAttributeValue(new Date(1262304000000L));
        rootRow.addChildRow(rowData);

        printResults("PARAMETERS_MAP Type Date", rootRow);

        /**
         * Test a PARAMETERS_MAP row of type float, that
         * has an attributePath that is more than one level deep.
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("prevEpoch"));
        rowData.addAttribute(epochCD.getAttribute("protocolParameters"));
        rowData.setPropName("someKey");
        rowData.setPropType(Type.FLOAT_64);
        rowData.setAttributeOperator(Operator.EQUALS);
        rowData.setAttributeValue(new Double(12.3));
        rootRow.addChildRow(rowData);

        printResults("PARAMETERS_MAP Nested", rootRow);

        /**
         * Test a PER_USER_PARAMETERS_MAP row.
         *
         *      Any properties.someKey(int) != "34"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("properties"));
        rowData.setPropName("someKey");
        rowData.setPropType(Type.INT_32);
        rowData.setAttributeOperator(Operator.NOT_EQUALS);
        rowData.setAttributeValue(new Integer(34));
        rootRow.addChildRow(rowData);

        printResults("PER_USER_PARAMETERS_MAP", rootRow);

        /**
         * Test a PER_USER_PARAMETERS_MAP row.
         *
         * nextEpoch.nextEpoch.prevEpoch.Any properties.someKey(int) != "34"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ANY);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("prevEpoch"));
        rowData.addAttribute(epochCD.getAttribute("properties"));
        rowData.setPropName("someKey");
        rowData.setPropType(Type.INT_32);
        rowData.setAttributeOperator(Operator.NOT_EQUALS);
        rowData.setAttributeValue(new Integer(34));
        rootRow.addChildRow(rowData);

        printResults("PER_USER_PARAMETERS_MAP Nested", rootRow);

        /**
         * Test handling a reference.
         *
         *  nextEpoch.nextEpoch is null
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.setAttributeOperator(Operator.IS_NULL);
        rootRow.addChildRow(rowData);

        printResults("Reference isnull()", rootRow);

        /**
         * Test compound row.
         *
         *      All of the following
         *        None of the following
         *          protocolID =~~ "xyz"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.setCollectionOperator(CollectionOperator.NONE);
        rootRow.addChildRow(rowData);

        rowData4 = new RowData();
        rowData4.addAttribute(epochCD.getAttribute("protocolID"));
        rowData4.setAttributeOperator(Operator.MATCHES_CASE_INSENSITIVE);
        rowData4.setAttributeValue("xyz");
        rowData.addChildRow(rowData4);

        printResults("Compound Operators Nested", rootRow);

        /**
         * Test compound row.
         *
         *      Any of the following
         *        All of the following
         *          None of the following
         *            protocolID =~~ "xyz"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.setCollectionOperator(CollectionOperator.ANY);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.setCollectionOperator(CollectionOperator.ALL);
        rowData.addChildRow(rowData2);

        rowData3 = new RowData();
        rowData3.setCollectionOperator(CollectionOperator.NONE);
        rowData2.addChildRow(rowData3);

        rowData4 = new RowData();
        rowData4.addAttribute(epochCD.getAttribute("protocolID"));
        rowData4.setAttributeOperator(Operator.MATCHES_CASE_INSENSITIVE);
        rowData4.setAttributeValue("xyz");
        rowData3.addChildRow(rowData4);

        printResults("Compound Operators In Different Positions", rootRow);

        /**
         * Test compound row.
         *
         *      All of the following
         *        All of the following
         *          Any of the following
         *            None of the following
         *              protocolID =~~ "xyz"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.setCollectionOperator(CollectionOperator.ALL);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.setCollectionOperator(CollectionOperator.ANY);
        rowData.addChildRow(rowData2);

        rowData3 = new RowData();
        rowData3.setCollectionOperator(CollectionOperator.NONE);
        rowData2.addChildRow(rowData3);

        rowData4 = new RowData();
        rowData4.addAttribute(epochCD.getAttribute("protocolID"));
        rowData4.setAttributeOperator(Operator.MATCHES_CASE_INSENSITIVE);
        rowData4.setAttributeValue("xyz");
        rowData3.addChildRow(rowData4);

        printResults("Compound Operators In Different Positions", rootRow);

        /**
         * Test compound row.
         *
         *      Any of the following
         *        Any of the following
         *          All of the following
         *            None of the following
         *              protocolID =~~ "xyz"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ANY);

        rowData = new RowData();
        rowData.setCollectionOperator(CollectionOperator.ANY);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.setCollectionOperator(CollectionOperator.ALL);
        rowData.addChildRow(rowData2);

        rowData3 = new RowData();
        rowData3.setCollectionOperator(CollectionOperator.NONE);
        rowData2.addChildRow(rowData3);

        rowData4 = new RowData();
        rowData4.addAttribute(epochCD.getAttribute("protocolID"));
        rowData4.setAttributeOperator(Operator.MATCHES_CASE_INSENSITIVE);
        rowData4.setAttributeValue("xyz");
        rowData3.addChildRow(rowData4);

        printResults("Compound Operators In Different Positions", rootRow);

        /**
         * Test compound row.
         *
         *      None of the following
         *        None of the following
         *          All of the following
         *            Any of the following
         *              protocolID =~~ "xyz"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.NONE);

        rowData = new RowData();
        rowData.setCollectionOperator(CollectionOperator.NONE);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.setCollectionOperator(CollectionOperator.ALL);
        rowData.addChildRow(rowData2);

        rowData3 = new RowData();
        rowData3.setCollectionOperator(CollectionOperator.ANY);
        rowData2.addChildRow(rowData3);

        rowData4 = new RowData();
        rowData4.addAttribute(epochCD.getAttribute("protocolID"));
        rowData4.setAttributeOperator(Operator.MATCHES_CASE_INSENSITIVE);
        rowData4.setAttributeValue("xyz");
        rowData3.addChildRow(rowData4);

        printResults("Compound Operators In Different Positions", rootRow);

        /**
         * Test compound row with PARAMETERS_MAP and PER_USER child.
         *
         *  Modified rootRow:
         *  Epoch | Any
         *    Epoch | nextEpoch.My DerivedResponses None
         *      DerivedResponse | derivationParameters.somekey(boolean) is true
         *      DerivedResponse | My Keywords Count == "5"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ANY);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("myderivedResponses"));
        rowData.setCollectionOperator(CollectionOperator.NONE);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.addAttribute(derivedResponseCD.getAttribute(
                              "derivationParameters"));
        rowData2.setPropName("someKey");
        rowData2.setPropType(Type.BOOLEAN);
        rowData2.setAttributeOperator(Operator.IS_TRUE);
        rowData.addChildRow(rowData2);

        rowData2 = new RowData();
        rowData2.addAttribute(derivedResponseCD.getAttribute("mykeywords"));
        rowData2.setCollectionOperator(CollectionOperator.COUNT);
        rowData2.setAttributeValue(new Integer(5));
        rowData.addChildRow(rowData2);

        /**
         * Test compound row with PARAMETERS_MAP, PER_USER children.
         *
         *  Modified rootRow:
         *  Epoch | Any
         *    Epoch | nextEpoch.My DerivedResponses None
         *      DerivedResponse | derivationParameters.somekey(boolean) is true
         *      DerivedResponse | My Keywords Count == "5"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ANY);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("myderivedResponses"));
        rowData.setCollectionOperator(CollectionOperator.NONE);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.addAttribute(derivedResponseCD.getAttribute(
                              "derivationParameters"));
        rowData2.setPropName("someKey");
        rowData2.setPropType(Type.BOOLEAN);
        rowData2.setAttributeOperator(Operator.IS_TRUE);
        rowData.addChildRow(rowData2);

        rowData2 = new RowData();
        rowData2.addAttribute(derivedResponseCD.getAttribute("mykeywords"));
        rowData2.setCollectionOperator(CollectionOperator.COUNT);
        rowData2.setAttributeValue(new Integer(5));
        rowData.addChildRow(rowData2);

        printResults("Nested PER_USER With PARAMETERS_MAP & PER_USER Children",
                     rootRow);

        /**
         * Test compound row with PARAMETERS_MAP, PER_USER, and
         * PER_USER_PARAMETERS_MAP child.
         *
         *  Epoch | Any
         *    Epoch | nextEpoch.My DerivedResponses None
         *      DerivedResponse | derivationParameters.someKey(boolean) is true
         *      DerivedResponse | My Keywords Count == "5"
         *      DerivedResponse | externalDevice.My Property.someKey2(float) ==
         *                                                               "34.5"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ANY);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("myderivedResponses"));
        rowData.setCollectionOperator(CollectionOperator.NONE);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.addAttribute(derivedResponseCD.getAttribute(
                              "derivationParameters"));
        rowData2.setPropName("someKey");
        rowData2.setPropType(Type.BOOLEAN);
        rowData2.setAttributeOperator(Operator.IS_TRUE);
        rowData.addChildRow(rowData2);

        rowData2 = new RowData();
        rowData2.addAttribute(derivedResponseCD.getAttribute("mykeywords"));
        rowData2.setCollectionOperator(CollectionOperator.COUNT);
        rowData2.setAttributeValue(new Integer(5));
        rowData.addChildRow(rowData2);

        rowData2 = new RowData();
        rowData2.addAttribute(derivedResponseCD.getAttribute("externalDevice"));
        rowData2.addAttribute(externalDeviceCD.getAttribute("myproperties"));
        rowData2.setPropName("someKey2");
        rowData2.setPropType(Type.FLOAT_64);
        rowData2.setAttributeOperator(Operator.EQUALS);
        rowData2.setAttributeValue(new Double(34.5));
        rowData.addChildRow(rowData2);

        printResults("Nested PER_USER With PM, PU, and PUPM Children",
                     rootRow);

        /**
         * Test compound row where class changes between parent and child.
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.NONE);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("responses"));
        rowData.setCollectionOperator(CollectionOperator.NONE);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.setCollectionOperator(CollectionOperator.NONE);
        rowData.addChildRow(rowData2);

        rowData3 = new RowData();
        rowData3.addAttribute(responseCD.getAttribute("samplingRate"));
        rowData3.setAttributeOperator(Operator.GREATER_THAN_EQUALS);
        rowData3.setAttributeValue(new Double(55.2));
        rowData2.addChildRow(rowData3);

        printResults("Compound Row, Class Change Between Parent And Child",
                     rootRow);
    }


    /**
     * This is just a single test of what I am working on right now.
     */
    public static void runOneTest() {

        RowData rowData;
        RowData rowData2;
        RowData rootRow;
        ExpressionTree expression;

        ClassDescription epochCD = DataModel.getClassDescription("Epoch");
        ClassDescription epochGroupCD = DataModel.getClassDescription(
            "EpochGroup");
        ClassDescription sourceCD = DataModel.getClassDescription("Source");
        ClassDescription responseCD = DataModel.getClassDescription("Response");
        ClassDescription derivedResponseCD = DataModel.getClassDescription(
            "DerivedResponse");
        ClassDescription externalDeviceCD = DataModel.getClassDescription(
            "ExternalDevice");

/*
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        //rowData.addAttribute(epochCD.getAttribute("epochGroup"));
        //rowData.addAttribute(epochGroupCD.getAttribute("source"));
        //rowData.addAttribute(epochCD.getAttribute("owner"));
        rowData.addAttribute(epochCD.getAttribute("incomplete"));
        //rowData.addAttribute(Attribute.IS_NULL);
        //rowData.setAttributeOperator(Operator.IS_NULL);
        rowData.setAttributeOperator(Operator.IS_TRUE);
        //rowData.setAttributeOperator(Operator.EQUALS);
        //rowData.setAttributeValue("xyz");
        rootRow.addChildRow(rowData);
*/
        /*
        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("protocolID"));
        rowData.setAttributeOperator(Operator.NOT_EQUALS);
        rowData.setAttributeValue("abc");
        rootRow.addChildRow(rowData);
        */

/*
        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("epochGroup"));
        rowData.addAttribute(epochGroupCD.getAttribute("source"));
        rowData.setAttributeOperator(Operator.IS_NULL);
        rootRow.addChildRow(rowData);

        testTranslation(rootRow);
*/
/*
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.NONE);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("responses"));
        rowData.setCollectionOperator(CollectionOperator.ALL);
        rowData.setCollectionOperator2(CollectionOperator.ANY);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.addAttribute(responseCD.getAttribute("uuid"));
        rowData2.setAttributeOperator(Operator.EQUALS);
        rowData2.setAttributeValue("xyz");
        rowData.addChildRow(rowData2);

        rowData2 = new RowData();
        rowData2.addAttribute(responseCD.getAttribute("samplingRate"));
        rowData2.setAttributeOperator(Operator.NOT_EQUALS);
        rowData2.setAttributeValue(new Double(1.23));
        rowData.addChildRow(rowData2);

        testTranslation(rootRow);
*/
/*
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ANY);

        rowData = new RowData();
        //rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("keywords"));
        rowData.setCollectionOperator(CollectionOperator.ANY);
        rowData.setCollectionOperator2(CollectionOperator.ANY);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.addAttribute(epochCD.getAttribute("uuid"));
        rowData2.setAttributeOperator(Operator.EQUALS);
        rowData2.setAttributeValue("xyz");
        rowData.addChildRow(rowData2);
*/
/*
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("protocolParameters"));
        rowData.setPropName("someTimeKey");
        rowData.setPropType(Type.DATE_TIME);
        rowData.setAttributeOperator(Operator.EQUALS);
        rowData.setAttributeValue(new Date(1262304000000L));
        rootRow.addChildRow(rowData);
*/
/*
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("prevEpoch"));
        rowData.addAttribute(epochCD.getAttribute("properties"));
        rowData.setPropName("someKey");
        rowData.setPropType(Type.INT_32);
        rowData.setAttributeOperator(Operator.NOT_EQUALS);
        rowData.setAttributeValue(new Integer(34));
        rootRow.addChildRow(rowData);
*/
/*
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ANY);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("myderivedResponses"));
        rowData.setCollectionOperator(CollectionOperator.NONE);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.addAttribute(derivedResponseCD.getAttribute(
                              "derivationParameters"));
        rowData2.setPropName("someKey");
        rowData2.setPropType(Type.BOOLEAN);
        rowData2.setAttributeOperator(Operator.IS_TRUE);
        rowData.addChildRow(rowData2);
*/
        rootRow = new RowData();
        rootRow.setClassUnderQualification(derivedResponseCD);
        rootRow.setCollectionOperator(CollectionOperator.ANY);

/*
        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("myderivedResponses"));
        rowData.setCollectionOperator(CollectionOperator.NONE);
        rootRow.addChildRow(rowData);
        */

        rowData2 = new RowData();
        rowData2.addAttribute(derivedResponseCD.getAttribute("mykeywords"));
        rowData2.setCollectionOperator2(CollectionOperator.ANY);
        rowData2.setCollectionOperator(CollectionOperator.COUNT);
        rowData2.setAttributeOperator(Operator.EQUALS);
        rowData2.setAttributeValue(new Integer(4));
        rootRow.addChildRow(rowData2);
/*
        rootRow = RowData.createTestRowData();
        System.out.println("\nRowData:\n"+rootRow);
*/
        System.out.println("\nRowData:\n"+rootRow);
        testTranslation(rootRow);
        /*
        rootRow.testSerialization();
        System.out.println("\nRowData:\n"+rootRow);
        expression = ExpressionTranslator.createExpressionTree(rootRow);
        System.out.println("\nExpression:\n"+expression);
        rootRow = ExpressionTranslator.createRowData(expression);
        System.out.println("\nExpression Translated To RowData:\n"+rootRow);
        */
    }


    /**
     * This is just a convenience method to call the other
     * testTranslation() method with the verbose flag set
     * to true and the exitOnFirstError flag set to true.
     */
    private static boolean testTranslation(Object someKindOfTree) {
        return(testTranslation(someKindOfTree, true, true));
    }


    /**
     * Test the translation of a RowData expression tree to
     * an ExpressionTree, AND test the translation of an
     * ExpressionTree to a RowData expression tree.
     *
     * You pass either a RowData root row object or
     * pass an ExpressionTree object.  The code will translate
     * from one class to the other and then back again, checking
     * that the results match.  If they don't match, an error
     * message is printed.
     *
     * @param someKindOfTree Pass either a RowData root row object
     * or pass an ExpressionTree object.
     *
     * @param verbose Pass true if you want the trees to be printed.
     * Pass false if all you want to see is the success or fail message.
     *
     * @param exitOnFirstError Pass true if you want this method to
     * call System.exit(1) if it will return false.
     *
     * @return Returns true if the translation worked.  False otherwise.
     */
    private static boolean testTranslation(Object someKindOfTree,
                                           boolean verbose,
                                           boolean exitOnFirstError) {

        RowData rootRow = null;
        ExpressionTree expressionTree = null;

        if (someKindOfTree instanceof RowData)
            rootRow = (RowData)someKindOfTree;
        else if (someKindOfTree instanceof ExpressionTree)
            expressionTree = (ExpressionTree)someKindOfTree;
        else {
            String s = "You must pass an Object of type RowData or "+
                "ExpressionTree.  You passed: "+someKindOfTree;
            throw(new IllegalArgumentException(s));
        }

        boolean same;
        if (rootRow != null) {
            System.out.println("\nStarting With RowData:\n"+rootRow);
            expressionTree = ExpressionTranslator.createExpressionTree(rootRow);
            System.out.println("\nRowData Translated To Expression:\n"+
                expressionTree);
            RowData newRowData = ExpressionTranslator.createRowData(
                expressionTree);
            System.out.println("\nExpressionTree Translated Back To RowData:\n"+
                newRowData);

            same = rootRow.toString(true, "").equals(
                newRowData.toString(true, ""));
        }
        else {
            System.out.println("\nStarting With ExpressionTree:\n"+
                expressionTree);
            rootRow = ExpressionTranslator.createRowData(expressionTree);
            System.out.println("\nExpressionTree Translated To RowData:\n"+
                rootRow);
            ExpressionTree newExpressionTree = ExpressionTranslator.
                createExpressionTree(rootRow);
            System.out.println("\nRowData Translated Back To Expression:\n"+
                newExpressionTree);

            same = expressionTree.toString().equals(
                newExpressionTree.toString());
        }

        if (same)
            System.out.println("\nOriginal and translated versions are "+
                               "the same.");
        else
            System.out.println("\nERROR:  Original and translated versions "+
                               "are different.");

        if (exitOnFirstError && !same)
            System.exit(1);

        return(same);
    }


    private static void printResults(String label, RowData rootRow) {

        System.out.println("\n===== "+label+" =====");

        /*
        ExpressionBuilder.ReturnValue returnValue;
        returnValue = ExpressionBuilder.editExpression(rootRow);
        if (returnValue.status != ExpressionBuilder.RETURN_STATUS_OK)
            System.exit(returnValue.status);
        */

        System.out.println("\nOriginal RowData:\n"+rootRow);

        ExpressionTree expression = ExpressionTranslator.createExpressionTree(
            rootRow);
        System.out.println("\nTranslated To Expression:\n"+expression);

        System.out.print("\nTest RowData Serialization: ");
        rootRow.testSerialization();

        System.out.print("\nTest ExpressionTree Serialization: ");
        expression.testSerialization();

        System.out.print("\nTest Translation: ");
        testTranslation(rootRow, true, true);

        //rootRow = ExpressionTranslator.createRowData(expression);
        //System.out.println("\nExpression Translated To RowData:\n"+rootRow);
    }


    /**
     * This is a simple test program for this class.
     */
    public static void main(String[] args) {

        System.out.println("ExpressionTranslator test is starting...");

        runAllTests();
        //runOneTest();

        System.out.println("\nExpressionTranslator test is ending.");
    }
}
