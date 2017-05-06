package com.github.rahulsom.grooves.test

import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Base test to replicate across example projects.
 *
 * @author Rahul Somasunderam
 */
abstract class AbstractPatientSpec extends Specification {

    /**
     * Provides a preconfigured {@link RESTClient} that can be given a path to execute a HTTP query.
     *
     * @return Preconfigured RESTClient
     */
    abstract RESTClient getRest()

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
        given:
        def resp = rest.get(path: '/patient/account/3.json',
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
        given:
        def resp = rest.get(path: "/patient/show/${id}.json") as HttpResponseDecorator

        expect:
        with(resp) {
            status == 200
            contentType == 'application/json'
        }
        with(resp.data) {
            it.id == id
            it.uniqueId == uniqueId
        }

        where:
        id | name          || uniqueId
        1  | 'John Lennon' || '42'
        2  | 'Ringo Starr' || '43'
    }

    @Unroll
    void "#name - Health works"() {
        given:
        def resp = rest.get(path: "/patient/health/${id}.json") as HttpResponseDecorator

        expect:
        with(resp) {
            status == 200
            contentType == 'application/json'
        }
        with(resp.data) {
            it.aggregateId == id || it.aggregate.id == id
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
        given:
        def resp = rest.get(path: "/patient/health/${id}.json",
                query: [version: version,]) as HttpResponseDecorator

        expect:
        with(resp) {
            status == 200
            contentType == 'application/json'
        }
        with(resp.data) {
            it.aggregateId == id || it.aggregate.id == id
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
        given:
        def resp = rest.get(path: "/patient/health/${id}.json",
                query: [date: date,]) as HttpResponseDecorator

        expect:
        with(resp) {
            status == 200
            contentType == 'application/json'
        }
        with(resp.data) {
            it.aggregateId == id || it.aggregate.id == id
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
        given:
        def resp = rest.get(path: '/patient/account/4.json',
                query: [version: version,]) as HttpResponseDecorator

        expect:
        with(resp) {
            status == 200
            contentType == 'application/json'
        }
        with(resp.data) {
            it.balance == balance
            it.moneyMade == moneyMade
            it.deprecatesIds == deprecatedIds || it.deprecates*.id == deprecatedIds
            it.aggregateId == aggregateId || it.aggregate.id == aggregateId
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
        given:
        def resp = rest.get(path: '/patient/account/5.json',
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
        version || balance | moneyMade | aggregateId | deprecatedIds
        1       || 0.0     | 0.0       | 5           | null
        2       || -100.25 | 100.25    | 5           | null
        3       || 148.68  | 100.25    | 5           | [4,]
    }

    void "George Harrison and George Harrison MBE are merged"() {
        given:
        HttpResponseDecorator resp = null

        when:
        resp = rest.get(path: '/patient/account/5.json') as HttpResponseDecorator

        then:
        with(resp) {
            status == 200
            contentType == 'application/json'
        }
        with(resp.data) {
            it.aggregateId == 5 || it.aggregate.id == 5
            it.deprecatesIds == [4,] || it.deprecates*.id == [4,]
            it.balance == 148.68
            it.moneyMade == 100.25
            it.lastEventPosition == 3
            Date.parse('yyyy-MM-dd', it.lastEventTimestamp.toString()[0..10]).
                    format('yyyyMMdd') == '20160128'
        }

        when:
        resp = rest.get(path: '/patient/account/4.json') as HttpResponseDecorator

        then:
        with(resp) {
            status == 200
            contentType == 'application/json'
        }
        with(resp.data) {
            it.aggregateId == 5 || it.aggregate.id == 5
            it.deprecatesIds == [4,] || it.deprecates*.id == [4,]
            it.balance == 148.68
            it.moneyMade == 100.25
            it.lastEventPosition == 3
            Date.parse('yyyy-MM-dd', it.lastEventTimestamp.toString()[0..10]).
                    format('yyyyMMdd') == '20160128'
        }
    }
}
