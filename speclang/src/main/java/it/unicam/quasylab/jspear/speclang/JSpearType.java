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

public interface JSpearType {

    JSpearType ERROR_TYPE = new JSpearErrorType();
    JSpearType BOOLEAN_TYPE = new JSpearBooleanType();
    JSpearType REAL_TYPE = new JSpearRealType();
    JSpearType INTEGER_TYPE = new JSpearIntegerType();

    static JSpearType merge(JSpearType switchType, JSpearType caseType) {
        return null;
    }

    static JSpearType emptyArrayType() {
        return null;
    }

    static JSpearType arrayOf(JSpearType type) {
        return null;
    }

    default boolean isCompatibleWith(JSpearType switchType) {
        return this.equals(switchType);
    }

    default boolean isNumerical() {
        return false;//TODO: Fix!!!
    }

    default boolean isAnArray() {
        return false;
    }

    default JSpearType getContent() {
        return JSpearType.ERROR_TYPE; //FIXME!!!
    }

    default boolean isError() {
        return true; //FIXME!!!
    }

    enum JSpearTypeEnum {
        REAL,
        INTEGER,
        BOOLEAN,
        ERROR,
        CUSTOM;
    }

    double valueOf(double v);

    double valueOf(int v);

    double valueOf(String v);

    double valueOf(boolean b);

    class JSpearErrorType implements JSpearType {
        @Override
        public double valueOf(double v) {
            return Double.NaN;
        }

        @Override
        public double valueOf(int v) {
            return Double.NaN;
        }

        @Override
        public double valueOf(String v) {
            return Double.NaN;
        }

        @Override
        public double valueOf(boolean b) {
            return Double.NaN;
        }
    }

    class JSpearBooleanType implements JSpearType {
        @Override
        public double valueOf(double v) {
            return 0;
        }

        @Override
        public double valueOf(int v) {
            return 0;
        }

        @Override
        public double valueOf(String v) {
            return 0;
        }

        @Override
        public double valueOf(boolean b) {
            return 0;
        }
    }

    class JSpearRealType implements JSpearType {
        @Override
        public double valueOf(double v) {
            return 0;
        }

        @Override
        public double valueOf(int v) {
            return 0;
        }

        @Override
        public double valueOf(String v) {
            return 0;
        }

        @Override
        public double valueOf(boolean b) {
            return 0;
        }
    }

    class JSpearIntegerType implements JSpearType {
        @Override
        public double valueOf(double v) {
            return 0;
        }

        @Override
        public double valueOf(int v) {
            return 0;
        }

        @Override
        public double valueOf(String v) {
            return 0;
        }

        @Override
        public double valueOf(boolean b) {
            return 0;
        }
    }
}
