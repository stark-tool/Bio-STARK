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

import java.util.Map;

public sealed interface JSpearValue permits JSpearBoolean, JSPearInteger, JSpearReal, JSpearArray, JSpearCustomValue  {

    JSpearType getJSpearType();

//    TODO: Add enum for each operator (the string is also used to retrieve it)
//    enum UnaryOperator {
//        PLUS,
//        MINUS,
//        ACOS,
//        ASIN,
//        ATAN,
//        CBRT,
//        CEIL,
//        COS,
//                Map.entry("cbrt", Math::cbrt),
//                Map.entry("ceil", Math::ceil),
//                Map.entry("cos", Math::cos),
//                Map.entry("cosh", Math::cosh),
//                Map.entry("exp", Math::exp),
//                Map.entry("expm1", Math::expm1),
//                Map.entry("floor", Math::floor),
//                Map.entry("log", Math::log),
//                Map.entry("log10", Math::log10),
//                Map.entry("log1p", Math::log1p),
//                Map.entry("signum", Math::signum),
//                Map.entry("sin", Math::sin),
//                Map.entry("sinh", Math::sinh),
//                Map.entry("sqrt", Math::sqrt),
//                Map.entry("tan", Math::tan)
//
//    }

}
