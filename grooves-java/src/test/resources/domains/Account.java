package domains;

import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.java.Aggregate;
import org.jetbrains.annotations.Nullable;

@Aggregate
public class Account implements AggregateType<Long> {
    Long id;

    @Nullable
    @Override
    public Long getId() {
        return id;
    }

}