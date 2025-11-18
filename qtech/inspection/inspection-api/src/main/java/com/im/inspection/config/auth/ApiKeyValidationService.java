package com.im.inspection.config.auth;

// @Service
// public class ApiKeyValidationService {
//     private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ApiKeyValidationService.class);
//
//     // @Autowired
//     private RestTemplate restTemplate;
//
//     @Value("${spring.profiles.active}")
//     private String env;
//
//     public boolean validateApiKey(String apiKey) {
//         if (apiKey == null || apiKey.trim().isEmpty()) {
//             throw new IllegalArgumentException("API Key cannot be null or empty");
//         }
//
//         String authServiceUrl = null;
//         if ("dev".equals(env)) {
//             authServiceUrl = "http://localhost:8077/im/auth/validate";
//         } else if ("prod".equals(env)) {
//             authServiceUrl = "http://your-auth-service/im/auth/xx";
//         } else {
//             throw new RuntimeException("Invalid environment: " + env);
//         }
//
//         HttpHeaders headers = new HttpHeaders();
//         headers.set("Authorization", "Bearer " + apiKey);
//
//         HttpEntity<String> entity = new HttpEntity<>(headers);
//         try {
//             ResponseEntity<Boolean> response = restTemplate.exchange(authServiceUrl, HttpMethod.POST, entity, Boolean.class);
//             return Optional.ofNullable(response.getBody()).orElse(false);
//         } catch (RestClientException e) {
//             // Log the exception details
//             logger.error(">>>>> Error validating API key", e);
//             throw new RuntimeException("Failed to validate API key", e);
//         }
//     }
// }