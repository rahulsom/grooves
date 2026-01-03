package com.github.rahulsom.grooves.test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PatientHealthDTO(
        String aggregateId, PatientDTO aggregate, String name, Long lastEventPosition, List<ProcedureDTO> procedures) {
    public String getEffectiveAggregateId() {
        if (aggregateId != null) return aggregateId;
        if (aggregate != null) return aggregate.id();
        return null;
    }
}
