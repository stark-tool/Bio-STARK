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
 * This type is used to represent predicates over array elements that are used in the array operators.
 */
public final class JSpearArrayElementPredicateType implements JSpearType {

    private static JSpearArrayElementPredicateType instance;

    public static JSpearType getInstance() {
        if (instance == null) {
            instance = new JSpearArrayElementPredicateType();
        }
        return instance;
    }


    private JSpearArrayElementPredicateType() {}

    @Override
    public JSpearType merge(JSpearType other) {
        boolean randomType = false;
        if (other instanceof JSpearRandomType) {
            randomType = true;
            other = other.deterministicType();
        }
        if ((other instanceof JSpearBooleanType)||(other instanceof JSpearArrayElementPredicateType)) {
            return (randomType?new JSpearRandomType(this):this);
        }
        return ERROR_TYPE;
    }

    @Override
    public boolean isCompatibleWith(JSpearType other) {
        return (other instanceof JSpearBooleanType)||(other instanceof JSpearArrayElementPredicateType);
    }

    @Override
    public boolean isNumerical() {
        return false;
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
        return isCompatibleWith(other);
    }
}
