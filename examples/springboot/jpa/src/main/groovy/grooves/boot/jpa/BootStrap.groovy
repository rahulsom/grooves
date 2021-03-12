package grooves.boot.jpa

import com.github.rahulsom.grooves.test.GroovyEventsDsl
import com.github.rahulsom.grooves.test.OnSpec
import grooves.boot.jpa.domain.*
import grooves.boot.jpa.queries.PatientAccountQuery
import grooves.boot.jpa.queries.PatientHealthQuery
import grooves.boot.jpa.repositories.PatientEventRepository
import grooves.boot.jpa.repositories.PatientRepository
import grooves.boot.jpa.repositories.ZipcodeEventRepository
import grooves.boot.jpa.repositories.ZipcodeRepository
import groovy.transform.CompileDynamic
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

import java.text.SimpleDateFormat
import java.util.function.Consumer
import java.util.function.Supplier

/**
 * Initializes Application with usable data.
 *
 * @author Rahul Somasunderam
 */
@Component
@SuppressWarnings(['DuplicateNumberLiteral', 'DuplicateStringLiteral'])
@CompileDynamic
class BootStrap implements InitializingBean {

    private static final String START_DATE = '2016-01-01'

    @Autowired PatientAccountQuery patientAccountQuery
    @Autowired PatientEventRepository patientEventRepository
    @Autowired PatientHealthQuery patientHealthQuery
    @Autowired PatientRepository patientRepository
    @Autowired ZipcodeEventRepository zipcodeEventRepository
    @Autowired ZipcodeRepository zipcodeRepository

    @Transactional
    @Override
    void afterPropertiesSet() throws Exception {
        init()
    }

    void init() {
        setupJohnLennon()
        setupRingoStarr()
        setupPaulMcCartney()
        setupFreddieMercury()
        setupTinaFeyAndSarahPalin()

        def campbell = on(zipcodeRepository.save(new Zipcode(uniqueId: '95008'))) {
            apply new ZipcodeCreated(name: 'Campbell, California', timestamp: currDate)
        }
        def santanaRow = on(zipcodeRepository.save(new Zipcode(uniqueId: '95128'))) {
            apply new ZipcodeCreated(name: 'Santana Row, San Jose, California', timestamp: currDate)
        }
        linkZipcodesAndPatients(campbell, santanaRow)
    }

    Date currDate = Date.parse('yyyy-MM-dd', START_DATE)

    Zipcode on(Zipcode zipcode, @DelegatesTo(OnSpec) Closure closure) {
        def eventSaver = { zipcodeEventRepository.save(it) } as Consumer
        def positionSupplier = {
            zipcodeEventRepository.countByAggregateId(zipcode.id) + 1
        } as Supplier<Long>
        def dateSupplier = { currDate += 1; currDate }
        new GroovyEventsDsl<Zipcode, Long, ZipcodeEvent>().on(
            zipcode, eventSaver, positionSupplier, dateSupplier, closure)
    }

    Patient on(Patient patient, @DelegatesTo(OnSpec) Closure closure) {
        def eventSaver = { patientEventRepository.save(it) } as Consumer
        def positionSupplier = {
            patientEventRepository.countByAggregateId(patient.id) + 1
        } as Supplier<Long>
        def dateSupplier = { currDate += 1; currDate }
        new GroovyEventsDsl<Patient, Long, PatientEvent>().on(
            patient, eventSaver, positionSupplier, dateSupplier, closure)
    }

    @SuppressWarnings(['SimpleDateFormatMissingLocale'])
    private static Date d(String input) {
        new SimpleDateFormat('yyyy-MM-dd HH:mm:ss').parse(input)
    }

