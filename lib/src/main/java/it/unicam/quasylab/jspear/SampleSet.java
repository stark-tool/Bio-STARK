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

import org.apache.commons.math3.random.RandomGenerator;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Instances of this class are used to model a set of data states.
 */
public class SampleSet<T extends SystemState> {

    private final List<T> states;

    /**
     * Creates an empty sample set.
     */
    public SampleSet() {
        this(new LinkedList<>());
    }

    private SampleSet(List<T> states) {
        this.states = states;
    }

    public static <T extends SystemState> SampleSet<T> generate(RandomGenerator rg, Function<RandomGenerator, T> generator, int size) {
        return new SampleSet<>(IntStream.range(0, size).mapToObj(i -> generator.apply(rg)).toList());
    }


    /**
     * Adds a new state to this sample set.
     *
     * @param state a data state.
     */
    public void add(T state) {
        states.add(state);
    }

    /**
     * Returns the number of items in this sample set.
     *
     * @return the number of items in this sample set.
     */
    public int size() {
        return states.size();
    }

    /**
     * Given a penalty function described by means of an expression returns a (sorted array) containing its evaluation
     * on each element of the data set.
     *
     * @param f a penalty function.
     * @return a sorted array containing all the lements in
     */
    public synchronized double[] evalPenaltyFunction(DataStateExpression f) {
        return states.stream().map(SystemState::getDataState).mapToDouble(f).sorted().toArray();
    }

    /**
     * Returns the distance between this sample set and <code>other</code> computed according to
     * the function <code>f</code>.
     *
     * @param f penalty function used to compute the distance.
     * @param other sample set to compare.
     * @return the distance between this sample set and <code>other</code> computed according to
     * the function <code>f</code>.
     */
    public synchronized double distance(DataStateExpression f, SampleSet<T> other) {
        if (other.size()%other.size()!=0) {
            throw new IllegalArgumentException("Incompatible size of data sets!");
        }
        double[] thisData = this.evalPenaltyFunction(f);
        double[] otherData = other.evalPenaltyFunction(f);
        int k = otherData.length/thisData.length;
        return IntStream.range(0, thisData.length).parallel()
                .mapToDouble(i -> IntStream.range(0, k).mapToDouble(j -> Math.max(0, otherData[i*k+j]-thisData[i])).sum())
                .sum()/otherData.length;
    }

    /**
     * Returns a sequential stream of this sample set.
     *
     * @return a sequential stream of this sample set.
     */
    public Stream<T> stream() {
        return states.stream();
    }

    /**
     * Returns a sample set obtained by applying the given operator to all the elements of this sample set.
     * @param function operator to apply.
     * @return a sample set obtained by applying the given operator to all the elements of this sample set.
     */
    public SampleSet<T> apply(UnaryOperator<T> function) {
        return new SampleSet<>(this.stream().parallel().map(function).toList());
    }

    /**
     * Returns a new sample set obtained by applying a given function to all the elements of this sample set.
     * @param rg random generator used to sample random values.
     * @param function function used to generate a new element.
     * @return a new sample set obtained by applying <code>k</code> times a given random function to all the elements of this sample set.
     */
    public SampleSet<T> apply(RandomGenerator rg, BiFunction<RandomGenerator, T, T> function) {
        return new SampleSet<>(
                this.stream().parallel().map(s -> function.apply(rg, s)).toList()
        );
    }

    /**
     * Returns a sample set obtained from this one by replicating all the elements the given number of times.
     *
     * @param k number of copies.
     * @return a sample set obtained from this one by replicating all the elements the given number of times.
     */
    public SampleSet<T> replica(int k) {
        return new SampleSet<>(
                this.stream().flatMap(e -> IntStream.range(0, k).mapToObj(i -> e)).toList()
        );
    }
}
