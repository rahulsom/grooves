package com.github.rahulsom.grooves;

import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class AcceptanceTestFixture {
    @AllArgsConstructor
    @Getter
    public static class Aggregate {
        private int id;

        @Override
        public String toString() {
            return MessageFormat.format("A{0}", id);
        }
    }

    public interface Storable {

    }

    @AllArgsConstructor
    @Getter
    public static class Event implements Storable {
        private int id;
        private int aggregateId;
        private EventType eventType;

        @NonNull
        public EventType getEventType() {
            return eventType;
        }

        private int version;
        private String data;

        @Override
        public String toString() {
            return MessageFormat.format("A{1}.E{0}-{2}-{3}", id, aggregateId, eventType, version);
        }
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static class Snapshot implements Storable {
        private int aggregateId;
        private int version;
        private String summary;
        private List<String> deprecatedAggregates;

        @Override
        public String toString() {
            return MessageFormat.format("A{0}.S{1}", aggregateId, version);
        }
    }

    /**
     * Creates a {@link GroovesQuery} with a datastore based on objects passed to it.
     *
     * @param objects vararg of {@link Snapshot} or {@link Event}
     * @return The {@link GroovesQuery} to use for testing
     */
    @NotNull
    public static GroovesQuery<Aggregate, Integer, Snapshot, Event, Integer> createQuery(
            Storable... objects) {
        List<Snapshot> knownSnapshots = Arrays.stream(objects)
                .filter(it -> it instanceof Snapshot).map(it -> (Snapshot) it)
                .toList();
        List<Event> events = Arrays.stream(objects)
                .filter(it -> it instanceof Event).map(it -> (Event) it)
                .toList();

        return GroovesQueryBuilder.<Aggregate, Integer, Snapshot, Event, Integer>builder()
            .snapshotProvider((aggregate, version) ->
                knownSnapshots.stream()
                    .filter(it -> it.getAggregateId() == aggregate.getId())
                    .sorted(Comparator.comparing(Snapshot::getVersion).reversed())
                    .filter(it -> version == null || it.getVersion() <= version)
                    .findFirst()
                    .orElse(null))
            .emptySnapshotProvider(aggregate ->
                new Snapshot(aggregate.getId(), 0, "", new ArrayList<>()))
            .eventsProvider((aggregates, version, lastSnapshot) ->
                events.stream()
                    .filter(it ->
                        aggregates.stream().anyMatch(x -> it.getAggregateId() == x.getId())
                                && (version == null || it.getVersion() <= version)
                                && it.getVersion() > lastSnapshot.getVersion()
                    )
                    .sorted(Comparator.comparing(Event::getVersion))
            )
            .exceptionHandler((exception, snapshot, event) -> {
                snapshot.setSummary(snapshot.getSummary() + exception.getMessage() + ",");
                return EventApplyOutcome.CONTINUE;
            })
            .eventHandler((event, snapshot) -> {
                snapshot.setSummary(snapshot.getSummary() + event.getData() + ",");
                return EventApplyOutcome.CONTINUE;
            })
            .eventClassifier(Event::getEventType)
            .eventVersionProvider(Event::getVersion)
            .applyMoreEventsPredicate(snapshot -> true)
            .deprecator((snapshot, event) ->
                snapshot.getDeprecatedAggregates().add(event.getData().split(",")[0]))
            .snapshotVersionSetter(Snapshot::setVersion)
            .deprecatedByProvider(event -> {
                List<Integer> ints = Arrays.stream(event.data.split(","))
                        .map(Integer::parseInt)
                        .toList();
                return new DeprecatedByResult<>(new Aggregate(ints.get(0)), ints.get(1));
            })
            .revertedEventProvider(event -> events.stream()
                .filter(it -> it.getId() == Integer.parseInt(event.data))
                .findAny()
                .orElse(null))
            .eventIdProvider(Event::getId)
            .build()
            .toQuery();
    }
}
