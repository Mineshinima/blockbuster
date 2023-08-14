package com.mineshinima.mclib.utils;

/**
 * The Java Cloneable interface is only a marker interface to enforce
 * a convention to override the protected Object.clone() method with a public method.
 * This does not allow for dynamic validation whether an object actually has a public clone() method
 * and makes working with generics more difficult.
 *
 * This interface should provide better knowledge at compile time whether a generic object
 * has a public copy method.
 * @param <T> by convention this should be the type of the class that inherits this interface.
 *           This allows for a better usage with generics when copying.
 *           As this convention cannot be enforced, it is your fault if you get ClassCastExceptions ;) .
 */
public interface ICopy<T>
{
    T copy();

    void copy(T origin);
}
