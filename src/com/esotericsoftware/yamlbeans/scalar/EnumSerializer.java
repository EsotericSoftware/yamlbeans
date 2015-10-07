package com.esotericsoftware.yamlbeans.scalar;

/**
 * Serializer interface for Enumerations.
 * Uses the same interface as ScalarSerializer, but enforces the class to be an Enum.
 *
 * @author Arno Moonen <arno.moonen@altran.com>
 */
public interface EnumSerializer<T extends Enum> extends ScalarSerializer<T> {
}