    @SuppressWarnings(['MethodSize', 'AbcMetric'])
    private void linkZipcodesAndPatients(Zipcode campbell, Zipcode santanaRow) {
        def i1 = on(patientRepository.save(new Patient(uniqueId: 'I1'))) {
            apply new PatientCreated(timestamp: d('2016-01-02 02:56:02'), name: 'LISA BAKER')
            apply new ProcedurePerformed(timestamp: d('2016-01-04 02:56:02'), code: 'FLUSHOT', cost: 32.40)
            apply new ProcedurePerformed(timestamp: d('2016-01-05 02:56:02'), code: 'ANNUALPHYSICAL',
                cost: 170.00)
            apply new ProcedurePerformed(timestamp: d('2016-01-07 02:56:02'), code: 'GLUCOSETEST', cost: 78.93)
        }
        join(i1, campbell, d('2016-01-04 12:14:20'))

        def i2 = on(patientRepository.save(new Patient(uniqueId: 'I2'))) {
            apply new PatientCreated(timestamp: d('2016-01-02 09:45:28'), name: 'CAROL BENNETT')
            apply new ProcedurePerformed(timestamp: d('2016-01-03 09:45:28'), code: 'FLUSHOT', cost: 32.40)
            apply new ProcedurePerformed(timestamp: d('2016-01-04 09:45:28'), code: 'ANNUALPHYSICAL',
                cost: 170.00)
            apply new ProcedurePerformed(timestamp: d('2016-01-07 09:45:28'), code: 'GLUCOSETEST', cost: 78.93)
        }
        join(i2, campbell, d('2016-01-12 19:52:05'))
        disjoin(i2, campbell, d('2016-01-22 19:52:05'))
        join(i2, santanaRow, d('2016-01-23 19:52:05'))
        disjoin(i2, santanaRow, d('2016-01-26 19:52:05'))
        join(i2, campbell, d('2016-01-27 19:52:05'))

        def i3 = on(patientRepository.save(new Patient(uniqueId: 'I3'))) {
            apply new PatientCreated(timestamp: d('2016-01-02 17:53:55'), name: 'JESSICA TUCKER')
            apply new ProcedurePerformed(timestamp: d('2016-01-07 17:53:55'), code: 'FLUSHOT', cost: 32.40)
            apply new ProcedurePerformed(timestamp: d('2016-01-10 17:53:55'), code: 'ANNUALPHYSICAL',
                cost: 170.00)
            apply new ProcedurePerformed(timestamp: d('2016-01-12 17:53:55'), code: 'GLUCOSETEST', cost: 78.93)
            apply new ProcedurePerformed(timestamp: d('2016-01-15 17:53:55'), code: 'LIPIDTEST', cost: 102.55)
        }
        join(i3, campbell, d('2016-01-03 21:25:31'))

        def i4 = on(patientRepository.save(new Patient(uniqueId: 'I4'))) {
            apply new PatientCreated(timestamp: d('2016-01-02 07:29:07'), name: 'KATHLEEN HARPER')
            apply new ProcedurePerformed(timestamp: d('2016-01-03 07:29:07'), code: 'FLUSHOT', cost: 32.40)
        }
        join(i4, santanaRow, d('2020-06-25 19:57:38'))

        def i5 = on(patientRepository.save(new Patient(uniqueId: 'I5'))) {
            apply new PatientCreated(timestamp: d('2016-01-02 02:06:56'), name: 'ANN ZIMMERMAN')
            apply new ProcedurePerformed(timestamp: d('2016-01-04 02:06:56'), code: 'FLUSHOT', cost: 32.40)
            apply new ProcedurePerformed(timestamp: d('2016-01-05 02:06:56'), code: 'ANNUALPHYSICAL',
                cost: 170.00)
            apply new ProcedurePerformed(timestamp: d('2016-01-06 02:06:56'), code: 'GLUCOSETEST', cost: 78.93)
            apply new ProcedurePerformed(timestamp: d('2016-01-07 02:06:56'), code: 'LIPIDTEST', cost: 102.55)
            apply new ProcedurePerformed(timestamp: d('2016-01-09 02:06:56'), code: 'XRAY_WRIST', cost: 70.42)
        }
        join(i5, santanaRow, d('2016-01-08 16:03:15'))
        disjoin(i5, santanaRow, d('2016-01-16 16:03:15'))
        join(i5, campbell, d('2016-01-17 16:03:15'))

        def i6 = on(patientRepository.save(new Patient(uniqueId: 'I6'))) {
            apply new PatientCreated(timestamp: d('2016-01-02 02:56:02'), name: 'KATHERINE CARDENAS')
            apply new ProcedurePerformed(timestamp: d('2016-01-04 02:56:02'), code: 'FLUSHOT', cost: 32.40)
            apply new ProcedurePerformed(timestamp: d('2016-01-05 02:56:02'), code: 'ANNUALPHYSICAL',
                cost: 170.00)
            apply new ProcedurePerformed(timestamp: d('2016-01-07 02:56:02'), code: 'GLUCOSETEST', cost: 78.93)
        }
        join(i6, campbell, d('2016-01-04 12:14:20'))

        def i7 = on(patientRepository.save(new Patient(uniqueId: 'I7'))) {
            apply new PatientCreated(timestamp: d('2016-01-02 09:48:41'), name: 'JANE HICKMAN')
            apply new ProcedurePerformed(timestamp: d('2016-01-04 09:48:41'), code: 'FLUSHOT', cost: 32.40)
        }
        join(i7, campbell, d('2016-01-10 12:58:36'))
        disjoin(i7, campbell, d('2016-01-20 12:58:36'))
        join(i7, santanaRow, d('2016-01-21 12:58:36'))
        disjoin(i7, santanaRow, d('2016-01-30 12:58:36'))
        join(i7, campbell, d('2016-01-31 12:58:36'))

        def i8 = on(patientRepository.save(new Patient(uniqueId: 'I8'))) {
            apply new PatientCreated(timestamp: d('2016-01-02 17:53:55'), name: 'ANNIE NIEVES')
            apply new ProcedurePerformed(timestamp: d('2016-01-07 17:53:55'), code: 'FLUSHOT', cost: 32.40)
            apply new ProcedurePerformed(timestamp: d('2016-01-10 17:53:55'), code: 'ANNUALPHYSICAL',
                cost: 170.00)
            apply new ProcedurePerformed(timestamp: d('2016-01-12 17:53:55'), code: 'GLUCOSETEST', cost: 78.93)
            apply new ProcedurePerformed(timestamp: d('2016-01-15 17:53:55'), code: 'LIPIDTEST', cost: 102.55)
        }
        join(i8, campbell, d('2016-01-03 21:25:31'))

        def i9 = on(patientRepository.save(new Patient(uniqueId: 'I9'))) {
            apply new PatientCreated(timestamp: d('2016-01-02 07:29:07'), name: 'SYLVIA CARLISLE')
            apply new ProcedurePerformed(timestamp: d('2016-01-03 07:29:07'), code: 'FLUSHOT', cost: 32.40)
        }
        join(i9, santanaRow, d('2016-01-03 12:48:52'))

        def i10 = on(patientRepository.save(new Patient(uniqueId: 'I10'))) {
            apply new PatientCreated(timestamp: d('2016-01-02 01:57:19'), name: 'JOANNE KELSEY')
            apply new ProcedurePerformed(timestamp: d('2016-01-04 01:57:19'), code: 'FLUSHOT', cost: 32.40)
            apply new ProcedurePerformed(timestamp: d('2016-01-05 01:57:19'), code: 'ANNUALPHYSICAL',
                cost: 170.00)
            apply new ProcedurePerformed(timestamp: d('2016-01-10 01:57:19'), code: 'GLUCOSETEST', cost: 78.93)
        }
        join(i10, santanaRow, d('2016-01-12 23:33:09'))
        disjoin(i10, santanaRow, d('2016-01-14 23:33:09'))
        join(i10, campbell, d('2016-01-15 23:33:09'))
        disjoin(i10, campbell, d('2016-01-24 23:33:09'))
        join(i10, santanaRow, d('2016-01-25 23:33:09'))
        disjoin(i10, santanaRow, d('2016-01-28 23:33:09'))
        join(i10, campbell, d('2016-01-29 23:33:09'))
    }

