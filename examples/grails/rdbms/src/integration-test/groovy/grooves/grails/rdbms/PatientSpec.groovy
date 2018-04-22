package grooves.grails.rdbms

import com.github.rahulsom.grooves.test.AbstractPatientSpec
import grails.test.mixin.integration.Integration
import grails.transaction.Transactional
import groovyx.net.http.RESTClient
import org.springframework.beans.factory.annotation.Value

import static groovyx.net.http.ContentType.JSON

/**
 * Acceptance test that tests against Grails with Hibernate
 *
 * @author Rahul Somasunderam
 */
@Integration
class PatientSpec extends AbstractPatientSpec {

    @Value('${local.server.port}')
    Integer serverPort

    @Override
    RESTClient getRest() {
        new RESTClient("http://localhost:${serverPort ?: 8080}/", JSON)
    }

    @Transactional
    void 'deprecations are correctly stored'() {
        expect:
        PatientDeprecatedBy.findAll().every { it.converse != null }
        PatientDeprecates.findAll().every { it.converse != null }
    }
}
