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

package it.unicam.quasylab.jspear.robtl;

public enum TruthValues {
    TRUE,
    FALSE,
    UNKNOWN;


    public static TruthValues and(TruthValues value1, TruthValues value2) {
        if (value1 == TruthValues.FALSE || value2 == TruthValues.FALSE) return TruthValues.FALSE;
        if (value1 == TruthValues.TRUE && value2 == TruthValues.TRUE) return TruthValues.TRUE;
        return TruthValues.UNKNOWN;
    }

    public static TruthValues or(TruthValues value1, TruthValues value2) {
        if (value1 == TruthValues.TRUE || value2 == TruthValues.TRUE) return TruthValues.TRUE;
        if (value1 == TruthValues.FALSE && value2 == TruthValues.FALSE) return TruthValues.FALSE;
        return TruthValues.UNKNOWN;
    }

    public static TruthValues neg(TruthValues value) {
        if (value == TruthValues.UNKNOWN) return TruthValues.UNKNOWN;
        if (value == TruthValues.FALSE) return TruthValues.TRUE;
        return TruthValues.FALSE;
    }

    public static TruthValues imply(TruthValues value1, TruthValues value2) {
        return TruthValues.or(TruthValues.neg(value1), value2);
    }
}