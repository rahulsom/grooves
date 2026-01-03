package com.github.rahulsom.grooves;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GroovesQueryBuilderTest {
    @Test
    @DisplayName("Should list problems if no fields are configured")
    void shouldListProblemsIfNoFieldsAreConfigured() {
        final var actualException = assertThrows(
                IllegalStateException.class,
                () -> GroovesQueryBuilder.builder().build().toQuery());

        assertThat(actualException).isNotNull();
        assertThat(actualException.getMessage()).isEqualTo("snapshotProvider is not set");
        assertThat(Arrays.stream(actualException.getSuppressed()).map(Throwable::getMessage))
                .containsExactly(
                        "emptySnapshotProvider is not set",
                        "eventsProvider is not set",
                        "exceptionHandler is not set",
                        "eventHandler is not set",
                        "eventClassifier is not set",
                        "applyMoreEventsPredicate is not set",
                        "deprecator is not set",
                        "eventVersionProvider is not set",
                        "snapshotVersionSetter is not set",
                        "deprecatedByProvider is not set",
                        "revertedEventProvider is not set",
                        "eventIdProvider is not set");
    }

    @Test
    @DisplayName("Should list problems if some fields are configured")
    void shouldListProblemsIfSomeFieldsAreConfigured() {
        final var actualException = assertThrows(IllegalStateException.class, () -> GroovesQueryBuilder.builder()
                .emptySnapshotProvider(o -> "Hello 0")
                .build()
                .toQuery());

        assertThat(actualException).isNotNull();
        assertThat(actualException.getMessage()).isEqualTo("snapshotProvider is not set");
        assertThat(Arrays.stream(actualException.getSuppressed()).map(Throwable::getMessage))
                .containsExactly(
                        "eventsProvider is not set",
                        "exceptionHandler is not set",
                        "eventHandler is not set",
                        "eventClassifier is not set",
                        "applyMoreEventsPredicate is not set",
                        "deprecator is not set",
                        "eventVersionProvider is not set",
                        "snapshotVersionSetter is not set",
                        "deprecatedByProvider is not set",
                        "revertedEventProvider is not set",
                        "eventIdProvider is not set");
    }

    @Test
    @DisplayName("Should create the query if all fields are configured")
    void shouldCreateQueryIfAllFieldsAreConfigured() {
        final var groovesQuery = GroovesQueryBuilder.<UUID, Integer, List<String>, String, Integer>builder()
                .snapshotProvider((aggregate, version) -> null)
                .emptySnapshotProvider(aggregate -> emptyList())
                .eventsProvider((aggregate, version, lastSnapshot) -> Stream.empty())
                .exceptionHandler((exception, strings, s) -> EventApplyOutcome.CONTINUE)
                .eventHandler((s, strings) -> EventApplyOutcome.CONTINUE)
                .eventClassifier(s -> EventType.Normal)
                .applyMoreEventsPredicate(strings -> true)
                .deprecator((strings, deprecatingAggregate) -> {})
                .eventVersionProvider(e -> 1)
                .snapshotVersionSetter((s, v) -> {})
                .deprecatedByProvider(s -> new DeprecatedByResult<>(UUID.randomUUID(), 3))
                .revertedEventProvider(s -> s)
                .eventIdProvider(s -> 3)
                .build()
                .toQuery();

        assertThat(groovesQuery).isNotNull();
    }
}
