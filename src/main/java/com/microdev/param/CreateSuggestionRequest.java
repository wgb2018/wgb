package com.microdev.param;

import lombok.Data;

@Data
public class CreateSuggestionRequest {
    private String userId;
    private String suggestionContent;
}
