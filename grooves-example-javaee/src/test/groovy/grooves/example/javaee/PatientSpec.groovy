package grooves.example.javaee

import com.github.rahulsom.grooves.test.AbstractPatientSpec
import com.github.rahulsom.grooves.test.RestClient

/**
 * Acceptance test that tests against Springboot with JPA
 *
 * @author Rahul Somasunderam
 */
class PatientSpec extends AbstractPatientSpec {

    @Override
    RestClient getRest() {
        new RestClient("http://localhost:9080/grooves-example-javaee/")
    }
}
