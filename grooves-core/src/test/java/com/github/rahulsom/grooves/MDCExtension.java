package com.github.rahulsom.grooves;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class MDCExtension implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void afterEach(@NotNull ExtensionContext context) {
        LoggerFactory.getLogger(getLoggerName(context)).info(
                "End test - {}.{}\n\n", getTestClass(context), getTestMethod(context));
        MDC.remove("test");
    }

    @Override
    public void beforeEach(@NotNull ExtensionContext context) {
        MDC.put("test", getTestClass(context) + "." + getTestMethod(context));
        LoggerFactory.getLogger(getLoggerName(context)).info(
                "Begin test - {}.{}", getTestClass(context), getTestMethod(context));
    }

    private String getTestMethod(ExtensionContext context) {
        return context.getTestMethod().get().getName();
    }

    @Nullable
    private String getTestClass(ExtensionContext context) {
        return context.getTestClass().map(Class::getSimpleName).orElse(null);
    }

    @NotNull
    private String getLoggerName(ExtensionContext context) {
        return context.getTestClass().map(Class::getName).orElse(getClass().getName());
    }
}
