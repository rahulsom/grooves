package grooves.example.javaee.domain;

// tag::documented[]
import com.github.rahulsom.grooves.java.Aggregate;

import java.io.Serializable;

@Aggregate // <1>
public class Patient implements Serializable {
    private Long id;
    private String uniqueId;
    // end::documented[]

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

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
