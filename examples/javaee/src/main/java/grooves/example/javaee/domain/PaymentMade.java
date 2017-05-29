package grooves.example.javaee.domain;

import java.math.BigDecimal;

public class PaymentMade extends PatientEvent {
    private BigDecimal amount;

    @Override
    public String getAudit() {
        return "amount:" + amount;
    }

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
