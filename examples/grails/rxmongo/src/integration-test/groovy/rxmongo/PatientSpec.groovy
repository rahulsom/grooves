package rxmongo

import com.github.rahulsom.grooves.test.AbstractPatientSpec
import grails.testing.mixin.integration.Integration
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

    RESTClient getRest() {
        new RESTClient("http://localhost:${serverPort ?: 8080}/", JSON)
    }

}
