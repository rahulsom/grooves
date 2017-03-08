package com.github.rahulsom.grooves.test

import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient
import spock.lang.Specification

/**
 * Base test to replicate across example projects
 */
abstract class AbstractPatientSpec extends Specification {

    abstract Integer getServerPort()

    def "Patient List works"() {
        given:
        def rest = new RESTClient("http://localhost:${serverPort?:8080}/", ContentType.JSON)

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

    def "John Lennon - Show works"() {
        given:
        def rest = new RESTClient("http://localhost:${serverPort}/", ContentType.JSON)

        when:
        def resp = rest.get(path: '/patient/show/1.json')

        then:
        with(resp) {
            status == 200
            contentType == "application/json"
        }
        with(resp.data) {
            it.id == 1
            it.uniqueId == '42'
        }
    }

    def "Ringo Starr - Show works"() {
        given:
        def rest = new RESTClient("http://localhost:${serverPort}/", ContentType.JSON)

        when:
        def resp = rest.get(path: '/patient/show/2.json')

        then:
        with(resp) {
            status == 200
            contentType == "application/json"
        }
        with(resp.data) {
            it.id == 2
            it.uniqueId == '43'
        }
    }

    def "John Lennon - Health works"() {
        given:
        def rest = new RESTClient("http://localhost:${serverPort}/", ContentType.JSON)

        when:
        def resp = rest.get(path: '/patient/health/1.json')
        println resp.data

        then:
        with(resp) {
            status == 200
            contentType == "application/json"
        }
        with(resp.data) {
            it.aggregateId == 1
            it.name == 'John Lennon'
            it.lastEvent == 6
            it.procedures.size() == 3
            it.procedures*.code == ['FLUSHOT', 'GLUCOSETEST', 'ANNUALPHYSICAL']
        }
    }

    def "Ringo Starr - Health works"() {
        given:
        def rest = new RESTClient("http://localhost:${serverPort}/", ContentType.JSON)

        when:
        def resp = rest.get(path: '/patient/health/2.json')
        println resp.data

        then:
        with(resp) {
            status == 200
            contentType == "application/json"
        }
        with(resp.data) {
            it.aggregateId == 2
            it.name == 'Ringo Starr'
            it.lastEvent == 6
            it.procedures.size() == 3
            it.procedures*.code == ['ANNUALPHYSICAL', 'GLUCOSETEST', 'FLUSHOT']
        }
    }

    def "John Lennon v2 - Health works"() {
        given:
        def rest = new RESTClient("http://localhost:${serverPort}/", ContentType.JSON)

        when:
        def resp = rest.get(path: '/patient/health/1.json', query: [version: 2])
        println resp.data

        then:
        with(resp) {
            status == 200
            contentType == "application/json"
        }
        with(resp.data) {
            it.aggregateId == 1
            it.name == 'John Lennon'
            it.lastEvent == 2
            it.procedures.size() == 1
            it.procedures*.code == ['FLUSHOT']
        }
    }

    def "Ringo Starr v2 - Health works"() {
        given:
        def rest = new RESTClient("http://localhost:${serverPort}/", ContentType.JSON)

        when:
        def resp = rest.get(path: '/patient/health/2.json', query: [version: 2])
        println resp.data

        then:
        with(resp) {
            status == 200
            contentType == "application/json"
        }
        with(resp.data) {
            it.aggregateId == 2
            it.name == 'Ringo Starr'
            it.lastEvent == 2
            it.procedures.size() == 1
            it.procedures*.code == ['ANNUALPHYSICAL']
        }
    }
}
