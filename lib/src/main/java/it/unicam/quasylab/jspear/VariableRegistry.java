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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoublePredicate;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * Instances of used to define the set of variables occurring in a model.
 */
public final class VariableRegistry {

    private final Map<String,Variable> variableRegistry;

    private int cellCounter = 0;

    /**
     * Creates a registry containing the given variables. An {@link IllegalArgumentException} is thrown if
     * a duplicated name occurs among the parameters.
     */
    public VariableRegistry() {
        this.variableRegistry = new HashMap<>();
    }

    public static VariableRegistry create(String ... names) {
        VariableRegistry vr = new VariableRegistry();
        for (String name : names) {
            vr.addVariable(name);
        }
        return vr;
    }

    public Variable addVariable(String name) {
        return this.addVariable(name, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }
    public Variable addVariable(String name, double minValue, double maxValue) {
        return addVariable(name, 1, minValue, maxValue);
    }

    private Variable addVariable(String name, int size, double minValue, double maxValue) {
        if (this.variableRegistry.containsKey(name)) {
            throw new IllegalArgumentException(String.format("Duplicated name %s.", name));
        } else {
            Variable v = new Variable(this, name, cellCounter, size, minValue, maxValue);
            this.cellCounter += size;
            this.variableRegistry.put(name, v);
            return v;
        }
    }


    public Variable addArray(String name, int length) {
        return this.addArray(name, length, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    public Variable addArray(String name, int length, double minValue, double maxValue) {
        return addVariable(name, length, minValue, maxValue);
    }


    /**
     * Returns the variable in this registry with the given name. A <code>null</code> value is
     * returned if no variable in the registry exists with the given name.
     *
     * @param name variable name.
     * @return the variable in this registry with the given name.
     */
    public synchronized Variable getVariable(String name) {
        return variableRegistry.get(name);
    }

    /**
     * Returns the number of cells needed to store the variables in this registry.
     *
     * @return the number of variables in this registry.
     */
    public int size() {
        return cellCounter;
    }

    /**
     * Returns the number of variables in this registry.
     *
     * @return the number of variables in this registry.
     */
    public int numberOfVariables() {
        return this.variableRegistry.size();
    }

    /**
     * Returns the index of the variable with the given name if exists, otherwise <code>-1</code> is
     * returned.
     *
     * @param name variable name.
     * @return the index of the variable with the given name.
     */
    public int getIndexOf(String name) {
        Variable v = getVariable(name);
        if (v == null) {
            return -1;
        } else {
            return v.getFirstCellIndex();
        }
    }

    public int getIndexOf(String name, int i) {
        Variable v = getVariable(name);
        if ((v == null)||(i<0)||(i>v.getSize())) {
            return -1;
        } else {
            return v.getFirstCellIndex()+i;
        }
    }

    /**
     * Returns a predicate used to check if in the data set the given variable is
     * greater or equal
     *
     * @param var
     * @return
     */
    public Predicate<DataState> greaterOrEqualThan(String var, double value) {
        return getPredicate(var, RelationOperator.GREATER_OR_EQUAL_THAN, value);
    }

    public Predicate<DataState> greaterThan(String var, double value) {
        return getPredicate(var, RelationOperator.GREATER_THAN, value);
    }

    public Predicate<DataState> equalsTo(String var, double value) {
        return getPredicate(var, RelationOperator.EQUAL_TO, value);
    }

    public Predicate<DataState> lessOrEqualThan(String var, double value) {
        return getPredicate(var, RelationOperator.LESS_OR_EQUAL_THAN, value);
    }

    public Predicate<DataState> lessThan(String var, double value) {
        return getPredicate(var, RelationOperator.LESS_THAN, value);
    }


    private Predicate<DataState> getPredicate(String var, RelationOperator op, double value) {
        Variable variable = getVariable(var);
        if (variable == null) {
            throw new IllegalArgumentException(String.format("Variable %s is unknown!",var));
        }
        return (ds -> op.eval(ds.getValue(variable), value));
    }


    public DataStateFunction set(String var, double value) {
        return set(var, (rg, ds) -> value);
    }

    public DataStateFunction set(String var, DataStateRandomExpression expr ) {
        Variable variable = getVariable(var);
        if (variable == null) {
            throw new IllegalArgumentException(String.format("Variable %s is unknown!",var));
        }
        return (rg, ds) -> ds.set(variable, expr.eval(rg, ds));
    }

    public DataStateRandomExpression get(String name) {
        Variable variable = getVariable(name);
        if (variable == null) {
            throw new IllegalArgumentException(String.format("Variable %s is unknown!", name));
        }
        return (rg, ds) -> ds.getValue(variable);
    }


    public DataStateUpdate getUpdate(String name, double value) {
        return getUpdate(getVariable(name), value);
    }

    public DataStateUpdate getUpdate(Variable var, double value) {
        return new DataStateUpdate(var, Math.max(var.minValue(), Math.min(value, var.maxValue())));
    }

//    public DataStateUpdate getArrayUpdate(String name, double[] values) {
//        return getArrayUpdate(getVariable(name), values);
//    }

    private List<DataStateUpdate> getArrayUpdate(Variable var, double[] values) {
        if (var.getSize() != values.length) {
            throw new IllegalStateException("Assignment of arrays with different size!");
        }
        return IntStream.range(0, var.getSize()).mapToObj(i -> getUpdate(var, i, values[i])).toList();
    }

    private List<DataStateUpdate> getArrayUpdate(Variable var, int from, int to, double[] values) {
        if (var.getSize() != values.length) {
            throw new IllegalStateException("Assignment of arrays with different size!");
        }
        return IntStream.range(0, var.getSize()).mapToObj(i -> getUpdate(var, i, values[i])).toList();
    }

    public DataStateUpdate getUpdate(String name, int i, double value) {
        return getUpdate(getVariable(name), i, value);
    }

    private DataStateUpdate getUpdate(Variable var, int i, double value) {
        if (i<var.getSize()) {
            return getUpdate(var, i, value);
        } else {
            throw new IllegalStateException("Illegal index!");//TODO: Handle this out of bound access at runtime!
        }
    }

    public double getMaxValueInArray(Variable var, DataState ds) {
        return IntStream.range(0, var.getSize()).map(i -> i+var.getFirstCellIndex()).mapToDouble(ds::getValue).max().orElse(Double.NaN);
    }

    public double getMaxValueInArray(Variable var, DataState ds, DoublePredicate predicate) {
        return IntStream.range(0, var.getSize()).map(i -> i+var.getFirstCellIndex()).mapToDouble(ds::getValue).filter(predicate).max().orElse(Double.NaN);
    }

    public double getMinValueInArray(Variable var, DataState ds) {
        return IntStream.range(0, var.getSize()).map(i -> i+var.getFirstCellIndex()).mapToDouble(ds::getValue).min().orElse(Double.NaN);
    }

    public double getMinValueInArray(Variable var, DataState ds, DoublePredicate predicate) {
        return IntStream.range(0, var.getSize()).map(i -> i+var.getFirstCellIndex()).mapToDouble(ds::getValue).filter(predicate).min().orElse(Double.NaN);
    }


    public double countValuesInArray(Variable var, DataState ds, DoublePredicate predicate) {
        return IntStream.range(0, var.getSize()).map(i -> i+var.getFirstCellIndex()).mapToDouble(ds::getValue).filter(predicate).count();
    }

    public double getMeanValueInArray(Variable var, DataState ds) {
        return IntStream.range(0, var.getSize()).map(i -> i+var.getFirstCellIndex()).mapToDouble(ds::getValue).average().orElse(Double.NaN);
    }

    public double getMeanValueInArray(Variable var, DataState ds, DoublePredicate predicate) {
        return IntStream.range(0, var.getSize()).map(i -> i+var.getFirstCellIndex()).mapToDouble(ds::getValue).filter(predicate).average().orElse(Double.NaN);
    }


}
