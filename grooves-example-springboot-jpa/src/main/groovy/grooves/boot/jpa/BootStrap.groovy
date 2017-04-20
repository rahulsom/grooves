package grooves.boot.jpa

import com.github.rahulsom.grooves.api.EventsDsl
import grooves.boot.jpa.domain.*
import grooves.boot.jpa.queries.PatientAccountQuery
import grooves.boot.jpa.queries.PatientHealthQuery
import grooves.boot.jpa.repositories.PatientEventRepository
import grooves.boot.jpa.repositories.PatientRepository
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

import java.util.function.Consumer
import java.util.function.Supplier

@Component
class BootStrap implements InitializingBean {

    @Autowired PatientRepository patientRepository
    @Autowired PatientEventRepository patientEventRepository
    @Autowired PatientAccountQuery patientAccountQuery
    @Autowired PatientHealthQuery patientHealthQuery

    void init() {
        createJohnLennon()
        createRingoStarr()
        createPaulMcCartney()
    }

    private Patient createJohnLennon() {
        def patient = patientRepository.save(new Patient(uniqueId: '42'))

        on(patient) {
            apply new PatientCreated(name: 'John Lennon')
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

    private Patient createRingoStarr() {
        def patient = patientRepository.save(new Patient(uniqueId: '43'))

        on(patient) {
            apply new PatientCreated(name: 'Ringo Starr')
            apply new ProcedurePerformed(code: 'ANNUALPHYSICAL', cost: 170.00)
            apply new ProcedurePerformed(code: 'GLUCOSETEST', cost: 78.93)
            apply new PaymentMade(amount: 100.25)

            snapshotWith patientAccountQuery
            snapshotWith patientHealthQuery

            apply new ProcedurePerformed(code: 'FLUSHOT', cost: 32.40)
            apply new PaymentMade(amount: 180.00)

            snapshotWith patientAccountQuery
            snapshotWith patientHealthQuery
        }
    }

    private Patient createPaulMcCartney() {
        def patient = patientRepository.save(new Patient(uniqueId: '44'))

        on(patient) {
            apply new PatientCreated(name: 'Paul McCartney')
            apply new ProcedurePerformed(code: 'ANNUALPHYSICAL', cost: 170.00)
            def gluc = apply new ProcedurePerformed(code: 'GLUCOSETEST', cost: 78.93)
            apply new PaymentMade(amount: 100.25)
            apply new PatientEventReverted(revertedEventId: gluc.id)
            def pmt = apply new PaymentMade(amount: 30.00)

            snapshotWith patientAccountQuery
            snapshotWith patientHealthQuery

            apply new PatientEventReverted(revertedEventId: pmt.id)
            apply new PaymentMade(amount: 60.00)


            snapshotWith patientAccountQuery
            snapshotWith patientHealthQuery

            apply new PaymentMade(amount: 60.00)

            snapshotWith patientAccountQuery
            snapshotWith patientHealthQuery
        }

    }

    Date currDate = Date.parse('yyyy-MM-dd', '2016-01-01')

    Patient on(Patient patient, @DelegatesTo(EventsDsl.OnSpec) Closure closure) {
        def eventSaver = { patientEventRepository.save(it) } as Consumer
        def positionSupplier = { patientEventRepository.countByAggregateId(patient.id) + 1 } as Supplier<Long>
        def userSupplier = { 'anonymous' }
        def dateSupplier = { currDate += 1; currDate }
        new EventsDsl<Patient, Long, PatientEvent>().on(patient, eventSaver, positionSupplier, userSupplier, dateSupplier, closure)
    }

    @Transactional
    @Override
    void afterPropertiesSet() throws Exception {
        init()
    }
}
