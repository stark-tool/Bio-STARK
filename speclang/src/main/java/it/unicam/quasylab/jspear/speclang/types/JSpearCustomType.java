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

package it.unicam.quasylab.jspear.speclang.types;

import java.util.Objects;

public final class JSpearCustomType implements JSpearType {

    private final String customTypeName;

    private final String[] typeElements;

    public JSpearCustomType(String customTypeName, String[] typeElements) {
        this.customTypeName = customTypeName;
        this.typeElements = typeElements;
    }


    @Override
    public JSpearType merge(JSpearType other) {
        if (this.equals(other)) {
            return this;
        }
        return JSpearCustomType.ERROR_TYPE;
    }

    @Override
    public boolean isCompatibleWith(JSpearType other) {
        return this.equals(other);
    }

    @Override
    public boolean isNumerical() {
        return false;
    }

    @Override
    public boolean isAnArray() {
        return false;
    }

    @Override
    public boolean isError() {
        return false;
    }

    @Override
    public boolean canBeMergedWith(JSpearType other) {
        return this.equals(other);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JSpearCustomType that = (JSpearCustomType) o;
        return customTypeName.equals(that.customTypeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customTypeName);
    }

    @Override
    public boolean isCustom() {
        return true;
    }
}
