package grooves.example.javaee.domain;

// tag::documented[]
import com.github.rahulsom.grooves.api.AggregateType;
import com.github.rahulsom.grooves.java.Aggregate;

import java.io.Serializable;

@Aggregate // <1>
public class Patient implements AggregateType<Long>, // <2>
        Serializable {
    private Long id; // <3>
    private String uniqueId;
    // end::documented[]

    @Override
    public String toString() {
        return String.format("Patient{id=%d, uniqueId='%s'}", id, uniqueId);
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public Patient(String uniqueId) {
        this.uniqueId = uniqueId;
    }
    // tag::documented[]
}
// end::documented[]
