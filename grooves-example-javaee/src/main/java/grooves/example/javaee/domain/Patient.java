package grooves.example.javaee.domain;

// tag::documented[]
import com.github.rahulsom.grooves.java.Aggregate;
import java.io.Serializable;
import lombok.Data;

@Data
@Aggregate // <1>
public class Patient implements Serializable {
    private Long id;
    private final String uniqueId;
}
// end::documented[]
