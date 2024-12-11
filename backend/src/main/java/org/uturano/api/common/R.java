package org.uturano.api.common;

import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class R {
    private Boolean status = false;
    private String message;
    private Map<String,Object> result = new HashMap<>();
    private Map<String, String> testResults = new HashMap<>();
}
