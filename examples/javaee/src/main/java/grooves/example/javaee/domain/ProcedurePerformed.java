package grooves.example.javaee.domain;

import java.math.BigDecimal;

public class ProcedurePerformed extends PatientEvent {

    private String code;
    private BigDecimal cost;

    @Override
    public String getAudit() {
        return "code: " + code + "; cost: " + cost;
    }

    @Override
    public String toString() {
        return String.format("ProcedurePerformed{code='%s', cost=%s}", code, cost);
    }

    public ProcedurePerformed(String code, BigDecimal cost) {
        this.code = code;
        this.cost = cost;
    }

    public String getCode() {
        return code;
    }

    public BigDecimal getCost() {
        return cost;
    }
}
