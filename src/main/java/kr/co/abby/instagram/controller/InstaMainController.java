package kr.co.abby.instagram.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class InstaMainController {

	@RequestMapping(value = "")
	public String home() {
		return "home.html";
		
	}
	
}
