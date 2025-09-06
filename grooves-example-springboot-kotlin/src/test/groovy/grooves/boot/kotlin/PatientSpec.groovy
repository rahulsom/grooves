package grooves.boot.kotlin

import com.github.rahulsom.grooves.test.AbstractPatientSpec
import com.github.rahulsom.grooves.test.RestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Acceptance test that tests against Springboot with MongoDB
 *
 * @author Rahul Somasunderam
 */
@ContextConfiguration
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
class PatientSpec extends AbstractPatientSpec {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.0")

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri") {
            mongoDBContainer.start()
            mongoDBContainer.getReplicaSetUrl("test")
        }
    }

    @LocalServerPort
    int serverPort

    @Override
    RestClient getRest() {
        new RestClient("http://localhost:${serverPort ?: 8080}/")
    }
}
