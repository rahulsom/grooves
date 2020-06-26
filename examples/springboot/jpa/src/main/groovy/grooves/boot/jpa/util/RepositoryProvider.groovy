package grooves.boot.jpa.util

import grooves.boot.jpa.repositories.ZipcodeRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class RepositoryProvider {
    static ZipcodeRepository zipcodeRepository

    @Autowired
    RepositoryProvider(ZipcodeRepository zipcodeRepository) {
        RepositoryProvider.zipcodeRepository = zipcodeRepository
    }
}
