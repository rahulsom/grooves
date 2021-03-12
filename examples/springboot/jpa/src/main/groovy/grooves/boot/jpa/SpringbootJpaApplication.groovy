package grooves.boot.jpa

import groovy.transform.CompileStatic
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
@CompileStatic
class SpringbootJpaApplication {

    static void main(String[] args) {
        SpringApplication.run SpringbootJpaApplication, args
    }
}
