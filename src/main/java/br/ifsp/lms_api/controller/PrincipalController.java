package br.ifsp.lms_api.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Validated
@RestController
@RequestMapping("/")
public class PrincipalController {

    @GetMapping
    public String getMethodName() {
        return "Bem vindos ao LMS API";
    }


}
