import com.github.rahulsom.grooves.api.AggregateType
import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.events.RevertEvent
import com.github.rahulsom.grooves.api.snapshots.Snapshot
import com.github.rahulsom.grooves.groovy.transformations.Aggregate
import com.github.rahulsom.grooves.groovy.transformations.Event
import groovy.transform.CompileStatic
import rx.Observable

import static rx.Observable.empty
import static rx.Observable.just

@CompileStatic @Aggregate class Account implements AggregateType<Long> {
    Long id
}

@CompileStatic abstract class Transaction implements BaseEvent<Account, Long, Transaction> {
    Account aggregate
    RevertEvent<Account, Long, Transaction> revertedBy
    Long id, position
    Date timestamp
    String createdBy, audit

    Observable<Account> getAggregateObservable() { just(aggregate) }
}

@CompileStatic @Event(Account) class CashDeposit extends Transaction {}

@CompileStatic @Event(Account) class CashWithdrawal extends Transaction {}

@CompileStatic class Balance implements Snapshot<Account, String, Long, Transaction> {
    String id
    Long lastEventPosition
    Date lastEventTimestamp
    Account aggregate, deprecatedBy
    Set<Account> deprecates

    @Override
    Observable<Account> getAggregateObservable() {
        (aggregate ? just(aggregate) : empty()) as Observable<Account>
    }

    @Override Observable<Account> getDeprecatedByObservable() {
        (deprecatedBy ? just(deprecatedBy) : empty()) as Observable<Account>
    }

    @Override
    Observable<Account> getDeprecatesObservable() {
        Observable.from(deprecates.toList())
    }
}
