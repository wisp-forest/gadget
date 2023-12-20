package io.wispforest.gadget.shell;

import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.util.InfallibleClosable;
import jdk.jshell.*;
import jdk.jshell.execution.DirectExecutionControl;
import jdk.jshell.execution.LocalExecutionControl;
import jdk.jshell.execution.LocalExecutionControlProvider;
import jdk.jshell.spi.ExecutionControl;
import jdk.jshell.spi.ExecutionControlProvider;
import jdk.jshell.spi.ExecutionEnv;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class Shell implements InfallibleClosable {
    private final JShell jShell;
    private final Consumer<String> outConsumer;
    private final Consumer<String> errConsumer;

    public Shell(Consumer<String> outConsumer, Consumer<String> errConsumer) {
        this.outConsumer = outConsumer;
        this.errConsumer = errConsumer;
        jShell = JShell.builder()
            .out(new ConsumerPrintStream(outConsumer))
            .err(new ConsumerPrintStream(errConsumer))
            .executionEngine(new ExecutionControlProvider() {
                @Override
                public String name() {
                    return "gadget_shell";
                }

                @Override
                public ExecutionControl generate(ExecutionEnv env, Map<String, String> parameters) throws Throwable {
                    return new DirectExecutionControl(new ShellLoaderDelegate());
                }
            }, Map.of())
            .build();
    }

    public void run(String code) {
        List<SnippetEvent> events = jShell.eval(code);

        for (var event : events) {
            for (var diag : (Iterable<Diag>) jShell.diagnostics(event.snippet())::iterator) {
                errConsumer.accept(diag.getMessage(Locale.ROOT));
            }

            Gadget.LOGGER.info("event: {}", event);
        }
    }

    @Override
    public void close() {
        jShell.close();
    }
}
