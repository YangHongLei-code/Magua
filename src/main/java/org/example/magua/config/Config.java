package org.example.magua.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: yhl
 * @CreateTime: 2026-08-20  14:49
 * @Description: TODO
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Config {
    @JsonProperty("api_key")
    private String apiKey;


}
