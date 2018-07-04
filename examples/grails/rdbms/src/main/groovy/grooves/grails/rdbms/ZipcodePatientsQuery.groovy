package grooves.grails.rdbms

import com.github.rahulsom.grooves.api.EventApplyOutcome
import com.github.rahulsom.grooves.grails.GormJoinSupport
import org.reactivestreams.Publisher

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE
import static rx.Observable.just
import static rx.RxReactiveStreams.toPublisher

/**
 * Finds patients within a zipcode
 *
 * @author Rahul Somasunderam
 */
class ZipcodePatientsQuery implements GormJoinSupport<
        Long, Zipcode,
        Long, ZipcodeEvent,
        Long, Patient,
        String, ZipcodePatients,
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
    Publisher<EventApplyOutcome> onException(
            Exception e, ZipcodePatients snapshot, ZipcodeEvent event) {
        toPublisher(just(CONTINUE))
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
