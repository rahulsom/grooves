package grooves.grails.mongo

import com.github.rahulsom.grooves.test.AbstractPatientSpec
import grails.test.mixin.integration.Integration
import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient
import org.springframework.beans.factory.annotation.Value
import spock.lang.Unroll

/**
 * Acceptance test that tests against Grails with Hibernate and Mongo
 *
 * @author Rahul Somasunderam
 */
@Integration
class PatientSpec extends AbstractPatientSpec {

    @Value('${local.server.port}')
    Integer serverPort

    @Override
    RESTClient getRest() {
        new RESTClient("http://localhost:${serverPort ?: 8080}/", ContentType.JSON)
    }

    @Unroll
    void "#location - current patients join works"() {
        given:
        def resp = rest.get(path: "/zipcode/patients/${id}.json".toString())

        expect:
        with(resp) {
            status == 200
            contentType == 'application/json'
        }
        with(resp.data) {
            it.aggregateId == id
            it.joinedIds.toSet() == patients.collect { it + 5 }.toSet()
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
        def resp = rest.get(path: "/zipcode/patients/${id}.json", params: [version: version])

        expect:
        with(resp) {
            status == 200
            contentType == 'application/json'
        }
        with(resp.data) {
            it.aggregateId == id
            it.joinedIds == patients.collect { it + 5 }
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

}
