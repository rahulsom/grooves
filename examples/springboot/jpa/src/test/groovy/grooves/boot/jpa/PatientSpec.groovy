package grooves.boot.jpa

import com.github.rahulsom.grooves.test.AbstractPatientSpec
import grooves.boot.jpa.domain.PatientDeprecatedBy
import grooves.boot.jpa.domain.PatientDeprecates
import grooves.boot.jpa.repositories.PatientEventRepository
import groovyx.net.http.RESTClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.embedded.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Transactional
import spock.lang.Unroll

import static groovyx.net.http.ContentType.JSON
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Acceptance test that tests against Springboot with JPA
 *
 * @author Rahul Somasunderam
 */
@ContextConfiguration
@SpringBootTest(webEnvironment = RANDOM_PORT)
class PatientSpec extends AbstractPatientSpec {

    @LocalServerPort private int serverPort
    @Autowired PatientEventRepository patientEventRepository

    @Override
    RESTClient getRest() {
        new RESTClient("http://localhost:${serverPort ?: 8080}/", JSON)
    }

    @Unroll
    void "#location - current patients join works"() {
        given:
        def resp = rest.get(path: "/zipcode/patients/${id}".toString())

        expect:
        with(resp) {
            status == 200
            // contentType == 'application/json'
        }
        with(resp.data) {
            it.aggregateId == id
            it.joinedIds.toSorted() == patients.collect { it + 7 }.toSorted()
        }

        where:
        id | location      | version || patients                   | lastEventPosition
        1  | 'Campbell'    | null    || [1, 2, 3, 5, 6, 7, 8, 10,] | 33
        2  | 'Santana Row' | null    || [4, 9,]                    | 37
    }

    @Unroll
    @SuppressWarnings(['CyclomaticComplexity',])
    void "#location - version #version patients join works"() {
        given:
        def resp = rest.get(path: "/zipcode/patients/${id}", params: [version: version])

        expect:
        with(resp) {
            status == 200
            // contentType == 'application/json'
        }
        with(resp.data) {
            it.aggregateId == id
            it.joinedIds.toSorted() == patients.collect { it + 7 }.toSorted()
            it.lastEventPosition == version
        }

        where:
        id | location      | version || patients
        1  | 'Campbell'    | 1       || []
        1  | 'Campbell'    | 2       || [1,]
        1  | 'Campbell'    | 3       || [1, 2,]
        1  | 'Campbell'    | 4       || [1,]
        1  | 'Campbell'    | 5       || [1, 2,]
        1  | 'Campbell'    | 6       || [1, 2, 3,]
        1  | 'Campbell'    | 7       || [1, 2, 3, 5,]
        1  | 'Campbell'    | 8       || [1, 2, 3, 5, 6,]
        1  | 'Campbell'    | 9       || [1, 2, 3, 5, 6, 7,]
        1  | 'Campbell'    | 10      || [1, 2, 3, 5, 6,]
        1  | 'Campbell'    | 11      || [1, 2, 3, 5, 6, 7,]
        1  | 'Campbell'    | 12      || [1, 2, 3, 5, 6, 7, 8,]
        1  | 'Campbell'    | 13      || [1, 2, 3, 5, 6, 7, 8, 10,]
        1  | 'Campbell'    | 14      || [1, 2, 3, 5, 6, 7, 8,]
        1  | 'Campbell'    | 15      || [1, 2, 3, 5, 6, 7, 8, 10,]
        2  | 'Santana Row' | 1       || []
        2  | 'Santana Row' | 2       || [2,]
        2  | 'Santana Row' | 3       || []
        2  | 'Santana Row' | 4       || [4,]
        2  | 'Santana Row' | 5       || [4, 5,]
        2  | 'Santana Row' | 6       || [4,]
        2  | 'Santana Row' | 7       || [4, 7,]
        2  | 'Santana Row' | 8       || [4,]
        2  | 'Santana Row' | 9       || [4, 9,]
        2  | 'Santana Row' | 10      || [4, 9, 10,]
        2  | 'Santana Row' | 11      || [4, 9,]
        2  | 'Santana Row' | 12      || [4, 9, 10,]
        2  | 'Santana Row' | 13      || [4, 9,]
    }

    @Transactional
    void 'deprecations are correctly stored'() {
        expect:
        patientEventRepository.findAll().findAll { it instanceof PatientDeprecatedBy }.every { it.converse != null }
        patientEventRepository.findAll().findAll { it instanceof PatientDeprecates }.every { it.converse != null }
    }
}
