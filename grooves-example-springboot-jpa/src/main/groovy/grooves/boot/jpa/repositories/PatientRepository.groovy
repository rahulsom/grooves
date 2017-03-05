package grooves.boot.jpa.repositories

import grooves.boot.jpa.domain.Patient
import org.springframework.data.jpa.repository.JpaRepository

interface PatientRepository extends JpaRepository<Patient, Long> {
}
