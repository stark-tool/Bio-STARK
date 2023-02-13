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

import it.unicam.quasylab.jspear.ds.DataState;
import it.unicam.quasylab.jspear.ds.DataStateExpression;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
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
     * @return a sorted array containing all the elements in
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
        if (other.size() >= this.size()) {
            if (other.size() % this.size() != 0) {
                throw new IllegalArgumentException("Incompatible size of data sets!");
            }
            double[] thisData = this.evalPenaltyFunction(f);
            double[] otherData = other.evalPenaltyFunction(f);
            int k = otherData.length / thisData.length;
            return IntStream.range(0, thisData.length).parallel()
                    .mapToDouble(i -> IntStream.range(0, k).mapToDouble(j -> Math.max(0, otherData[i * k + j] - thisData[i])).sum())
                    .sum() / otherData.length;
        }
        else{
            if (this.size() % other.size() != 0) {
                throw new IllegalArgumentException("Incompatible size of data sets!");
            }
            double[] thisData = this.evalPenaltyFunction(f);
            double[] otherData = other.evalPenaltyFunction(f);
            int k = thisData.length / otherData.length;
            return IntStream.range(0, otherData.length).parallel()
                    .mapToDouble(i -> IntStream.range(0, k).mapToDouble(j -> Math.max(0, otherData[i] - thisData[i*k+j])).sum())
                    .sum() / otherData.length;
        }
    }

    /**
     * Returns the confidence interval of the evaluation of the distance between this sample set and <code>other</code> computed according to
     * the function <code>f</code>. The confidence interval is evaluated by means of the empirical bootstrap method.
     *
     * @param f penalty function used to compute the distance.
     * @param other sample set to compare.
     * @param m number of applications of bootstrapping
     * @param z the desired quantile of the standard-normal distribution
     * @return the limits of the confidence interval of the evaluation of the distance between this sample set and <code>other</code> computed according to
     * the function <code>f</code>.
     */

    public synchronized double[] bootstrapDistance(DataStateExpression f, SampleSet<T> other, int m, double z) {
        Random rand = new Random();
        if (this.size()%other.size()!=0 && other.size()%this.size()!=0) {
            throw new IllegalArgumentException("Incompatible size of data sets!");
        }
        double[] W = new double[m];
        double WSum = 0.0;
        for (int i = 0; i<m; i++){
            SampleSet<T> thisSampleSet = new SampleSet<>(this.stream().parallel().map(j -> this.states.get(rand.nextInt(this.size()))).toList());
            SampleSet<T> otherSampleSet = new SampleSet<>(other.stream().parallel().map(j -> other.states.get(rand.nextInt(other.size()))).toList());
            W[i] = thisSampleSet.distance(f,otherSampleSet);
            WSum += W[i];
        }
        double BootMean = WSum/m;
        double StandardError = Math.sqrt(IntStream.range(0,m).mapToDouble(j->Math.pow(W[j]-BootMean,2)).sum()/(m-1));
        double[] CI = new double[2];
        CI[0] = Math.max(0,BootMean - z*StandardError);
        CI[1] = Math.min(BootMean + z*StandardError,1);
        return CI;
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