    private void join(Patient patient, Zipcode zipcode, Date date) {
        on(patient) {
            apply new PatientAddedToZipcode(timestamp: date, zipcode: zipcode)
        }
        on(zipcode) {
            apply new ZipcodeGotPatient(timestamp: date, patient: patient)
        }
    }

    private void disjoin(Patient patient, Zipcode zipcode, Date date) {
        on(patient) {
            apply new PatientRemovedFromZipcode(timestamp: date, zipcode: zipcode)
        }
        on(zipcode) {
            apply new ZipcodeLostPatient(timestamp: date, patient: patient)
        }
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

    private Patient setupFreddieMercury() {
        def patient = patientRepository.save(new Patient(uniqueId: '45'))
        def patient2 = patientRepository.save(new Patient(uniqueId: '46'))

        on(patient) {
            apply new PatientCreated(name: 'Farrokh Bulsara')
            apply new ProcedurePerformed(code: 'ANNUALPHYSICAL', cost: 170.00)
            apply new ProcedurePerformed(code: 'GLUCOSETEST', cost: 78.93)

            snapshotWith patientAccountQuery
            snapshotWith patientHealthQuery
        }

        on(patient2) {
            apply new PatientCreated(name: 'Freddie Mercury')
            apply new PaymentMade(amount: 100.25)

            snapshotWith patientAccountQuery
            snapshotWith patientHealthQuery
        }

        currDate += 1
        merge(patient, patient2)
        patient
    }

    private Patient setupTinaFeyAndSarahPalin() {
        def patient = patientRepository.save(new Patient(uniqueId: '47'))
        def patient2 = patientRepository.save(new Patient(uniqueId: '48'))

        on(patient) {
            apply new PatientCreated(name: 'Tina Fey')
            apply new ProcedurePerformed(code: 'ANNUALPHYSICAL', cost: 170.00)
            apply new ProcedurePerformed(code: 'GLUCOSETEST', cost: 78.93)

            snapshotWith patientAccountQuery
            snapshotWith patientHealthQuery
        }

        on(patient2) {
            apply new PatientCreated(name: 'Sarah Palin')
            apply new PaymentMade(amount: 100.25)

            snapshotWith patientAccountQuery
            snapshotWith patientHealthQuery
        }

        currDate += 1
        def mergeEvent = merge(patient, patient2)
        on(mergeEvent.aggregate) {
            apply new PatientEventReverted(revertedEventId: mergeEvent.id)
        }
        on(patient2) {
            apply new PatientEventReverted(revertedEventId: mergeEvent.converse.id)
        }
        patient
    }

    /**
     *
     * @param self The aggregate to be deprecated
     * @param into The aggregate to survive
     * @return the deprecatedBy event
     */
    private PatientDeprecatedBy merge(Patient self, Patient into) {
        def e1 = new PatientDeprecatedBy(
            aggregate: self,
            deprecator: into,
            timestamp: currDate,
            position: patientEventRepository.countByAggregateId(self.id) + 1,)
        def e2 = new PatientDeprecates(
            aggregate: into,
            deprecated: self,
            timestamp: currDate,
            converse: e1,
            position: patientEventRepository.countByAggregateId(into.id) + 1,)
        e1.converse = e2
        patientEventRepository.save([e1, e2,])
        e2.converse
    }
}
