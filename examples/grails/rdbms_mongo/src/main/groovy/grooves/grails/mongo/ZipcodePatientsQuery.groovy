package grooves.grails.mongo

import com.github.rahulsom.grooves.api.EventApplyOutcome
import com.github.rahulsom.grooves.grails.GormJoinSupport
import rx.Observable

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE
import static rx.Observable.just

/**
 * Finds patients within a zipcode
 *
 * @author Rahul Somasunderam
 */
class ZipcodePatientsQuery implements GormJoinSupport<
        Zipcode, Long, ZipcodeEvent, Long, Patient, String, ZipcodePatients,
        ZipcodeGotPatient, ZipcodeLostPatient> {

    final Class snapshotClass = ZipcodePatients
    final Class disjoinEventClass = ZipcodeLostPatient
    final Class joinEventClass = ZipcodeGotPatient
    final Class eventClass = ZipcodeEvent

    @Override
    ZipcodePatients createEmptySnapshot() {
        new ZipcodePatients(deprecatesIds: [], procedureCounts: [], joinedIds: [])
    }

    @Override
    boolean shouldEventsBeApplied(ZipcodePatients snapshot) {
        true
    }

    @Override
    void addToDeprecates(ZipcodePatients snapshot, Zipcode deprecatedAggregate) {
        // ignore for now
    }

    @Override
    Observable<EventApplyOutcome> onException(
            Exception e, ZipcodePatients snapshot, ZipcodeEvent event) {
        just CONTINUE
    }

    @Override
    ZipcodePatients detachSnapshot(ZipcodePatients snapshot) {
        if (snapshot.isAttached()) {
            snapshot.discard()
            snapshot.id = null
        }
        snapshot
    }

}
