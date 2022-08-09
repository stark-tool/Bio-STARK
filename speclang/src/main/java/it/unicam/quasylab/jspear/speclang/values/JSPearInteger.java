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

public final class JSPearInteger implements JSpearValue {
    private final int value;

    public JSPearInteger(int value) {
        this.value = value;
    }

    @Override
    public JSpearType getJSpearType() {
        return JSpearType.INTEGER_TYPE;
    }

    @Override
    public JSpearValue sum(JSpearValue v) {
        if (v.isInteger()) {
            return new JSPearInteger(this.value+v.integerOf());
        }
        if (v.isReal()) {
            return new JSpearReal(this.value+v.doubleOf());
        }
        return JSpearValue.ERROR_VALUE;
    }

    @Override
    public JSpearValue product(JSpearValue v) {
        if (v.isInteger()) {
            return new JSPearInteger(this.value*v.integerOf());
        }
        if (v.isReal()) {
            return new JSpearReal(this.value*v.doubleOf());
        }
        return JSpearValue.ERROR_VALUE;
    }

    @Override
    public JSpearValue subtraction(JSpearValue v) {
        if (v.isInteger()) {
            return new JSPearInteger(this.value-v.integerOf());
        }
        if (v.isReal()) {
            return new JSpearReal(this.value-v.doubleOf());
        }
        return JSpearValue.ERROR_VALUE;
    }

    @Override
    public JSpearValue division(JSpearValue v) {
        //TODO: Handle division by zero if needed!
        if (v.isInteger()) {
            return new JSPearInteger(this.value/v.integerOf());
        }
        if (v.isReal()) {
            return new JSpearReal(this.value/v.doubleOf());
        }
        return JSpearValue.ERROR_VALUE;
    }

    @Override
    public JSpearValue modulo(JSpearValue v) {
        if (v.isInteger()) {
            return new JSPearInteger(this.value%v.integerOf());
        }
        if (v.isReal()) {
            return new JSpearReal(this.value%v.doubleOf());
        }
        return JSpearValue.ERROR_VALUE;
    }

    @Override
    public JSpearValue isLessThan(JSpearValue v) {
        if (v.isInteger()||v.isReal()) {
            return JSpearBoolean.getBooleanValue(this.value < v.doubleOf());
        }
        return JSpearValue.ERROR_VALUE;
    }

    @Override
    public JSpearValue isLessOrEqualThan(JSpearValue v) {
        if (v.isInteger()||v.isReal()) {
            return JSpearBoolean.getBooleanValue(this.value <= v.doubleOf());
        }
        return JSpearValue.ERROR_VALUE;
    }

    @Override
    public JSpearValue isEqualTo(JSpearValue v) {
        if (v.isInteger()||v.isReal()) {
            return JSpearBoolean.getBooleanValue(this.value == v.doubleOf());
        }
        return JSpearValue.ERROR_VALUE;
    }

    @Override
    public JSpearValue isGreaterOrEqualThan(JSpearValue v) {
        if (v.isInteger()||v.isReal()) {
            return JSpearBoolean.getBooleanValue(this.value >= v.doubleOf());
        }
        return JSpearValue.ERROR_VALUE;
    }

    @Override
    public JSpearValue isGreaterThan(JSpearValue v) {
        if (v.isInteger()||v.isReal()) {
            return JSpearBoolean.getBooleanValue(this.value > v.doubleOf());
        }
        return JSpearValue.ERROR_VALUE;
    }

    @Override
    public double doubleOf() {
        return this.value;
    }

    @Override
    public int integerOf() {
        return this.value;
    }

    @Override
    public boolean isInteger() {
        return true;
    }
}
