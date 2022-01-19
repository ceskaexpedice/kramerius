/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package cz.incad.kramerius.utils.java;

import java.io.Serializable;

//import javafx.beans.NamedArg;

/**
 * <p>A convenience class to represent pairs of values.</p>
 *
 * @since JavaFX 2.0
 */
public class Pair<K, V> implements Serializable {

    /**
     * First value of this <code>Pair</code>.
     */
    private K first;

    /**
     * Gets the first value for this pair.
     *
     * @return first value for this pair
     */
    public K getFirst() {
        return first;
    }

    /**
     * Second value of this this <code>Pair</code>.
     */
    private V second;

    /**
     * Gets the second value for this pair.
     *
     * @return second value for this pair
     */
    public V getSecond() {
        return second;
    }

    /**
     * Creates a new pair
     *
     * @param first  The first value for this pair
     * @param second The second value to use for this pair
     */
    public Pair( K first, V second) {
        this.first = first;
        this.second = second;
    }

    /**
     * <p><code>String</code> representation of this
     * <code>Pair</code>.</p>
     *
     * <p>The default first/second delimiter ';' is always used.</p>
     *
     * @return <code>String</code> representation of this <code>Pair</code>
     */
    @Override
    public String toString() {
        return first + ";" + second;
    }

    /**
     * <p>Generate a hash code for this <code>Pair</code>.</p>
     *
     * <p>The hash code is calculated using both the first and
     * the second value of the <code>Pair</code>.</p>
     *
     * @return hash code for this <code>Pair</code>
     */
    @Override
    public int hashCode() {
        // name's hashCode is multiplied by an arbitrary prime number (13)
        // in order to make sure there is a difference in the hashCode between
        // these two parameters:
        //  first: a  second: aa
        //  first: aa second: a
        return first.hashCode() * 13 + (second == null ? 0 : second.hashCode());
    }

    /**
     * <p>Test this <code>Pair</code> for equality with another
     * <code>Object</code>.</p>
     *
     * <p>If the <code>Object</code> to be tested is not a
     * <code>Pair</code> or is <code>null</code>, then this method
     * returns <code>false</code>.</p>
     *
     * <p>Two <code>Pair</code>s are considered equal if and only if
     * both the first and the second values are equal.</p>
     *
     * @param o the <code>Object</code> to test for
     *          equality with this <code>Pair</code>
     * @return <code>true</code> if the given <code>Object</code> is
     * equal to this <code>Pair</code> else <code>false</code>
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Pair) {
            Pair pair = (Pair) o;
            if (first != null ? !first.equals(pair.first) : pair.first != null) return false;
            if (second != null ? !second.equals(pair.second) : pair.second != null) return false;
            return true;
        }
        return false;
    }
}

