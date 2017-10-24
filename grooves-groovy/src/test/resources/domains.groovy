import com.github.rahulsom.grooves.api.AggregateType
import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.events.RevertEvent
import com.github.rahulsom.grooves.api.snapshots.JavaSnapshot
import com.github.rahulsom.grooves.groovy.transformations.Aggregate
import com.github.rahulsom.grooves.groovy.transformations.Event
import groovy.transform.CompileStatic
import org.reactivestreams.Publisher

import static io.reactivex.Flowable.empty

@CompileStatic @Aggregate class Account implements AggregateType<Long> {
    Long id
}

@CompileStatic abstract class Transaction implements BaseEvent<Long, Account, Long, Transaction> {
    Account aggregate
    RevertEvent<Long, Account, Long, Transaction> revertedBy
    Long id, position
    Date timestamp

    Publisher<Account> getAggregateObservable() { empty() }
}

@CompileStatic @Event(Account) class CashDeposit extends Transaction {}

@CompileStatic @Event(Account) class CashWithdrawal extends Transaction {}

@CompileStatic class Balance implements JavaSnapshot<Long, Account, String, Long, Transaction> {
    String id
    Long lastEventPosition
    Date lastEventTimestamp
    Account aggregate, deprecatedBy
    Set<Account> deprecates

    @Override Publisher<Account> getAggregateObservable() { empty() }

    @Override Publisher<Account> getDeprecatedByObservable() { empty() }

    @Override Publisher<Account> getDeprecatesObservable() { empty() }
}
