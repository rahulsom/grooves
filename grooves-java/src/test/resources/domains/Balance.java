package domains;

import com.github.rahulsom.grooves.api.snapshots.JavaSnapshot;
import org.jetbrains.annotations.Nullable;
import rx.Observable;

import java.util.Date;
import java.util.ArrayList;
import java.util.Set;

import static rx.Observable.empty;
import static rx.Observable.from;
import static rx.Observable.just;

public class Balance implements JavaSnapshot<Long, Account, String, Long, Transaction> {
    String id;
    Long lastEventPosition;
    Date lastEventTimestamp;
    Account aggregate, deprecatedBy;
    Set<Account> deprecates;

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Nullable
    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setLastEventPosition(Long lastEventPosition) {
        this.lastEventPosition = lastEventPosition;
    }

    @Override
    public void setLastEventTimestamp(Date lastEventTimestamp) {
        this.lastEventTimestamp = lastEventTimestamp;
    }

    @Nullable
    @Override
    public Long getLastEventPosition() {
        return lastEventPosition;
    }

    @Nullable
    @Override
    public Date getLastEventTimestamp() {
        return lastEventTimestamp;
    }

    @Override
    public void setAggregate(Account aggregate) {
        this.aggregate = aggregate;
    }

    @Override
    public void setDeprecatedBy(Account deprecatedBy) {
        this.deprecatedBy = deprecatedBy;
    }

    @Override
    public Observable<Account> getAggregateObservable() {
        return (aggregate != null ? just(aggregate) : empty());
    }

    @Override
    public Observable<Account> getDeprecatedByObservable() {
        return (deprecatedBy != null ? just(deprecatedBy) : empty());
    }

    @Override
    public Observable<Account> getDeprecatesObservable() {
        return from(new ArrayList(deprecates));
    }
}
