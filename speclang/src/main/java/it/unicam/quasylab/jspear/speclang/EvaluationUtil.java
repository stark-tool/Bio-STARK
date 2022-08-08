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

package it.unicam.quasylab.jspear.speclang;

import java.util.Map;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

public class EvaluationUtil {

    private final static Map<String, DoubleBinaryOperator> binaryOperators = Map.of(
            "+", (x,y) -> x+y,
            "*", (x,y) -> x*y,
            "-", (x,y) -> x-y,
            "/", (x,y) -> x/y, //TODO: Check how to handle division by zero!
            "%", (x,y) -> x%y,
            "atan2", Math::atan2,
            "hypot", Math::hypot,
            "max",   Math::max,
            "min",   Math::min,
            "pow",   Math::pow
    );

    private final static Map<String, DoubleUnaryOperator> unaryOperators = Map.ofEntries(
            Map.entry("+", x -> +x),
            Map.entry("-", x -> -x),
            Map.entry("acos", Math::acos),
            Map.entry("asin", Math::asin),
            Map.entry("atan", Math::atan),
            Map.entry("cbrt", Math::cbrt),
            Map.entry("ceil", Math::ceil),
            Map.entry("cos", Math::cos),
            Map.entry("cosh", Math::cosh),
            Map.entry("exp", Math::exp),
            Map.entry("expm1", Math::expm1),
            Map.entry("floor", Math::floor),
            Map.entry("log", Math::log),
            Map.entry("log10", Math::log10),
            Map.entry("log1p", Math::log1p),
            Map.entry("signum", Math::signum),
            Map.entry("sin", Math::sin),
            Map.entry("sinh", Math::sinh),
            Map.entry("sqrt", Math::sqrt),
            Map.entry("tan", Math::tan)
    );

    public static DoubleBinaryOperator getBinaryOperator(String functionName) {
        return binaryOperators.getOrDefault(functionName, (x,y) -> Double.NaN);
    }

    public static DoubleUnaryOperator getUnaryOperator(String functionName) {
        return unaryOperators.getOrDefault(functionName, x -> Double.NaN);
    }

    public static boolean evalRelation(String op, double v1, double v2) {
        return switch (op) {
            case "<" -> v1<v2;
            case "<=" -> v1<=v2;
            case "==" -> v1==v2;
            case ">=" -> v1>=v2;
            case ">" -> v1>v2;
            default -> false;
        };
    }
}
