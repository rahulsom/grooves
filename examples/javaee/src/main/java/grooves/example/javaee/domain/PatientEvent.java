package grooves.example.javaee.domain;

import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.events.RevertEvent;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

import javax.xml.bind.annotation.XmlTransient;
import java.util.Date;

import static rx.Observable.empty;
import static rx.Observable.just;
import static rx.RxReactiveStreams.toPublisher;

// tag::documented[]

public abstract class PatientEvent implements BaseEvent<Long, Patient, Long, PatientEvent> { // <1>
    @Getter @Setter private Patient aggregate;
    @Getter @Setter private Long id;
    @Getter @Setter
    private RevertEvent<Long, Patient, Long, PatientEvent> revertedBy;  // <2>
    @Getter @Setter private Date timestamp; // <3>
    @Getter @Setter private Long position; // <4>

    // end::documented[]
    @XmlTransient
    // tag::documented[]
    @Override
    @NotNull
    public Publisher<Patient> getAggregateObservable() { // <5>
        return toPublisher(aggregate != null ? just(aggregate) : empty());
    }
}
// end::documented[]
