package grooves.grails.mongo

import com.github.rahulsom.grooves.api.EventApplyOutcome
import com.github.rahulsom.grooves.grails.GormJoinSupport
import grails.compiler.GrailsCompileStatic
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE

class ZipcodePatientsQuery implements GormJoinSupport<
        Zipcode, Long, ZipcodeEvent, Long, Patient, String, ZipcodePatients, ZipcodeGotPatient, ZipcodeLostPatient> {

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
    ZipcodeEvent unwrapIfProxy(ZipcodeEvent event) {
        GrailsHibernateUtil.unwrapIfProxy(event) as ZipcodeEvent
    }

    @Override
    EventApplyOutcome onException(Exception e, ZipcodePatients snapshot, ZipcodeEvent event) {
        CONTINUE
    }
}
