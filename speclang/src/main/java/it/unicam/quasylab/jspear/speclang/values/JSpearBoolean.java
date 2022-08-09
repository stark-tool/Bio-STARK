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

public final class JSpearBoolean implements JSpearValue {

    public static final JSpearValue TRUE = new JSpearBoolean(true);
    public static final JSpearValue FALSE = new JSpearBoolean(false);
    private final boolean value;

    private JSpearBoolean(boolean value) {
        this.value = value;
    }

    @Override
    public JSpearValue negate() {
        return getBooleanValue(!this.value);
    }

    public static JSpearValue getBooleanValue(boolean value) {
        return (value?TRUE:FALSE);
    }

    @Override
    public JSpearValue and(JSpearValue v) {
        if (v.isBoolean()) {
            return getBooleanValue(this.value&&v.booleanOf());
        }
        return JSpearValue.ERROR_VALUE;
    }

    @Override
    public JSpearValue or(JSpearValue v) {
        if (v.isBoolean()) {
            return getBooleanValue(this.value||v.booleanOf());
        }
        return JSpearValue.ERROR_VALUE;
    }

    @Override
    public boolean booleanOf() {
        return this.value;
    }

    @Override
    public boolean isBoolean() {
        return true;
    }


    @Override
    public JSpearType getJSpearType() {
        return JSpearType.BOOLEAN_TYPE;
    }
}
