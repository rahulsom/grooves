package grooves.boot.kotlin

import com.github.rahulsom.grooves.test.AbstractPatientSpec
import com.github.rahulsom.grooves.test.RestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ContextConfiguration

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
    RestClient getRest() {
        new RestClient("http://localhost:${serverPort ?: 8080}/")
    }
}
