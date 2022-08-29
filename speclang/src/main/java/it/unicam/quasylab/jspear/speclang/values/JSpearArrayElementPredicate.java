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

import java.util.function.DoublePredicate;

public final class JSpearArrayElementPredicate implements JSpearValue {

    public static final JSpearValue TRUE = new JSpearArrayElementPredicate(d -> true);
    public static final JSpearValue FALSE = new JSpearArrayElementPredicate(d -> false);
    private final DoublePredicate predicate;

    public JSpearArrayElementPredicate(DoublePredicate predicate) {
        this.predicate = predicate;
    }


    @Override
    public JSpearType getJSpearType() {
        return JSpearType.ARRAY_ELEMENT_PREDICATE;
    }

    public  JSpearValue negate() {
        return new JSpearArrayElementPredicate(predicate.negate());
    }

    public JSpearValue and(JSpearValue other) {
        if (other instanceof JSpearBoolean booleanValue) {
            return (booleanValue.value()?this:FALSE);
        }
        if (other instanceof JSpearArrayElementPredicate predicateValue) {
            return new JSpearArrayElementPredicate(this.predicate.and(predicateValue.predicate));
        }
        return JSpearValue.ERROR_VALUE;
    }

    public JSpearValue or(JSpearValue other) {
        if (other instanceof JSpearBoolean booleanValue) {
            return (booleanValue.value()?TRUE:this);
        }
        if (other instanceof JSpearArrayElementPredicate predicateValue) {
            return new JSpearArrayElementPredicate(this.predicate.or(predicateValue.predicate));
        }
        return JSpearValue.ERROR_VALUE;
    }

    public DoublePredicate getPredicate() {
        return predicate;
    }
}
