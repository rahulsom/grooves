package com.github.rahulsom.grooves.test

import groovy.transform.CompileDynamic
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.text.SimpleDateFormat

/**
 * Base test to replicate across example projects.
 *
 * @author Rahul Somasunderam
 */
@SuppressWarnings(['UnnecessaryGetter', 'GroovyAssignabilityCheck'])
@CompileDynamic
abstract class AbstractPatientSpec extends Specification {

    @SuppressWarnings(['Instanceof', 'MethodParameterTypeRequired', 'AbstractPatientSpec',])
    static Date getDate(def ts) {
        if (ts instanceof String) {
            Date.parse('yyyy-MM-dd', ts[0..10])
        } else if (ts instanceof Long) {
            new Date(ts)
        } else {
            null
        }
    }

    /**
     * Provides a preconfigured RESTClient that can be given a path to execute a HTTP query.
     *
     * @return Preconfigured RESTClient
     */
    abstract RESTClient getRest()

    @Shared private List<String> ids

    void setup() {
        if (!ids) {
            def resp = rest.get(path: 'patient') as HttpResponseDecorator
            ids = resp.data*.id
        }
    }

    void "Patient List works"() {
        when:
        def resp = rest.get(path: 'patient') as HttpResponseDecorator

        then:
        with(resp) {
            status == 200
            // contentType == 'application/json'
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
        def resp = rest.get(path: "patient/account/${id}",
                query: [version: version,]) as HttpResponseDecorator

        expect:
        with(resp) {
            status == 200
            // contentType == 'application/json'
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
        def resp = rest.get(path: "patient/show/${theId}") as HttpResponseDecorator

        expect:
        with(resp) {
            status == 200
            // contentType == 'application/json'
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
        def resp = rest.get(path: "patient/health/${theId}") as HttpResponseDecorator

        expect:
        with(resp) {
            status == 200
            // contentType == 'application/json'
        }
        with(resp.data) {
            it.aggregateId == theId || it.aggregate.id == theId
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
        def resp = rest.get(path: "patient/health/${theId}",
                query: [version: version,]) as HttpResponseDecorator

        expect:
        with(resp) {
            status == 200
            // contentType == 'application/json'
        }
        with(resp.data) {
            it.aggregateId == theId || it.aggregate.id == theId
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
        def resp = rest.get(path: "patient/health/${theId}",
                query: [date: "${date}T00:00:00.000Z",]) as HttpResponseDecorator

        expect:
        with(resp) {
            status == 200
            // contentType == 'application/json'
        }
        with(resp.data) {
            it.aggregateId == theId || it.aggregate.id == theId
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
    void "Farrokh Bulsara's balance is correct at version #version"() {
        getRest()
        def theId4 = ids[4 - 1]
        def theAggregateId = ids[aggregateId - 1]
        def theDeprecatedIds = deprecatedIds?.collect { ids[it - 1] } ?: null
        given:
        def resp = rest.get(path: "patient/account/${theId4}",
                query: [version: version,]) as HttpResponseDecorator

        expect:
        with(resp) {
            status == 200
            // contentType == 'application/json'
        }
        with(resp.data) {
            it.balance == balance
            it.moneyMade == moneyMade
            it.deprecatesIds == theDeprecatedIds || it.deprecates*.id == theDeprecatedIds
            it.aggregateId == theAggregateId || it.aggregate.id == theAggregateId
        }

        where:
        version || balance | moneyMade | aggregateId | deprecatedIds
        1       || 0.0     | 0.0       | 4           | null
        2       || 170.0   | 0.0       | 4           | null
        3       || 248.93  | 0.0       | 4           | null
        4       || 148.68  | 100.25    | 5           | [4,]
    }

    @Unroll
    void "Freddie Mercury's balance is correct at version #version"() {
        getRest()
        def theId5 = ids[5 - 1]
        def theId4 = ids[4 - 1]
        given:
        def resp = rest.get(path: "patient/account/${theId5}",
                query: [version: version,]) as HttpResponseDecorator

        expect:
        with(resp) {
            status == 200
            // contentType == 'application/json'
        }
        with(resp.data) {
            it.balance == balance
            it.moneyMade == moneyMade
            it.aggregateId == theId5 || it.aggregate.id == theId5
            deprecatedIds == null || it.deprecates*.id == [theId4] || it.deprecatesIds == [theId4]
        }

        where:
        version || balance | moneyMade | aggregateId | deprecatedIds
        1       || 0.0     | 0.0       | 5           | null
        2       || -100.25 | 100.25    | 5           | null
        3       || 148.68  | 100.25    | 5           | [4,]
    }

    void "Farrokh Bulsara and Freddie Mercury are merged"() {
        getRest()
        HttpResponseDecorator resp
        def theId5 = ids[5 - 1]
        def theId4 = ids[4 - 1]
        def theId4AsRange = [theId4]

        when:
        resp = rest.get(path: "patient/account/${theId5}") as HttpResponseDecorator

        then:
        with(resp) {
            status == 200
            // contentType == 'application/json'
        }
        with(resp.data) {
            it.aggregateId == theId5 || it.aggregate.id == theId5
            it.deprecatesIds == theId4AsRange || it.deprecates*.id == theId4AsRange
            it.balance == 148.68
            it.moneyMade == 100.25
            it.lastEventPosition == 3

            new SimpleDateFormat('yyyyMMdd', Locale.US).format(getDate(it.lastEventTimestamp)) == '20160128'
        }

        when:
        resp = rest.get(path: "patient/account/${theId4}") as HttpResponseDecorator

        then:
        with(resp) {
            status == 200
            // contentType == 'application/json'
        }
        with(resp.data) {
            it.aggregateId == theId5 || it.aggregate.id == theId5
            it.deprecatesIds == theId4AsRange || it.deprecates*.id == theId4AsRange
            it.balance == 148.68
            it.moneyMade == 100.25
            it.lastEventPosition == 3

            new SimpleDateFormat('yyyyMMdd', Locale.US).format(getDate(it.lastEventTimestamp)) == '20160128'
        }
    }

    @Unroll
    void "Reverting a merge works - version #version of #id is #name"() {
        getRest()

        given:
        def resp = rest.get(path: "patient/account/${ids[id - 1]}",
                query: [version: version,]) as HttpResponseDecorator

        expect:
        with(resp) {
            status == 200
            // contentType == 'application/json'
        }
        with(resp.data) {
            it.name == name
            it.balance == balance
        }

        where:
        id | version || name          | balance
        6  | 1       || 'Tina Fey'    | 0.0
        6  | 2       || 'Tina Fey'    | 170.00
        6  | 3       || 'Tina Fey'    | 248.93
        6  | 4       || 'Sarah Palin' | 148.68 // Redirect to correct version to pass
        6  | 5       || 'Tina Fey'    | 248.93
        7  | 1       || 'Sarah Palin' | 0.0
        7  | 2       || 'Sarah Palin' | -100.25
        7  | 3       || 'Sarah Palin' | 148.68
        7  | 4       || 'Sarah Palin' | -100.25
    }

    @Unroll
    void "Reverting a merge works - on date #date of #id is #name"() {
        getRest()

        given:
        def resp = rest.get(path: "patient/account/${ids[id - 1]}",
                query: [date: "${date}T00:00:00.000Z",]) as HttpResponseDecorator

        expect:
        with(resp) {
            status == 200
            // // contentType == 'application/json'
        }
        with(resp.data) {
            it.name == name
            it.balance == balance
        }

        where:
        id | date         || name          | balance
        6  | '2016-01-29' || 'Tina Fey'    | 0.0
        6  | '2016-01-30' || 'Tina Fey'    | 170.00
        6  | '2016-01-31' || 'Tina Fey'    | 248.93
        6  | '2016-02-03' || 'Sarah Palin' | 148.68
        6  | '2016-02-05' || 'Tina Fey'    | 248.93
        7  | '2016-02-01' || 'Sarah Palin' | 0.0
        7  | '2016-02-02' || 'Sarah Palin' | -100.25
        7  | '2016-02-03' || 'Sarah Palin' | 148.68
        7  | '2016-02-05' || 'Sarah Palin' | -100.25
    }

}
