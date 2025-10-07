package com.github.rahulsom.grooves.asciidoctor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class SvgBuilderTest {
    @ParameterizedTest
    @ValueSource(
            strings = {
                "SimpleEvents",
                "RevertEvent",
                "RevertEventEffective",
                "RevertOnRevert",
                "RevertOnRevertEffective",
                "MergeAggregates",
                "MergeAggregatesEffective",
                "RevertMergeBefore",
                "RevertMergeAfter",
                "JoinExample",
                "DisjoinExample",
                "ParallelEvents",
                "ConcurrentEvents"
            })
    public void test(final String name) throws IOException {
        File file = Files.createTempFile("SimpleEvents", "svg").toFile();

        String inputText = null;
        try (InputStream stream = this.getClass().getResourceAsStream("/" + name + ".esdiag.txt")) {
            if (stream != null) {
                inputText = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
        new SvgBuilder(inputText).write(file);
        if (!new File("src/test/resources/" + name + ".esdiag.svg").exists()) {
            Files.write(
                    new File("src/test/resources/" + name + ".esdiag.svg").toPath(), Files.readAllBytes(file.toPath()));
        }

        String expectedText = null;
        try (InputStream stream1 = this.getClass().getResourceAsStream("/" + name + ".esdiag.svg")) {
            if (stream1 != null) {
                expectedText = new String(stream1.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
        Assertions.assertEquals(expectedText, Files.readString(file.toPath()));
    }
}
