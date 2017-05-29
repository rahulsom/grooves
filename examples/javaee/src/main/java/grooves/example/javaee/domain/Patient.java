package grooves.example.javaee.domain;

import com.github.rahulsom.grooves.api.AggregateType;

import java.io.Serializable;

public class Patient implements AggregateType<Long>, Serializable {
    private Long id;
    private String uniqueId;

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
}
