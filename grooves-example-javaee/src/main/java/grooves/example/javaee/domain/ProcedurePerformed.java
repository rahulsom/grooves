package grooves.example.javaee.domain;

import com.github.rahulsom.grooves.java.Event;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Event(Patient.class)
@EqualsAndHashCode(callSuper = true)
@Data
public class ProcedurePerformed extends PatientEvent {

    private final String code;
    private final BigDecimal cost;
}
