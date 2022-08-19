/*
 * JSpear: a SimPle Environment for statistical estimation of Adaptation and Reliability.
 *
 *              Copyright (C) 2020.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.unicam.quasylab.jspear.speclang.types;

/**
 * A class representing an integer type.
 */
public final class JSpearIntegerType implements JSpearType {

    private static JSpearIntegerType instance;

    public static JSpearIntegerType getInstance() {
        if (instance == null) {
            instance = new JSpearIntegerType();
        }
        return instance;
    }

    public JSpearIntegerType() {}

    @Override
    public JSpearType merge(JSpearType other) {
        if ((this == other)||(other==ANY_TYPE)) return this;
        if ((other.deterministicType()==REAL_TYPE)||(other.deterministicType()==INTEGER_TYPE)) return other;
        return JSpearType.ERROR_TYPE;
    }

    @Override
    public boolean isCompatibleWith(JSpearType other) {
        return (other instanceof JSpearIntegerType);
    }

    @Override
    public boolean isNumerical() {
        return true;
    }

    @Override
    public boolean isAnArray() {
        return false;
    }

    @Override
    public boolean isError() {
        return false;
    }

    @Override
    public boolean canBeMergedWith(JSpearType other) {
        JSpearType deterministicOther = other.deterministicType();
        return (this==deterministicOther)||(other == JSpearType.ANY_TYPE)||(deterministicOther==REAL_TYPE);
    }

    @Override
    public boolean isInteger() {
        return true;
    }

    @Override
    public String toString() {
        return JSpearType.INTEGER_TYPE_STRING;
    }

}
