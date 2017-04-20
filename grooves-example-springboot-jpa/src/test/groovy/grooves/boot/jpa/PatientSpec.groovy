package grooves.boot.jpa

import com.github.rahulsom.grooves.test.AbstractPatientSpec
import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient
import org.springframework.boot.context.embedded.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

@ContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PatientSpec extends AbstractPatientSpec {

    @LocalServerPort
    int serverPort

    @Override
    RESTClient getRest() {
        new RESTClient("http://localhost:${serverPort ?: 8080}/", ContentType.JSON)
    }
}
