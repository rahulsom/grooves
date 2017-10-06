package grooves.example.javaee.domain;

import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.events.RevertEvent;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import rx.Observable;

import javax.xml.bind.annotation.XmlTransient;
import java.util.Date;

// tag::documented[]
import static rx.Observable.empty;
import static rx.Observable.just;

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
    public Observable<Patient> getAggregateObservable() { // <5>
        return aggregate != null ? just(aggregate) : empty();
    }
}
// end::documented[]
