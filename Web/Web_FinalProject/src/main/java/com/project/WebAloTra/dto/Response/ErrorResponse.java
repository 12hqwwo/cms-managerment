package com.project.WebAloTra.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String message;
    private String error;
    private long timestamp;

    public ErrorResponse(String message) {
        this.message = message;
        this.error = "Error";
        this.timestamp = System.currentTimeMillis();
    }

    public ErrorResponse(String message, String error) {
        this.message = message;
        this.error = error;
        this.timestamp = System.currentTimeMillis();
    }
}
