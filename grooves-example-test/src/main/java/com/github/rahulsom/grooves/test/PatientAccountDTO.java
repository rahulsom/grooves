package com.github.rahulsom.grooves.test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PatientAccountDTO(
        BigDecimal balance,
        BigDecimal moneyMade,
        List<String> deprecatesIds,
        List<PatientDTO> deprecates,
        String aggregateId,
        PatientDTO aggregate,
        Long lastEventPosition,
        Object lastEventTimestamp,
        String name) {
    public String getEffectiveAggregateId() {
        if (aggregateId != null) return aggregateId;
        if (aggregate != null) return aggregate.id();
        return null;
    }

    public List<String> getEffectiveDeprecatesIds() {
        if (deprecatesIds != null) return deprecatesIds;
        if (deprecates != null) return deprecates.stream().map(PatientDTO::id).toList();
        return null;
    }
}
