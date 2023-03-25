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

package it.unicam.quasylab.jspear.cli;

import java.io.File;

public class StarkMessages {


    private static final String WORKING_DIRECTORY_MESSAGE = "Working directory: %s";
    private static final String LIST_MESSAGE = "Content:";
    private static final String ILLEGAL_ACCESS = "An error occurred while accessing to %s";
    private static final String LOAD_MESSAGE = "Specification %s has been successfully loaded.";
    private static final String QUIT_MESSAGE = "See you next time!";

    public static String currentWorkingDirectory(File newDirectory) {
        return String.format(WORKING_DIRECTORY_MESSAGE, newDirectory.getAbsolutePath());
    }

    public static String listMessage() {
        return LIST_MESSAGE;
    }

    public static String illegalAccess(File file) {
        return String.format(ILLEGAL_ACCESS, file.getAbsolutePath());
    }

    public static String loadMessage(String fileName) {
        return String.format(LOAD_MESSAGE, fileName);
    }

    public static String quitMessage() {
        return QUIT_MESSAGE;
    }
}
