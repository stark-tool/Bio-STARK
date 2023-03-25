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

import it.unicam.quasylab.jspear.speclang.parsing.ParseErrorCollector;
import it.unicam.quasylab.jspear.speclang.parsing.ParseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import java.io.File;
import java.util.List;

public class StarkInterpreter {

    private final StarkEnvironment starkEnvironment;

    private File workingDirectory;


    public StarkInterpreter() throws StarkCommandExecutionException {
        this(System.getProperty("user.dir"));
    }


    public StarkInterpreter(String workingDirectory) throws StarkCommandExecutionException {
        this(new File(workingDirectory));
    }


    public StarkInterpreter(File workingDirectory) throws StarkCommandExecutionException {
        setWorkingDirectory(workingDirectory);
        this.starkEnvironment = new StarkEnvironment();
    }

    private void setWorkingDirectory(File workingDirectory) throws StarkCommandExecutionException {
        if (!workingDirectory.exists()) {
            throw new StarkCommandExecutionException(StarkCommandExecutionException.fileDoesNotExists(workingDirectory));
        }
        if (!workingDirectory.isDirectory()) {
            throw new StarkCommandExecutionException(StarkCommandExecutionException.fileIsNotADirectory(workingDirectory));
        }
        this.workingDirectory = workingDirectory;
    }

    public StarkCommandExecutionResult executeCommand(String cmd) throws StarkCommandExecutionException {
        return executeCommand(parseCommand(CharStreams.fromString(cmd)));
    }

    private StarkCommandExecutionResult executeCommand(StarkScriptParser.ScriptCommandContext cmd) {
        return cmd.accept(new StarkCommandVisitor());
    }


    private StarkScriptParser.ScriptCommandContext parseCommand(CharStream source) throws StarkCommandExecutionException {
        ParseErrorCollector errors = new ParseErrorCollector();
        StarkScriptParser.ScriptCommandContext result = getParser(errors, source).scriptCommand();
        if (errors.withErrors()) {
            throw new StarkCommandExecutionException(StarkCommandExecutionException.ILLEGAL_COMMAND, errors.getSyntaxErrorList().stream().map(Object::toString).toList());
        } else {
            return result;
        }
    }

    private StarkScriptParser.StarkScriptContext parseScript(CharStream source) throws StarkCommandExecutionException {
        ParseErrorCollector errors = new ParseErrorCollector();
        StarkScriptParser.StarkScriptContext result = getParser(errors, source).starkScript();
        if (errors.withErrors()) {
            throw new StarkCommandExecutionException(StarkCommandExecutionException.ILLEGAL_COMMAND, errors.getSyntaxErrorList().stream().map(Object::toString).toList());
        } else {
            return result;
        }
    }

    private StarkScriptParser getParser(ParseErrorCollector errors, CharStream source) {
        StarkScriptLexer lexer = new StarkScriptLexer(source);
        CommonTokenStream tokens =  new CommonTokenStream(lexer);
        StarkScriptParser parser = new StarkScriptParser(tokens);
        ParseErrorListener errorListener = new ParseErrorListener(errors);
        parser.addErrorListener(errorListener);
        return parser;
    }

    public class StarkCommandVisitor extends StarkScriptBaseVisitor<StarkCommandExecutionResult> {

        @Override
        public StarkCommandExecutionResult visitChangeDirectoryCommand(StarkScriptParser.ChangeDirectoryCommandContext ctx) {
            return changeDirectory(getFileName(ctx.target.getText()));
        }

        @Override
        public StarkCommandExecutionResult visitListCommand(StarkScriptParser.ListCommandContext ctx) {
            return list();
        }

        @Override
        public StarkCommandExecutionResult visitCwdCommand(StarkScriptParser.CwdCommandContext ctx) {
            return cwd();
        }

        @Override
        public StarkCommandExecutionResult visitLoadCommand(StarkScriptParser.LoadCommandContext ctx) {
            return load(getFileName(ctx.target.getText()));
        }

        @Override
        public StarkCommandExecutionResult visitQuitCommand(StarkScriptParser.QuitCommandContext ctx) {
            return quit();
        }
    }

    private StarkCommandExecutionResult quit() {
        return new StarkCommandExecutionResult(StarkMessages.quitMessage(), true, true);
    }

    private StarkCommandExecutionResult load(String fileName) {
        return load(new File(workingDirectory, fileName));
    }

    private StarkCommandExecutionResult load(File file) {
        try {
            this.starkEnvironment.loadSpecification(file);
            return new StarkCommandExecutionResult(StarkMessages.loadMessage(file.getAbsolutePath()),true);
        } catch (StarkCommandExecutionException e) {
            return new StarkCommandExecutionResult(e.getMessage(),e.getReasons());
        }
    }

    private String getFileName(String target) {
        return target.substring(1,target.length()-1);
    }

    private StarkCommandExecutionResult cwd() {
        return new StarkCommandExecutionResult(StarkMessages.currentWorkingDirectory(workingDirectory), true);
    }

    private StarkCommandExecutionResult list() {
        String[] content = workingDirectory.list();
        if (content == null) {
            return new StarkCommandExecutionResult(StarkMessages.illegalAccess(workingDirectory), false);
        } else {
            return new StarkCommandExecutionResult(StarkMessages.listMessage(), List.of(content), true);
        }
    }

    private StarkCommandExecutionResult changeDirectory(String dir) {
        try {
            File newDirectory = new File(workingDirectory, dir);
            setWorkingDirectory(newDirectory);
            return cwd();
        } catch (StarkCommandExecutionException e) {
            return new StarkCommandExecutionResult(e.getMessage(),e.getReasons());
        }
    }
}
