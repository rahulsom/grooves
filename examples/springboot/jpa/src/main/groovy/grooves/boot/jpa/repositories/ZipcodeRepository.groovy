package grooves.boot.jpa.repositories

import grooves.boot.jpa.domain.Zipcode
import org.springframework.data.jpa.repository.JpaRepository

interface ZipcodeRepository extends JpaRepository<Zipcode, Long> {
}
