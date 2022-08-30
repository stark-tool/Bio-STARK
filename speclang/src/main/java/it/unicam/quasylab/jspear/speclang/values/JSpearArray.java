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
import java.util.Objects;
import java.util.function.DoublePredicate;
import java.util.function.IntToDoubleFunction;
import java.util.stream.IntStream;

public final class JSpearArray implements JSpearValue {

    private final IntToDoubleFunction elements;

    private final int size;

    public JSpearArray(int size, IntToDoubleFunction elements) {
        this.size = size;
        this.elements = elements;
    }

    public JSpearArray(JSpearValue[] elements) {
        this(elements.length, i -> JSpearValue.doubleOf(elements[i]));
    }

    public JSpearArray(double[] elements) {
        this(elements.length, i -> elements[i]);
    }

    @Override
    public JSpearType getJSpearType() {
        return JSpearType.ARRAY_TYPE;
    }


    public JSpearValue select(JSpearValue from, JSpearValue to) {
        if (from instanceof JSPearInteger fromInt) {
            if (to instanceof JSPearInteger toInt) {
                return new JSpearArray(toInt.value() - fromInt.value(), i -> this.elements.applyAsDouble(i + fromInt.value()));
            }
        }
        return JSpearValue.ERROR_VALUE;
    }


    public JSpearValue select(JSpearValue index) {
        if (index instanceof JSPearInteger fromInt) {
            if ((fromInt.value() >= 0) || (fromInt.value() < size)) {
                return new JSpearReal(this.elements.applyAsDouble(((JSPearInteger) index).value()));
            }
        }
        return JSpearValue.ERROR_VALUE;
    }


    public JSpearValue maxElement(JSpearValue guard) {
        if (guard instanceof JSpearBoolean booleanValue) {
            return (booleanValue.value() ? maxElement() : JSpearReal.NEGATIVE_INFINITY);
        }
        if (guard instanceof JSpearArrayElementPredicate predicateValue) {
            return maxElement(predicateValue.getPredicate());
        }
        return JSpearValue.ERROR_VALUE;
    }

    public JSpearValue maxElement() {
        return maxElement(d -> true);
    }

    private JSpearValue maxElement(DoublePredicate predicate) {
        return new JSpearReal(IntStream.range(0, size).mapToDouble(this.elements).filter(predicate).max().orElse(Double.NEGATIVE_INFINITY));
    }

    public JSpearValue minElement(JSpearValue guard) {
        if (guard instanceof JSpearBoolean booleanValue) {
            return (booleanValue.value() ? minElement() : JSpearReal.POSITIVE_INFINITY);
        }
        if (guard instanceof JSpearArrayElementPredicate predicateValue) {
            return minElement(predicateValue.getPredicate());
        }
        return JSpearValue.ERROR_VALUE;
    }

    public JSpearValue minElement() {
        return minElement(d -> true);
    }

    private JSpearValue minElement(DoublePredicate predicate) {
        return new JSpearReal(IntStream.range(0, size).mapToDouble(this.elements).filter(predicate).min().orElse(Double.POSITIVE_INFINITY));
    }

    public JSpearValue meanElement(JSpearValue guard) {
        if (guard instanceof JSpearBoolean booleanValue) {
            return (booleanValue.value() ? meanElement() : JSpearReal.POSITIVE_INFINITY);
        }
        if (guard instanceof JSpearArrayElementPredicate predicateValue) {
            return meanElement(predicateValue.getPredicate());
        }
        return JSpearValue.ERROR_VALUE;
    }

    public JSpearValue meanElement() {
        return meanElement(d -> true);
    }

    private JSpearValue meanElement(DoublePredicate predicate) {
        return new JSpearReal(IntStream.range(0, size).mapToDouble(this.elements).filter(predicate).average().orElse(0.0));
    }

    public JSpearValue count(JSpearValue guard) {
        if (guard instanceof JSpearBoolean booleanValue) {
            return (booleanValue.value() ? count() : new JSPearInteger(0));
        }
        if (guard instanceof JSpearArrayElementPredicate predicateValue) {
            return count(predicateValue.getPredicate());
        }
        return JSpearValue.ERROR_VALUE;
    }

    public JSpearValue count() {
        return count(d -> true);
    }

    private JSpearValue count(DoublePredicate predicate) {
        return new JSPearInteger((int) IntStream.range(0, size).mapToDouble(this.elements).filter(predicate).count());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JSpearArray that = (JSpearArray) o;
        if (size == that.size) {
            return IntStream.range(0, size).allMatch(i -> this.elements.applyAsDouble(i)==that.elements.applyAsDouble(i));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(IntStream.range(0, size).mapToDouble(this.elements).toArray()), size);
    }

    public int lenght() {
        return this.size;
    }

    public double get(int i) {
        return this.elements.applyAsDouble(i);
    }
}