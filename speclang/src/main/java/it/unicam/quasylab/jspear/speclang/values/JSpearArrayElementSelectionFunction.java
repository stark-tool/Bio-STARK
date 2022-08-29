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

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

public final class JSpearArrayElementSelectionFunction implements JSpearValue {

    private final DoubleUnaryOperator selectionFunction;

    public JSpearArrayElementSelectionFunction(DoubleUnaryOperator selectionFunction) {
        this.selectionFunction = selectionFunction;
    }

    public JSpearArrayElementSelectionFunction() {
        this(d -> d);
    }

    @Override
    public JSpearType getJSpearType() {
        return JSpearType.ARRAY_ELEMENT_SELECTION_FUNCTION;
    }

    public JSpearValue apply(DoubleUnaryOperator op) {
        return new JSpearArrayElementSelectionFunction(selectionFunction.andThen(op));
    }

    public JSpearValue sum(JSpearValue v) {
        if (v instanceof JSPearInteger intValue) {
            return new JSpearArrayElementSelectionFunction(d -> selectionFunction.applyAsDouble(d)+intValue.value());
        }
        if (v instanceof JSpearReal realValue) {
            return new JSpearArrayElementSelectionFunction(d -> selectionFunction.applyAsDouble(d)+realValue.value());
        }
        if (v instanceof JSpearArrayElementSelectionFunction function) {
            return new JSpearArrayElementSelectionFunction(d -> this.selectionFunction.applyAsDouble(d)+function.selectionFunction.applyAsDouble(d));
        }
        return JSpearValue.ERROR_VALUE;
    }

    public JSpearValue product(JSpearValue v) {
        if (v instanceof JSPearInteger intValue) {
            return new JSpearArrayElementSelectionFunction(d -> selectionFunction.applyAsDouble(d)*intValue.value());
        }
        if (v instanceof JSpearReal realValue) {
            return new JSpearArrayElementSelectionFunction(d -> selectionFunction.applyAsDouble(d)*realValue.value());
        }
        if (v instanceof JSpearArrayElementSelectionFunction function) {
            return new JSpearArrayElementSelectionFunction(d -> this.selectionFunction.applyAsDouble(d)*function.selectionFunction.applyAsDouble(d));
        }
        return JSpearValue.ERROR_VALUE;

    }

    public JSpearValue subtraction(JSpearValue v) {
        if (v instanceof JSPearInteger intValue) {
            return new JSpearArrayElementSelectionFunction(d -> selectionFunction.applyAsDouble(d)-intValue.value());
        }
        if (v instanceof JSpearReal realValue) {
            return new JSpearArrayElementSelectionFunction(d -> selectionFunction.applyAsDouble(d)-realValue.value());
        }
        if (v instanceof JSpearArrayElementSelectionFunction function) {
            return new JSpearArrayElementSelectionFunction(d -> this.selectionFunction.applyAsDouble(d)-function.selectionFunction.applyAsDouble(d));
        }
        return JSpearValue.ERROR_VALUE;
    }

    public JSpearValue division(JSpearValue v) {
        if (v instanceof JSPearInteger intValue) {
            return new JSpearArrayElementSelectionFunction(d -> selectionFunction.applyAsDouble(d)/intValue.value());
        }
        if (v instanceof JSpearReal realValue) {
            return new JSpearArrayElementSelectionFunction(d -> selectionFunction.applyAsDouble(d)/realValue.value());
        }
        if (v instanceof JSpearArrayElementSelectionFunction function) {
            return new JSpearArrayElementSelectionFunction(d -> this.selectionFunction.applyAsDouble(d)/function.selectionFunction.applyAsDouble(d));
        }
        return JSpearValue.ERROR_VALUE;
    }

    public JSpearValue modulo(JSpearValue v) {
        if (v instanceof JSPearInteger intValue) {
            return new JSpearArrayElementSelectionFunction(d -> selectionFunction.applyAsDouble(d)%intValue.value());
        }
        if (v instanceof JSpearReal realValue) {
            return new JSpearArrayElementSelectionFunction(d -> selectionFunction.applyAsDouble(d)%realValue.value());
        }
        if (v instanceof JSpearArrayElementSelectionFunction function) {
            return new JSpearArrayElementSelectionFunction(d -> this.selectionFunction.applyAsDouble(d)%function.selectionFunction.applyAsDouble(d));
        }
        return JSpearValue.ERROR_VALUE;

    }

