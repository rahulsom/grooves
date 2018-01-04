import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.events.RevertEvent
import com.github.rahulsom.grooves.api.snapshots.Snapshot
import com.github.rahulsom.grooves.groovy.transformations.Aggregate
import com.github.rahulsom.grooves.groovy.transformations.Event
import groovy.transform.CompileStatic
import org.reactivestreams.Publisher
import rx.Observable

import static rx.Observable.*
import static rx.RxReactiveStreams.toPublisher

@CompileStatic @Aggregate class Account {
    Long id
}

@CompileStatic abstract class Transaction implements BaseEvent<Account, Long, Transaction> {
    Account aggregate
    RevertEvent<Account, Long, Transaction> revertedBy
    Long id
    long position
    Date timestamp

    Publisher<Account> getAggregateObservable() {
        toPublisher(aggregate ? just(aggregate) : empty()) as Publisher<Account>
    }
}

@CompileStatic @Event(Account) class CashDeposit extends Transaction {}

@CompileStatic @Event(Account) class CashWithdrawal extends Transaction {}

@CompileStatic class Balance implements Snapshot<Account, String, Long, Transaction> {
    String id
    long lastEventPosition
    Date lastEventTimestamp
    Account aggregate, deprecatedBy
    Set<Account> deprecates

    @Override
    Publisher<Account> getAggregateObservable() {
        toPublisher(aggregate ? just(aggregate) : empty()) as Publisher<Account>
    }

    @Override Publisher<Account> getDeprecatedByObservable() {
        toPublisher((deprecatedBy ? just(deprecatedBy) : empty()) as Observable<Account>)
    }

    @Override
    Publisher<Account> getDeprecatesObservable() {
        toPublisher(from(deprecates.toList()))
    }
}
