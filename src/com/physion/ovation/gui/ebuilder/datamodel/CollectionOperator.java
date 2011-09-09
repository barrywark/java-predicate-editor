package com.physion.ovation.gui.ebuilder.datamodel;


/**
 */
public enum CollectionOperator {

    /**
     * 
     */
    COUNT, ANY, ALL, NONE;


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
