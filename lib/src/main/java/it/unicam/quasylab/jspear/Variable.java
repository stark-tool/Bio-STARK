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

/**
 * Class used to identify a variable in a model.
 */
public final class Variable {
    /**
     * This is a reference to the registry where the variable is alloated.
     */
    private final VariableRegistry registry;

    /**
     * Variable name.
     */
    private final String name;

    /**
     * Minimum value that this variable can assume.
     */
    private final double minValue;

    /**
     * Maximum value that this variable can assume.
     */
    private final double maxValue;

    /**
     * Number of cells occupied by this variable
     */
    private final int size;

    /**
     * Index of the first cell associated with this variable.
     */
    private final int firstCellIndex;


    /**
     * Creates a new variable storing a single double value in the given cell.
     *
     * @param registry variable registry containing this variable.
     * @param name name of this variable.
     * @param cellIndex index of the cell where the value of this variable is stored.
     */
    public Variable(VariableRegistry registry, String name, int cellIndex) {
        this(registry, name, cellIndex, 1);
    }

    public Variable(VariableRegistry registry, String name, int firstCellIndex, int size) {
        this(registry, name, firstCellIndex, size, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }


    /**
         */
    public Variable(VariableRegistry registry, String name, int firstCellIndex, int size, double minValue, double maxValue) {
        this.registry = registry;
        this.firstCellIndex = firstCellIndex;
        this.name = name;
        this.size = size;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }


    public Variable(VariableRegistry registry, String name, int cellIndex, double minValue, double maxValue) {
        this(registry, name, cellIndex, 1, minValue, maxValue);
    }

    public VariableRegistry registry() {
        return registry;
    }

    public int getFirstCellIndex() {
        return firstCellIndex;
    }

    public String name() {
        return name;
    }

    public double minValue() {
        return minValue;
    }

    public double maxValue() {
        return maxValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Variable) obj;
        return Objects.equals(this.registry, that.registry) &&
                this.firstCellIndex == that.firstCellIndex &&
                this.size == that.getSize() &&
                Objects.equals(this.name, that.name) &&
                Double.doubleToLongBits(this.minValue) == Double.doubleToLongBits(that.minValue) &&
                Double.doubleToLongBits(this.maxValue) == Double.doubleToLongBits(that.maxValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(registry, firstCellIndex, size, name, minValue, maxValue);
    }

    @Override
    public String toString() {
        return "Variable[" +
                "registry=" + registry + ", " +
                "index=" + firstCellIndex + ", " +
                "size=" + size + ", " +
                "name=" + name + ", " +
                "minValue=" + minValue + ", " +
                "maxValue=" + maxValue + ']';
    }


    public int getSize() {
        return size;
    }
}
