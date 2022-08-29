package grooves.example.javaee.domain;

import com.github.rahulsom.grooves.api.events.DeprecatedBy;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

import javax.xml.bind.annotation.XmlTransient;

import static rx.Observable.just;
import static rx.RxReactiveStreams.toPublisher;

@EqualsAndHashCode(callSuper = true)
@Data
public class PatientDeprecatedBy extends PatientEvent implements
        DeprecatedBy<Patient, Long, PatientEvent> {
    @XmlTransient
    private PatientDeprecates converse;
    private final Patient deprecator;

    @Override
    @NotNull
    @XmlTransient
    public Publisher<PatientDeprecates> getConverseObservable() {
        return toPublisher(just(converse));
    }

    @NotNull
    @XmlTransient
    public Publisher<Patient> getDeprecatorObservable() {
        return toPublisher(just(deprecator));
    }

    @Override
    public String toString() {
        return String.format("PatientDeprecatedBy{converse=%s, deprecator=%s}",
                converse.getId(), deprecator.getId());
    }
}
