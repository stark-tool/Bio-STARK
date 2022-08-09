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

import it.unicam.quasylab.jspear.speclang.types.JSpearType;

import java.util.Arrays;
import java.util.OptionalDouble;
import java.util.function.Predicate;

public final class JSpearArray implements JSpearValue {

    private final JSpearValue[] elements;

    public JSpearArray(JSpearValue[] elements) {
        this.elements = elements;
    }

    @Override
    public JSpearType getJSpearType() {
        return JSpearType.ARRAY_TYPE;
    }

    @Override
    public JSpearValue select(JSpearValue v) {
        if (v.isInteger()) {
            return elements[v.integerOf()];
        }
        return JSpearValue.ERROR_VALUE;
    }

    @Override
    public JSpearValue select(JSpearValue from, JSpearValue to) {
        if (from.isInteger()&&to.isInteger()) {
            return new JSpearArray(Arrays.stream(this.elements, from.integerOf(), to.integerOf()).toArray(JSpearValue[]::new));
        }
        return JSpearValue.ERROR_VALUE;
    }

    @Override
    public JSpearValue maxElement(Predicate<JSpearValue> predicate) {
        OptionalDouble oValue = Arrays.stream(this.elements).filter(predicate).mapToDouble(JSpearValue::doubleOf).max();
        if (oValue.isPresent()) {
            return new JSpearReal(oValue.getAsDouble());
        } else {
            return new JSpearReal(Double.NEGATIVE_INFINITY);
        }
    }


    @Override
    public JSpearValue minElement(Predicate<JSpearValue> predicate) {
        OptionalDouble oValue = Arrays.stream(this.elements).filter(predicate).mapToDouble(JSpearValue::doubleOf).min();
        if (oValue.isPresent()) {
            return new JSpearReal(oValue.getAsDouble());
        } else {
            return new JSpearReal(Double.POSITIVE_INFINITY);
        }
    }


    @Override
    public JSpearValue meanElement(Predicate<JSpearValue> predicate) {
        OptionalDouble oValue = Arrays.stream(this.elements).filter(predicate).mapToDouble(JSpearValue::doubleOf).average();
        if (oValue.isPresent()) {
            return new JSpearReal(oValue.getAsDouble());
        } else {
            return new JSpearReal(0.0);
        }
    }


    @Override
    public JSpearValue count(Predicate<JSpearValue> predicate) {
        return new JSPearInteger((int) Arrays.stream(this.elements).filter(predicate).count());
    }

    @Override
    public JSpearValue count() {
        return new JSPearInteger(this.elements.length);
    }
}
