package grooves.grails.mongo

import com.github.rahulsom.grooves.test.AbstractPatientSpec
import grails.test.mixin.integration.Integration
import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient
import org.springframework.beans.factory.annotation.Value
import spock.lang.IgnoreRest
import spock.lang.Unroll

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
            contentType == "application/json"
        }
        with(resp.data) {
            it.aggregateId == id
            it.joinedIds == patients
            it.lastEventPosition == lastEventPosition
        }

        where:
        id | location      | version || patients                   | lastEventPosition
        1  | 'Campbell'    | null    || [3, 4, 5, 7, 8, 9, 10, 12] | 33
        2  | 'Santana Row' | null    || [6, 11]                    | 37
    }

    @Unroll
    void "#location - version #version patients join works"() {
        given:
        def resp = rest.get(path: "/zipcode/patients/${id}.json".toString(), params: [version: version])

        expect:
        with(resp) {
            status == 200
            contentType == "application/json"
        }
        with(resp.data) {
            it.aggregateId == id
            it.joinedIds == patients
            it.lastEventPosition == version
        }

        where:
        id | location      | version || patients
        1  | 'Campbell'    | 1       || []
        1  | 'Campbell'    | 2       || [3]
        1  | 'Campbell'    | 3       || [3, 4]
        1  | 'Campbell'    | 4       || [3]
        1  | 'Campbell'    | 5       || [3, 4]
        1  | 'Campbell'    | 6       || [3, 4, 5]
        1  | 'Campbell'    | 7       || [3, 4, 5, 7]
        1  | 'Campbell'    | 8       || [3, 4, 5, 7, 8]
        1  | 'Campbell'    | 9       || [3, 4, 5, 7, 8, 9]
        1  | 'Campbell'    | 10      || [3, 4, 5, 7, 8]
        1  | 'Campbell'    | 11      || [3, 4, 5, 7, 8, 9]
        1  | 'Campbell'    | 12      || [3, 4, 5, 7, 8, 9, 10]
        1  | 'Campbell'    | 13      || [3, 4, 5, 7, 8, 9, 10, 12]
        1  | 'Campbell'    | 14      || [3, 4, 5, 7, 8, 9, 10]
        1  | 'Campbell'    | 15      || [3, 4, 5, 7, 8, 9, 10, 12]
        2  | 'Santana Row' | 1       || []
        2  | 'Santana Row' | 2       || [4]
        2  | 'Santana Row' | 3       || []
        2  | 'Santana Row' | 4       || [6]
        2  | 'Santana Row' | 5       || [6, 7]
        2  | 'Santana Row' | 6       || [6]
        2  | 'Santana Row' | 7       || [6, 9]
        2  | 'Santana Row' | 8       || [6]
        2  | 'Santana Row' | 9       || [6, 11]
        2  | 'Santana Row' | 10      || [6, 11, 12]
        2  | 'Santana Row' | 11      || [6, 11]
        2  | 'Santana Row' | 12      || [6, 11, 12]
        2  | 'Santana Row' | 13      || [6, 11]
    }

}
