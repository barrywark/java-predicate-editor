package com.physion.ovation.gui.ebuilder.datatypes;


/**
 * Please note, "REFERENCE" is not a primitive type.  That value is
 * used to denote types that are a reference to a class of some sort.
 */
public enum Type {

    /**
     * These are the "primitive" types.
     */
    BOOLEAN, UTF_8_STRING, INT_16, INT_32, FLOAT_64, DATE_TIME,

    /**
     * This is a "reference" type.  I.e. a reference to a class of
     * some sort.
     */
    REFERENCE,

    /**
     * This is "per-user" reference type.
     * (Not sure if this is really correct.)
     */
    PER_USER;


    /**
     * This returns true if this Type is a "primitive" type.
     * E.g. an int, float, boolean, etc.
     */
    public boolean isPrimitive() {
        return((this != REFERENCE) && (this != PER_USER));
    }


    /**
     * This is a simple test program for this class.
     */
    public static void main(String[] args) {

        System.out.println("Type test is starting...");

        for (Type type : Type.values())
            System.out.println(type+".isPrimitive() = "+type.isPrimitive());

        System.out.println("Type test is ending.");
    }
}
