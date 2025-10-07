package grooves.example.javaee.domain;

import com.github.rahulsom.grooves.java.Event;
import java.math.BigDecimal;
import lombok.*;

@Event(Patient.class)
@EqualsAndHashCode(callSuper = true)
@Data
public class PaymentMade extends PatientEvent {
    private final BigDecimal amount;
}
