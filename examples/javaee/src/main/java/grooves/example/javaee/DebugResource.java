package grooves.example.javaee;

import grooves.example.javaee.domain.PatientAccount;
import grooves.example.javaee.domain.PatientEvent;
import grooves.example.javaee.domain.PatientHealth;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by rahul on 5/28/17.
 */
@Path("/debug")
public class DebugResource {
    @Inject
    private Database database;

    /**
     * Lists all patients.
     *
     * @return List of patients
     */
    @GET
    @Produces("application/json")
    @Path("/events")
    public List<PatientEvent> list() {
        return database.events().collect(toList());
    }

    /**
     * Lists all patients.
     *
     * @return List of patients
     */
    @GET
    @Produces("application/json")
    @Path("/account")
    public List<PatientAccount> account() {
        return database.snapshots(PatientAccount.class).collect(toList());
    }

    /**
     * Lists all patients.
     *
     * @return List of patients
     */
    @GET
    @Produces("application/json")
    @Path("/health")
    public List<PatientHealth> health() {
        return database.snapshots(PatientHealth.class).collect(toList());
    }

}
