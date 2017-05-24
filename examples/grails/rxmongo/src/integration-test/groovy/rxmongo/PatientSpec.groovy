package rxmongo

import grails.test.mixin.integration.Integration
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import org.springframework.beans.factory.annotation.Value
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static groovyx.net.http.ContentType.JSON

/**
 * Acceptance test that tests against Grails with Hibernate
 *
 * @author Rahul Somasunderam
 */
@Integration
@SuppressWarnings(['IgnoreRest', 'UnnecessaryGetter'])
class PatientSpec extends Specification {

    @Shared List<String> ids

    @Value('${local.server.port}')
    Integer serverPort

    RESTClient getRest() {
        def client = new RESTClient("http://localhost:${serverPort ?: 8080}/", JSON)
        if (!ids) {
            def resp = client.get(path: '/patient.json') as HttpResponseDecorator
            ids = resp.data*.id
        }
        client
    }

    /**
     * This needs to run first. It sets the ids field.
     */
    void "Patient List works"() {
        when:
        def resp = rest.get(path: '/patient.json') as HttpResponseDecorator

        then:
        with(resp) {
            status == 200
            contentType == 'application/json'
        }
        with(resp.data) { List<Map<String, Object>> it ->
            it[0].uniqueId == '42'
            it[1].uniqueId == '43'
        }
    }

    @Unroll
    void "Paul McCartney's balance is correct at version #version"() {
        getRest()
        def id = ids[3 - 1]

        given:
        def resp = rest.get(path: "/patient/account/${id}.json",
                query: [version: version,]) as HttpResponseDecorator

        expect:
        with(resp) {
            status == 200
            contentType == 'application/json'
        }
        with(resp.data) {
            it.balance == balance
            it.moneyMade == moneyMade
        }

        where:
        version | balance | moneyMade
        1       | 0.0     | 0.0
        2       | 170.0   | 0.0
        3       | 248.93  | 0.0
        4       | 148.68  | 100.25
        5       | 69.75   | 100.25
        6       | 39.75   | 130.25
        7       | 69.75   | 100.25
        8       | 9.75    | 160.25
        9       | -50.25  | 220.25
    }

    @Unroll
    void "#name - Show works"() {
        getRest()
        def theId = ids[id - 1]
        given:
        def resp = rest.get(path: "/patient/show/${theId}.json") as HttpResponseDecorator

        expect:
        with(resp) {
            status == 200
            contentType == 'application/json'
        }
        with(resp.data) {
            it.id == theId
            it.uniqueId == uniqueId
        }

        where:
        id | name          || uniqueId
        1  | 'John Lennon' || '42'
        2  | 'Ringo Starr' || '43'
    }

    @Unroll
    void "#name - Health works"() {
        getRest()
        def theId = ids[id - 1]
        given:
        def resp = rest.get(path: "/patient/health/${theId}.json") as HttpResponseDecorator

        expect:
        with(resp) {
            status == 200
            contentType == 'application/json'
        }
        with(resp.data) {
            it.aggregateId == theId
            it.name == name
            it.lastEventPosition == lastEventPos
            it.procedures.size() == codes.size()
            it.procedures*.code == codes
        }

        where:
        id | name             || lastEventPos | codes
        1  | 'John Lennon'    || 6            | ['FLUSHOT', 'GLUCOSETEST', 'ANNUALPHYSICAL',]
        2  | 'Ringo Starr'    || 6            | ['ANNUALPHYSICAL', 'GLUCOSETEST', 'FLUSHOT',]
        3  | 'Paul McCartney' || 9            | ['ANNUALPHYSICAL',]
    }

    @Unroll
    void "#name by Version #version - Health works"() {
        getRest()
        def theId = ids[id - 1]
        given:
        def resp = rest.get(path: "/patient/health/${theId}.json",
                query: [version: version,]) as HttpResponseDecorator

        expect:
        with(resp) {
            status == 200
            contentType == 'application/json'
        }
        with(resp.data) {
            it.aggregateId == theId
            it.name == name
            it.lastEventPosition == version
            it.procedures.size() == codes.size()
            it.procedures*.code == codes
        }

        where:
        id | version || name             | codes
        1  | 1       || 'John Lennon'    | []
        1  | 2       || 'John Lennon'    | ['FLUSHOT',]
        1  | 3       || 'John Lennon'    | ['FLUSHOT', 'GLUCOSETEST',]
        1  | 5       || 'John Lennon'    | ['FLUSHOT', 'GLUCOSETEST', 'ANNUALPHYSICAL',]
        2  | 1       || 'Ringo Starr'    | []
        2  | 2       || 'Ringo Starr'    | ['ANNUALPHYSICAL',]
        2  | 3       || 'Ringo Starr'    | ['ANNUALPHYSICAL', 'GLUCOSETEST',]
        2  | 5       || 'Ringo Starr'    | ['ANNUALPHYSICAL', 'GLUCOSETEST', 'FLUSHOT',]
        3  | 1       || 'Paul McCartney' | []
        3  | 2       || 'Paul McCartney' | ['ANNUALPHYSICAL',]
        3  | 3       || 'Paul McCartney' | ['ANNUALPHYSICAL', 'GLUCOSETEST',]
        3  | 5       || 'Paul McCartney' | ['ANNUALPHYSICAL',]
    }

