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
     * This is a "parameters map" type.
     */
    PARAMETERS_MAP,

    /**
     * This is "per-user" reference type.
     */
    PER_USER,

    /**
     * This is "per-user parameters map" reference type.
     * As of September 2011, the only attribute of this
     * type is the EntityBase.properties attribute.
     */
    PER_USER_PARAMETERS_MAP;


    /**
     * This returns true if this Type is a "primitive" type.
     * E.g. an int, float, boolean, etc.
     */
    public boolean isPrimitive() {
        return((this == BOOLEAN) ||
               (this == UTF_8_STRING) ||
               (this == INT_16) ||
               (this == INT_32) ||
               (this == FLOAT_64) ||
               (this == DATE_TIME));
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
