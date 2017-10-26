package grooves.example.javaee.queries;

import com.github.rahulsom.grooves.api.EventApplyOutcome;
import com.github.rahulsom.grooves.api.snapshots.Snapshot;
import com.github.rahulsom.grooves.queries.QuerySupport;
import com.github.rahulsom.grooves.queries.internal.Pair;
import grooves.example.javaee.Database;
import grooves.example.javaee.domain.Patient;
import grooves.example.javaee.domain.PatientEvent;
import org.apache.commons.lang3.SerializationUtils;
import rx.Observable;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE;
import static grooves.example.javaee.Database.isTimestampInRange;
import static java.util.stream.Collectors.toList;
import static rx.Observable.from;
import static rx.Observable.just;

// tag::documented[]
public interface CustomQuerySupport<
        SnapshotT extends Snapshot<Long, Patient, Long, Long, PatientEvent> & Serializable, // <1>
        QueryT extends CustomQuerySupport<SnapshotT, QueryT>// <2>
        > extends QuerySupport<Long, Patient, Long, PatientEvent, Long, SnapshotT, QueryT> { // <3>

    // end::documented[]
    Database getDatabase();

    Class<SnapshotT> getSnapshotClass();

    // tag::documented[]
    @Override
    default Observable<SnapshotT> getSnapshot(long maxPosition, Patient aggregate) {
        // <4>
        // end::documented[]
        final Stream<SnapshotT> stream = getDatabase().snapshots(getSnapshotClass());
        return from(stream::iterator)
                .flatMap(it -> just(it).zipWith(it.getAggregateObservable(), Pair::new))
                .filter(it -> it.getSecond().equals(aggregate)
                        && it.getFirst().getLastEventPosition() < maxPosition)
                .map(Pair::getFirst)
                .sorted((x, y) -> x.getLastEventPosition().compareTo(y.getLastEventPosition()))
                .takeFirst(it -> true)
                .filter(Objects::nonNull)
                .map(this::copy);
        // tag::documented[]
    }

    @Override
    default Observable<SnapshotT> getSnapshot(Date maxTimestamp, Patient aggregate) {
        // <5>
        // end::documented[]
        final Stream<SnapshotT> stream = getDatabase().snapshots(getSnapshotClass());
        return from(stream::iterator)
                .flatMap(it -> just(it).zipWith(it.getAggregateObservable(), Pair::new))
                .filter(it -> it.getSecond().equals(aggregate)
                        && it.getFirst().getLastEventTimestamp().compareTo(maxTimestamp) < 1)
                .map(Pair::getFirst)
                .sorted((x, y) -> x.getLastEventPosition().compareTo(y.getLastEventPosition()))
                .takeFirst(it -> true)
                .filter(Objects::nonNull)
                .map(this::copy);
        // tag::documented[]
    }
    // end::documented[]

    default SnapshotT copy(SnapshotT it) {
        return SerializationUtils.clone(it);
    }

    // tag::documented[]
    @Override
    default boolean shouldEventsBeApplied(SnapshotT snapshot) { // <6>
        return true;
    }

    @Override
    default Observable<EventApplyOutcome> onException(
            Exception e, SnapshotT snapshot, PatientEvent event) { // <7>
        getLog().error("Error computing snapshot", e);
        return just(CONTINUE);
        // tag::documented[]
    }

    @Override
    default Observable<PatientEvent> getUncomputedEvents(
            Patient aggregate, SnapshotT lastSnapshot, long version) {
        // <8>
        // end::documented[]
        Predicate<PatientEvent> patientEventPredicate = x -> {
            Long eventPosition = x.getPosition();
            Long snapshotPosition = 0L;
            if (lastSnapshot != null) {
                snapshotPosition = lastSnapshot.getLastEventPosition();
            }
            return Objects.equals(x.getAggregate(), aggregate)
                    && (snapshotPosition == null || eventPosition > snapshotPosition)
                    && eventPosition <= version;
        };
        final List<PatientEvent> patientEvents = getDatabase().events()
                .filter(patientEventPredicate)
                .collect(toList());
        return from(patientEvents);
        // tag::documented[]
    }

    @Override
    default Observable<PatientEvent> getUncomputedEvents(
            Patient aggregate, SnapshotT lastSnapshot, Date snapshotTime) {
        // <9>
        // end::documented[]
        Predicate<PatientEvent> patientEventPredicate = it -> aggregate.equals(it.getAggregate())
                && isTimestampInRange(
                lastSnapshot.getLastEventTimestamp(), it.getTimestamp(), snapshotTime);
        final List<PatientEvent> patientEvents = getDatabase().events()
                .filter(patientEventPredicate)
                .collect(toList());
        return from(patientEvents);
        // tag::documented[]
    }

}
// end::documented[]
