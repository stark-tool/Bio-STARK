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

/**
 * Instances of this class acts as an interpreter of Stark commands.
 */
package it.unicam.quasylab.jspear.cli;

import it.unicam.quasylab.jspear.SystemSpecification;
import it.unicam.quasylab.jspear.speclang.SpecificationLoader;

import java.io.File;
import java.io.IOException;

public class StarkInterpreter {

    private static final String LOAD_ERROR_MESSAGE = "An error occurred while loading specification.";
    private static final String NOSPECIFICATION_IS_LOADED = "No STARTK specification has been loaded";
    private SystemSpecification specification;

    public boolean loadSpecification(File fileName) throws StarkCommandExecutionException {
        SpecificationLoader loader = new SpecificationLoader();
        try {
            SystemSpecification loadedSpecification = loader.loadSpecification(fileName);
            if (loadedSpecification == null) {
                throw new StarkCommandExecutionException(LOAD_ERROR_MESSAGE, loader.getErrorMessage());
            } else {
                this.specification = loadedSpecification;
                return true;
            }
        } catch (IOException e) {
            throw new StarkCommandExecutionException(e);
        }
    }

    public String[] getFormulas() throws StarkCommandExecutionException {
        if (specification != null) {
            return specification.getFormulas();
        } else {
            throw new StarkCommandExecutionException(NOSPECIFICATION_IS_LOADED);
        }
    }

    public String[] getPenalties() throws StarkCommandExecutionException {
        if (specification != null) {
            return specification.getPenalties();
        } else {
            throw new StarkCommandExecutionException(NOSPECIFICATION_IS_LOADED);
        }
    }

    public double[][] simulate(int size, int deadline, String selected) {
        return null;
    }
}
