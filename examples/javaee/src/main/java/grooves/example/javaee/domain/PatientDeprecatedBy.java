package grooves.example.javaee.domain;

import com.github.rahulsom.grooves.api.events.DeprecatedBy;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

import javax.xml.bind.annotation.XmlTransient;

import static io.reactivex.Flowable.just;

public class PatientDeprecatedBy extends PatientEvent implements
        DeprecatedBy<Long, Patient, Long, PatientEvent> {
    @Setter private PatientDeprecates converse;
    @Getter private Patient deprecator;

    @NotNull
    @Override
    @XmlTransient
    public Publisher<PatientDeprecates> getConverseObservable() {
        return just(converse);
    }

    @NotNull
    @XmlTransient
    public Publisher<Patient> getDeprecatorObservable() {
        return just(deprecator);
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

    public PatientDeprecatedBy(Patient deprecator) {
        this.deprecator = deprecator;
    }
}
