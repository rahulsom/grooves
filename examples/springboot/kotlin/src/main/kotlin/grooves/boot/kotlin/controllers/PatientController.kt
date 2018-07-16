package grooves.boot.kotlin.controllers

import grooves.boot.kotlin.queries.PatientAccountQuery
import grooves.boot.kotlin.queries.PatientHealthQuery
import grooves.boot.kotlin.repositories.PatientEventRepository
import grooves.boot.kotlin.repositories.PatientRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.Calendar
import java.util.Calendar.HOUR

@RestController
class PatientController constructor(
    @Autowired val patientRepository: PatientRepository,
    @Autowired val patientAccountQuery: PatientAccountQuery,
    @Autowired val patientHealthQuery: PatientHealthQuery,
    @Autowired val patientEventRepository: PatientEventRepository
) {

    @GetMapping("/patient")
    fun list() =
        patientRepository.findAllByOrderByUniqueIdAsc()

    @GetMapping("/patientEvent")
    fun events() =
        patientEventRepository.findAll()

    @GetMapping("/patient/show/{id}")
    fun show(@PathVariable id: String) =
        patientRepository.findById(id)

    @GetMapping("/patient/event/{id}")
    fun patientEvents(@PathVariable id: String) =
        patientEventRepository.findAllByAggregateIdIn(listOf(id))

    @GetMapping("/patient/account/{id}")
    fun account(
        @PathVariable id: String,
        @RequestParam(required = false) version: Long?,
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
        @RequestParam(required = false) date: Instant?
    ) =
        patientRepository.findById(id)
            .flatMap { patient ->
                Mono.from(version?.let { patientAccountQuery.computeSnapshot(patient, it) }
                        ?: date?.let {
                            patientAccountQuery.computeSnapshot(
                                patient, extractDate(it)
                            )
                        } ?: patientAccountQuery.computeSnapshot(patient, Long.MAX_VALUE))
            }

    private fun extractDate(instant: Instant) =
        Calendar.getInstance().let {
            val millisecondsSinceEpoch = instant.toEpochMilli()
            it.timeInMillis = millisecondsSinceEpoch
            /*
             * TODO: This appears to be a timezone bug in SpringBoot + Mongo.
             * Verify the problem and fix the code.
             */
            it.add(HOUR, 12)
            it.time
        }

    @GetMapping("/patient/health/{id}")
    fun health(
        @PathVariable id: String,
        @RequestParam(required = false) version: Long?,
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
        @RequestParam(required = false) date: Instant?
    ) =
        patientRepository.findById(id)
            .flatMap { patient ->
                Mono.from(version?.let { patientHealthQuery.computeSnapshot(patient, version) }
                        ?: date?.let {
                            patientHealthQuery.computeSnapshot(
                                patient, extractDate(it)
                            )
                        } ?: patientHealthQuery.computeSnapshot(patient, Long.MAX_VALUE))
            }
}