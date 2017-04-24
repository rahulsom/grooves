package grooves.example.grails.mongo

import com.github.rahulsom.genealogy.NameDbUsa
import com.github.rahulsom.grooves.api.EventsDsl
import grooves.grails.mongo.*

import java.util.function.Consumer

/**
 * Initializes Application with usable data
 */
@SuppressWarnings(['DuplicateNumberLiteral', 'DuplicateStringLiteral', 'InsecureRandom'])
class BootStrap {

    public static final int ONE_DAY = 24 * 60 * 60 * 1000
    public static final String START_DATE = '2016-01-01'

    def init = { servletContext ->
        def campbell = on(new Zipcode(uniqueId: '95008').save(flush: true, failOnError: true)) {
            apply new ZipcodeCreated(name: 'Campbell, California', timestamp: date(START_DATE))
        }
        def santanaRow = on(new Zipcode(uniqueId: '95128').save(flush: true, failOnError: true)) {
            apply new ZipcodeCreated(name: 'Santana Row, San Jose, California',
                    timestamp: date(START_DATE),)
        }
        setupJohnLennon()
        setupRingoStarr()
        setupPaulMcCartney()
        setupGeorgeHarrison()

        linkZipcodesAndPatients(campbell, santanaRow)
        linkZipcodesAndDoctors(campbell, santanaRow)
    }
    private void linkZipcodesAndDoctors(Zipcode campbell, Zipcode santanaRow) {
        def names = NameDbUsa.instance
        for (int i = 0; i < 10; i++) {
            def seed = i * 0.07 + 0.03
            def random = new Random((Long.MAX_VALUE * seed) as long)

            currDate = new Date(Date.parse('yyyy-MM-dd', START_DATE).time
                    + random.nextInt(ONE_DAY))

            def doctor = new Doctor(uniqueId: "A${i}").save(flush: true, failOnError: true)
            on(doctor) {
                apply new DoctorCreated(
                        name: "${names.getMaleName(seed)} ${names.getLastName(seed)}")
            }

            currDate = new Date(Date.parse('yyyy-MM-dd', START_DATE).time
                    + random.nextInt(ONE_DAY))
            def zipChanges = random.nextInt(4) + 1
            def zipcode = random.nextBoolean() ? campbell : santanaRow
            Zipcode lastZipcode = null
            zipChanges.times {
                currDate += random.nextInt(10) + 1
                on(doctor) {
                    if (lastZipcode) {
                        apply new DoctorRemovedFromZipcode(joinAggregate: lastZipcode,
                                timestamp: currDate,)
                    }
                    apply new DoctorAddedToZipcode(joinAggregate: zipcode, timestamp: currDate)
                }
                if (lastZipcode) {
                    on(lastZipcode) {
                        apply new ZipcodeLostDoctor(joinAggregate: doctor, timestamp: currDate)
                    }
                }
                on(zipcode) {
                    apply new ZipcodeGotDoctor(joinAggregate: doctor, timestamp: currDate)
                }
                lastZipcode = zipcode
                zipcode = zipcode == campbell ? santanaRow : campbell
            }
        }
    }
    private void linkZipcodesAndPatients(Zipcode campbell, Zipcode santanaRow) {
        def names = NameDbUsa.instance
        for (int i = 0; i < 10; i++) {
            def seed = i * 0.05 + 0.12
            def random = new Random((Long.MAX_VALUE * seed) as long)

            currDate = new Date(Date.parse('yyyy-MM-dd', START_DATE).time
                    + random.nextInt(ONE_DAY))

            def patient = new Patient(uniqueId: "I${i}").save(flush: true, failOnError: true)
            on(patient) {
                def sp = delegate
                apply new PatientCreated(
                        name: "${names.getFemaleName(seed)} ${names.getLastName(seed)}")
                def numberOfProcedures = random.nextInt(procedures.size())
                setupProcedures(numberOfProcedures, sp, random)
            }

            currDate = new Date(Date.parse('yyyy-MM-dd', START_DATE).time
                    + random.nextInt(ONE_DAY))
            def zipChanges = random.nextInt(4) + 1
            def zipcode = random.nextBoolean() ? campbell : santanaRow
            Zipcode lastZipcode = null
            zipChanges.times {
                currDate += random.nextInt(10) + 1
                on(patient) {
                    if (lastZipcode) {
                        apply new PatientRemovedFromZipcode(joinAggregate: lastZipcode,
                                timestamp: currDate,)
                    }
                    apply new PatientAddedToZipcode(joinAggregate: zipcode, timestamp: currDate)
                }
                if (lastZipcode) {
                    on(lastZipcode) {
                        apply new ZipcodeLostPatient(joinAggregate: patient, timestamp: currDate)
                    }
                }
                on(zipcode) {
                    apply new ZipcodeGotPatient(joinAggregate: patient, timestamp: currDate)
                }
                lastZipcode = zipcode
                zipcode = zipcode == campbell ? santanaRow : campbell
            }
        }
    }
    private void setupProcedures(int numberOfProcedures, EventsDsl.OnSpec sp, Random random) {
        for (int p = 0; p < numberOfProcedures; p++) {
            currDate += random.nextInt(5)
            def key = procedures.keySet().toList()[p]
            sp.apply new ProcedurePerformed(code: key, cost: procedures[key])
        }
    }

    def procedures = [
            FLUSHOT       : 32.40,
            ANNUALPHYSICAL: 170.00,
            GLUCOSETEST   : 78.93,
            LIPIDTEST     : 102.55,
            XRAY_WRIST    : 70.42,
            XRAY_BACK     : 104.40,
    ]

