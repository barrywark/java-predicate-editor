/**
 * Copyright (c) 2011. Physion Consulting LLC
 * All rights reserved.
 */
package com.physion.ebuilder.datatypes;


/**
 * There are four collection operators:  Count, Any, All, None.
 *
 * Count is kind of special, because it does not really apply to
 * a collection, but instead is a count of the number of references
 * to an attribute that has a to-many relationship.
 * For example, the epochGroup attribute has a to-many relationship
 * with a collection of Epoch objects.
 *
 * The other operators, Any/All/None, are used to specify whether
 * an expression is true if any, all, or none of the expressions
 * in the collection are true.
 */
public enum CollectionOperator {

    /**
     * These are the four values we can have:
     *
     *      CollectionOperator.COUNT
     *      CollectionOperator.ANY
     *      CollectionOperator.ALL
     *      CollectionOperator.NONE
     */
    COUNT, ANY, ALL, NONE;


    /**
     * An array of all the CollectionOperator values that is
     * useful when creating a ComboBoxModel to be used for a
     * JComboBox.  This is all operators except for the COUNT
     * operator.
     */
    private static CollectionOperator[] compoundCollectionOperators = null;


    /**
     * This returns true if the current value is one
     * that denotes a "compound" operator.
     * E.g. ANY, ALL, NONE.
     */
    public boolean isCompoundOperator() {
        return(this != COUNT);
    }


    /**
     * Get this CollectionOperator value as a user friendly string
     * suitable for displaying in the GUI.
     */
    public String toString() {

        switch(this) {
            case COUNT:
                return("Count");
            case ANY:
                return("Any");
            case ALL:
                return("All");
            case NONE:
                return("None");
        }

        return("ERROR: CollectionOperator.toString() = "+super.toString());
    }


    /**
     * This returns an array of all CollectionOperators that are
     * "Compound" operators.  I.e. all operators except for the
     * COUNT operator.
     *
     * The array returned can be used to create a ComboBoxModel.
     */
    public static CollectionOperator[] getCompoundCollectionOperators() {

        /**
         * Initialize the values in the compoundCollectionOperators array
         * if they have not been created before.
         */
        if (compoundCollectionOperators == null) {

            compoundCollectionOperators = new CollectionOperator[
                CollectionOperator.values().length-1];

            int index = 0;
            for (CollectionOperator collectionOperator :
                 CollectionOperator.values())
                if (collectionOperator != CollectionOperator.COUNT)
                    compoundCollectionOperators[index++] = collectionOperator;
        }

        return(compoundCollectionOperators);
    }


    /**
     * This is a simple test program for this class.
     */
    /*
    public static void main(String[] args) {

        System.out.println("CollectionOperator test is starting...");

        for (CollectionOperator collectionOperator :
             CollectionOperator.values())
            System.out.println(collectionOperator+".isCompoundOperator() = "+
                               collectionOperator.isCompoundOperator());

        System.out.println("CollectionOperator test is ending.");
    }
    */
}
