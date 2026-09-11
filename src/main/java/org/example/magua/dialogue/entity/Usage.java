package org.example.magua.dialogue.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Usage {

    //返回的token数
    @JsonProperty("completion_tokens")
    private int completionTokens;

    //请求的token数
    @JsonProperty("prompt_tokens")
    private int promptTokens;

    //缓存命中的token数
    @JsonProperty("prompt_cache_hit_tokens")
    private int promptCacheHitTokens;

    //缓存未命中的token数
    @JsonProperty("prompt_cache_miss_tokens")
    private int promptCacheMissTokens;

    //该次所有token数
    @JsonProperty("total_tokens")
    private int totalTokens;

    //思维链的token数（明细对象）
    @JsonProperty("completion_tokens_details")
    private CompletionTokensDetails completionTokensDetails = new CompletionTokensDetails();

    @Data
    public  class CompletionTokensDetails {
        //思维链的token数
        @JsonProperty("reasoning_tokens")
        private int reasoningTokens;
    }

}
