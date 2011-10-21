package com.physion.ovation.gui.ebuilder.datatypes;

/**
 * This is used to specify the relationship of an attribute
 * to the class which contains it.  For example, an Epoch
 * has an attribute "epochGroups" that has is a reference
 * "to-many" Epoch objects.  But, an "owner" relationship
 * is be a "to-one" relationship.
 *
 * Please note, N_A means "not applicable" and is not really
 * a type of cardinality.
 */
public enum Cardinality {
    N_A, TO_ONE, TO_MANY
}
