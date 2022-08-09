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

package it.unicam.quasylab.jspear.speclang.values;

import it.unicam.quasylab.jspear.DataState;
import it.unicam.quasylab.jspear.Variable;
import it.unicam.quasylab.jspear.speclang.types.JSpearCustomType;
import it.unicam.quasylab.jspear.speclang.types.JSpearType;

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public sealed interface JSpearValue permits JSpearBoolean, JSPearInteger, JSpearReal, JSpearArray, JSpearCustomValue, JSpearErrorValue  {

    JSpearValue ERROR_VALUE = new JSpearErrorValue();

    static JSpearValue of(JSpearType type, Variable variable, DataState ds) {
        if (type.isAnArray()) {
            JSpearValue[] elements = IntStream.range(variable.getFirstCellIndex(), variable.getFirstCellIndex()+variable.getSize())
                    .mapToObj(i -> JSpearValue.realValue(ds.getValue(i)))
                    .toArray(JSpearValue[]::new);
            return new JSpearArray(elements);
        }
        if (type.isInteger()) {
            return new JSPearInteger((int) ds.getValue(variable.getFirstCellIndex()));
        }
        if (type.isReal()) {
            return new JSpearReal(ds.getValue(variable.getFirstCellIndex()));
        }
        if (type.isBoolean()) {
            return JSpearBoolean.getBooleanValue(ds.getValue(variable.getFirstCellIndex())>0);
        }
        if (type.isCustom()) {
            return new JSpearCustomValue((JSpearCustomType) type, (int) ds.getValue(variable.getFirstCellIndex()));
        }
        return JSpearValue.ERROR_VALUE;
    }

    static JSpearValue realValue(double value) {
        return new JSpearReal(value);
    }

    JSpearType getJSpearType();

    default JSpearValue sum(JSpearValue v) {
        return JSpearValue.ERROR_VALUE;
    }

    default JSpearValue product(JSpearValue v)  {
        return JSpearValue.ERROR_VALUE;
    }

    default JSpearValue subtraction(JSpearValue v)  {
        return JSpearValue.ERROR_VALUE;
    }

    default  JSpearValue division(JSpearValue v)  {
        return JSpearValue.ERROR_VALUE;
    }

    default  JSpearValue modulo(JSpearValue v)  {
        return JSpearValue.ERROR_VALUE;
    }

    default  JSpearValue apply(DoubleBinaryOperator op, JSpearValue v)  {
        return JSpearValue.ERROR_VALUE;
    }

    default  JSpearValue apply(DoubleUnaryOperator op) {
        return JSpearValue.ERROR_VALUE;
    }

    default JSpearValue negate() {
        return JSpearValue.ERROR_VALUE;
    }

    default JSpearValue isLessThan(JSpearValue v) {
        return JSpearBoolean.FALSE;
    }

    default JSpearValue isLessOrEqualThan(JSpearValue v) {
        return JSpearBoolean.FALSE;
    }

    default JSpearValue isEqualTo(JSpearValue v) {
        return JSpearBoolean.FALSE;
    }

    default JSpearValue isGreaterOrEqualThan(JSpearValue v) {
        return JSpearBoolean.FALSE;
    }

    default JSpearValue isGreaterThan(JSpearValue v) {
        return JSpearBoolean.FALSE;
    }

    default JSpearValue and(JSpearValue v) {
        return JSpearValue.ERROR_VALUE;
    }

    default JSpearValue select(JSpearValue v) {
        return JSpearValue.ERROR_VALUE;
    }

    default JSpearValue select(JSpearValue from, JSpearValue to) {
        return JSpearValue.ERROR_VALUE;
    }

   default JSpearValue or(JSpearValue v)  {
       return JSpearValue.ERROR_VALUE;
   }

    default boolean booleanOf()  {
        return false;
    }


    default double doubleOf() {
        return Double.NaN;
    }

    default int integerOf() {
        return Integer.MIN_VALUE;
    }

    default boolean isInteger() {
        return false;
    }

    default boolean isReal() {
        return false;
    }

    default boolean isBoolean() {
        return false;
    }

    default JSpearValue maxElement(Predicate<JSpearValue> predicate) {
        return JSpearValue.ERROR_VALUE;
    }

    default JSpearValue maxElement() {
        return maxElement(v -> true);
    }

    default JSpearValue minElement(Predicate<JSpearValue> predicate) {
        return JSpearValue.ERROR_VALUE;
    }

    default JSpearValue minElement() {
        return minElement(v -> true);
    }

    default JSpearValue meanElement(Predicate<JSpearValue> predicate) {
        return JSpearValue.ERROR_VALUE;
    }

    default JSpearValue meanElement()  {
        return meanElement(v -> true);
    }

    default JSpearValue count(Predicate<JSpearValue> predicate) {
        return JSpearValue.ERROR_VALUE;
    }

    default JSpearValue count()  {
        return count(v -> true);
    }


}
