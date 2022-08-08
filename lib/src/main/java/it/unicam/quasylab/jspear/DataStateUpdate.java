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

package it.unicam.quasylab.jspear;

import java.util.Objects;

public final class DataStateUpdate {
    private final int cellIndex;
    private final double value;
    private final Variable variable;


    public DataStateUpdate(Variable variable, double value) {
        this(variable, 0, value);
    }

    public DataStateUpdate(Variable variable, int cellIndex, double value) {
        if (variable == null) {
            throw new IllegalArgumentException("No variable provided!" );
        }
        this.variable = variable;
        this.cellIndex = cellIndex;
        this.value = value;
    }

    public Variable getVariable() {
        return variable;
    }

    public int getUpdatedCell() {
        return cellIndex;
    }

    public int getUpdatedVariableElement() {
        return cellIndex-variable.getFirstCellIndex();
    }

    public double value() {
        return value;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataStateUpdate that = (DataStateUpdate) o;
        return cellIndex == that.cellIndex && Double.compare(that.value, value) == 0 && getVariable().equals(that.getVariable());
    }

    @Override
    public int hashCode() {
        return Objects.hash(cellIndex, value, getVariable());
    }

    @Override
    public String toString() {
        return this.variable.name()+"@"+getUpdatedVariableElement()+"="+value;
    }
}
