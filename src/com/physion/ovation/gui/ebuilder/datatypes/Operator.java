/**
 * Copyright (c) 2011. Physion Consulting LLC
 * All rights reserved.
 */
package com.physion.ovation.gui.ebuilder.datatypes;

import java.util.Arrays;
import java.util.ArrayList;

/**
 * These are the "primitive", (as opposed to "collection"), operators
 * that the system uses.  For example "==", ">", "is null", "is true".
 * But not collection operators like Any, All, None.
 */
public enum Operator {

    EQUALS, NOT_EQUALS, LESS_THAN, GREATER_THAN, LESS_THAN_EQUALS,
    GREATER_THAN_EQUALS, MATCHES_CASE_SENSITIVE, MATCHES_CASE_INSENSITIVE,
    DOES_NOT_MATCH_CASE_SENSITIVE, DOES_NOT_MATCH_CASE_INSENSITIVE,

    IS_NULL, IS_NOT_NULL,

    IS_TRUE, IS_FALSE;


    public static final Operator[] OPERATORS_BOOLEAN =
        {IS_TRUE, IS_FALSE};

    public static final Operator[] OPERATORS_ARITHMATIC =
        {EQUALS, NOT_EQUALS, LESS_THAN, GREATER_THAN, LESS_THAN_EQUALS,
         GREATER_THAN_EQUALS};

    public static final Operator[] OPERATORS_STRING =
        {EQUALS, NOT_EQUALS, LESS_THAN, GREATER_THAN, LESS_THAN_EQUALS,
         GREATER_THAN_EQUALS, MATCHES_CASE_SENSITIVE, MATCHES_CASE_INSENSITIVE,
         DOES_NOT_MATCH_CASE_SENSITIVE, DOES_NOT_MATCH_CASE_INSENSITIVE};

    public static final Operator[] OPERATORS_DATE_TIME =
        {EQUALS, NOT_EQUALS, LESS_THAN, GREATER_THAN, LESS_THAN_EQUALS,
         GREATER_THAN_EQUALS, IS_NULL, IS_NOT_NULL};


    /**
     * Get a string version of this enum value that can be used
     * in the GUI.
     */
    public String toString() {

        switch (this) {
            case EQUALS:
                return("==");
            case NOT_EQUALS:
                return("!=");
            case LESS_THAN:
                return("<");
            case GREATER_THAN:
                return(">");
            case LESS_THAN_EQUALS:
                return("<=");
            case GREATER_THAN_EQUALS:
                return(">=");
            case MATCHES_CASE_SENSITIVE:
                return("=~");
            case MATCHES_CASE_INSENSITIVE:
                return("=~~");
            case DOES_NOT_MATCH_CASE_SENSITIVE:
                return("!~");
            case DOES_NOT_MATCH_CASE_INSENSITIVE:
                return("!~~");
            case IS_NULL:
                return("is null");
            case IS_NOT_NULL:
                return("is not null");
            case IS_TRUE:
                return("is true");
            case IS_FALSE:
                return("is false");
            default:
                System.err.println("ERROR:  Unhandled Operator enum.\n"+
                    "If this happens, you need to update the switch\n"+
                    "statement in Operator.toString().");
                return("ERROR");
        }
    }


    /**
     * Get the Operator that corresponds to the passed in string.
     */
    public static Operator fromString(String string) {

        for (Operator operator : values()) {
            if (operator.toString().equals(string)) {
                return(operator);
            }
        }

        System.err.println("ERROR:  Illegal string passed to method\n"+
            "Operator.fromString().\n"+
            "If this happens, there is a bug in the code.");
        /**
         * We might want to throw an exception.
         */
        return(null);
    }


    /**
     * Returns true if the passed in operator is a legal boolean
     * operator.  E.g. it is "is true" or "is false".
     */
    public static boolean isOperatorBoolean(Operator operator) {

        ArrayList<Operator> operators = new ArrayList<Operator>(
            Arrays.asList(OPERATORS_BOOLEAN));
        return(operators.contains(operator));
    }


    /**
     * Returns true if the passed in operator is a legal arithmatic
     * operator.  E.g. it is "==", "!=", ">=", etc.
     */
    public static boolean isOperatorArithmatic(Operator operator) {

        ArrayList<Operator> operators = new ArrayList<Operator>(
            Arrays.asList(OPERATORS_ARITHMATIC));
        return(operators.contains(operator));
    }


    /**
     * Returns true if the passed in operator is a legal date/time
     * operator.  E.g. it is "==", "!=", "is null", etc.
     */
    public static boolean isOperatorDateTime(Operator operator) {

        ArrayList<Operator> operators = new ArrayList<Operator>(
            Arrays.asList(OPERATORS_DATE_TIME));
        return(operators.contains(operator));
    }


    /**
     * Returns true if the passed in operator is a legal string
     * operator.  E.g. it is "==", "!=", ">=", "~~=", etc.
     */
    public static boolean isOperatorString(Operator operator) {

        ArrayList<Operator> operators = new ArrayList<Operator>(
            Arrays.asList(OPERATORS_STRING));
        return(operators.contains(operator));
    }



    private String toStringDebug() {
        return(super.toString()+": "+toString());
    }


    /**
     * This is a simple test program for this class.
     */
    public static void main(String[] args) {

        System.out.println("Operator test is starting...");

        for (Operator operator : Operator.values())
            System.out.println(operator.toStringDebug());

        System.out.println("Operator test is ending.");
    }
}
