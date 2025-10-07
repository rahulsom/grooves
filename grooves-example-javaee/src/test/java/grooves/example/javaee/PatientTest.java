package grooves.example.javaee;

import com.github.rahulsom.grooves.test.AbstractPatientTest;
import com.github.rahulsom.grooves.test.RestClient;
import java.io.File;
import java.time.Duration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

/**
 * Acceptance test using Testcontainers with Open Liberty.
 *
 * @author Rahul Somasunderam
 */
class PatientTest extends AbstractPatientTest {

    static GenericContainer<?> libertyContainer;

    @BeforeAll
    public static void setupSpec() {
        var warFile = new File(System.getProperty("war.file.path", "build/libs/grooves-example-javaee-0.1.war"));
        if (!warFile.exists()) {
            throw new IllegalStateException(
                    "WAR file not found at " + warFile.getAbsolutePath() + ". Run './gradlew war' first.");
        }

        libertyContainer = new GenericContainer<>("openliberty/open-liberty:full-java17-openj9-ubi")
                .withExposedPorts(9080)
                .withCopyFileToContainer(
                        MountableFile.forHostPath(warFile.toPath()), "/config/dropins/grooves-example-javaee.war")
                .withCopyFileToContainer(MountableFile.forClasspathResource("server.xml"), "/config/server.xml")
                .withEnv("WLP_LOGGING_CONSOLE_LOGLEVEL", "INFO")
                .withEnv("WLP_LOGGING_CONSOLE_FORMAT", "SIMPLE")
                .withLogConsumer(outputFrame -> System.out.println(
                        "[LIBERTY] " + outputFrame.getUtf8String().trim()))
                .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(5)));

        libertyContainer.start();
    }

    @AfterAll
    public static void cleanupSpec() {
        if (libertyContainer != null) {
            libertyContainer.stop();
        }
    }

    @Override
    public RestClient getRest() {
        var host = libertyContainer.getHost();
        var port = libertyContainer.getMappedPort(9080);
        return new RestClient("http://" + host + ":" + port + "/grooves-example-javaee/");
    }

    @SuppressWarnings("EmptyMethod")
    @Test
    public void contextLoads() {
        // Test to ensure Spring context loads
    }
}
