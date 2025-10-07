package grooves.example.javaee.domain;

import com.github.rahulsom.grooves.java.Event;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Event(Patient.class)
@EqualsAndHashCode(callSuper = true)
@Data
public class ProcedurePerformed extends PatientEvent {

    private final String code;
    private final BigDecimal cost;
}
