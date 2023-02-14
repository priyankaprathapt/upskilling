package com.springsecurity.SpringSecurity.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class Security {
	
	@GetMapping("/greet")
	public String get() {
		
		return "welcome to ust";
	}

}
