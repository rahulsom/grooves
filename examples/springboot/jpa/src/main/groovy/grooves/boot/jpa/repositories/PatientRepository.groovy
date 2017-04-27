package grooves.boot.jpa.repositories

import grooves.boot.jpa.domain.Patient
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Jpa backed repository for the Patient Aggregate
 *
 * @author Rahul Somasunderam
 */
interface PatientRepository extends JpaRepository<Patient, Long> {
}
