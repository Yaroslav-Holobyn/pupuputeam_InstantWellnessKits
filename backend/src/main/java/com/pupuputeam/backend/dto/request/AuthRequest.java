package com.pupuputeam.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AuthRequest {
    @JsonProperty("email")
    private String userEmail;
    private String password;
}
