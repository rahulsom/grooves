package grooves.example.javaee;

import com.github.rahulsom.grooves.api.EventsDsl;
import com.github.rahulsom.grooves.api.OnSpec;
import com.github.rahulsom.grooves.api.snapshots.Snapshot;
import grooves.example.javaee.domain.*;
import grooves.example.javaee.queries.PatientAccountQuery;
import grooves.example.javaee.queries.PatientHealthQuery;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Singleton
@Startup
public class BootStrap {

    private static final String ANNUALPHYSICAL_NAME = "ANNUALPHYSICAL";
    private static final String ANNUALPHYSICAL_COST = "170.00";
    private static final String FLUSHOT_NAME = "FLUSHOT";
    private static final String FLUSHOT_COST = "32.40";
    private static final String GLUCOSETEST_NAME = "GLUCOSETEST";
    private static final String GLUCOSETEST_COST = "78.93";
    private static final String ANONYMOUS = "anonymous";

    private static long idGenerator = 1;

    private Date currDate = date("2016-01-01");
    private PatientAccountQuery patientAccountQuery;
    private PatientHealthQuery patientHealthQuery;
    private Database database;

    @Inject
    public void setPatientAccountQuery(PatientAccountQuery patientAccountQuery) {
        this.patientAccountQuery = patientAccountQuery;
    }

    @Inject
    public void setPatientHealthQuery(PatientHealthQuery patientHealthQuery) {
        this.patientHealthQuery = patientHealthQuery;
    }

    @Inject
    public void setDatabase(Database database) {
        this.database = database;
    }

    @PostConstruct
    void init() {
        setupJohnLennon();
        setupRingoStarr();
        setupPaulMcCartney();
        setupFreddieMercury();
        setupTinaFeyAndSarahPalin();
    }

    private Patient save(Patient patient) {
        patient.setId(database.patients().count() + 1L);
        database.addPatient(patient);
        return patient;
    }

    private Patient setupJohnLennon() {

        Patient patient = save(new Patient("42"));

        return on(patient, it -> {
            it.apply(new PatientCreated("John Lennon"));
            it.apply(new ProcedurePerformed(FLUSHOT_NAME, new BigDecimal(FLUSHOT_COST)));
            it.apply(new ProcedurePerformed(GLUCOSETEST_NAME, new BigDecimal(GLUCOSETEST_COST)));
            it.apply(
                    new PaymentMade(new BigDecimal("100.25")));

            it.snapshotWith(patientAccountQuery);
            it.snapshotWith(patientHealthQuery);

            it.apply(new ProcedurePerformed(ANNUALPHYSICAL_NAME,
                    new BigDecimal(ANNUALPHYSICAL_COST)));
            it.apply(new PaymentMade(new BigDecimal("180.00")));

            it.snapshotWith(patientAccountQuery);
            it.snapshotWith(patientHealthQuery);
        });
    }

    private Patient setupRingoStarr() {
        Patient patient = save(new Patient("43"));

        return on(patient, it -> {
            it.apply(new PatientCreated("Ringo Starr"));
            it.apply(new ProcedurePerformed(ANNUALPHYSICAL_NAME,
                    new BigDecimal(ANNUALPHYSICAL_COST)));
            it.apply(new ProcedurePerformed(GLUCOSETEST_NAME, new BigDecimal(GLUCOSETEST_COST)));
            it.apply(new PaymentMade(new BigDecimal("100.25")));

            it.snapshotWith(patientAccountQuery);
            it.snapshotWith(patientHealthQuery);

            it.apply(new ProcedurePerformed(FLUSHOT_NAME, new BigDecimal(FLUSHOT_COST)));
            it.apply(new PaymentMade(new BigDecimal("180.00")));

            it.snapshotWith(patientAccountQuery);
            it.snapshotWith(patientHealthQuery);
        });
    }

    private Patient setupPaulMcCartney() {
        Patient patient = save(new Patient("44"));

        return on(patient, it -> {
            it.apply(new PatientCreated("Paul McCartney"));
            it.apply(new ProcedurePerformed(ANNUALPHYSICAL_NAME,
                    new BigDecimal(ANNUALPHYSICAL_COST)));
            ProcedurePerformed gluc = (ProcedurePerformed) it.apply(
                    new ProcedurePerformed(GLUCOSETEST_NAME, new BigDecimal(GLUCOSETEST_COST)));
            it.apply(new PaymentMade(new BigDecimal("100.25")));
            it.apply(new PatientEventReverted(gluc.getId()));
            PaymentMade pmt = (PaymentMade) it.apply(new PaymentMade(new BigDecimal("30.00")));

            it.snapshotWith(patientAccountQuery);
            it.snapshotWith(patientHealthQuery);

            it.apply(new PatientEventReverted(pmt.getId()));
            it.apply(new PaymentMade(new BigDecimal("60.00")));

            it.snapshotWith(patientAccountQuery);
            it.snapshotWith(patientHealthQuery);

            it.apply(new PaymentMade(new BigDecimal("60.00")));

            it.snapshotWith(patientAccountQuery);
            it.snapshotWith(patientHealthQuery);
        });

    }

