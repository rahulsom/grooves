package grooves.example.javaee.domain;

import static rx.Observable.just;
import static rx.RxReactiveStreams.toPublisher;

import com.github.rahulsom.grooves.api.events.Deprecates;
import javax.xml.bind.annotation.XmlTransient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

@EqualsAndHashCode(callSuper = true)
@Data
public class PatientDeprecates extends PatientEvent implements Deprecates<Patient, Long, PatientEvent> {
    @XmlTransient
    private final PatientDeprecatedBy converse;

    private final Patient deprecated;

    @NotNull
    @Override
    @XmlTransient
    public Publisher<PatientDeprecatedBy> getConverseObservable() {
        return toPublisher(just(converse));
    }

    @NotNull
    @Override
    @XmlTransient
    public Publisher<Patient> getDeprecatedObservable() {
        return toPublisher(just(deprecated));
    }

    @Override
    public String toString() {
        return String.format("PatientDeprecates{converse=%d, deprecated=%d}", converse.getId(), deprecated.getId());
    }
}
