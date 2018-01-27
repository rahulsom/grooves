package grooves.example.javaee.domain;

// tag::documented[]
import com.github.rahulsom.grooves.java.Aggregate;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Aggregate // <1>
public class Patient implements Serializable {
    @Getter @Setter private Long id;
    @Getter @Setter private String uniqueId;
    // end::documented[]

    @Override
    public String toString() {
        return String.format("Patient{id=%d, uniqueId='%s'}", id, uniqueId);
    }

    public Patient(String uniqueId) {
        this.uniqueId = uniqueId;
    }
    // tag::documented[]
}
// end::documented[]
