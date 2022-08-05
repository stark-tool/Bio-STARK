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

package it.unicam.quasylab.jspear.speclang;

public enum JSpearType {

    ERROR_TYPE,
    BOOLEAN_TYPE,
    INTEGER_TYPE,
    REAL_TYPE,
    ARRAY_TYPE,
    ANY_TYPE;



    static JSpearType merge(JSpearType one, JSpearType other) {
        if ((other==ANY_TYPE)||(one.isCompatibleWith(other))) {
            return one;
        }
        if ((one==ANY_TYPE)||(other.isCompatibleWith(one))) {
            return other;
        }
        return ERROR_TYPE;
    }

    /**
     * This method returns true if <code>this</code> type is compatible with
     * the <code>other</code>. Namely, if we can assign to a variable of <code>this</code>
     * type a value of the <code>other</code> type.
     *
     * @param other a type.
     * @return true if <code>this</code> type is compatible with the <code>other</code>.
     */
    public boolean isCompatibleWith(JSpearType other) {
        return (this==ERROR_TYPE)||(this==other)||((this==REAL_TYPE)&&(other==INTEGER_TYPE));
    }

    boolean isNumerical() {
        return (this==INTEGER_TYPE)||(this==REAL_TYPE);
    }

    boolean isAnArray() {
        return (this==ARRAY_TYPE);
    }


    boolean isError() {
        return (this==ERROR_TYPE);
    }


    public boolean canBeMergedWith(JSpearType other) {
        return (this==ANY_TYPE)||(other==ANY_TYPE)||(this.isCompatibleWith(other)||other.isCompatibleWith(this));
    }
}
