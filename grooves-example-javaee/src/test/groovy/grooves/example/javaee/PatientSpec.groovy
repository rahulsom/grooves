package grooves.example.javaee

import com.github.rahulsom.grooves.test.AbstractPatientSpec
import com.github.rahulsom.grooves.test.RestClient
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.MountableFile
import org.testcontainers.containers.wait.strategy.Wait
import spock.lang.Shared
import java.time.Duration

/**
 * Acceptance test using Testcontainers with Open Liberty
 *
 * @author Rahul Somasunderam
 */
class PatientSpec extends AbstractPatientSpec {

    @Shared
    GenericContainer<?> libertyContainer

    def setupSpec() {
        def warFile = new File(System.getProperty("war.file.path", "build/libs/grooves-example-javaee-0.1.war"))
        if (!warFile.exists()) {
            throw new IllegalStateException("WAR file not found at ${warFile.absolutePath}. Run './gradlew war' first.")
        }

        libertyContainer = new GenericContainer<>("openliberty/open-liberty:full-java17-openj9-ubi")
                .withExposedPorts(9080)
                .withCopyFileToContainer(
                        MountableFile.forHostPath(warFile.toPath()),
                        "/config/dropins/grooves-example-javaee.war"
                )
                .withCopyFileToContainer(
                        MountableFile.forClasspathResource("server.xml"),
                        "/config/server.xml"
                )
                .withEnv("WLP_LOGGING_CONSOLE_LOGLEVEL", "INFO")
                .withEnv("WLP_LOGGING_CONSOLE_FORMAT", "SIMPLE")
                .withLogConsumer({ outputFrame -> 
                    println "[LIBERTY] ${outputFrame.utf8String.trim()}"
                })
                .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(5)))

        libertyContainer.start()
    }

    def cleanupSpec() {
        if (libertyContainer) {
            libertyContainer.stop()
        }
    }

    @Override
    RestClient getRest() {
        def host = libertyContainer.host
        def port = libertyContainer.getMappedPort(9080)
        new RestClient("http://${host}:${port}/grooves-example-javaee/")
    }
}