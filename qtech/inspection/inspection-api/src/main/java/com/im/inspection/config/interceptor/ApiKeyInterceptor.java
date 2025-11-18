package com.im.inspection.config.interceptor;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/01/09 13:44:08
 * desc   :
 */

// @Component
// public class ApiKeyInterceptor implements HandlerInterceptor {
//
//     // @Autowired
//     private ApiKeyValidationService apiKeyValidationService;
//
//     @Override
//     public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//         // 从请求头中获取 API Key
//         String apiKey = request.getHeader("Authorization");
//         if (apiKey == null || !apiKey.startsWith("Bearer ")) {
//             response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//             response.getWriter().write("API Key is missing or invalid");
//             return false;
//         }
//
//         apiKey = apiKey.substring(7);  // 去掉 "Bearer " 前缀
//
//         // 验证 API Key
//         boolean isValid = apiKeyValidationService.validateApiKey(apiKey);
//         if (!isValid) {
//             response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//             response.getWriter().write("Invalid API Key");
//             return false;
//         }
//         return true;  // 验证通过，继续执行
//     }
// }
