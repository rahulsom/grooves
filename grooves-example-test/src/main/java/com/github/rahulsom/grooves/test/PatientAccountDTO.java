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
        String name) {}
