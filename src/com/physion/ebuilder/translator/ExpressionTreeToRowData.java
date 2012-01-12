/**
 * Copyright (c) 2011. Physion Consulting LLC
 * All rights reserved.
 */
package com.physion.ebuilder.translator;

import com.physion.ebuilder.datamodel.DataModel;
import com.physion.ebuilder.datamodel.RowData;
import com.physion.ebuilder.datatypes.*;
import com.physion.ebuilder.expression.*;

import java.util.ArrayList;
import java.util.List;


/**
 * This class is used to translate an ExpressionTree into a RowData.
 *
 * The ExpressionTree object is the class that most closely
 * maps to PQL.  It is the format that the "other" code in
 * the system understands.  The RowData object is the class
 * that represents the GUI's "view" of the data.
 *
 * The only method you will probably need to use is the
 * translate() method.
 *
 * @see RowDataToExpressionTree
 */
public class ExpressionTreeToRowData
    implements Translator {

    /**
     * This method turns the passed in ExpressionTree into a
     * RowData object.
     *
     * To turn a RowData into an ExpressionTree, use
     * the RowDataToExpressionTree.translate() class and method.
     *
     * @return translated RowData
     * @param expressionTree ExpressionTree to translate
     */
    public static RowData translate(ExpressionTree expressionTree) {

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
        String name = expressionTree.getClassUnderQualification();
        ClassDescription cuq = DataModel.getClassDescription(name);
        rootRow.setClassUnderQualification(cuq);

        IOperatorExpression oe = (IOperatorExpression)expressionTree.
            getRootExpression();

        rootRow.setCollectionOperator(getCOForOE(oe));

        /**
         * Convert the oe into a list of child RowData objects and
         * add them to the rootRow.
         */

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

        List<IExpression> operandList = oe.getOperandList();
        for (IExpression ex : operandList) {
            if (!(ex instanceof IOperatorExpression)) {
                String s = "Root IOperatorExpression("+oe.getOperatorName()+
                    ") had an operand that was not an IOperatorExpression().";
                throw(new IllegalArgumentException(s));
            }

            createAndAddChildRows(rootRow, (IOperatorExpression)ex, cuq);
        }

        return(rootRow);
    }


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
     *
     * @return child rows
     * @param oe operator expression
     */
    private static List<RowData> createChildRows(IOperatorExpression oe,
        ClassDescription classDescription) {

        /*
        System.out.println("\nEnter createChildRows()");
        System.out.println("oe: "+(Expression)oe);
        System.out.println("classDescription: "+classDescription);
        */

        List<RowData> childRows = new ArrayList<RowData>();

        /**
         * If the oe is null, return an empty list.
         */
        if (oe == null)
            return(childRows);

        List<IExpression> ol = oe.getOperandList();

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
            isElementsOfTypeOperator(tempEx)) {
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
             *              StringLiteralValueExpression(someKey)
             *              AttributeExpression(this)
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
             * Its second operand is the "path" to the attribute.
             *
             * Its first operand is the property "key".
             */

            if (oeAttributePath.getOperandList().size() != 2) {
                String s = "PER_USER_PARAMETERS_MAP IOperatorExpression("+
                    oeAttributePath.getOperatorName()+
                    ") does not have two operands.";
                throw(new IllegalArgumentException(s));
            }


            /**
             * Turn the first operand into the property name/key
             * for the row.
             */

            exTemp = oeAttributePath.getOperandList().get(0);
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
             * Now turn the second operand into the target.
             */

            exTemp = oeAttributePath.getOperandList().get(1);
            //setAttributePath(rowData, oeAttributePath, classDescription);
            //setAttributePath2(rowData, oeAttributePath, classDescription);
            setAttributePath1(rowData, oeAttributePath, classDescription);

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

                List<IExpression> operandList = oe.getOperandList();
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
                    //setAttributePath(rowData, firstOperand, classDescription);
                    //setAttributePath2(rowData, firstOperand, classDescription);
                    setAttributePath1(rowData, firstOperand, classDescription);
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
     *
     * @param rowData RowData
     * @param operandList list of operands
     * @param classDescription DataModel class description
     * @param attributeOperator operator
     */
    private static void setAttributeOperatorPathAndValue(RowData rowData,
        List<IExpression> operandList, ClassDescription classDescription,
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
        //setAttributePath(rowData, ex, classDescription);

        //System.out.println("\n*** Calling getChildmostClassDescription");
        //ClassDescription xxx = getChildmostClassDescription(ex,
        //                                                    classDescription);
        //System.out.println("*** childmostCD = "+xxx+"\n");

        //setAttributePath2(rowData, ex, classDescription);
        setAttributePath1(rowData, ex, classDescription);

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
     * @param classDescription data model class description
     */
    private static void createAndAddChildRows(RowData rowData,
        IOperatorExpression oe, ClassDescription classDescription) {

        List<RowData> childRows = createChildRows(oe, classDescription);
        rowData.addChildRows(childRows);
    }


    /**
     * This returns true if the passed in IExpression is
     * a PER_USER_OR_CUSTOM_REFERENCE_OPERATOR Attribute like
     * "keywords", "mykeywords", etc.
     *
     * @return true if the given expression is a per-user operator
     * @param ex
     * @param cd
     */
    private static boolean isPerUserOperator(IExpression ex,
                                             ClassDescription cd) {

        if (ex instanceof IOperatorExpression) {

            IOperatorExpression oe = (IOperatorExpression)ex;
            String name = oe.getOperatorName();

            //Attribute attribute = DataModel.getAttribute(name);
            Attribute attribute = cd.getAttribute(name);
            if ((attribute != null) &&
                (attribute.getType() ==
                 Type.PER_USER_OR_CUSTOM_REFERENCE_OPERATOR)) {
                return(true);
            }
        }

        return(false);
    }


    /**
     * This returns true if the passed in IExpression is
     * a PER_USER_PARAMETERS_MAP Attribute like "properties",
     * "myproperties", etc.
     *
     * @return true if the given expression is a per-user parameters
     * map operator
     * @param ex
     * @param cd
     */
    private static boolean isPerUserParametersMapOperator(IExpression ex,
                                                          ClassDescription cd) {

        if (ex instanceof IOperatorExpression) {

            IOperatorExpression oe = (IOperatorExpression)ex;
            String name = oe.getOperatorName();

            //Attribute attribute = DataModel.getAttribute(name);
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
     *
     * @return true if the given expression is an elements of type operator
     * @param ex
     */
    private static boolean isElementsOfTypeOperator(IExpression ex) {

        if (ex instanceof IOperatorExpression) {

            IOperatorExpression oe = (IOperatorExpression)ex;
            String name = oe.getOperatorName();

            if (OE_ELEMENTS_OF_TYPE.equals(name))
                return(true);
        }

        return(false);
    }


    /**
     * Parse a PARAMETERS_MAP attribute path.
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
     * The other, (i.e. right) operand should be AttributeExpression(value).
     * TODO:  Should we check that this is actually the case?
     *
     * The OperatorExpression(as) node should have two
     * operands:  OperatorExpression(parameter) and
     * ClassLiteralValueExpression(ovation.<someType>).
     * Where <someType> is a value like: ovation.DateValue,
     * ovation.FloatingPointValue, ovation.IntegerValue, etc.
     *
     * @param leftOperand Should be OperatorExpression(parameter)
     * @param rightOperand Tells us the property type.  e.g. int,
     * float, boolean, etc.
     */
    private static ClassDescription parseAsExpression(RowData rowData,
        IOperatorExpression leftOperand,
        IClassLiteralValueExpression rightOperand,
        ClassDescription classDescription, ClassDescription childmostCD) {

        /**
         * First, because it is simple and easy, handle
         * the rightOperand which gives us the property type.
         */
        Type type = getTypeForCLVE(rightOperand);
        rowData.setPropType(type);

        /**
         * Now deal with the leftOperand, which should be
         * OperatorExpression(parameter).
         *
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

        if (leftOperand.getOperandList().size() != 2) {
            String s = "IOperatorExpression(parameter) does not "+
                "have two operands.";
            throw(new IllegalArgumentException(s));
        }

        IExpression exTemp = leftOperand.getOperandList().get(0);
        //System.out.println("Calling setAttributePath2 to process path");
        ClassDescription childCD;
        childCD = setAttributePath2(rowData, exTemp, classDescription,
                                    childmostCD);
        //System.out.println("Returned childCD = "+childCD);

        exTemp = leftOperand.getOperandList().get(1);
        if (!(exTemp instanceof IStringLiteralValueExpression)) {
            String s = "IOperatorExpression(parameter)'s second "+
                "operand is not of type "+
                "IStringLiteralValueExpression.";
            throw(new IllegalArgumentException(s));
        }

        IStringLiteralValueExpression slve =
            (IStringLiteralValueExpression)exTemp;

        if ((slve == null) || (slve.getValue() == null)) {
            String s = "IOperatorExpression(parameter)'s second "+
                "operand is of type "+
                "IStringLiteralValueExpression but has a null value.  "+
                "Have you failed to call RowData.setPropName("+
                "\"<some string>\")?";
            throw(new IllegalArgumentException(s));
        }

        rowData.setPropName(slve.getValue().toString());

        return(childCD);
    }


    private static ClassDescription parseDotExpression(RowData rowData,
        IExpression leftOperand, IExpression rightOperand,
        ClassDescription classDescription, ClassDescription childmostCD) {
    
        ClassDescription childCD;

        /**
         * Parse the leftOperand.
         */
        childCD = setAttributePath2(rowData, leftOperand, classDescription,
                                    childmostCD);

        /**
         * Now check to make sure we want to parse the rightOperand.
         * We don't want to if the leftOperand is the "as" operator.
         */

        if (leftOperand instanceof IOperatorExpression) {
            IOperatorExpression oe = (IOperatorExpression)leftOperand;
            if (OE_AS.equals(oe.getOperatorName())) {

                /**
                 * Do NOT parse the rightOperand, which must be
                 * AttributeExpression(value).
                 */
                return(childCD);
            }
        }

        /**
         * Parse the rightOperand.
         */
        childCD = setAttributePath2(rowData, rightOperand, childCD,
                                    childmostCD);

        return(childCD);
    }


    /**
     * Parse an OperatorExpression(count) subtree, adding
     * Attributes to the passed in rowData's attributePath.
     *
     * @param rowData The RowData object for one row that
     * we are constructing.
     *
     * @param onlyOperand This should be the only operand in the
     * OperatorExpression(count) list of operands.
     *
     * @param classDescription The ClassDescription that is the
     * starting class for whatever Attributes are in the onlyOperand's
     * attributePath.
     *
     * @return The childmost ClassDescription of the attributePath.
     * I.e. the class of the rightmost Attribute.
     */
    private static ClassDescription parseCountExpression(RowData rowData,
        IExpression onlyOperand, ClassDescription classDescription,
        ClassDescription childmostCD) {
    
        ClassDescription childCD;

        childCD = setAttributePath2(rowData, onlyOperand, classDescription,
                                    childmostCD);

        return(childCD);
    }


    /**
     * Parse a PER_USER_PARAMETERS_MAP attribute.
     * For example, "My Property" OperatorExpression(myproperties) or
     * "Any Property" OperatorExpression(properties).
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
     *          StringLiteralValueExpression(someKey)
     *          OperatorExpression(.)
     *            OperatorExpression(.)
     *              AttributeExpression(nextEpoch)
     *              AttributeExpression(nextEpoch)
     *            AttributeExpression(prevEpoch)
     *        ClassLiteralValueExpression(ovation.IntegerValue)
     *      OperatorExpression(!=)
     *        AttributeExpression(value)
     *        Int32LiteralValueExpression(34)
     *
     * @param leftOperand An IStringLiteralValueExpression that is
     * the name of the property.  E.g. "prop27".
     *
     * @param rightOperand An IAttributeExpression such as "this", or an
     * IOperatorExpression such as the "." operator.
     *
     */
    private static ClassDescription parsePerUserParametersMap(RowData rowData,
        String attributeName,
        IStringLiteralValueExpression leftOperand,
        IExpression rightOperand, ClassDescription classDescription,
        ClassDescription childmostCD) {

        ClassDescription childCD = classDescription;

        /**
         * Now (possibly) parse the rightOperand.
         * Check whether the rightOperand is the special
         * AttributeExpression(this) value.
         */
        if ((rightOperand instanceof IAttributeExpression) &&
            AE_THIS.equals(((IAttributeExpression)rightOperand).
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
             * Traverse the subtree that defines the
             * attribute path to the special
             * operator.  E.g. traverse the
             * nextEpoch.nextEpoch.prevEpoch of the
             * example attribute path described above.
             */
            childCD = setAttributePath2(rowData, rightOperand,
                                        classDescription, childmostCD);
        }

        Attribute attribute = childCD.getAttribute(attributeName);
        /*
        System.out.println("Adding attribute \""+
            childCD.getName()+"."+attribute.getQueryName()+
            "\" to path.");
        */
        rowData.addAttribute(attribute);

        return(childCD);
    }


    /**
     * Parse a PER_USER_OR_CUSTOM_REFERENCE_OPERATOR attribute.
     * For example:
     *
     *      "My Keywords" OperatorExpression(mykeywords) 
     *      "Any Property" OperatorExpression(keywords)
     *      "My Notes" OperatorExpression(mynotes)
     *      "Any Notes" OperatorExpression(notes)
     *
     * @param rowData The RowData object for one row that
     * we are constructing.
     *
     * @param attributeName The name of the Attribute.
     * For example:  notes, mynotes, mykeywords.
     *
     * @param onlyOperand This should be the only operand in the
     * OperatorExpression(****) list of operands.
     *
     * @param classDescription The ClassDescription that is the
     * starting class for whatever Attributes are in the onlyOperand's
     * attributePath.
     *
     * @return The childmost ClassDescription of the attributePath.
     * I.e. the class of the rightmost Attribute.
     */
    private static ClassDescription parsePerUserOrCustomReferenceOperator(
        RowData rowData, String attributeName, IExpression onlyOperand,
        ClassDescription classDescription, ClassDescription childmostCD) {

        ClassDescription childCD = classDescription;

        if (!(onlyOperand instanceof IAttributeExpression) &&
            !(onlyOperand instanceof IOperatorExpression)) {

            String s = "IOperatorExpression("+attributeName+
                ")'s first operand is not of type IAttributeExpression, "+
                "nor is it of type IOperatorExpression.  "+
                "onlyOperand = "+onlyOperand;
            throw(new IllegalArgumentException(s));
        }
    
        /**
         * If the onlyOperand is the AttributeExpression(this) value,
         * ignore it as far as the RowData object's attributePath is
         * concerned.
         *
         * I.e. only call setAttributePath2() if it is NOT 
         * AttributeExpression(this).
         */
        boolean isAEThis = false;
        if ((onlyOperand instanceof IAttributeExpression) &&
            AE_THIS.equals(((IAttributeExpression)onlyOperand).
                             getAttributeName())) {
            isAEThis = true;
        }

        if (!isAEThis) {

            //System.out.println("Calling setAttributePath with onlyOperand");
            childCD = setAttributePath2(rowData, onlyOperand, classDescription,
                                        childmostCD);
        }

        Attribute attribute = childCD.getAttribute(attributeName);
        /*
        System.out.println("Adding attribute \""+
            childCD.getName()+"."+attribute.getQueryName()+
            "\" to path.");
        */
        rowData.addAttribute(attribute);

        return(childCD);
    }


    /**
     * Get the ClassDescription of the childmost Attribute in
     * the expression.
     */
    private static ClassDescription getChildmostClassDescription(
        IExpression ex, ClassDescription classDescription) {

        /*
        System.out.println("\nEnter getChildmostClassDescription");
        System.out.println("ex: "+((Expression)ex));
        System.out.println("classDescription: "+classDescription);
        */

        ClassDescription childCD = classDescription;

        if (ex instanceof IOperatorExpression) {

            IOperatorExpression oe = (IOperatorExpression)ex;
            //System.out.println("oe = "+oe.getOperatorName());

            IExpression leftOperand = oe.getOperandList().get(0);
            IExpression rightOperand = null;
            if (oe.getOperandList().size() > 1) {
                rightOperand = oe.getOperandList().get(1);
            }

            //System.out.println("leftOperand = "+leftOperand);
            //System.out.println("rightOperand = "+rightOperand);

            if (OE_DOT.equals(oe.getOperatorName())) {

                IAttributeExpression rightOperandAE = (IAttributeExpression)
                    rightOperand;
                 
                childCD = getChildmostClassDescription(leftOperand,
                    classDescription);

                /*
                System.out.println("Looking for attribute \""+
                    childCD.getName()+"."+rightOperandAE.getAttributeName()+
                    "\"");
                */
                Attribute attribute = childCD.getAttribute(
                    rightOperandAE.getAttributeName());

                if (attribute != null) {

                    /*
                    System.out.println("Found attribute \""+
                        childCD.getName()+"."+attribute.getQueryName()+
                        "\" in path.");
                    */
                    return(attribute.getClassDescription());
                }
                else {
                    return(childCD);
                }
            }
            else {
                childCD = getChildmostClassDescription(leftOperand,
                    classDescription);

                if (childCD == null) {
                    childCD = classDescription;
                }
            }
        }
        else if (ex instanceof IAttributeExpression) {
        
            IAttributeExpression ae = (IAttributeExpression)ex;
            //System.out.println("ae = "+ae.getAttributeName());

            Attribute attribute = classDescription.getAttribute(
                ae.getAttributeName());

            if ((attribute != null) &&
                (attribute.getClassDescription() != null)) {
                /*
                System.out.println("Found attribute \""+
                    classDescription.getName()+"."+attribute.getQueryName()+
                    "\" in path.");
                */
                childCD = attribute.getClassDescription();
            }

            return(childCD);
        }

        return(childCD);
    }


    /**
     * All this method does is first call getChildmostClassDescription(),
     * and then call setAttributePath2() using that value.
     *
     * This is because we don't know how to properly parse the
     * tree until we know what sort of Attributes we are parsing,
     * e.g. PER_USER_PARAMETER, etc., and we don't know that until
     * we can get the Attribute from the ClassDescription that contains it.
     */
    private static ClassDescription setAttributePath1(RowData rowData,
        IExpression ex, ClassDescription classDescription) {

        /**
         * Get the ClassDescription of the childmost Attribute.
         */
        ClassDescription childmostCD = getChildmostClassDescription(
            ex, classDescription);

        return(setAttributePath2(rowData, ex, classDescription, childmostCD));
    }


    /**
     * @return The ClassDescription (if applicable) of the
     * Attribute that we parsed and added last to the RowData's attributePath.
     */
    private static ClassDescription setAttributePath2(RowData rowData,
        IExpression ex, ClassDescription classDescription,
            ClassDescription childmostCD) {

        /*
        System.out.println("\nEnter setAttributePath2");
        System.out.println("rowData: "+rowData.getRowString());
        System.out.println("ex: "+((Expression)ex));
        System.out.println("classDescription: "+classDescription);
        System.out.println("ex instanceof IAttributeExpression = "+
                           (ex instanceof IAttributeExpression));
        */

        if (ex instanceof IOperatorExpression) {

            IOperatorExpression oe = (IOperatorExpression)ex;
            //System.out.println("oe = "+oe.getOperatorName());

            IExpression leftOperand = oe.getOperandList().get(0);
            IExpression rightOperand = null;
            if (oe.getOperandList().size() > 1) {
                rightOperand = oe.getOperandList().get(1);
            }

            //System.out.println("leftOperand = "+leftOperand);
            //System.out.println("rightOperand = "+rightOperand);

            ClassDescription childCD = null;

            if (OE_DOT.equals(oe.getOperatorName())) {
                childCD = parseDotExpression(rowData, leftOperand, rightOperand,
                                             classDescription, childmostCD);
            }
            else if (OE_COUNT.equals(oe.getOperatorName())) {
                childCD = parseCountExpression(rowData, leftOperand,
                                               classDescription, childmostCD);
            }
            else if (OE_AS.equals(oe.getOperatorName())) {

                if (!(leftOperand instanceof IOperatorExpression)) {
                    String s = "IOperatorExpression(as)'s first "+
                        "operand is not of type "+
                        "IOperatorExpression.  leftOperand = "+leftOperand;
                    throw(new IllegalArgumentException(s));
                }
                IOperatorExpression leftOperandOE =
                    (IOperatorExpression)leftOperand;
                if (!OE_PARAMETER.equals(leftOperandOE.getOperatorName())) {
                    String s = "IOperatorExpression(as) does not have "+
                        "IOperatorExpression(parameter) as its first "+
                        "operand.";
                    throw(new IllegalArgumentException(s));
                }
                if (!(rightOperand instanceof IClassLiteralValueExpression)) {
                    String s = "IOperatorExpression(as)'s second "+
                        "operand is not of type "+
                        "IClassLiteralValueExpression.  rightOperand = "+
                        rightOperand;
                    throw(new IllegalArgumentException(s));
                }

                childCD = parseAsExpression(rowData, leftOperandOE,
                    (IClassLiteralValueExpression)rightOperand,
                    classDescription, childmostCD);
            }
            //else if (isPerUserParametersMapOperator(oe, classDescription)) {
            else if (isPerUserParametersMapOperator(oe, childmostCD)) {

                if (!(leftOperand instanceof IStringLiteralValueExpression)) {
                    String s = "IOperatorExpression("+oe.getOperatorName()+
                        ")'s first operand is not of type "+
                        "IStringLiteralValueExpression.  leftOperand = "+
                        leftOperand;
                    throw(new IllegalArgumentException(s));
                }
                childCD = parsePerUserParametersMap(rowData,
                    oe.getOperatorName(),
                    (IStringLiteralValueExpression)leftOperand, rightOperand,
                    classDescription, childmostCD);
            }
            //else if (isPerUserOperator(oe, classDescription)) {
            else if (isPerUserOperator(oe, childmostCD)) {

                childCD = parsePerUserOrCustomReferenceOperator(rowData,
                    oe.getOperatorName(), leftOperand,
                    classDescription, childmostCD);
            }
            /* Should not get any of these
            else if (OE_ANY.equals(oe.getOperatorName())) {
            }
            else if (OE_OR.equals(oe.getOperatorName())) {
            }
            else if (OE_ALL.equals(oe.getOperatorName())) {
            }
            else if (OE_IS_NULL.equals(oe.getOperatorName())) {
            }
            else if (OE_PARAMETER.equals(oe.getOperatorName())) {
            }
            */
            else {
                /**
                 * An engineer needs to write some code to handle this
                 * new/unexpected operator.
                 */
                String s = "Unhandled IOperatorExpression: "+
                    oe.getOperatorName();
                throw(new IllegalArgumentException(s));
            }

            /*
            if (childCD == null) {
                childCD = classDescription;
            }
            */

            //System.out.println("Return childCD = "+childCD);
            return(childCD);
        }
        else if (ex instanceof IAttributeExpression) {
        
            IAttributeExpression ae = (IAttributeExpression)ex;
            //System.out.println("ae = "+ae.getAttributeName());

            Attribute attribute = classDescription.getAttribute(
                ae.getAttributeName());
            
            ClassDescription childCD = classDescription;
            if (attribute != null) {
                /*
                System.out.println("Adding attribute \""+
                    classDescription.getName()+"."+attribute.getQueryName()+
                    "\" to path.");
                */
                rowData.addAttribute(attribute);

                if (attribute.getClassDescription() != null) {
                    childCD = attribute.getClassDescription();
                }
            }
            else {
                /**
                 * Could be an Attribute such as "is null".
                 */
            }

            return(childCD);
        }
        else {
            /**
             * An engineer needs to write some code to handle this
             * new/unexpected operator, or the input ExpresssionTree
             * is structured badly.
             */
            String s = "Unhandled IExpression: "+ex.toString();
            throw(new IllegalArgumentException(s));
        }

        //System.out.println("Return null");
        //return(null);
        //return(classDescription);  // return value not matter?
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
         * The passed in IOperatorExpression cannot be mapped to an
         * attribute Operator.
         */
        return(null);
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
}
