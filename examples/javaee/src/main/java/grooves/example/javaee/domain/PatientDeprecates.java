package grooves.example.javaee.domain;

import com.github.rahulsom.grooves.api.events.Deprecates;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

import javax.xml.bind.annotation.XmlTransient;

import static io.reactivex.Flowable.just;

public class PatientDeprecates extends PatientEvent
        implements Deprecates<Long, Patient, Long, PatientEvent> {
    private PatientDeprecatedBy converse;
    @Getter private Patient deprecated;

    @NotNull
    @Override
    @XmlTransient
    public Publisher<PatientDeprecatedBy> getConverseObservable() {
        return just(converse);
    }

    @NotNull
    @Override
    @XmlTransient
    public Publisher<Patient> getDeprecatedObservable() {
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
