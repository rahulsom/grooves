package com.github.rahulsom.grooves.test

import groovyx.net.http.RESTClient
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Base test to replicate across example projects
 */
abstract class AbstractPatientSpec extends Specification {

    abstract RESTClient getRest()

    void "Patient List works"() {
        when:
        def resp = rest.get(path: '/patient.json')

        then:
        with(resp) {
            status == 200
            contentType == "application/json"
        }
        with(resp.data) {
            it.size() == 2
            it[0].uniqueId == '42'
            it[1].uniqueId == '43'
        }
    }

    @Unroll
    void "#name - Show works"() {
        given:
        def resp = rest.get(path: "/patient/show/${id}.json".toString())

        expect:
        with(resp) {
            status == 200
            contentType == "application/json"
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
        def resp = rest.get(path: "/patient/health/${id}.json".toString())
        println resp.data

        expect:
        with(resp) {
            status == 200
            contentType == "application/json"
        }
        with(resp.data) {
            it.aggregateId == id
            it.name == name
            it.lastEvent == lastEvent
            it.procedures.size() == codes.size()
            it.procedures*.code == codes
        }

        where:
        id | name          || lastEvent | codes
        1  | 'John Lennon' || 6         | ['FLUSHOT', 'GLUCOSETEST', 'ANNUALPHYSICAL']
        2  | 'Ringo Starr' || 6         | ['ANNUALPHYSICAL', 'GLUCOSETEST', 'FLUSHOT']
    }

    @Unroll
    void "#name by Version #version - Health works"() {
        given:
        def resp = rest.get(path: "/patient/health/${id}.json".toString(), query: [version: version])
        println resp.data

        expect:
        with(resp) {
            status == 200
            contentType == "application/json"
        }
        with(resp.data) {
            it.aggregateId == id
            it.name == name
            it.lastEvent == version
            it.procedures.size() == codes.size()
            it.procedures*.code == codes
        }

        where:
        id | version || name          | codes
        1  | 1       || 'John Lennon' | []
        1  | 2       || 'John Lennon' | ['FLUSHOT']
        1  | 3       || 'John Lennon' | ['FLUSHOT', 'GLUCOSETEST']
        // 1  | 5       || 'John Lennon' | ['FLUSHOT', 'GLUCOSETEST', 'ANNUALPHYSICAL']
        2  | 1       || 'Ringo Starr' | []
        2  | 2       || 'Ringo Starr' | ['ANNUALPHYSICAL']
        2  | 3       || 'Ringo Starr' | ['ANNUALPHYSICAL', 'GLUCOSETEST']
        // 2  | 5       || 'Ringo Starr' | ['ANNUALPHYSICAL', 'GLUCOSETEST', 'FLUSHOT']
    }

    @Unroll
    def "#name by Date #date - Health works"() {
        given:
        def resp = rest.get(path: "/patient/health/${id}.json".toString(), query: [date: date])
        println resp.data

        expect:
        with(resp) {
            status == 200
            contentType == "application/json"
        }
        with(resp.data) {
            it.aggregateId == id
            it.name == name
            it.lastEvent == 2
            it.procedures.size() == codes.size()
            it.procedures*.code == codes
        }

        where:
        id | date         || lastEvent | name          | codes
        1  | '2016-01-03' || 2         | 'John Lennon' | ['FLUSHOT']
        2  | '2016-01-09' || 2         | 'Ringo Starr' | ['ANNUALPHYSICAL']
    }
}
