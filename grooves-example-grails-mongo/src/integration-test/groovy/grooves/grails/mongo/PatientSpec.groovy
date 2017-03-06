package grooves.grails.mongo

import grails.test.mixin.integration.Integration
import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient
import org.springframework.beans.factory.annotation.Value
import spock.lang.Shared
import spock.lang.Specification

@Integration
class PatientSpec extends Specification {

    @Shared
    @Value('${local.server.port}')
    Integer serverPort

    def "Patient List works"() {
        given:
        def rest = new RESTClient("http://localhost:${serverPort}/", ContentType.JSON)

        when:
        def resp = rest.get(path: '/patient.json')

        then:
        with(resp) {
            status == 200
            contentType == "application/json"
        }
        with(resp.data) {
            it.size() == 1
            it[0].id == 1
            it[0].uniqueId == '42'
        }
    }

    def "Patient Show works"() {
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

    def "Patient Health works"() {
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
            it.aggregate.id == 1
            it.aggregate.uniqueId == '42'
            it.name == 'John Smith'
            it.lastEvent == 6
            it.procedures.size() == 3
            it.procedures*.code == ['FLUSHOT', 'GLUCOSETEST', 'ANNUALPHYSICAL']
        }
    }
}
