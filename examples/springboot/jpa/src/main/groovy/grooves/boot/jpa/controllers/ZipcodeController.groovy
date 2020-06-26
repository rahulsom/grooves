package grooves.boot.jpa.controllers

import grooves.boot.jpa.domain.Zipcode
import grooves.boot.jpa.domain.ZipcodePatients
import grooves.boot.jpa.queries.ZipcodePatientsQuery
import grooves.boot.jpa.repositories.ZipcodeRepository
import groovy.util.logging.Slf4j
import io.reactivex.Flowable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@SuppressWarnings(['ThrowRuntimeException'])
@Slf4j
class ZipcodeController {
    @Autowired ZipcodeRepository zipcodeRepository
    @Autowired ZipcodePatientsQuery zipcodePatientsQuery

    @GetMapping('/zipcode')
    List<Zipcode> list() {
        zipcodeRepository.findAll()
    }

    @GetMapping('/zipcode/show/{id}')
    Zipcode show(@PathVariable Long id) {
        zipcodeRepository.getOne(id)
    }

    @GetMapping('/zipcode/patients/{id}')
    ResponseEntity<ZipcodePatients.Representation> patients(
        @PathVariable Long id,
        @RequestParam(required = false) Long version,
        @RequestParam(required = false) @DateTimeFormat(pattern = 'yyyy-MM-dd') Date date) {
        def zipcode = zipcodeRepository.getOne(id)

        def computation = version ?
            zipcodePatientsQuery.computeSnapshot(zipcode, version) :
            date ?
                zipcodePatientsQuery.computeSnapshot(zipcode, date) :
                zipcodePatientsQuery.computeSnapshot(zipcode, Long.MAX_VALUE)

        def resp = Flowable.fromPublisher(computation).blockingFirst()

        if (!resp) {
            throw new RuntimeException('Could not compute health snapshot')
        }
        ResponseEntity.ok(resp.toJson())
    }
}
