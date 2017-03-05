package grooves.boot.jpa

import com.github.rahulsom.grooves.api.EventsDsl
import grooves.boot.jpa.domain.Patient
import grooves.boot.jpa.domain.PatientCreated
import grooves.boot.jpa.domain.PaymentMade
import grooves.boot.jpa.domain.ProcedurePerformed
import grooves.boot.jpa.queries.PatientAccountQuery
import grooves.boot.jpa.queries.PatientHealthQuery
import grooves.boot.jpa.repositories.PatientEventRepository
import grooves.boot.jpa.repositories.PatientRepository
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

import javax.persistence.EntityManager

@Component
class BootStrap implements InitializingBean {

    @Autowired EntityManager entityManager
    @Autowired PatientRepository patientRepository
    @Autowired PatientEventRepository patientEventRepository
    @Autowired PatientAccountQuery patientAccountQuery
    @Autowired PatientHealthQuery patientHealthQuery

    void init() {
        def patient = patientRepository.save(new Patient(uniqueId: '42'))

        on(patient) {
            apply new PatientCreated(name: 'John Smith')
            apply new ProcedurePerformed(code: 'FLUSHOT', cost: 32.40)
            apply new ProcedurePerformed(code: 'GLUCOSETEST', cost: 78.93)
            apply new PaymentMade(amount: 100.25)

            snapshotWith patientAccountQuery
            snapshotWith patientHealthQuery

            apply new ProcedurePerformed(code: 'ANNUALPHYSICAL', cost: 170.00)
            apply new PaymentMade(amount: 180.00)

            snapshotWith patientAccountQuery
            snapshotWith patientHealthQuery
        }

    }

    void on(Patient patient, @DelegatesTo(EventsDsl.OnSpec) Closure closure) {
        def entitySaver = { patientEventRepository.save(it) }
        def positionSupplier = { patientEventRepository.countByAggregateId(patient.id) + 1 }
        EventsDsl.on(patient, entitySaver, positionSupplier, closure)
    }
    @Transactional
    @Override
    void afterPropertiesSet() throws Exception {
        init()
    }
}
