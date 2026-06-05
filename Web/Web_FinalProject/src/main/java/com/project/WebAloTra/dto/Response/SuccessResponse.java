package com.project.WebAloTra.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SuccessResponse {
    private String message;
    private Object data;
    private long timestamp;

    public SuccessResponse(String message, Object data) {
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
}
