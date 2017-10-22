package domains;

import com.github.rahulsom.grooves.api.snapshots.JavaSnapshot;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Publisher;

import java.util.Date;
import java.util.ArrayList;
import java.util.Set;

import static io.reactivex.Flowable.empty;
import static io.reactivex.Flowable.fromIterable;
import static io.reactivex.Single.just;

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
    public Publisher<Account> getAggregateObservable() {
        return aggregate != null ? just(aggregate).toFlowable() : empty();
    }

    @Override
    public Publisher<Account> getDeprecatedByObservable() {
        return deprecatedBy != null ? just(deprecatedBy).toFlowable() : empty();
    }

    @Override
    public Publisher<Account> getDeprecatesObservable() {
        return fromIterable(new ArrayList(deprecates));
    }
}
