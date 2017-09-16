package grooves.example.javaee.queries;

import com.github.rahulsom.grooves.api.EventApplyOutcome;
import com.github.rahulsom.grooves.java.Query;
import grooves.example.javaee.Database;
import grooves.example.javaee.domain.*;
import rx.Observable;

import javax.inject.Inject;
import java.math.BigDecimal;

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE;
import static rx.Observable.just;

// tag::documented[]
@Query(aggregate = Patient.class, snapshot = PatientAccount.class) // <10>
public class PatientAccountQuery
        implements CustomQuerySupport<PatientAccount, PatientAccountQuery> { // <11>

    // end::documented[]
    @Inject
    private Database database;

    @Override
    public Database getDatabase() {
        return database;
    }

    @Override
    public Class<PatientAccount> getSnapshotClass() {
        return PatientAccount.class;
    }

    // tag::documented[]
    @Override
    public PatientAccount createEmptySnapshot() { // <12>
        return new PatientAccount();
    }
    // end::documented[]

    @Override
    public void addToDeprecates(PatientAccount snapshot, Patient deprecatedAggregate) {
        snapshot.getDeprecates().add(deprecatedAggregate);
    }

    /**
     * Applies patient created.
     * @param event the event.
     * @param snapshot The snapshot.
     * @return the result of apply
     */
    // tag::documented[]
    public Observable<EventApplyOutcome> applyPatientCreated(
            PatientCreated event, PatientAccount snapshot) { // <13>
        if (snapshot.getAggregate() == event.getAggregate()) {
            snapshot.setName(event.getName());
        }
        return just(CONTINUE); // <14>
    }

    // end::documented[]
    /**
     * Applies procedure performed.
     * @param event the event.
     * @param snapshot The snapshot.
     * @return the result of apply
     */
    // tag::documented[]
    public Observable<EventApplyOutcome> applyProcedurePerformed(
            ProcedurePerformed event, PatientAccount snapshot) {
        final double cost = event.getCost().doubleValue();
        final double originalBalance = snapshot.getBalance().doubleValue();

        snapshot.setBalance(BigDecimal.valueOf(originalBalance + cost));

        return just(CONTINUE);
    }

    // end::documented[]
    /**
     * Applies Payment made.
     * @param event the event.
     * @param snapshot The snapshot.
     * @return the result of apply
     */
    // tag::documented[]
    public Observable<EventApplyOutcome> applyPaymentMade(
            PaymentMade event, PatientAccount snapshot) {

        final double amount = event.getAmount().doubleValue();
        final double originalBalance = snapshot.getBalance().doubleValue();
        final double originalMoneyMade = snapshot.getMoneyMade().doubleValue();

        snapshot.setBalance(BigDecimal.valueOf(originalBalance - amount));
        snapshot.setMoneyMade(BigDecimal.valueOf(originalMoneyMade + amount));

        return just(CONTINUE);
    }

}
// end::documented[]
