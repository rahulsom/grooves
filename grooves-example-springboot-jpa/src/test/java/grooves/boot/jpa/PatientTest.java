package grooves.boot.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.github.rahulsom.grooves.test.AbstractPatientTest;
import com.github.rahulsom.grooves.test.RestClient;
import com.github.rahulsom.grooves.test.RestRequest;
import grooves.boot.jpa.domain.PatientDeprecatedBy;
import grooves.boot.jpa.domain.PatientDeprecates;
import grooves.boot.jpa.repositories.PatientEventRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * Acceptance test that tests against Springboot with JPA.
 *
 * @author Rahul Somasunderam
 */
@ContextConfiguration
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class PatientTest extends AbstractPatientTest {
    @Override
    public RestClient getRest() {
        final int port = serverPort;
        return new RestClient("http://localhost:" + (DefaultGroovyMethods.asBoolean(port) ? port : 8080) + "/");
    }

    @LocalServerPort
    private int serverPort;

    @Autowired
    private PatientEventRepository patientEventRepository;

    @ParameterizedTest
    @DisplayName("{1} - current patients join works")
    @CsvSource(textBlock = """
            1, Campbell, 1|2|3|5|6|7|8|10, 33
            2, 'Santana Row', 4|9, 37
            """)
    void currentPatientsJoinWorks(long id, String location, String patientsStr, int lastEventPosition) {
        var patients = Arrays.stream(patientsStr.split("\\|"))
                .mapToLong(Long::parseLong)
                .boxed()
                .toList();
        var expectedJoinedIds = patients.stream().map(p -> p + 7).sorted().toList();

        var resp = getRest().<Map<String, Object>>get(new RestRequest("zipcode/patients/" + id));

        assertThat(resp.getStatus()).isEqualTo(200);
        var data = resp.getData();
        assertThat(data.get("aggregateId").toString()).isEqualTo(String.valueOf(id));

        @SuppressWarnings("unchecked")
        var joinedIds = (List<Number>) data.get("joinedIds");
        var actualJoinedIds = joinedIds.stream().map(Number::longValue).sorted().toList();
        assertThat(actualJoinedIds).isEqualTo(expectedJoinedIds);
    }

    @ParameterizedTest
    @DisplayName("{1} - version {2} patients join works")
    @CsvSource(textBlock = """
            1, Campbell, 1, ''
            1, Campbell, 2, '1'
            1, Campbell, 3, '1|2'
            1, Campbell, 4, '1'
            1, Campbell, 5, '1|2'
            1, Campbell, 6, '1|2|3'
            1, Campbell, 7, '1|2|3|5'
            1, Campbell, 8, '1|2|3|5|6'
            1, Campbell, 9, '1|2|3|5|6|7'
            1, Campbell, 10, '1|2|3|5|6'
            1, Campbell, 11, '1|2|3|5|6|7'
            1, Campbell, 12, '1|2|3|5|6|7|8'
            1, Campbell, 13, '1|2|3|5|6|7|8|10'
            1, Campbell, 14, '1|2|3|5|6|7|8'
            1, Campbell, 15, '1|2|3|5|6|7|8|10'
            2, 'Santana Row', 1, ''
            2, 'Santana Row', 2, '2'
            2, 'Santana Row', 3, ''
            2, 'Santana Row', 4, '4'
            2, 'Santana Row', 5, '4|5'
            2, 'Santana Row', 6, '4'
            2, 'Santana Row', 7, '4|7'
            2, 'Santana Row', 8, '4'
            2, 'Santana Row', 9, '4|9'
            2, 'Santana Row', 10, '4|9|10'
            2, 'Santana Row', 11, '4|9'
            2, 'Santana Row', 12, '4|9|10'
            2, 'Santana Row', 13, '4|9'
            """)
    void versionedPatientsJoinWorks(long id, String location, int version, String patientsStr) {
        List<Long> patients;
        if (patientsStr.isEmpty()) {
            patients = List.of();
        } else {
            patients = Arrays.stream(patientsStr.split("\\|"))
                    .mapToLong(Long::parseLong)
                    .boxed()
                    .toList();
        }
        var expectedJoinedIds = patients.stream().map(p -> p + 7).sorted().toList();

        var resp = getRest()
                .<Map<String, Object>>get(new RestRequest("zipcode/patients/" + id, Map.of("version", version)));

        assertThat(resp.getStatus()).isEqualTo(200);
        var data = resp.getData();
        assertThat(data.get("aggregateId").toString()).isEqualTo(String.valueOf(id));
        assertThat(data.get("lastEventPosition")).isEqualTo(version);

        @SuppressWarnings("unchecked")
        var joinedIds = (List<Number>) data.get("joinedIds");
        var actualJoinedIds = joinedIds.stream().map(Number::longValue).sorted().toList();
        assertThat(actualJoinedIds).isEqualTo(expectedJoinedIds);
    }

    @Test
    @DisplayName("Deprecations are correctly stored")
    @Transactional
    void deprecationsAreCorrectlyStored() {
        var allEvents = patientEventRepository.findAll();

        var deprecatedByEvents = allEvents.stream()
                .filter(event -> event instanceof PatientDeprecatedBy)
                .map(event -> (PatientDeprecatedBy) event)
                .toList();

        var deprecatesEvents = allEvents.stream()
                .filter(event -> event instanceof PatientDeprecates)
                .map(event -> (PatientDeprecates) event)
                .toList();

        assertThat(deprecatedByEvents).allMatch(event -> event.getConverse() != null);
        assertThat(deprecatesEvents).allMatch(event -> event.getConverse() != null);
    }
}
