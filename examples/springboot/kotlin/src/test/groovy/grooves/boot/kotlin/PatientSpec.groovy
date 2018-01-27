package grooves.boot.kotlin

import com.github.rahulsom.grooves.test.AbstractPatientSpec
import groovyx.net.http.RESTClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.ContextConfiguration

import static groovyx.net.http.ContentType.JSON
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Acceptance test that tests against Springboot with JPA
 *
 * @author Rahul Somasunderam
 */
@ContextConfiguration
@SpringBootTest(webEnvironment = RANDOM_PORT)
class PatientSpec extends AbstractPatientSpec {

    @LocalServerPort
    int serverPort

    @Override
    RESTClient getRest() {
        new RESTClient("http://localhost:${serverPort ?: 8080}/", JSON)
    }
}
