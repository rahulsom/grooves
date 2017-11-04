package domains;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.java.Aggregate;

@Aggregate
public class Account implements AggregateType<Long> {
    Long id;

    @Override
    public Long getId() {
        return id;
    }

}