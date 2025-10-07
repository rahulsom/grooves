package grooves.example.javaee;

import com.github.rahulsom.grooves.api.snapshots.Snapshot;
import grooves.example.javaee.domain.Patient;
import grooves.example.javaee.domain.PatientEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;
import javax.ejb.Singleton;
import org.jetbrains.annotations.NotNull;

@Singleton
public class Database {

    private final List<PatientEvent> eventList = new ArrayList<>();
    private final List<Patient> patientList = new ArrayList<>();
    private final List snapshotList = new ArrayList();

    /**
     * Gets all events in the database.
     *
     * @return All events in the database
     */
    public Stream<PatientEvent> events() {
        return eventList.stream();
    }

    /**
     * Gets all patients in the database.
     *
     * @return All patients in the database
     */
    public Stream<Patient> patients() {
        return patientList.stream();
    }

    /**
     * Finds snapshots of type.
     *
     * @param clazz The class representing the type
     * @param <T>   The type
     *
     * @return A Stream of type
     */
    @NotNull
    public <T extends Snapshot<?, ?, ?, ?>> Stream<T> snapshots(Class<T> clazz) {
        return (Stream<T>) snapshotList.stream().filter(it -> clazz.isAssignableFrom(it.getClass()));
    }

    /**
     * Adds a patient to the database.
     *
     * @param patient the patient to add
     */
    public void addPatient(Patient patient) {
        patientList.add(patient);
    }

    /**
     * Adds an event to the database.
     *
     * @param event the event to add
     */
    public void addEvent(PatientEvent event) {
        eventList.add(event);
    }

    /**
     * Adds a snapshot to the database.
     *
     * @param snapshot the snapshot to add
     */
    public void addSnapshot(Object snapshot) {
        snapshotList.add(snapshot);
    }

    /**
     * Checks if a timestamp is in a range.
     *
     * @param lowerBoundExclusive the lower bound of the range
     * @param timestamp           the timestamp to check
     * @param upperBoundInclusive the upper bound of the range
     * @return true if the timestamp is in the range, false otherwise
     */
    public static boolean isTimestampInRange(Date lowerBoundExclusive, Date timestamp, Date upperBoundInclusive) {
        return (lowerBoundExclusive == null || timestamp.compareTo(lowerBoundExclusive) > 0)
                && timestamp.compareTo(upperBoundInclusive) <= 0;
    }
}
