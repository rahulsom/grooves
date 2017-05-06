package grooves.grails.mongo

import com.github.rahulsom.grooves.api.EventApplyOutcome
import com.github.rahulsom.grooves.grails.GormJoinSupport
import rx.Observable

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE
import static rx.Observable.just

/**
 * Queries for Doctor Patient relationships
 *
 * @author Rahul Somasunderam
 */
class DoctorPatientsQuery implements GormJoinSupport<
        Doctor, Long, DoctorEvent, Long, Patient, String, DoctorPatients, DoctorGotPatient,
        DoctorLostPatient> {

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
    Observable<EventApplyOutcome> onException(
            Exception e, DoctorPatients snapshot, DoctorEvent event) {
        just CONTINUE
    }
}
