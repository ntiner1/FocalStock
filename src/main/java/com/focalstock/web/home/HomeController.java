package com.focalstock.web.home;

import com.focalstock.web.forms.Ticker;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class HomeController {

  @GetMapping("/")
  public String home(Model model) {
    model.addAttribute("ticker", new Ticker());
    return "home";
  }

  @PostMapping("/")
  public String tickerSubmit(@ModelAttribute Ticker ticker) {
    // Call ticker api
    return "info";
  }

}
