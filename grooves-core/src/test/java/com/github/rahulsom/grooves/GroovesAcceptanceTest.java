package com.github.rahulsom.grooves;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static com.github.rahulsom.grooves.AcceptanceTestFixture.*;
import static com.github.rahulsom.grooves.EventType.*;
import static org.assertj.core.api.Assertions.assertThat;

public class GroovesAcceptanceTest {
    @Test
    void shouldApplyEventsIfSnapshotProviderReturnsNull() {
        Aggregate queryAggregate = new Aggregate(1);

        GroovesQuery<Aggregate, Integer, Snapshot, Event, Integer> groovesQuery =
                createQuery(
                    new Event(1, 1, Normal, 1, "Line 1"),
                    new Event(2, 1, Normal, 2, "Line 2")
                );

        Snapshot snapshot1 = groovesQuery.computeSnapshot(queryAggregate, 1);
        assertThat(snapshot1.getSummary()).isEqualTo("Line 1,");
        assertThat(snapshot1.getVersion()).isEqualTo(1);

        Snapshot snapshot2 = groovesQuery.computeSnapshot(queryAggregate, 2);
        assertThat(snapshot2.getSummary()).isEqualTo("Line 1,Line 2,");
        assertThat(snapshot2.getVersion()).isEqualTo(2);
    }

    @Test
    void shouldApplyEventsIfSnapshotProviderReturnsValue() {
        Aggregate queryAggregate = new Aggregate(1);

        GroovesQuery<Aggregate, Integer, Snapshot, Event, Integer> groovesQuery =
                createQuery(
                    new Snapshot(1, 2, "Line 1,Line 2,", new ArrayList<>()),
                    new Event(3, 1, Normal, 3, "Line 3"),
                    new Event(4, 1, Normal, 4, "Line 4")
                );

        Snapshot snapshot3 = groovesQuery.computeSnapshot(queryAggregate, 3);
        assertThat(snapshot3.getSummary()).isEqualTo("Line 1,Line 2,Line 3,");
        assertThat(snapshot3.getVersion()).isEqualTo(3);

        Snapshot snapshot4 = groovesQuery.computeSnapshot(queryAggregate, 4);
        assertThat(snapshot4.getSummary()).isEqualTo("Line 1,Line 2,Line 3,Line 4,");
        assertThat(snapshot4.getVersion()).isEqualTo(4);
    }

    @Test
    void shouldRevertEventIfNotApplied() {
        Aggregate queryAggregate = new Aggregate(1);

        GroovesQuery<Aggregate, Integer, Snapshot, Event, Integer> groovesQuery =
                createQuery(
                    new Event(1, 1, Normal, 1, "Line 1"),
                    new Event(2, 1, Normal, 2, "Line 2"),
                    new Event(3, 1, Revert, 3, "1")
                );

        Snapshot snapshot1 = groovesQuery.computeSnapshot(queryAggregate, 1);
        assertThat(snapshot1.getSummary()).isEqualTo("Line 1,");
        assertThat(snapshot1.getVersion()).isEqualTo(1);

        Snapshot snapshot2 = groovesQuery.computeSnapshot(queryAggregate, 2);
        assertThat(snapshot2.getSummary()).isEqualTo("Line 1,Line 2,");
        assertThat(snapshot2.getVersion()).isEqualTo(2);

        Snapshot snapshot3 = groovesQuery.computeSnapshot(queryAggregate, 3);
        assertThat(snapshot3.getSummary()).isEqualTo("Line 2,");
        assertThat(snapshot3.getVersion()).isEqualTo(3);
    }

    @Test
    void shouldRevertRevertsEventIfNotApplied() {
        GroovesQuery<Aggregate, Integer, Snapshot, Event, Integer> groovesQuery =
                createQuery(
                    new Event(1, 1, Normal, 1, "Line 1"),
                    new Event(2, 1, Normal, 2, "Line 2"),
                    new Event(3, 1, Revert, 3, "1"),
                    new Event(4, 1, Normal, 4, "Line 4"),
                    new Event(5, 1, Revert, 5, "3")
                );

        Snapshot snapshot = groovesQuery.computeSnapshot(new Aggregate(1), 5);
        assertThat(snapshot.getSummary()).isEqualTo("Line 1,Line 2,Line 4,");
        assertThat(snapshot.getVersion()).isEqualTo(5);
    }

    @Test
    void shouldRevertEventIfApplied() {
        GroovesQuery<Aggregate, Integer, Snapshot, Event, Integer> groovesQuery =
                createQuery(
                    new Event(1, 1, Normal, 1, "Line 1"),
                    new Event(2, 1, Normal, 2, "Line 2"),
                    new Snapshot(1, 2, "Line 1,Line 2,", new ArrayList<>()),
                    new Event(3, 1, Revert, 3, "1")
                );

        Snapshot snapshot = groovesQuery.computeSnapshot(new Aggregate(1), 3);
        assertThat(snapshot.getSummary()).isEqualTo("Line 2,");
        assertThat(snapshot.getVersion()).isEqualTo(3);
    }

    @Test
    void shouldMergeAggregatesAndReturnRedirect() {
        GroovesQuery<Aggregate, Integer, Snapshot, Event, Integer> groovesQuery =
                createQuery(
                    new Event(1, 1, Normal, 1, "1.1"),
                    new Event(2, 2, Normal, 1, "2.1"),
                    new Event(3, 1, Normal, 2, "1.2"),
                    new Event(4, 2, Normal, 2, "2.2"),
                    new Event(5, 1, Deprecates, 3, "2,6"),
                    new Event(6, 2, DeprecatedBy, 3, "1,5")
                );

        GroovesResult<Snapshot, Aggregate, Integer> result =
                groovesQuery.computeSnapshot(new Aggregate(2), 3, false);
        assertThat(result).isInstanceOf(GroovesResult.Redirect.class);
        Aggregate aggregate =
                ((GroovesResult.Redirect<Snapshot, Aggregate, Integer>) result).getAggregate();
        Integer version = ((GroovesResult.Redirect<Snapshot, Aggregate, Integer>) result).getAt();

        assertThat(aggregate.getId()).isEqualTo(1);
        assertThat(version).isEqualTo(3);
    }

    @Test
    void shouldMergeAggregatesAndReturnResult() {
        GroovesQuery<Aggregate, Integer, Snapshot, Event, Integer> groovesQuery =
                createQuery(
                    new Event(1, 1, Normal, 1, "1.1"),
                    new Event(2, 2, Normal, 1, "2.1"),
                    new Event(3, 1, Normal, 2, "1.2"),
                    new Event(4, 2, Normal, 2, "2.2"),
                    new Event(5, 1, Deprecates, 3, "2,6"),
                    new Event(6, 2, DeprecatedBy, 3, "1,5")
                );

        Snapshot snapshot = groovesQuery.computeSnapshot(new Aggregate(2), 3);
        assertThat(snapshot.getAggregateId()).isEqualTo(1);
        assertThat(snapshot.getDeprecatedAggregates()).containsExactly("2");
        assertThat(snapshot.getSummary()).isEqualTo("1.1,2.1,1.2,2.2,");
    }

}
