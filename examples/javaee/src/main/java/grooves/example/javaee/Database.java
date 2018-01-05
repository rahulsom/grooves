package grooves.example.javaee;

import com.github.rahulsom.grooves.api.snapshots.Snapshot;
import grooves.example.javaee.domain.Patient;
import grooves.example.javaee.domain.PatientEvent;
import org.jetbrains.annotations.NotNull;

import javax.ejb.Singleton;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

@Singleton
public class Database {

    private final List<PatientEvent> eventList = new ArrayList<>();
    private final List<Patient> patientList = new ArrayList<>();
    private final List snapshotList = new ArrayList();

    public Stream<PatientEvent> events() {
        return eventList.stream();
    }

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
    public <T extends Snapshot> Stream<T> snapshots(Class<T> clazz) {
        return (Stream<T>) snapshotList
                .stream()
                .filter(it -> clazz.isAssignableFrom(it.getClass()));
    }

    public void addPatient(Patient patient) {
        patientList.add(patient);
    }

    public void addEvent(PatientEvent event) {
        eventList.add(event);
    }

    public void addSnapshot(Object snapshot) {
        snapshotList.add(snapshot);
    }

    public static boolean isTimestampInRange(
            Date lowerBoundExclusive, Date timestamp, Date upperBoundInclusive) {
        return (lowerBoundExclusive == null || timestamp.compareTo(lowerBoundExclusive) > 0)
                && timestamp.compareTo(upperBoundInclusive) <= 0;
    }

}
