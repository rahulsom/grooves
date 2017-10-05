package grooves.example.javaee.domain;

import com.github.rahulsom.grooves.java.Event;

import java.math.BigDecimal;

@Event(Patient.class)
public class PaymentMade extends PatientEvent {
    private BigDecimal amount;

    @Override
    public String toString() {
        return String.format("PaymentMade{amount=%s}", amount);
    }

    public PaymentMade(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
