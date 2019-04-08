package grooves.example.javaee.domain;

// tag::documented[]
import com.github.rahulsom.grooves.java.Aggregate;
import lombok.Data;
import java.io.Serializable;

@Data
@Aggregate // <1>
public class Patient implements Serializable {
    private Long id;
    private final String uniqueId;
}
// end::documented[]
