package com.focalstock.web.home;

import java.util.Map;

import com.focalstock.web.forms.Ticker;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
//import org.json.JSONException;
//import org.json.JSONObject;
import org.springframework.boot.json.JacksonJsonParser;

@Controller
public class HomeController {

  private final RestTemplate restTemplate;
  private final JacksonJsonParser parser;

  private static final String token = "pk_3b0d1fa40bd248e1a9af5517d84ab0a6";

  public HomeController(final RestTemplateBuilder restTemplateBuilder) {
    this.restTemplate = restTemplateBuilder.build();
    parser = new JacksonJsonParser();
  }

  @GetMapping("/")
  public String home(Model model) {
    model.addAttribute("ticker", new Ticker());
    return "home";
  }

  @PostMapping("/")
  public String tickerSubmit(@ModelAttribute Ticker ticker, Model model) {
    String response = "";
    try {
      response = restTemplate.getForObject("https://cloud.iexapis.com/beta/stock/{ticker}/company?token={token}", String.class, ticker.getTicker(), token);
      final Map<String, Object> obj = parser.parseMap(response);
      model.addAttribute("company", obj.get("companyName").toString());
      model.addAttribute("website", obj.get("website").toString());
    } catch (HttpClientErrorException e) {
      response = e.getMessage();
    }
    System.out.println(response);
    return "info";
  }

}
