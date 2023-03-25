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

import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

public class StarkShell implements Runnable {


    private static final String WELCOME_MESSAGE = "Start using Stark...";
    private final PrintStream output;
    private final PrintStream error;

    private final Scanner input;

    private final StarkInterpreter interpreter;

    public StarkShell(PrintStream output, PrintStream error, Scanner input) throws StarkCommandExecutionException {
        this.output = output;
        this.error = error;
        this.input = input;
        this.interpreter = new StarkInterpreter();
    }

    public StarkShell() throws StarkCommandExecutionException {
        this(System.out, System.err, new Scanner(System.in));
    }


    public void run() {
        boolean flag = true;
        showWelcomeMessage();
        while (flag) {
            flag = readAndExecute();
        }
    }

    public void run(List<String> commands) {
        for (String cmd: commands) {
            if (!execute(cmd)) {
                return ;
            }
        }
    }

    private boolean readAndExecute() {
        showPrompt();
        return execute(this.input.nextLine());
    }

    private boolean execute(String cmd) {
        return showCommandResult(interpreter.executeCommand(cmd));
    }

    private boolean showCommandResult(StarkCommandExecutionResult commandResult) {
        PrintStream stream = (commandResult.result()?this.output:this.error);
        stream.println(commandResult.message());
        for (String str: commandResult.details()) {
            stream.println(str);
        }
        return !commandResult.quit();
    }

    private void showPrompt() {
        this.output.print("> ");
        this.output.flush();
    }

    private void showWelcomeMessage() {
        this.output.println(WELCOME_MESSAGE);
    }

    public static void main(String[] args) {
        StarkShell shell = null;
        try {
            shell = new StarkShell();
            shell.run();
        } catch (StarkCommandExecutionException e) {
            System.err.println(e.getMessage());
        }
    }
}
