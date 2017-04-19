package grooves.grails.mongo

import com.github.rahulsom.grooves.test.AbstractPatientSpec
import grails.test.mixin.integration.Integration
import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator
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
            it.joinedIds == patients.collect { it + 5 }
            it.lastEventPosition == lastEventPosition
        }

        where:
        id | location      | version || patients                   | lastEventPosition
        1  | 'Campbell'    | null    || [1, 2, 3, 5, 6, 7, 8, 10,] | 33
        2  | 'Santana Row' | null    || [4, 9,]                    | 37
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

    @Unroll
    void "George Harrison's balance is correct at version #version"() {
        given:
        def resp = rest.get(path: "/patient/account/4.json".toString(), params: [version: version])

        expect:
        with(resp) {
            status == 200
            contentType == "application/json"
        }
        with(resp.data) {
            it.balance == balance
            it.moneyMade == moneyMade
            it.deprecatesIds == deprecatedIds
            it.aggregateId == aggregateId
        }

        where:
        version || balance | moneyMade | aggregateId | deprecatedIds
        1       || 0.0     | 0.0       | 4           | null
        2       || 170.0   | 0.0       | 4           | null
        3       || 248.93  | 0.0       | 4           | null
        4       || 148.68  | 100.25    | 5           | [4]
    }

    @Unroll
    void "George Harrison MBE's balance is correct at version #version"() {
        given:
        def resp = rest.get(path: "/patient/account/5.json".toString(), params: [version: version])

        expect:
        with(resp) {
            status == 200
            contentType == "application/json"
        }
        with(resp.data) {
            it.balance == balance
            it.moneyMade == moneyMade
        }

        where:
        version || balance | moneyMade | aggregateId | deprecatedIds
        1       || 0.0     | 0.0       | 5           | null
        2       || -100.25 | 100.25    | 5           | null
        3       || 148.68  | 100.25    | 5           | [4]
    }

    void "George Harrison and George Harrison MBE are merged"() {
        given:
        HttpResponseDecorator resp = null

        when:
        resp = rest.get(path: "/patient/account/5.json".toString())

        then:
        with(resp) {
            status == 200
            contentType == "application/json"
        }
        with(resp.data) {
            it.aggregateId == 5
            it.deprecatesIds == [4]
            it.balance == 148.68
            it.moneyMade == 100.25
            it.lastEventPosition == 3
            Date.parse("yyyy-MM-dd", it.lastEventTimestamp.toString()[0..10]).format('yyyyMMdd') == '20160128'
        }

        when:
        resp = rest.get(path: "/patient/account/4.json".toString())

        then:
        with(resp) {
            status == 200
            contentType == "application/json"
        }
        with(resp.data) {
            it.aggregateId == 5
            it.deprecatesIds == [4]
            it.balance == 148.68
            it.moneyMade == 100.25
            it.lastEventPosition == 3
            Date.parse("yyyy-MM-dd", it.lastEventTimestamp.toString()[0..10]).format('yyyyMMdd') == '20160128'
        }

    }

}
