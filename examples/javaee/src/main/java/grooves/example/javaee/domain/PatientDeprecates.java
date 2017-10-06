package grooves.example.javaee.domain;

import com.github.rahulsom.grooves.api.events.Deprecates;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import rx.Observable;

import javax.xml.bind.annotation.XmlTransient;

import static rx.Observable.just;

public class PatientDeprecates extends PatientEvent
        implements Deprecates<Long, Patient, Long, PatientEvent> {
    private PatientDeprecatedBy converse;
    @Getter private Patient deprecated;

    @NotNull
    @Override
    @XmlTransient
    public Observable<PatientDeprecatedBy> getConverseObservable() {
        return just(converse);
    }

    @NotNull
    @Override
    @XmlTransient
    public Observable<Patient> getDeprecatedObservable() {
        return just(deprecated);
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

    public PatientDeprecates(Patient deprecated, PatientDeprecatedBy converse) {
        this.converse = converse;
        this.deprecated = deprecated;
    }
}
