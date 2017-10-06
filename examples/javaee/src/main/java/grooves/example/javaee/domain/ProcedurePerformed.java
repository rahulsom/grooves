package grooves.example.javaee.domain;

import com.github.rahulsom.grooves.java.Event;
import lombok.Getter;

import java.math.BigDecimal;

@Event(Patient.class)
public class ProcedurePerformed extends PatientEvent {

    @Getter private String code;
    @Getter private BigDecimal cost;

    @Override
    public String toString() {
        return String.format("ProcedurePerformed{code='%s', cost=%s}", code, cost);
    }

    public ProcedurePerformed(String code, BigDecimal cost) {
        this.code = code;
        this.cost = cost;
    }
}
