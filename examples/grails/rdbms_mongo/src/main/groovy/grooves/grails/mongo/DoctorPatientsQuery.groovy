package grooves.grails.mongo

import com.github.rahulsom.grooves.api.EventApplyOutcome
import com.github.rahulsom.grooves.grails.GormJoinSupport
import org.reactivestreams.Publisher

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE

/**
 * Queries for Doctor Patient relationships
 *
 * @author Rahul Somasunderam
 */
class DoctorPatientsQuery implements GormJoinSupport<
        Long, Doctor,
        Long, DoctorEvent,
        Long, Patient,
        String, DoctorPatients,
        DoctorGotPatient, DoctorLostPatient,
        DoctorPatientsQuery> {

    final Class snapshotClass = DoctorPatients
    final Class disjoinEventClass = DoctorLostPatient
    final Class joinEventClass = DoctorGotPatient
    final Class eventClass = DoctorEvent

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
        just CONTINUE toPublisher()
    }

    @Override
    DoctorPatients detachSnapshot(DoctorPatients snapshot) {
        if (snapshot.isAttached()) {
            snapshot.discard()
            snapshot.id = null
        }
        snapshot
    }

}
