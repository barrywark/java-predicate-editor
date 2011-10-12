package com.physion.ovation.gui.ebuilder.expression;

import java.util.ArrayList;

import com.physion.ovation.gui.ebuilder.datatypes.CollectionOperator;
//import com.physion.ovation.gui.ebuilder.datatypes.Attribute;
//import com.physion.ovation.gui.ebuilder.datatypes.Type;
//import com.physion.ovation.gui.ebuilder.datatypes.Operator;
import com.physion.ovation.gui.ebuilder.datamodel.RowData;
//import com.physion.ovation.gui.ebuilder.datamodel.DataModel;


/**
 */
public class OperatorExpressionImp 
    extends ExpressionImp
    implements OperatorExpression {

    private String operatorName;
    private ArrayList<Expression> operandList;


    public OperatorExpressionImp() {
        operatorName = null;
        operandList = new ArrayList<Expression>();
    }


    /**
     * Get the operatorName for this OperatorExpression.
     *
     * This method is required by the IOperatorExpression
     * interface.
     */
    @Override
    public String getOperatorName() {
        return(operatorName);
    }


    /**
     * This method returns the operandList.
     *
     * Please note, the returned list is NOT a copy
     * of this OperatorExpression's operandList, so
     * don't mess with it.
     *
     * Please note, the returned list might be empty,
     * but it is never null.
     *
     * This method is required by the IOperatorExpression
     * interface.
     */
    @Override
    public ArrayList<Expression> getOperandList() {
        return(operandList);
    }


    /**
     * Create an OperatorExpressionImp object from a
     * RowData object.
     */
    OperatorExpressionImp(RowData rowData) {
        this();

        operatorName = getOperatorName(rowData);

        for (RowData childRow : rowData.getChildRows()) {
            addOperand(childRow);
        }
    }


    private void addOperand(RowData rowData) {

        OperatorExpression operatorExpression =
            new OperatorExpressionImp(rowData);
        addOperand(operatorExpression);
    }


    /**
     * Convert the passed in RowData's CollectionOperator enum value
     * to the string value PQL expects.
     */
    private String getOperatorName(RowData rowData) {

        if (rowData.getCollectionOperator() != null)
            return(getCollectionOperatorName(rowData));
        else {
            if (rowData.getAttributeOperator() != null) {
                return(rowData.getAttributeOperator().toString());
            }
        }

        return("ERROR");
    }


    private String getCollectionOperatorName(RowData rowData) {

        /**
         * If this is NOT the root row, then the collection
         * operator is simply the lower case version of the
         * collection operator's string value.
         * E.g. All becomes "all".
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

            case NONE:
                return("none?");

            case COUNT:
                return("count?");

            default:
                return("ERROR");
        }
    }


    private void addOperand(Expression expression) {
        operandList.add(expression);
    }


    /**
     * Get a string version of this class that can be used
     * for debugging purposes.
     */
    public String toString() {
        return(toString(""));
    }


    /**
     * Get a string version of this class that can be used
     * for debugging purposes.
     *
     * @param indent The lines of the returned string will
     * all be indented by (at least) this amount.
     */
    public String toString(String indent) {

        String string = indent;
        string += "OperatorExpression("+getOperatorName()+")\n";

        for (Expression expression : getOperandList()) {
            string += indent+expression.toString();
        }
        return(string);
    }
}
