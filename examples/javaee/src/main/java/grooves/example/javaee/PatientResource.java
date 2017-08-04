package grooves.example.javaee;

import grooves.example.javaee.domain.Patient;
import grooves.example.javaee.domain.PatientAccount;
import grooves.example.javaee.domain.PatientHealth;
import grooves.example.javaee.queries.PatientAccountQuery;
import grooves.example.javaee.queries.PatientHealthQuery;
import rx.Observable;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Path("/patient")
public class PatientResource {

    @Inject
    private Database database;

    @Inject
    private PatientHealthQuery patientHealthQuery;

    @Inject
    private PatientAccountQuery patientAccountQuery;

    /**
     * Lists all patients.
     *
     * @return List of patients
     */
    @GET
    @Produces("application/json")
    public List<Patient> list() {
        return database.patients().collect(toList());
    }

    /**
     * Lists all patients.
     *
     * @param id The patient's id
     *
     * @return List of patients
     */
    @GET
    @Path("show/{id}")
    @Produces("application/json")
    public Patient show(@PathParam("id") Long id) {
        final Optional<Patient> patientOptional =
                database.patients().filter(x -> Objects.equals(x.getId(), id)).findFirst();
        return patientOptional.orElse(null);
    }

    /**
     * Displays the health of a patient.
     *
     * @param id      The patients id
     * @param version The version requested
     * @param date    The date requested
     *
     * @return The Health of the patient
     */
    @GET
    @Path("health/{id}")
    @Produces("application/json")
    public PatientHealth health(
            @PathParam("id") Long id,
            @QueryParam("version") Long version,
            @QueryParam("date") Date date) {

        final Patient patient = database.patients()
                .filter(it -> Objects.equals(it.getId(), id))
                .findFirst()
                .get();

        Observable<PatientHealth> computation =
                version != null ?
                        patientHealthQuery.computeSnapshot(patient, version) :
                        date != null ?
                                patientHealthQuery.computeSnapshot(patient, date) :
                                patientHealthQuery.computeSnapshot(patient, Long.MAX_VALUE);

        final PatientHealth patientHealth = computation.toBlocking().first();

        if (patientHealth == null) {
            throw new RuntimeException("Could not compute account snapshot");
        }

        return patientHealth;
    }

    /**
     * Displays account information.
     *
     * @param id      The patients id
     * @param version The version requested
     * @param date    The date requested
     *
     * @return The Account of the patient
     */
    @GET
    @Path("account/{id}")
    @Produces("application/json")
    public PatientAccount account(
            @PathParam("id") Long id,
            @QueryParam("version") Long version,
            @QueryParam("date") Date date) {

        final Patient patient = database.patients()
                .filter(it -> Objects.equals(it.getId(), id))
                .findFirst()
                .get();

        Observable<PatientAccount> computation =
                version != null ?
                        patientAccountQuery.computeSnapshot(patient, version) :
                        date != null ?
                                patientAccountQuery.computeSnapshot(patient, date) :
                                patientAccountQuery.computeSnapshot(patient, Long.MAX_VALUE);

        final PatientAccount patientAccount = computation.toBlocking().first();

        if (patientAccount == null) {
            throw new RuntimeException("Could not compute account snapshot");
        }

        return patientAccount;
    }
}
