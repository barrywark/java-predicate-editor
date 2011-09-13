package com.physion.ovation.gui.ebuilder.datamodel;


/**
 * TODO:  Many member data and values in here should be statics.
 */
public enum CollectionOperator {

    /**
     * 
     */
    COUNT, ANY, ALL, NONE;


    private static CollectionOperator[] compoundCollectionOperators;


    static {

        compoundCollectionOperators = new CollectionOperator[
            CollectionOperator.values().length-1];

        int index = 0;
        for (CollectionOperator collectionOperator :
             CollectionOperator.values())
            if (collectionOperator != CollectionOperator.COUNT)
                compoundCollectionOperators[index++] = collectionOperator;

    }


    /**
     * This returns true if the current value is one
     * that denotes a "compound" operator.
     * E.g. ANY, ALL, NONE.
     */
    public boolean isCompoundOperator() {
        return(this != COUNT);
    }


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
        return(compoundCollectionOperators);
    }


    /**
     * This is a simple test program for this class.
     */
    public static void main(String[] args) {

        System.out.println("CollectionOperator test is starting...");

        for (CollectionOperator collectionOperator :
             CollectionOperator.values())
            System.out.println(collectionOperator+".isCompoundOperator() = "+
                               collectionOperator.isCompoundOperator());

        System.out.println("CollectionOperator test is ending.");
    }
}
