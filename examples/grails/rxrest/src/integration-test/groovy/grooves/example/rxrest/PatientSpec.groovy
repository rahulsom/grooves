package grooves.example.rxrest

import com.github.rahulsom.grooves.test.AbstractPatientSpec
import grails.test.mixin.integration.Integration
import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import org.springframework.beans.factory.annotation.Value

/**
 * Acceptance test that tests against Grails with Hibernate
 *
 * @author Rahul Somasunderam
 */
@Integration
class PatientSpec extends AbstractPatientSpec {

    void setupSpec() {
        for (int i = 0; i < 10; i ++) {
            try {
                sleep 5000
                rest.get path: '/patient.json'
                rest.get path: '/patient/account/1.json'
                def resp = rest.get path: '/patient/health/1.json'
                if ((resp as HttpResponseDecorator).status == 200) {
                    break
                }
            } catch (ignore) {
                // nothing needs to be done
            }
        }
    }

    @Value('${local.server.port}')
    Integer serverPort

    @Override
    RESTClient getRest() {
        new RESTClient("http://localhost:${serverPort ?: 8080}/", ContentType.JSON)
    }
}
