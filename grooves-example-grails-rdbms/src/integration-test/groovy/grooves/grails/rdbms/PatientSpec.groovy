package grooves.grails.rdbms

import com.github.rahulsom.grooves.test.AbstractPatientSpec
import grails.test.mixin.integration.Integration
import org.springframework.beans.factory.annotation.Value

@Integration
class PatientSpec extends AbstractPatientSpec {

    @Value('${local.server.port}')
    Integer serverPort

}
