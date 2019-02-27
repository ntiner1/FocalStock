package com.focalstock.web.home;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.focalstock.web.forms.Ticker;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.json.JsonParseException;

@Controller
public class HomeController {

  private final RestTemplate restTemplate;
  private final JacksonJsonParser parser;

  private static final String token = "pk_3b0d1fa40bd248e1a9af5517d84ab0a6";
  private static final String iexUrl = "https://cloud.iexapis.com/beta/stock/";
  private static final String historicalLength = "1d";

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
    try {
      final String response = restTemplate.getForObject(iexUrl + "{ticker}/company?token={token}", String.class, ticker.getTicker(), token);
      final Map<String, Object> tickerInfo = parser.parseMap(response);
      model.addAttribute("company", tickerInfo.get("companyName").toString());
      model.addAttribute("website", tickerInfo.get("website").toString());
      model.addAttribute("latestPrice", getLatestStockPrice(ticker.getTicker()));
      //List<String> historicalPrices = getHistoricalPrices(ticker.getTicker(), historicalLength);
      //System.out.println(historicalPrices);
    } catch (HttpClientErrorException e) {
      System.out.println("[ERROR] Company info: " + e.getMessage());
    }
    return "info";
  }

  private String getLatestStockPrice(final String ticker) {
    String latestPrice = "Not Listed";
    try {
      final String response = restTemplate.getForObject(iexUrl + "{ticker}/quote?token={token}", String.class, ticker, token);
      final Map<String, Object> stockPriceInfo = parser.parseMap(response);
      latestPrice = stockPriceInfo.get("latestPrice").toString();
    } catch (HttpClientErrorException e) {
      System.out.println("[ERROR] Latest stock price: " + e.getMessage());
    }
    return latestPrice;
  }

  private List<String> getHistoricalPrices(final String ticker, final String length) {
    List<String> historicalPrices = new ArrayList<String>();
    try {
      final String response = restTemplate.getForObject(iexUrl + "{ticker}/chart/" + length + "?token={token}", String.class, ticker, token);
      final List<Object> priceList = parser.parseList(response);
      final ObjectMapper objectMapper = new ObjectMapper();
      historicalPrices = priceList.stream()
        .map(elem -> {
          String average = "";
          try {
            average = parser.parseMap(objectMapper.writeValueAsString(elem)).get("marketAverage").toString();
          } catch (JsonProcessingException e) {
            System.out.println("[ERROR] Historical stock prices json issue: " + e.getMessage());
          }
          return average;
        })
        .collect(Collectors.toList());
    } catch (HttpClientErrorException e) {
      System.out.println("[ERROR] Historical stock prices client issue: " + e.getMessage());
    } catch (JsonParseException e) {
      System.out.println("[ERROR] Historical stock prices json issue: " + e.getMessage());
    }
    return historicalPrices;
  }
}
