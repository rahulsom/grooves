package grooves.example.javaee.domain;

import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.events.RevertEvent;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

import javax.xml.bind.annotation.XmlTransient;
import java.util.Date;

import static rx.Observable.empty;
import static rx.Observable.just;
import static rx.RxReactiveStreams.toPublisher;

// tag::documented[]
@Data
public abstract class PatientEvent implements BaseEvent<Patient, Long, PatientEvent> { // <1>
    private Patient aggregate;
    private Long id;
    private RevertEvent<Patient, Long, PatientEvent> revertedBy;  // <2>
    private Date timestamp; // <3>
    private long position; // <4>

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
