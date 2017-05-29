package grooves.example.javaee.domain;

import com.github.rahulsom.grooves.api.events.DeprecatedBy;
import rx.Observable;

import javax.xml.bind.annotation.XmlTransient;

import static rx.Observable.just;

public class PatientDeprecatedBy extends PatientEvent implements
        DeprecatedBy<Long, Patient, Long, PatientEvent> {
    private PatientDeprecates converse;
    private Patient deprecator;

    @Override
    @XmlTransient
    public Observable<PatientDeprecates> getConverseObservable() {
        return just(converse);
    }

    @XmlTransient
    public Observable<Patient> getDeprecatorObservable() {
        return just(deprecator);
    }

    @Override
    public String getAudit() {
        return "deprecatedBy:" + deprecator.getId();
    }

    @Override
    public String toString() {
        return String.format("PatientDeprecatedBy{converse=%s, deprecator=%s}",
                converse.getId(), deprecator.getId());
    }

    @XmlTransient
    public PatientDeprecates getConverse() {
        return converse;
    }

    public void setConverse(PatientDeprecates converse) {
        this.converse = converse;
    }

    public Patient getDeprecator() {
        return deprecator;
    }

    public PatientDeprecatedBy(Patient deprecator) {
        this.deprecator = deprecator;
    }
}
