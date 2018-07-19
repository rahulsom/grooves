package grooves.example.javaee.queries;

import com.github.rahulsom.grooves.api.EventApplyOutcome;
import com.github.rahulsom.grooves.api.snapshots.Snapshot;
import com.github.rahulsom.grooves.queries.QuerySupport;
import com.github.rahulsom.grooves.queries.internal.Pair;
import grooves.example.javaee.Database;
import grooves.example.javaee.domain.Patient;
import grooves.example.javaee.domain.PatientCreated;
import grooves.example.javaee.domain.PatientEvent;
import grooves.example.javaee.domain.PaymentMade;
import grooves.example.javaee.domain.ProcedurePerformed;
import org.apache.commons.lang3.SerializationUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Publisher;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.rahulsom.grooves.api.EventApplyOutcome.*;
import static grooves.example.javaee.Database.*;
import static java.util.stream.Collectors.*;
import static rx.Observable.*;
import static rx.RxReactiveStreams.*;

// tag::documented[]
public interface CustomQuerySupport<
        SnapshotT extends Snapshot<Patient, Long, Long, PatientEvent> & Serializable // <1>
        > extends QuerySupport<Patient, Long, PatientEvent, Long, SnapshotT> { // <2>

    // end::documented[]
    Database getDatabase();

    Class<SnapshotT> getSnapshotClass();

    @NotNull
    // tag::documented[]
    @Override
    default Publisher<SnapshotT> getSnapshot(long maxPosition, @NotNull Patient aggregate) {
        // <3>
        // end::documented[]
        final Stream<SnapshotT> stream = getDatabase().snapshots(getSnapshotClass());
        return toPublisher(from(stream::iterator)
                .flatMap(it -> just(it).zipWith(
                        toObservable(it.getAggregateObservable()), Pair::new))
                .filter(it -> it.getSecond().equals(aggregate)
                        && it.getFirst().getLastEventPosition() < maxPosition)
                .map(Pair::getFirst)
                .sorted((x, y) -> (int) (x.getLastEventPosition() - y.getLastEventPosition()))
                .takeFirst(it -> true)
                .map(this::copy));
        // tag::documented[]
    }

    @NotNull
    @Override
    default Publisher<SnapshotT> getSnapshot(
            @Nullable Date maxTimestamp, @NotNull Patient aggregate) {
        // <4>
        // end::documented[]
        final Stream<SnapshotT> stream = getDatabase().snapshots(getSnapshotClass());
        return toPublisher(from(stream::iterator)
                .flatMap(it -> just(it).zipWith(
                        toObservable(it.getAggregateObservable()), Pair::new))
                .filter(it -> it.getSecond().equals(aggregate)
                        && it.getFirst().getLastEventTimestamp().compareTo(maxTimestamp) < 1)
                .map(Pair::getFirst)
                .sorted((x, y) -> (int) (x.getLastEventPosition() - y.getLastEventPosition()))
                .takeFirst(it -> true)
                .map(this::copy));
        // tag::documented[]
    }
    // end::documented[]

    default SnapshotT copy(SnapshotT it) {
        return SerializationUtils.clone(it);
    }

    // tag::documented[]
    @Override
    default boolean shouldEventsBeApplied(@NotNull SnapshotT snapshot) { // <5>
        return true;
    }

    @NotNull
    @Override
    default Publisher<EventApplyOutcome> onException(
            @NotNull Exception e, @NotNull SnapshotT snapshot, @NotNull PatientEvent event) { // <6>
        getLog().error("Error computing snapshot", e);
        return toPublisher(just(CONTINUE));
        // tag::documented[]
    }

    @NotNull
    @Override
    default Publisher<PatientEvent> getUncomputedEvents(
            @NotNull Patient aggregate, @Nullable SnapshotT lastSnapshot, long version) {
        // <7>
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
        return toPublisher(from(patientEvents));
        // tag::documented[]
    }

    @NotNull
    @Override
    default Publisher<PatientEvent> getUncomputedEvents(
            @NotNull Patient aggregate, @Nullable SnapshotT lastSnapshot,
            @NotNull Date snapshotTime) {
        // <8>
        // end::documented[]
        Predicate<PatientEvent> patientEventPredicate = it -> aggregate.equals(it.getAggregate())
                && (lastSnapshot == null || isTimestampInRange(
                lastSnapshot.getLastEventTimestamp(), it.getTimestamp(), snapshotTime));
        final List<PatientEvent> patientEvents = getDatabase().events()
                .filter(patientEventPredicate)
                .collect(toList());
        return toPublisher(from(patientEvents));
        // tag::documented[]
    }

    /**
     * Applies patient created.
     * @param event the event.
     * @param snapshot The snapshot.
     * @return the result of apply, ignored by default
     */
    default Publisher<EventApplyOutcome> applyPatientCreated(
            PatientCreated event, SnapshotT snapshot) {
        return toPublisher(just(CONTINUE));
    }

    /**
     * Applies procedure performed.
     * @param event the event.
     * @param snapshot The snapshot.
     * @return the result of apply, ignored by default
     */
    default Publisher<EventApplyOutcome> applyProcedurePerformed(
            ProcedurePerformed event, SnapshotT snapshot) {
        return toPublisher(just(CONTINUE));
    }

    /**
     * Applies Payment made.
     * @param event the event.
     * @param snapshot The snapshot.
     * @return the result of apply, ignored by default
     */
    default Publisher<EventApplyOutcome> applyPaymentMade(
            PaymentMade event, SnapshotT snapshot) {
        return toPublisher(just(CONTINUE));
    }
}
// end::documented[]
