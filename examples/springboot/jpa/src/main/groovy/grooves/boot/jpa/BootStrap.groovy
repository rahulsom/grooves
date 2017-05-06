package grooves.boot.jpa

import com.github.rahulsom.grooves.api.EventsDsl
import com.github.rahulsom.grooves.groovy.GroovyEventsDsl
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

/**
 * Initializes Application with usable data.
 *
 * @author Rahul Somasunderam
 */
@Component
@SuppressWarnings(['DuplicateNumberLiteral', 'DuplicateStringLiteral'])
class BootStrap implements InitializingBean {

    @Autowired PatientRepository patientRepository
    @Autowired PatientEventRepository patientEventRepository
    @Autowired PatientAccountQuery patientAccountQuery
    @Autowired PatientHealthQuery patientHealthQuery

    void init() {
        setupJohnLennon()
        setupRingoStarr()
        setupPaulMcCartney()
        setupGeorgeHarrison()
    }

    private Patient setupJohnLennon() {
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

    private Patient setupRingoStarr() {
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

    private Patient setupPaulMcCartney() {
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

    private Patient setupGeorgeHarrison() {
        def patient = patientRepository.save(new Patient(uniqueId: '45'))
        def patient2 = patientRepository.save(new Patient(uniqueId: '46'))

        on(patient) {
            apply new PatientCreated(name: 'George Harrison')
            apply new ProcedurePerformed(code: 'ANNUALPHYSICAL', cost: 170.00)
            apply new ProcedurePerformed(code: 'GLUCOSETEST', cost: 78.93)

            snapshotWith patientAccountQuery
            snapshotWith patientHealthQuery
        }

        on(patient2) {
            apply new PatientCreated(name:
                    'George Harrison, Member of the Most Excellent Order of the British Empire')
            apply new PaymentMade(amount: 100.25)

            snapshotWith patientAccountQuery
            snapshotWith patientHealthQuery
        }

        currDate += 1
        merge(patient, patient2)
        patient
    }

    /**
     *
     * @param self The aggregate to be deprecated
     * @param into The aggregate to survive
     * @return
     */
    private PatientDeprecatedBy merge(Patient self, Patient into) {
        def e1 = new PatientDeprecatedBy(aggregate: self, createdBy: 'anonymous', deprecator: into,
                timestamp: currDate,
                position: patientEventRepository.countByAggregateId(self.id) + 1,)
        def e2 = new PatientDeprecates(aggregate: into, createdBy: 'anonymous', deprecated: self,
                timestamp: currDate, converse: e1,
                position: patientEventRepository.countByAggregateId(into.id) + 1,)
        e1.converse = e2
        patientEventRepository.save([e1, e2,])
        e2.converse
    }

    Date currDate = Date.parse('yyyy-MM-dd', '2016-01-01')

    Patient on(Patient patient, @DelegatesTo(EventsDsl.OnSpec) Closure closure) {
        def eventSaver = { patientEventRepository.save(it) } as Consumer
        def positionSupplier = {
            patientEventRepository.countByAggregateId(patient.id) + 1
        } as Supplier<Long>
        def userSupplier = { 'anonymous' }
        def dateSupplier = { currDate += 1; currDate }
        new GroovyEventsDsl<Patient, Long, PatientEvent>().on(
                patient, eventSaver, positionSupplier, userSupplier, dateSupplier,
                closure)
    }

    @Transactional
    @Override
    void afterPropertiesSet() throws Exception {
        init()
    }
}
