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

public sealed interface JSpearType permits JSpearErrorType, JSpearBooleanType, JSpearIntegerType, JSpearRealType, JSpearArrayType, JSpearCustomType, JSpearAnyType {


    JSpearType ERROR_TYPE = new JSpearErrorType();
    JSpearType BOOLEAN_TYPE = new JSpearBooleanType();
    JSpearType INTEGER_TYPE = new JSpearIntegerType();

    JSpearType REAL_TYPE = new JSpearRealType();

    JSpearType ARRAY_TYPE = new JSpearArrayType();

    JSpearType ANY_TYPE = new JSpearAnyType();



    JSpearType merge(JSpearType other);

    static JSpearType merge(JSpearType one, JSpearType other) {
        return one.merge(other);
    }

    /**
     * This method returns true if <code>this</code> type is compatible with
     * the <code>other</code>. Namely, if we can assign to a variable of <code>this</code>
     * type a value of the <code>other</code> type.
     *
     * @param other a type.
     * @return true if <code>this</code> type is compatible with the <code>other</code>.
     */
    boolean isCompatibleWith(JSpearType other);

    boolean isNumerical();

    boolean isAnArray();


    boolean isError();


    boolean canBeMergedWith(JSpearType other);
}
