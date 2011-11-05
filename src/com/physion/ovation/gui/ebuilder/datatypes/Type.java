/**
 * Copyright (c) 2011. Physion Consulting LLC
 * All rights reserved.
 */
package com.physion.ovation.gui.ebuilder.datatypes;


/**
 * These are the types an attribute can be.
 *
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
     * For example, the attributes:
     *
     *      IOBase.externalDeviceParameters
     *      Epoch.protocolParameters
     *      Stimulus.stimulusParameters
     *      DerivedResponse.derivationParameters
     *      AnalysisRecord.analysisParameters
     */
    PARAMETERS_MAP,

    /**
     * This is "per-user" reference type.
     * For example, the attributes:
     *
     *      TaggableEntityBase.keywords
     *      Project.analysisRecords
     *      Epoch.derivedResponses
     */
    PER_USER,

    /**
     * This is "per-user parameters map" reference type.
     * As of September 2011, the only attribute of this
     * type is the attribute:
     *
     *      EntityBase.properties
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
     * Get a string version of this enum value that can be used
     * in the GUI.
     */
    public String toString() {

        switch (this) {
            case BOOLEAN:
                return("boolean");
            case UTF_8_STRING:
                return("string");
            case INT_16:
                return("short");
            case INT_32:
                return("int");
            case FLOAT_64:
                return("float");
            case DATE_TIME:
                return("time");
            case REFERENCE:
                return("reference");
            case PER_USER:
                return("per-user");
            case PARAMETERS_MAP:
                return("parameters map");
            case PER_USER_PARAMETERS_MAP:
                return("per-user parameters map");
            default:
                System.err.println("ERROR:  Unhandled Type enum.\n"+
                    "If this happens, you need to update the switch\n"+ 
                    "statement in Type.toString().");
                return("ERROR");
        }
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
