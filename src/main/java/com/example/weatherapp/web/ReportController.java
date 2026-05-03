package com.example.weatherapp.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ReportController {

    public ReportController() {
    }

    @GetMapping("/report")
    public String report(@RequestParam("lat") double lat,
                         @RequestParam("lon") double lon,
                         @RequestParam(value = "city", required = false) String city,
                         Model model) {
        if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
            model.addAttribute("error", "Invalid coordinates.");
            return "report";
        }

        model.addAttribute("city", city);
        model.addAttribute("lat", lat);
        model.addAttribute("lon", lon);
        return "report";
    }
}