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
import java.util.stream.Stream;

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE;
import static java.util.stream.Collectors.toList;
import static rx.Observable.from;
import static rx.Observable.just;

public interface CustomQuerySupport<
        SnapshotT extends Snapshot<Patient, Long, Long, PatientEvent> & Serializable
        > extends QuerySupport<Patient, Long, PatientEvent, Long, SnapshotT> {

    Database getDatabase();

    Class<SnapshotT> getSnapshotClass();

    @Override
    default Observable<SnapshotT> getSnapshot(long maxPosition, Patient aggregate) {
        final Stream<SnapshotT> stream = getDatabase().snapshots(getSnapshotClass());
        return Observable.from(stream::iterator)
                .flatMap(it -> Observable.just(it).zipWith(it.getAggregateObservable(), Pair::new))
                .filter(it -> it.getSecond().equals(aggregate)
                        && it.getFirst().getLastEventPosition() < maxPosition)
                .map(Pair::getFirst)
                .sorted((x, y) -> x.getLastEventPosition().compareTo(y.getLastEventPosition()))
                .takeFirst(it -> true)
                .filter(Objects::nonNull)
                .map(this::copy);
    }

    @Override
    default Observable<SnapshotT> getSnapshot(Date maxTimestamp, Patient aggregate) {
        final Stream<SnapshotT> stream = getDatabase().snapshots(getSnapshotClass());
        return Observable.from(stream::iterator)
                .flatMap(it -> Observable.just(it).zipWith(it.getAggregateObservable(), Pair::new))
                .filter(it -> it.getSecond().equals(aggregate)
                        && it.getFirst().getLastEventTimestamp().compareTo(maxTimestamp) < 1)
                .map(Pair::getFirst)
                .sorted((x, y) -> x.getLastEventPosition().compareTo(y.getLastEventPosition()))
                .takeFirst(it -> true)
                .filter(Objects::nonNull)
                .map(this::copy);
    }

    default SnapshotT copy(SnapshotT it) {
        return (SnapshotT) SerializationUtils.clone(it);
    }

    @Override
    default boolean shouldEventsBeApplied(SnapshotT snapshot) {
        return true;
    }

    @Override
    default Observable<PatientEvent> findEventsForAggregates(List<Patient> aggregates) {
        final List<PatientEvent> patientEvents = getDatabase().events()
                .filter(x -> aggregates.contains(x.getAggregate()))
                .collect(toList());
        return from(patientEvents);
    }

    @Override
    default Observable<EventApplyOutcome> onException(
            Exception e, SnapshotT snapshot, PatientEvent event) {
        getLog().error("Error computing snapshot", e);
        return just(CONTINUE);
    }

    @Override
    default Observable<PatientEvent> getUncomputedEvents(
            Patient aggregate, SnapshotT lastSnapshot, long version) {
        final List<PatientEvent> patientEvents = getDatabase().events()
                .filter(x -> x.getAggregate().equals(aggregate)
                        && (lastSnapshot.getLastEventPosition() == null
                        || x.getPosition() > lastSnapshot.getLastEventPosition())
                        && x.getPosition() <= version)
                .collect(toList());
        return from(patientEvents);
    }

    @Override
    default Observable<PatientEvent> getUncomputedEvents(
            Patient aggregate, SnapshotT lastSnapshot, Date snapshotTime) {
        final List<PatientEvent> patientEvents = getDatabase().events()
                .filter(x -> {
                            final Date eventTime = x.getTimestamp();
                            final Date lastEventTime = lastSnapshot.getLastEventTimestamp();
                            return x.getAggregate().equals(aggregate)
                                    && (lastEventTime == null
                                    || eventTime.compareTo(lastEventTime) > 0)
                                    && eventTime.compareTo(snapshotTime) <= 0;
                        }
                )
                .collect(toList());
        return from(patientEvents);
    }

}
