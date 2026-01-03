package com.github.rahulsom.grooves.test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PatientDTO(String id, String uniqueId) {}
