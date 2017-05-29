package grooves.example.springboot.kotlin

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class SpringbootKotlinApplication

fun main(args: Array<String>) {
    SpringApplication.run(SpringbootKotlinApplication::class.java, *args)
}
