package com.github.rahulsom.grooves.queries.internal;

import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.events.DeprecatedBy;
import com.github.rahulsom.grooves.api.snapshots.internal.BaseSnapshot;
import rx.Observable;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Utility objects and methods to help with Queries.
 *
 * @author Rahul Somasunderam
 */
public class Utils {
    private Utils() {
    }

    public static final Collector<CharSequence, ?, String> JOIN_EVENTS =
            Collectors.joining(",\n    ", "[\n    ", "\n]");
    public static final Collector<CharSequence, ?, String> JOIN_EVENT_IDS =
            Collectors.joining(", ");

    public static <
            SnapshotT extends BaseSnapshot,
            EventT extends BaseEvent
            > Observable<? extends SnapshotT> returnOrRedirect(
            boolean redirect, List<EventT> events, SnapshotT it,
            Supplier<Observable<? extends SnapshotT>> redirectedSnapshot) {
        final EventT lastEvent =
                events.isEmpty() ? null : events.get(events.size() - 1);

        final boolean redirectToDeprecator =
                it.getDeprecatedBy() != null
                        && lastEvent != null
                        && lastEvent instanceof DeprecatedBy
                        && redirect;

        return redirectToDeprecator ?
                redirectedSnapshot.get() :
                Observable.just(it);
    }

}
