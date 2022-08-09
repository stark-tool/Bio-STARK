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
import org.apache.commons.math3.random.RandomGenerator;

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoublePredicate;
import java.util.function.DoubleUnaryOperator;

public final class JSpearReal implements JSpearValue {

    private final double value;

    public JSpearReal(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public static JSpearValue sampleNormal(RandomGenerator rg, JSpearValue v1, JSpearValue v2) {
        return new JSpearReal(rg.nextDouble()*v1.doubleOf()+v2.doubleOf());
    }

    public static JSpearValue sample(RandomGenerator rg, JSpearValue from, JSpearValue to) {
        double fromValue = from.doubleOf();
        double gapValue = to.doubleOf()-fromValue;
        return new JSpearReal(fromValue+rg.nextDouble()*gapValue);
    }

    @Override
    public JSpearType getJSpearType() {
        return JSpearType.REAL_TYPE;
    }


    @Override
    public JSpearValue sum(JSpearValue v) {
        return apply(Double::sum, v);
    }

    @Override
    public JSpearValue product(JSpearValue v) {
        return apply((x,y)->x*y, v);
    }

    @Override
    public JSpearValue subtraction(JSpearValue v) {
        return apply((x,y)->x-y, v);
    }

    @Override
    public JSpearValue division(JSpearValue v) {
        return apply((x,y)->x/y, v);
    }

    @Override
    public JSpearValue modulo(JSpearValue v) {
        return apply((x,y)->x%y, v);
    }

    @Override
    public JSpearValue apply(DoubleBinaryOperator op, JSpearValue v) {
        double otherValue = v.doubleOf();
        if (Double.isNaN(otherValue)) {
            return new JSpearReal(Double.NaN);
        } else {
            return new JSpearReal(op.applyAsDouble(this.value, otherValue));
        }
    }

    @Override
    public JSpearValue apply(DoubleUnaryOperator op) {
        if (Double.isNaN(this.value)) {
            return this;
        } else {
            return new JSpearReal(op.applyAsDouble(this.value));
        }
    }

    @Override
    public double doubleOf() {
        return this.value;
    }

    @Override
    public JSpearValue isLessThan(JSpearValue v) {
        return test(y -> this.value<y, v);
    }

    private static JSpearValue test(DoublePredicate p, JSpearValue v) {
        double otherValue = v.doubleOf();
        if (Double.isNaN(otherValue)) {
            return JSpearValue.ERROR_VALUE;
        } else {
            return JSpearBoolean.getBooleanValue(p.test(otherValue) );
        }
    }

    @Override
    public JSpearValue isLessOrEqualThan(JSpearValue v) {
        return test(y -> this.value<=y, v);
    }

    @Override
    public JSpearValue isEqualTo(JSpearValue v) {
        return test(y -> this.value==y, v);
    }

    @Override
    public JSpearValue isGreaterOrEqualThan(JSpearValue v) {
        return test(y -> this.value>=y, v);
    }

    @Override
    public JSpearValue isGreaterThan(JSpearValue v) {
        return test(y -> this.value>y, v);
    }

    @Override
    public int integerOf() {
        return (int) this.value;
    }

    @Override
    public boolean isReal() {
        return true;
    }
}
