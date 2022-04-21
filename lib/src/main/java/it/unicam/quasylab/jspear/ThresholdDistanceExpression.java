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

public final class ThresholdDistanceExpression implements DistanceExpression {

    private final double threshold;
    private final RelationOperator relop;
    private final DistanceExpression expression;

    public ThresholdDistanceExpression(DistanceExpression expression, RelationOperator relop, double threshold) {
        this.threshold = threshold;
        this.relop = relop;
        this.expression = expression;
    }


    @Override
    public double compute(int step, EvolutionSequence seq1, EvolutionSequence seq2) {
        return (relop.eval(expression.compute(step, seq1, seq2),threshold)?1.0:0.0);
    }
}
