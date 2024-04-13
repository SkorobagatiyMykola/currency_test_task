package ua.skorobahatyi.currency_app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/v1/test")
public class HealthController {
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> checkApp(){
        String message ="{\"status\": \"UP\", \"mood\": \"cool\",\"code\":200}";

        return new ResponseEntity<>(message,HttpStatus.OK);
    }

}
