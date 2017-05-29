package grooves.example.javaee.queries;

import com.github.rahulsom.grooves.api.EventApplyOutcome;
import grooves.example.javaee.Database;
import grooves.example.javaee.domain.*;
import rx.Observable;

import javax.inject.Inject;
import java.math.BigDecimal;

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE;
import static rx.Observable.just;

public class PatientAccountQuery implements CustomQuerySupport<PatientAccount> {

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

    @Override
    public PatientAccount createEmptySnapshot() {
        return new PatientAccount();
    }

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
    public Observable<EventApplyOutcome> applyPatientCreated(
            PatientCreated event, PatientAccount snapshot) {
        if (snapshot.getName() == null) {
            snapshot.setName(event.getName());
        }
        return just(CONTINUE);
    }

    /**
     * Applies procedure performed.
     * @param event the event.
     * @param snapshot The snapshot.
     * @return the result of apply
     */
    public Observable<EventApplyOutcome> applyProcedurePerformed(
            ProcedurePerformed event, PatientAccount snapshot) {
        final double cost = event.getCost().doubleValue();
        final double originalBalance = snapshot.getBalance().doubleValue();

        snapshot.setBalance(BigDecimal.valueOf(originalBalance + cost));

        return just(CONTINUE);
    }

    /**
     * Applies Payment made.
     * @param event the event.
     * @param snapshot The snapshot.
     * @return the result of apply
     */
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