    @Unroll
    def "#name by Date #date - Health works"() {
        getRest()
        def theId = ids[id - 1]
        given:
        def resp = rest.get(path: "/patient/health/${theId}.json",
                query: [date: date,]) as HttpResponseDecorator

        expect:
        with(resp) {
            status == 200
            contentType == 'application/json'
        }
        with(resp.data) {
            it.aggregateId == theId
            it.name == name
            it.lastEventPosition == lastEventPos
            it.procedures.size() == codes.size()
            it.procedures*.code == codes
        }

        where:
        id | date         || lastEventPos | name             | codes
        1  | '2016-01-03' || 2            | 'John Lennon'    | ['FLUSHOT',]
        2  | '2016-01-09' || 2            | 'Ringo Starr'    | ['ANNUALPHYSICAL',]
        3  | '2016-01-15' || 2            | 'Paul McCartney' | ['ANNUALPHYSICAL',]
        3  | '2016-01-16' || 3            | 'Paul McCartney' | ['ANNUALPHYSICAL', 'GLUCOSETEST',]
        3  | '2016-01-18' || 5            | 'Paul McCartney' | ['ANNUALPHYSICAL',]
    }

    @Unroll
    void "George Harrison's balance is correct at version #version"() {
        getRest()
        def theId4 = ids[4 - 1]
        def theAggregateId = ids[aggregateId - 1]
        given:
        def resp = rest.get(path: "/patient/account/${theId4}.json",
                query: [version: version,]) as HttpResponseDecorator

        expect:
        with(resp) {
            status == 200
            contentType == 'application/json'
        }
        with(resp.data) {
            it.balance == balance
            it.moneyMade == moneyMade
            deprecatedIds == null || it.deprecates*.id == [theId4]
            it.aggregateId == theAggregateId
        }

        where:
        version || balance | moneyMade | aggregateId | deprecatedIds
        1       || 0.0     | 0.0       | 4           | null
        2       || 170.0   | 0.0       | 4           | null
        3       || 248.93  | 0.0       | 4           | null
        4       || 148.68  | 100.25    | 5           | [4,]
    }

    @Unroll
    void "George Harrison MBE's balance is correct at version #version"() {
        getRest()
        def theId5 = ids[5 - 1]
        def theId4 = ids[4 - 1]
        given:
        def resp = rest.get(path: "/patient/account/${theId5}.json",
                query: [version: version,]) as HttpResponseDecorator

        expect:
        with(resp) {
            status == 200
            contentType == 'application/json'
        }
        with(resp.data) {
            it.balance == balance
            it.moneyMade == moneyMade
            it.aggregateId == theId5
            deprecatedIds == null || it.deprecates*.id == [theId4]
        }

        where:
        version || balance | moneyMade | aggregateId | deprecatedIds
        1       || 0.0     | 0.0       | 5           | null
        2       || -100.25 | 100.25    | 5           | null
        3       || 148.68  | 100.25    | 5           | [4,]
    }

    void "George Harrison and George Harrison MBE are merged"() {
        getRest()
        HttpResponseDecorator resp
        def theId5 = ids[5 - 1]
        def theId4 = ids[4 - 1]
        def theId4AsRange = [theId4]

        when:
        resp = rest.get(path: "/patient/account/${theId5}.json") as HttpResponseDecorator

        then:
        with(resp) {
            status == 200
            contentType == 'application/json'
        }
        with(resp.data) {
            it.aggregateId == theId5
            it.deprecates*.id == theId4AsRange
            it.balance == 148.68
            it.moneyMade == 100.25
            it.lastEventPosition == 3
            Date.parse('yyyy-MM-dd', it.lastEventTimestamp.toString()[0..10]).
                    format('yyyyMMdd') == '20160128'
        }

        when:
        resp = rest.get(path: "/patient/account/${theId4}.json") as HttpResponseDecorator

        then:
        with(resp) {
            status == 200
            contentType == 'application/json'
        }
        with(resp.data) {
            it.aggregateId == theId5
            it.deprecates*.id == theId4AsRange
            it.balance == 148.68
            it.moneyMade == 100.25
            it.lastEventPosition == 3
            Date.parse('yyyy-MM-dd', it.lastEventTimestamp.toString()[0..10]).
                    format('yyyyMMdd') == '20160128'
        }
    }

}