    private Patient setupFreddieMercury() {
        Patient patient = save(new Patient("45"));
        Patient patient2 = save(new Patient("46"));

        on(patient, it -> {
            it.apply(new PatientCreated("Farrokh Bulsara"));
            it.apply(new ProcedurePerformed(ANNUALPHYSICAL_NAME,
                    new BigDecimal(ANNUALPHYSICAL_COST)));
            it.apply(new ProcedurePerformed(GLUCOSETEST_NAME, new BigDecimal(GLUCOSETEST_COST)));

            it.snapshotWith(patientAccountQuery);
            it.snapshotWith(patientHealthQuery);
        });

        on(patient2, it -> {
            final String name = "Freddie Mercury";
            it.apply(new PatientCreated(name));
            it.apply(new PaymentMade(new BigDecimal("100.25")));

            it.snapshotWith(patientAccountQuery);
            it.snapshotWith(patientHealthQuery);
        });

        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(currDate);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        currDate = calendar.getTime();
        merge(patient, patient2);
        return patient;
    }

    private Patient setupTinaFeyAndSarahPalin() {
        Patient patient = save(new Patient("47"));
        Patient patient2 = save(new Patient("48"));

        on(patient, it -> {
            it.apply(new PatientCreated("Tina Fey"));
            it.apply(new ProcedurePerformed(ANNUALPHYSICAL_NAME,
                    new BigDecimal(ANNUALPHYSICAL_COST)));
            it.apply(new ProcedurePerformed(GLUCOSETEST_NAME, new BigDecimal(GLUCOSETEST_COST)));

            it.snapshotWith(patientAccountQuery);
            it.snapshotWith(patientHealthQuery);
        });

        on(patient2, it -> {
            final String name = "Sarah Palin";
            it.apply(new PatientCreated(name));
            it.apply(new PaymentMade(new BigDecimal("100.25")));

            it.snapshotWith(patientAccountQuery);
            it.snapshotWith(patientHealthQuery);
        });

        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(currDate);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        currDate = calendar.getTime();
        final PatientDeprecatedBy mergeEvent = merge(patient, patient2);

        on(patient, it -> it.apply(new PatientEventReverted(mergeEvent.getId())));
        on(patient2, it -> it.apply(new PatientEventReverted(mergeEvent.getConverse().getId())));

        return patient;
    }

    /**
     * Merges `self` into `into`.
     *
     * @param self The aggregate to be deprecated
     * @param into The aggregate to survive
     *
     * @return The DeprecatedBy event
     */
    private PatientDeprecatedBy merge(Patient self, Patient into) {
        PatientDeprecatedBy e1 = new PatientDeprecatedBy(into);

        e1.setAggregate(self);
        e1.setTimestamp(currDate);
        e1.setPosition(database.events().filter(x -> Objects.equals(x.getAggregate(), self))
                .count() + 1);
        e1.setId(database.events().count() + 1);

        PatientDeprecates e2 = new PatientDeprecates(self, e1);

        e2.setAggregate(into);
        e2.setTimestamp(currDate);
        e2.setPosition(database.events().filter(x -> Objects.equals(x.getAggregate(), into))
                .count() + 1);
        e2.setId(database.events().count() + 2);

        e1.setConverse(e2);

        database.addEvent(e1);
        database.addEvent(e2);
        return e2.getConverseObservable().toBlocking().single();
    }

    private Date date(String yyyyMMdd) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(yyyyMMdd);
        } catch (ParseException ignore) {
            return null;
        }
    }

    private Patient on(Patient patient, Consumer<OnSpec> closure) {
        Consumer eventSaver = patientEvent -> {
            if (patientEvent instanceof PatientEvent) {
                ((PatientEvent) patientEvent).setId(idGenerator++);
                database.addEvent((PatientEvent) patientEvent);
            } else {
                ((Snapshot) patientEvent).setId(idGenerator++);
                database.addSnapshot(patientEvent);
            }
        };

        Supplier<Long> positionSupplier =
                () -> database.events()
                        .filter(x -> x.getAggregateObservable()
                                .toBlocking()
                                .single()
                                .equals(patient))
                        .count() + 1;

        Supplier<Date> dateSupplier =
                () -> {
                    final Calendar calendar = Calendar.getInstance();
                    calendar.setTime(currDate);
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    currDate = calendar.getTime();
                    return currDate;
                };

        EventsDsl<Long, Patient, Long, PatientEvent> dsl = new EventsDsl<>();
        return dsl.on(patient, eventSaver, positionSupplier, dateSupplier,
                closure::accept);
    }

}