    Date date(String str) {
        Date.parse('yyyy-MM-dd', str)
    }

    private Patient setupJohnLennon() {
        def patient = new Patient(uniqueId: '42').save(flush: true, failOnError: true)

        on(patient) {
            apply new PatientCreated(name: 'John Lennon')
            apply new ProcedurePerformed(code: 'FLUSHOT', cost: 32.40)
            apply new ProcedurePerformed(code: 'GLUCOSETEST', cost: 78.93)
            apply new PaymentMade(amount: 100.25)

            snapshotWith new PatientAccountQuery()
            snapshotWith new PatientHealthQuery()

            apply new ProcedurePerformed(code: 'ANNUALPHYSICAL', cost: 170.00)
            apply new PaymentMade(amount: 180.00)

            snapshotWith new PatientAccountQuery()
            snapshotWith new PatientHealthQuery()
        }
    }

    private Patient setupRingoStarr() {
        def patient = new Patient(uniqueId: '43').save(flush: true, failOnError: true)

        on(patient) {
            apply new PatientCreated(name: 'Ringo Starr')
            apply new ProcedurePerformed(code: 'ANNUALPHYSICAL', cost: 170.00)
            apply new ProcedurePerformed(code: 'GLUCOSETEST', cost: 78.93)
            apply new PaymentMade(amount: 100.25)

            snapshotWith new PatientAccountQuery()
            snapshotWith new PatientHealthQuery()

            apply new ProcedurePerformed(code: 'FLUSHOT', cost: 32.40)
            apply new PaymentMade(amount: 180.00)

            snapshotWith new PatientAccountQuery()
            snapshotWith new PatientHealthQuery()
        }
    }

    private Patient setupPaulMcCartney() {
        def patient = new Patient(uniqueId: '44').save(flush: true, failOnError: true)

        on(patient) {
            apply new PatientCreated(name: 'Paul McCartney')
            apply new ProcedurePerformed(code: 'ANNUALPHYSICAL', cost: 170.00)
            def gluc = apply new ProcedurePerformed(code: 'GLUCOSETEST', cost: 78.93)
            apply new PaymentMade(amount: 100.25)
            apply new PatientEventReverted(revertedEventId: gluc.id)
            def pmt = apply new PaymentMade(amount: 30.00)

            snapshotWith new PatientAccountQuery()
            snapshotWith new PatientHealthQuery()

            apply new PatientEventReverted(revertedEventId: pmt.id)
            apply new PaymentMade(amount: 60.00)

            snapshotWith new PatientAccountQuery()
            snapshotWith new PatientHealthQuery()

            apply new PaymentMade(amount: 60.00)

            snapshotWith new PatientAccountQuery()
            snapshotWith new PatientHealthQuery()
        }

    }

    private Patient setupGeorgeHarrison() {
        def patient = new Patient(uniqueId: '45').save(flush: true, failOnError: true)
        def patient2 = new Patient(uniqueId: '46').save(flush: true, failOnError: true)

        on(patient) {
            apply new PatientCreated(name: 'George Harrison')
            apply new ProcedurePerformed(code: 'ANNUALPHYSICAL', cost: 170.00)
            apply new ProcedurePerformed(code: 'GLUCOSETEST', cost: 78.93)

            snapshotWith new PatientAccountQuery()
            snapshotWith new PatientHealthQuery()
        }

        on(patient2) {
            apply new PatientCreated(name:
                    'George Harrison, Member of the Most Excellent Order of the British Empire')
            apply new PaymentMade(amount: 100.25)

            snapshotWith new PatientAccountQuery()
            snapshotWith new PatientHealthQuery()
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
                position: PatientEvent.countByAggregate(self) + 1,)
        def e2 = new PatientDeprecates(aggregate: into, createdBy: 'anonymous', deprecated: self,
                timestamp: currDate, converse: e1,
                position: PatientEvent.countByAggregate(into) + 1,)
        e1.converse = e2
        e2.save(flush: true, failOnError: true)
        e2.converse
    }

    Date currDate = Date.parse('yyyy-MM-dd', START_DATE)

    Patient on(Patient patient, @DelegatesTo(EventsDsl.OnSpec) Closure closure) {
        def eventSaver = { it.save(flush: true, failOnError: true) } as Consumer
        def positionSupplier = { PatientEvent.countByAggregate(patient) + 1 }
        def userSupplier = { 'anonymous' }
        def dateSupplier = { currDate += 1; currDate }
        new EventsDsl<Patient, Long, PatientEvent>().on(
                patient, eventSaver, positionSupplier, userSupplier, dateSupplier, closure)
    }

    Zipcode on(Zipcode zipcode, @DelegatesTo(EventsDsl.OnSpec) Closure closure) {
        def eventSaver = { it.save(flush: true, failOnError: true) } as Consumer
        def positionSupplier = { ZipcodeEvent.countByAggregate(zipcode) + 1 }
        def userSupplier = { 'anonymous' }
        new EventsDsl<Zipcode, Long, ZipcodeEvent>().on(
                zipcode, eventSaver, positionSupplier, userSupplier, closure)
    }

    Doctor on(Doctor doctor, @DelegatesTo(EventsDsl.OnSpec) Closure closure) {
        def eventSaver = { it.save(flush: true, failOnError: true) } as Consumer
        def positionSupplier = { DoctorEvent.countByAggregate(doctor) + 1 }
        def userSupplier = { 'anonymous' }
        new EventsDsl<Doctor, Long, DoctorEvent>().on(
                doctor, eventSaver, positionSupplier, userSupplier, closure)
    }

    def destroy = {
    }
}
