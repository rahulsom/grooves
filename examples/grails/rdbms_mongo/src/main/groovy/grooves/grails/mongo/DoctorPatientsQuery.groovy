package grooves.grails.mongo

import com.github.rahulsom.grooves.api.EventApplyOutcome
import com.github.rahulsom.grooves.queries.JoinSupport
import org.reactivestreams.Publisher

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE
import static rx.Observable.*
import static rx.RxReactiveStreams.toPublisher

/**
 * Queries for Doctor Patient relationships
 *
 * @author Rahul Somasunderam
 */
//tag::joins[]
class DoctorPatientsQuery implements JoinSupport< // <1>
        Doctor, // <2>
        Long, DoctorEvent, // <3>
        Patient, // <4>
        String, DoctorPatients, // <5>
        DoctorGotPatient, DoctorLostPatient // <6>
        > { // <7>

    final Class disjoinEventClass = DoctorLostPatient // <8>
    final Class joinEventClass = DoctorGotPatient // <9>

    // Skipping familiar methods <10>
//end::joins[]
    @Override
    DoctorPatients createEmptySnapshot() {
        new DoctorPatients(deprecatesIds: [], procedureCounts: [], joinedIds: [])
    }

    @Override
    boolean shouldEventsBeApplied(DoctorPatients snapshot) {
        true
    }

    @Override
    void addToDeprecates(DoctorPatients snapshot, Doctor deprecatedAggregate) {
        // ignore for now
    }

    @Override
    Publisher<EventApplyOutcome> onException(
            Exception e, DoctorPatients snapshot, DoctorEvent event) {
        toPublisher(just(CONTINUE))
    }

    static DoctorPatients detachSnapshot(DoctorPatients snapshot) {
        if (snapshot.isAttached()) {
            snapshot.discard()
            snapshot.id = null
        }
        snapshot
    }

    @Override
    Publisher<DoctorPatients> getSnapshot(Date maxTimestamp, Doctor aggregate) {
        def snapshot = DoctorPatients.findAllByAggregateIdAndLastEventTimestampLessThanEquals(
                aggregate.id, maxTimestamp, [max: 1, offset: 0])
        if (snapshot) {
            toPublisher just(detachSnapshot(snapshot[0]))
        } else {
            toPublisher empty()
        }
    }

    @Override
    Publisher<DoctorPatients> getSnapshot(long maxPosition, Doctor aggregate) {
        def snapshot = DoctorPatients.findAllByAggregateIdAndLastEventPositionLessThanEquals(
                aggregate.id, maxPosition, [max: 1, offset: 0])
        if (snapshot) {
            toPublisher just(detachSnapshot(snapshot[0]))
        } else {
            toPublisher empty()
        }
    }

    @Override
    Publisher<DoctorEvent> getUncomputedEvents(
            Doctor aggregate, DoctorPatients lastSnapshot, Date snapshotTime) {
        toPublisher from(DoctorEvent.findAllByAggregateAndTimestampBetween(
                aggregate, lastSnapshot?.lastEventTimestamp, snapshotTime))
    }

    @Override
    Publisher<DoctorEvent> getUncomputedEvents(
            Doctor aggregate, DoctorPatients lastSnapshot, long version) {
        toPublisher from(DoctorEvent.findAllByAggregateAndPositionBetween(
                aggregate, lastSnapshot?.lastEventPosition, version))
    }

//tag::joins[]
}
//end::joins[]
