package grooves.example.javaee.domain;

import com.github.rahulsom.grooves.api.events.Deprecates;
import rx.Observable;

import javax.xml.bind.annotation.XmlTransient;

import static rx.Observable.just;

public class PatientDeprecates extends PatientEvent
        implements Deprecates<Long, Patient, Long, PatientEvent> {
    private PatientDeprecatedBy converse;
    private Patient deprecated;

    @Override
    @XmlTransient
    public Observable<PatientDeprecatedBy> getConverseObservable() {
        return just(converse);
    }

    @Override
    @XmlTransient
    public Observable<Patient> getDeprecatedObservable() {
        return just(deprecated);
    }

    @Override
    public String getAudit() {
        return "deprecates:" + deprecated.getId();
    }

    @Override
    public String toString() {
        return String.format("PatientDeprecates{converse=%d, deprecated=%d}",
                converse.getId(), deprecated.getId());
    }

    @XmlTransient
    public PatientDeprecatedBy getConverse() {
        return converse;
    }

    public Patient getDeprecated() {
        return deprecated;
    }

    public PatientDeprecates(Patient deprecated, PatientDeprecatedBy converse) {
        this.converse = converse;
        this.deprecated = deprecated;
    }
}
