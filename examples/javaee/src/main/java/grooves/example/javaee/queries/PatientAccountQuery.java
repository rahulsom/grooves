package grooves.example.javaee.queries;

import com.github.rahulsom.grooves.api.EventApplyOutcome;
import com.github.rahulsom.grooves.java.Query;
import grooves.example.javaee.Database;
import grooves.example.javaee.domain.*;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

import javax.inject.Inject;
import java.math.BigDecimal;

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE;
import static rx.Observable.just;
import static rx.RxReactiveStreams.toPublisher;

// tag::documented[]
@Query(aggregate = Patient.class, snapshot = PatientAccount.class) // <9>
public class PatientAccountQuery
        implements CustomQuerySupport<PatientAccount> { // <10>

    // end::documented[]
    @Inject @Getter
    private Database database;

    @Override
    public Class<PatientAccount> getSnapshotClass() {
        return PatientAccount.class;
    }

    // tag::documented[]
    @NotNull
    @Override
    public PatientAccount createEmptySnapshot() { // <11>
        return new PatientAccount();
    }
    // end::documented[]

    @Override
    public void addToDeprecates(
            @NotNull PatientAccount snapshot, @NotNull Patient deprecatedAggregate) {
        snapshot.getDeprecates().add(deprecatedAggregate);
    }

    /**
     * Applies patient created.
     * @param event the event.
     * @param snapshot The snapshot.
     * @return the result of apply
     */
    // tag::documented[]
    public Publisher<EventApplyOutcome> applyPatientCreated(
            PatientCreated event, PatientAccount snapshot) { // <12>
        if (snapshot.getAggregate() == event.getAggregate()) {
            snapshot.setName(event.getName());
        }
        return toPublisher(just(CONTINUE)); // <13>
    }

    // end::documented[]
    /**
     * Applies procedure performed.
     * @param event the event.
     * @param snapshot The snapshot.
     * @return the result of apply
     */
    // tag::documented[]
    public Publisher<EventApplyOutcome> applyProcedurePerformed(
            ProcedurePerformed event, PatientAccount snapshot) {
        final double cost = event.getCost().doubleValue();
        final double originalBalance = snapshot.getBalance().doubleValue();

        snapshot.setBalance(BigDecimal.valueOf(originalBalance + cost));

        return toPublisher(just(CONTINUE));
    }

    // end::documented[]
    /**
     * Applies Payment made.
     * @param event the event.
     * @param snapshot The snapshot.
     * @return the result of apply
     */
    // tag::documented[]
    public Publisher<EventApplyOutcome> applyPaymentMade(
            PaymentMade event, PatientAccount snapshot) {

        final double amount = event.getAmount().doubleValue();
        final double originalBalance = snapshot.getBalance().doubleValue();
        final double originalMoneyMade = snapshot.getMoneyMade().doubleValue();

        snapshot.setBalance(BigDecimal.valueOf(originalBalance - amount));
        snapshot.setMoneyMade(BigDecimal.valueOf(originalMoneyMade + amount));

        return toPublisher(just(CONTINUE));
    }

}
// end::documented[]
