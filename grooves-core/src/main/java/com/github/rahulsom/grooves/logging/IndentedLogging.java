package com.github.rahulsom.grooves.logging;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.LoggerFactory;

/**
 * AspectJ aspect that provides indented logging for methods annotated with @Trace.
 * This creates a hierarchical trace output with method calls and returns.
 */
@Aspect
public class IndentedLogging {
    private static final ThreadLocal<Integer> indentLevel = new ThreadLocal<>();
    private static final int INITIAL_INDENT = 1;

    /**
     * Increases the current indentation level for nested method calls.
     */
    public static void stepIn() {
        indentLevel.set(getIndentLevel() + 1);
    }

    /**
     * Decreases the current indentation level when returning from method calls.
     */
    public static void stepOut() {
        indentLevel.set(getIndentLevel() - 1);
    }

    /**
     * Returns the current indentation string based on the current nesting level.
     *
     * @return a string of spaces representing the current indentation level
     */
    public static String indent() {
        return " ".repeat(getIndentLevel() * 2);
    }

    private static int getIndentLevel() {
        var level = indentLevel.get();
        return level != null ? level : INITIAL_INDENT;
    }

    private static String eventsToString(List<?> list) {
        return "<... " + list.size() + " item(s)>";
    }

    /**
     * AspectJ advice that intercepts methods annotated with @Trace to provide indented logging.
     * Logs method entry with parameters and method exit with return values or exceptions.
     *
     * @param joinPoint the intercepted method call
     * @param trace the @Trace annotation instance
     * @return the result of the intercepted method call
     * @throws Throwable any exception thrown by the intercepted method
     */
    @SuppressWarnings({"unused", "UnusedParameters"})
    @Around(value = "@annotation(trace)", argNames = "joinPoint,trace")
    public Object around(ProceedingJoinPoint joinPoint, Trace trace) throws Throwable {
        var signature = joinPoint.getSignature();
        var classWithFunction = joinPoint.getTarget().getClass();
        var loggerName = classWithFunction.getName().replaceAll("\\$\\$Lambda.*$", "");
        var log = LoggerFactory.getLogger(loggerName);

        var methodName = signature.getName().equals("invoke")
                ? signature.getDeclaringType().getSimpleName().substring(0, 1).toLowerCase()
                        + signature.getDeclaringType().getSimpleName().substring(1)
                : signature.getName();

        var args = Arrays.stream(joinPoint.getArgs())
                .map(arg -> arg instanceof List<?> ? eventsToString((List<?>) arg) : arg)
                .map(String::valueOf)
                .collect(Collectors.joining(", "));

        log.trace("{}{}({})", indent(), methodName, args);
        stepIn();

        try {
            var result = joinPoint.proceed();
            stepOut();
            var listRender = result instanceof List<?> ? eventsToString((List<?>) result) : result;
            log.trace("{}{}({}) --> {}", indent(), methodName, args, listRender);
            return result;
        } catch (Throwable t) {
            stepOut();
            log.trace("{}{}({}) ~~> {}", indent(), methodName, args, t.getMessage(), t);
            throw t;
        }
    }
}
