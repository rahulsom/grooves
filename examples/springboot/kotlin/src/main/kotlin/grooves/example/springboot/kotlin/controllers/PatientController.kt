package grooves.example.springboot.kotlin.controllers

import grooves.example.springboot.kotlin.domain.Patient
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import rx.Observable

@Controller
class PatientController {

    @GetMapping("/patient.json")
    fun patient() = Observable.empty<Patient>()

}