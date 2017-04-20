package grooves.grails.mongo

import com.github.rahulsom.grooves.api.EventApplyOutcome
import com.github.rahulsom.grooves.grails.GormJoinSupport
import grails.compiler.GrailsCompileStatic
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE

@GrailsCompileStatic
class DoctorPatientsQuery extends
        GormJoinSupport<Doctor, Long, DoctorEvent, Long, Patient, String, DoctorPatients, DoctorGotPatient, DoctorLostPatient> {

    DoctorPatientsQuery() {
        super(Doctor, Long, DoctorEvent, Long, Patient, String, DoctorPatients, DoctorGotPatient, DoctorLostPatient)
    }

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
    DoctorEvent unwrapIfProxy(DoctorEvent event) {
        GrailsHibernateUtil.unwrapIfProxy(event) as DoctorEvent
    }

    @Override
    EventApplyOutcome onException(Exception e, DoctorPatients snapshot, DoctorEvent event) {
        CONTINUE
    }
}
