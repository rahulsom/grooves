package grooves.boot.kotlin;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.github.rahulsom.grooves.test.AbstractPatientTest;
import com.github.rahulsom.grooves.test.RestClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

@ContextConfiguration
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
public class PatientTest extends AbstractPatientTest {
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.0");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> {
            mongoDBContainer.start();
            return mongoDBContainer.getReplicaSetUrl("test");
        });
    }

    @LocalServerPort
    int serverPort;

    @Override
    public RestClient getRest() {
        return new RestClient("http://localhost:" + serverPort + "/");
    }

    @SuppressWarnings("EmptyMethod")
    @Test
    public void contextLoads() {
        // Test to ensure Spring context loads
    }
}
