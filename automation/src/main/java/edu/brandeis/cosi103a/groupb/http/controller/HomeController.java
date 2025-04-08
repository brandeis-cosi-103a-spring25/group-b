package edu.brandeis.cosi103a.groupb.http.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    @GetMapping("/")
    @ResponseBody
    public String home() {
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head>" +
               "  <title>ATG Game API</title>" +
               "  <style>" +
               "    body { font-family: Arial, sans-serif; margin: 0; padding: 20px; line-height: 1.6; }" +
               "    h1 { color: #333; }" +
               "    ul { list-style-type: none; padding: 0; }" +
               "    li { margin-bottom: 10px; }" +
               "    code { background-color: #f4f4f4; padding: 3px; border-radius: 3px; }" +
               "    .method { display: inline-block; padding: 3px 6px; border-radius: 3px; color: white; font-weight: bold; margin-right: 5px; }" +
               "    .get { background-color: #61affe; }" +
               "    .post { background-color: #49cc90; }" +
               "    .delete { background-color: #f93e3e; }" +
               "  </style>" +
               "</head>" +
               "<body>" +
               "  <h1>ATG Game API</h1>" +
               "  <p>Welcome to the ATG Game API. Available endpoints:</p>" +
               "  <ul>" +
               "    <li><span class='method get'>GET</span> <code>/api/players</code> - List available player types</li>" +
               "    <li><span class='method get'>GET</span> <code>/api/games</code> - List all games</li>" +
               "    <li><span class='method get'>GET</span> <code>/api/games/{id}</code> - Get specific game details</li>" +
               "    <li><span class='method post'>POST</span> <code>/api/games</code> - Create a new game</li>" +
               "    <li><span class='method delete'>DELETE</span> <code>/api/games/{id}</code> - Delete a game</li>" +
               "  </ul>" +
               "</body>" +
               "</html>";
    }
}