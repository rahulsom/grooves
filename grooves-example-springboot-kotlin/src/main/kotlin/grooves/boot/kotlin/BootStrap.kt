package grooves.boot.kotlin

import com.github.rahulsom.grooves.api.snapshots.Snapshot
import com.github.rahulsom.grooves.queries.QuerySupport
import com.github.rahulsom.grooves.test.OnSpec
import grooves.boot.kotlin.domain.Patient
import grooves.boot.kotlin.domain.PatientEvent
import grooves.boot.kotlin.domain.PatientEvent.Applicable.Created
import grooves.boot.kotlin.domain.PatientEvent.Applicable.PaymentMade
import grooves.boot.kotlin.domain.PatientEvent.Applicable.ProcedurePerformed
import grooves.boot.kotlin.queries.PatientAccountQuery
import grooves.boot.kotlin.queries.PatientHealthQuery
import grooves.boot.kotlin.repositories.PatientAccountRepository
import grooves.boot.kotlin.repositories.PatientBlockingRepository
import grooves.boot.kotlin.repositories.PatientEventBlockingRepository
import grooves.boot.kotlin.repositories.PatientHealthRepository
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone

@Component
class BootStrap constructor(
    @param:Autowired val patientRepository: PatientBlockingRepository,
    @param:Autowired val patientEventRepository: PatientEventBlockingRepository,
    @param:Autowired val patientAccountQuery: PatientAccountQuery,
    @param:Autowired val patientHealthQuery: PatientHealthQuery,
    @param:Autowired val patientAccountRepository: PatientAccountRepository,
    @param:Autowired val patientHealthRepository: PatientHealthRepository,
) {
    @PostConstruct
    fun init() {
        setupJohnLennon()
        setupRingoStarr()
        setupPaulMcCartney()
        setupFreddieMercury()
        setupTinaFeyAndSarahPalin()
    }

    fun setupJohnLennon() {
        on(patientRepository.save(Patient("42"))) {
            it.apply(Created("John Lennon"))
            it.apply(ProcedurePerformed("FLUSHOT", BigDecimal("32.40")))
            it.apply(ProcedurePerformed("GLUCOSETEST", BigDecimal("78.93")))
            it.apply(PaymentMade(BigDecimal("100.25")))

            snapshotWith(it, patientAccountQuery, patientAccountRepository)
            snapshotWith(it, patientHealthQuery, patientHealthRepository)

            it.apply(ProcedurePerformed("ANNUALPHYSICAL", BigDecimal("170.00")))
            it.apply(PaymentMade(BigDecimal("180.00")))

            snapshotWith(it, patientAccountQuery, patientAccountRepository)
            snapshotWith(it, patientHealthQuery, patientHealthRepository)
        }
    }

    fun setupRingoStarr() {
        on(patientRepository.save(Patient("43"))) {
            it.apply(Created("Ringo Starr"))
            it.apply(ProcedurePerformed("ANNUALPHYSICAL", BigDecimal("170.00")))
            it.apply(ProcedurePerformed("GLUCOSETEST", BigDecimal("78.93")))
            it.apply(PaymentMade(BigDecimal("100.25")))

            snapshotWith(it, patientAccountQuery, patientAccountRepository)
            snapshotWith(it, patientHealthQuery, patientHealthRepository)

            it.apply(ProcedurePerformed("FLUSHOT", BigDecimal("32.40")))
            it.apply(PaymentMade(BigDecimal("180.00")))

            snapshotWith(it, patientAccountQuery, patientAccountRepository)
            snapshotWith(it, patientHealthQuery, patientHealthRepository)
        }
    }

    fun setupPaulMcCartney() {
        on(patientRepository.save(Patient("44"))) {
            it.apply(Created("Paul McCartney"))
            it.apply(ProcedurePerformed("ANNUALPHYSICAL", BigDecimal("170.00")))

            val gluc = it.apply(ProcedurePerformed("GLUCOSETEST", BigDecimal("78.93")))

            it.apply(PaymentMade(BigDecimal("100.25")))
            it.apply(PatientEvent.Reverted(gluc.id!!))

            val pmt = it.apply(PaymentMade(BigDecimal("30.00")))

            snapshotWith(it, patientAccountQuery, patientAccountRepository)
            snapshotWith(it, patientHealthQuery, patientHealthRepository)

            it.apply(PatientEvent.Reverted(pmt.id!!))
            it.apply(PaymentMade(BigDecimal("60.00")))

            snapshotWith(it, patientAccountQuery, patientAccountRepository)
            snapshotWith(it, patientHealthQuery, patientHealthRepository)

            it.apply(PaymentMade(BigDecimal("60.00")))

            snapshotWith(it, patientAccountQuery, patientAccountRepository)
            snapshotWith(it, patientHealthQuery, patientHealthRepository)
        }
    }

    fun setupFreddieMercury() {
        val patient = patientRepository.save(Patient("45"))
        on(patient) {
            it.apply(Created("Farrokh Bulsara"))
            it.apply(ProcedurePerformed("ANNUALPHYSICAL", BigDecimal("170.00")))
            it.apply(ProcedurePerformed("GLUCOSE", BigDecimal("78.93")))

            snapshotWith(it, patientAccountQuery, patientAccountRepository)
            snapshotWith(it, patientHealthQuery, patientHealthRepository)
        }

        val patient2 = patientRepository.save(Patient("46"))
        on(patient2) {
            it.apply(Created("Freddie Mercury"))
            it.apply(PaymentMade(BigDecimal("100.25")))

            snapshotWith(it, patientAccountQuery, patientAccountRepository)
            snapshotWith(it, patientHealthQuery, patientHealthRepository)
        }

        currDate.add(Calendar.DATE, 1)
        merge(patient, patient2)
    }

    fun setupTinaFeyAndSarahPalin() {
        val patient = patientRepository.save(Patient("47"))
        on(patient) {
            it.apply(Created("Tina Fey"))
            it.apply(ProcedurePerformed("ANNUALPHYSICAL", BigDecimal("170.00")))
            it.apply(ProcedurePerformed("GLUCOSE", BigDecimal("78.93")))

            snapshotWith(it, patientAccountQuery, patientAccountRepository)
            snapshotWith(it, patientHealthQuery, patientHealthRepository)
        }

        val patient2 = patientRepository.save(Patient("48"))
        on(patient2) {
            it.apply(Created("Sarah Palin"))
            it.apply(PaymentMade(BigDecimal("100.25")))

            snapshotWith(it, patientAccountQuery, patientAccountRepository)
            snapshotWith(it, patientHealthQuery, patientHealthRepository)
        }

        currDate.add(Calendar.DATE, 1)
        val mergeEvent = merge(patient, patient2)

        on(patient) {
            it.apply(PatientEvent.Reverted(mergeEvent.id!!))
        }
        on(patient2) {
            it.apply(PatientEvent.Reverted(mergeEvent.converseId))
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun <
        SnapshotT : Snapshot<Patient, String, String, PatientEvent>,
        QueryT : QuerySupport<Patient, String, PatientEvent, String, SnapshotT>,
    > snapshotWith(
        it: OnSpec<
            Patient,
            String,
            PatientEvent,
            String,
            out Snapshot<Patient, String, String, PatientEvent>,
        >,
        query: QueryT,
        repository: ReactiveCrudRepository<SnapshotT, String>,
    ) = null
//            query.computeSnapshot(it.aggregate, Long.MAX_VALUE)
//                    .flatMap { repository.save(it).toObservable() }
//                    .toBlocking()
//                    .subscribe()

    /**
     *
     * @param self The aggregate to be deprecated
     * @param into The aggregate to survive
     * @return
     */
    private fun merge(
        self: Patient,
        into: Patient,
    ): PatientEvent.PatientDeprecatedBy {
        val e2 =
            patientEventRepository.save(
                PatientEvent.PatientDeprecates(self).also {
                    it.aggregate = into
                    it.timestamp = currDate.time
                    it.position = countEvents(into)
                },
            )

        val e1 =
            patientEventRepository.save(
                PatientEvent.PatientDeprecatedBy(into, e2.id!!).also {
                    it.aggregate = self
                    it.timestamp = currDate.time
                    it.position = countEvents(self)
                },
            )

        e2.converseId = e1.id

        patientEventRepository.save(e2)

        return e1
    }

    val currDate =
        Calendar
            .getInstance(TimeZone.getTimeZone("UTC"))
            .also {
                it.time =
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                        .parse("2016-01-01T00:00:00.000Z")
            }

    fun on(
        patient: Patient,
        closure: (
            OnSpec<Patient, String, PatientEvent, String, out Snapshot<Patient, String, String, PatientEvent>>,
        ) -> Unit,
    ): Patient {
        val eventSaver: (Any) -> Unit = {
            when (it) {
                is PatientEvent -> patientEventRepository.save(it)
            }
        }
        val positionSupplier = { countEvents(patient) }
        val timestampSupplier = {
            currDate.add(Calendar.DATE, 1)
            currDate.time
        }
        return com.github.rahulsom.grooves.test
            .EventsDsl<Patient, String, PatientEvent>()
            .on<String, Snapshot<Patient, String, String, PatientEvent>>(
                patient,
                eventSaver,
                positionSupplier,
                timestampSupplier,
                closure,
            )
    }

    private fun countEvents(patient: Patient) =
        patientEventRepository
            .findAllByAggregateIdIn(listOf(patient.id!!))
            .count() + 1L
}