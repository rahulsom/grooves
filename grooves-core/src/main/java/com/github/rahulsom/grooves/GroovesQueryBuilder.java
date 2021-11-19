package com.github.rahulsom.grooves;

import com.github.rahulsom.grooves.functions.*;
import com.github.rahulsom.grooves.impl.GroovesQueryImpl;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a {@link GroovesQuery}.
 *
 * @param <AggregateT>          The type of the Aggregate
 * @param <VersionOrTimestampT> The type of the fields that denotes version or timestamp
 * @param <SnapshotT>           The type of the Snapshot being computed
 * @param <EventT>              The type of the Event
 */
@Builder
public class GroovesQueryBuilder<AggregateT, VersionOrTimestampT, SnapshotT, EventT, EventIdT> {

    private SnapshotProvider<AggregateT, VersionOrTimestampT, SnapshotT> snapshotProvider;
    private EmptySnapshotProvider<AggregateT, SnapshotT> emptySnapshotProvider;
    private EventsProvider<AggregateT, VersionOrTimestampT, SnapshotT, EventT> eventsProvider;
    private ApplyMoreEventsPredicate<SnapshotT> applyMoreEventsPredicate;
    private Deprecator<SnapshotT, EventT> deprecator;
    private ExceptionHandler<SnapshotT, EventT> exceptionHandler;
    private EventHandler<EventT, SnapshotT> eventHandler;
    private EventClassifier<EventT> eventClassifier;
    private EventVersioner<EventT, VersionOrTimestampT> eventVersionProvider;
    private SnapshotVersioner<SnapshotT, VersionOrTimestampT> snapshotVersionSetter;
    private DeprecatedByProvider<EventT, AggregateT, EventIdT> deprecatedByProvider;
    private RevertedEventProvider<EventT> revertedEventProvider;
    private EventIdProvider<EventT, EventIdT> eventIdProvider;

    GroovesQuery<AggregateT, VersionOrTimestampT, SnapshotT, EventT, EventIdT> toQuery() {
        ArrayList<String> exceptions = new ArrayList<>();
        checkNotNull(exceptions, snapshotProvider, "snapshotProvider is not set");
        checkNotNull(exceptions, emptySnapshotProvider, "emptySnapshotProvider is not set");
        checkNotNull(exceptions, eventsProvider, "eventsProvider is not set");
        checkNotNull(exceptions, exceptionHandler, "exceptionHandler is not set");
        checkNotNull(exceptions, eventHandler, "eventHandler is not set");
        checkNotNull(exceptions, eventClassifier, "eventClassifier is not set");
        checkNotNull(exceptions, applyMoreEventsPredicate, "applyMoreEventsPredicate is not set");
        checkNotNull(exceptions, deprecator, "deprecator is not set");
        checkNotNull(exceptions, eventVersionProvider, "eventVersionProvider is not set");
        checkNotNull(exceptions, snapshotVersionSetter, "snapshotVersionSetter is not set");
        checkNotNull(exceptions, deprecatedByProvider, "deprecatedByProvider is not set");
        checkNotNull(exceptions, revertedEventProvider, "revertedEventProvider is not set");
        checkNotNull(exceptions, eventIdProvider, "eventIdProvider is not set");

        if (exceptions.isEmpty()) {
            return new GroovesQueryImpl<>(
                snapshotProvider,
                emptySnapshotProvider,
                eventsProvider,
                applyMoreEventsPredicate,
                eventClassifier,
                deprecator,
                exceptionHandler,
                eventHandler,
                eventVersionProvider,
                snapshotVersionSetter,
                deprecatedByProvider,
                revertedEventProvider,
                eventIdProvider
            );
        } else {
            String firstException = exceptions.remove(0);
            IllegalStateException illegalStateException = new IllegalStateException(firstException);
            exceptions.stream().map(IllegalStateException::new)
                .forEach(illegalStateException::addSuppressed);

            throw illegalStateException;
        }
    }

    private void checkNotNull(List<String> exceptions, Object function, String message) {
        if (function == null) {
            exceptions.add(message);
        }
    }
}