    public JSpearValue apply(DoubleBinaryOperator op, JSpearValue v) {
        if (v instanceof JSPearInteger intValue) {
            return new JSpearArrayElementSelectionFunction(d -> op.applyAsDouble(selectionFunction.applyAsDouble(d), intValue.value()));
        }
        if (v instanceof JSpearReal realValue) {
            return new JSpearArrayElementSelectionFunction(d -> op.applyAsDouble(selectionFunction.applyAsDouble(d), realValue.value()));
        }
        if (v instanceof JSpearArrayElementSelectionFunction function) {
            return new JSpearArrayElementSelectionFunction(d -> op.applyAsDouble(selectionFunction.applyAsDouble(d), function.selectionFunction.applyAsDouble(d)));
        }
        return JSpearValue.ERROR_VALUE;
    }

    public double apply(double d) {
        return this.selectionFunction.applyAsDouble(d);
    }


    public JSpearValue isLessThan(JSpearValue other) {
        if (other instanceof JSPearInteger intValue) {
            return new JSpearArrayElementPredicate(d -> this.selectionFunction.applyAsDouble(d) <intValue.value());
        }
        if (other instanceof JSpearReal realValue) {
            return new JSpearArrayElementPredicate(d -> this.selectionFunction.applyAsDouble(d) <realValue.value());
        }
        if (other instanceof JSpearArrayElementSelectionFunction function) {
            return new JSpearArrayElementPredicate(d -> this.selectionFunction.applyAsDouble(d) <function.selectionFunction.applyAsDouble(d));
        }
        return JSpearValue.ERROR_VALUE;
    }

    public JSpearValue isLessOrEqualThan(JSpearValue other) {
        if (other instanceof JSPearInteger intValue) {
            return new JSpearArrayElementPredicate(d -> this.selectionFunction.applyAsDouble(d) <= intValue.value());
        }
        if (other instanceof JSpearReal realValue) {
            return new JSpearArrayElementPredicate(d -> this.selectionFunction.applyAsDouble(d) <= realValue.value());
        }
        if (other instanceof JSpearArrayElementSelectionFunction function) {
            return new JSpearArrayElementPredicate(d -> this.selectionFunction.applyAsDouble(d) <= function.selectionFunction.applyAsDouble(d));
        }
        return JSpearValue.ERROR_VALUE;
    }

    public JSpearValue isEqualTo(JSpearValue other) {
        if (other instanceof JSPearInteger intValue) {
            return new JSpearArrayElementPredicate(d -> this.selectionFunction.applyAsDouble(d) == intValue.value());
        }
        if (other instanceof JSpearReal realValue) {
            return new JSpearArrayElementPredicate(d -> this.selectionFunction.applyAsDouble(d) == realValue.value());
        }
        if (other instanceof JSpearArrayElementSelectionFunction function) {
            return new JSpearArrayElementPredicate(d -> this.selectionFunction.applyAsDouble(d) == function.selectionFunction.applyAsDouble(d));
        }
        return JSpearValue.ERROR_VALUE;
    }

    public JSpearValue isGreaterOrEqualThan(JSpearValue other) {
        if (other instanceof JSPearInteger intValue) {
            return new JSpearArrayElementPredicate(d -> this.selectionFunction.applyAsDouble(d) >= intValue.value());
        }
        if (other instanceof JSpearReal realValue) {
            return new JSpearArrayElementPredicate(d -> this.selectionFunction.applyAsDouble(d) >= realValue.value());
        }
        if (other instanceof JSpearArrayElementSelectionFunction function) {
            return new JSpearArrayElementPredicate(d -> this.selectionFunction.applyAsDouble(d) >= function.selectionFunction.applyAsDouble(d));
        }
        return JSpearValue.ERROR_VALUE;
    }

    public JSpearValue isGreaterThan(JSpearValue other) {
        if (other instanceof JSPearInteger intValue) {
            return new JSpearArrayElementPredicate(d -> this.selectionFunction.applyAsDouble(d) > intValue.value());
        }
        if (other instanceof JSpearReal realValue) {
            return new JSpearArrayElementPredicate(d -> this.selectionFunction.applyAsDouble(d) > realValue.value());
        }
        if (other instanceof JSpearArrayElementSelectionFunction function) {
            return new JSpearArrayElementPredicate(d -> this.selectionFunction.applyAsDouble(d) > function.selectionFunction.applyAsDouble(d));
        }
        return JSpearValue.ERROR_VALUE;
    }


}
