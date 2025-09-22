# HttpKit HTTP客户端工具类使用文档

## 1. 概述

[HttpKit](file://E:\dossier\others\im-common\src\main\java\com\qtech\im\util\HttpKit.java#L30-L582) 是一个专业的HTTP客户端工具类，提供了完整且易用的HTTP通信功能。该工具类支持所有标准HTTP方法，具备连接池管理、异步请求、重试机制、GZIP压缩处理等高级特性，能够满足各种复杂的HTTP通信需求。

## 2. 核心特性

- **通用化**：支持GET、POST、PUT、DELETE等所有HTTP方法
- **规范化**：统一的API接口和响应处理
- **灵活性**：支持自定义请求头、参数、超时等配置
- **复用性**：连接池管理，支持并发请求
- **容错性**：完善的异常处理和重试机制
- **可靠性**：支持HTTPS、GZIP压缩、连接复用等
- **专业性**：遵循HTTP协议规范和最佳实践

## 3. 基本配置

### 3.1 默认配置常量
```java
// 超时配置
public static final int DEFAULT_CONNECT_TIMEOUT = 5000; // 连接超时5秒
public static final int DEFAULT_READ_TIMEOUT = 10000;   // 读取超时10秒

// 重试配置
public static final int DEFAULT_MAX_RETRIES = 3;        // 默认重试次数
public static final long DEFAULT_RETRY_INTERVAL = 1000; // 重试间隔1秒

// 默认请求头
public static final String DEFAULT_USER_AGENT = "HttpKit/1.0";
public static final String DEFAULT_CONTENT_TYPE = "application/json; charset=UTF-8";
```


## 4. 核心类介绍

### 4.1 HttpRequest - HTTP请求对象
用于构建和配置HTTP请求。

```java
// 创建GET请求
HttpRequest request = new HttpRequest(HttpMethod.GET, "https://api.example.com/users");

// 创建POST请求
HttpRequest request = new HttpRequest(HttpMethod.POST, "https://api.example.com/users");
```


#### 主要方法：
```java
// 设置请求方法
request.setMethod(HttpMethod.POST);

// 设置URL
request.setUrl("https://api.example.com/users");

// 添加请求头
request.addHeader("Authorization", "Bearer token123");

// 添加查询参数
request.addQueryParam("page", "1");
request.addQueryParam("size", "10");

// 设置请求体
request.setBody("{\"name\":\"John\",\"email\":\"john@example.com\"}");

// 设置内容类型
request.setContentType("application/json");

// 设置超时时间
request.setConnectTimeout(3000);
request.setReadTimeout(5000);
```


### 4.2 HttpResponse - HTTP响应对象
封装HTTP响应结果。

```java
// 检查请求是否成功
if (response.isSuccess()) {
    System.out.println("请求成功");
}

// 获取状态码
int statusCode = response.getStatusCode(); // 200, 404, 500等

// 获取状态消息
String statusMessage = response.getStatusMessage(); // "OK", "Not Found"等

// 获取响应体
String body = response.getBody();

// 获取响应头
String contentType = response.getHeader("Content-Type");
List<String> setCookies = response.getHeaders("Set-Cookie");
```


### 4.3 HttpMethod - HTTP方法枚举
```java
public enum HttpMethod {
    GET, POST, PUT, DELETE, HEAD, OPTIONS, PATCH
}
```


## 5. 基本使用方法

### 5.1 获取HttpKit实例
```java
// 获取全局单例实例
HttpKit httpKit = HttpKit.getInstance();
```


### 5.2 同步请求
```java
HttpKit httpKit = HttpKit.getInstance();

// 简单GET请求
HttpResponse response = httpKit.get("https://api.example.com/users");

// GET请求带查询参数
Map<String, String> queryParams = new HashMap<>();
queryParams.put("page", "1");
queryParams.put("size", "10");
HttpResponse response = httpKit.get("https://api.example.com/users", queryParams);

// POST请求发送JSON数据
String jsonData = "{\"name\":\"John\",\"email\":\"john@example.com\"}";
HttpResponse response = httpKit.post("https://api.example.com/users", jsonData);

// POST表单数据
Map<String, String> formData = new HashMap<>();
formData.put("username", "john");
formData.put("password", "secret");
HttpResponse response = httpKit.postForm("https://api.example.com/login", formData);

// PUT请求
String updateData = "{\"name\":\"John Doe\"}";
HttpResponse response = httpKit.put("https://api.example.com/users/123", updateData);

// DELETE请求
HttpResponse response = httpKit.delete("https://api.example.com/users/123");
```


### 5.3 高级请求配置
```java
// 构建自定义请求
HttpRequest request = new HttpRequest(HttpMethod.POST, "https://api.example.com/users");

// 设置请求头
request.addHeader("Authorization", "Bearer your-token-here");
request.addHeader("X-API-Key", "your-api-key");

// 设置查询参数
request.addQueryParam("timestamp", String.valueOf(System.currentTimeMillis()));

// 设置请求体
request.setBody("{\"name\":\"John\",\"email\":\"john@example.com\"}");

// 设置内容类型
request.setContentType("application/json");

// 设置超时时间
request.setConnectTimeout(3000); // 3秒连接超时
request.setReadTimeout(10000);   // 10秒读取超时

// 执行请求
HttpResponse response = httpKit.execute(request);
```


## 6. 异步请求

### 6.1 使用回调方式
```java
HttpKit httpKit = HttpKit.getInstance();

// 创建请求
HttpRequest request = new HttpRequest(HttpMethod.GET, "https://api.example.com/users");

// 异步执行
httpKit.executeAsync(request, new HttpKit.HttpCallback() {
    @Override
    public void onSuccess(HttpKit.HttpResponse response) {
        if (response.isSuccess()) {
            System.out.println("请求成功: " + response.getBody());
        } else {
            System.out.println("请求失败: " + response.getStatusCode());
        }
    }
    
    @Override
    public void onError(Exception error) {
        System.err.println("请求出错: " + error.getMessage());
        error.printStackTrace();
    }
});
```


### 6.2 使用Lambda表达式（Java 8+）
```java
HttpKit httpKit = HttpKit.getInstance();
HttpRequest request = new HttpRequest(HttpMethod.GET, "https://api.example.com/users");

// 使用Lambda表达式
httpKit.executeAsync(request, new HttpKit.HttpCallback() {
    @Override
    public void onSuccess(HttpKit.HttpResponse response) {
        System.out.println("Success: " + response.getBody());
    }
    
    @Override
    public void onError(Exception error) {
        System.err.println("Error: " + error.getMessage());
    }
});
```


## 7. 重试机制

### 7.1 默认重试
```java
// 使用默认重试配置（3次重试）
HttpResponse response = httpKit.execute(request);
```


### 7.2 自定义重试次数
```java
// 设置自定义重试次数
HttpResponse response = httpKit.execute(request, 5); // 最多重试5次
```


## 8. 错误处理

### 8.1 HttpException处理
```java
try {
    HttpResponse response = httpKit.execute(request);
    if (response.isSuccess()) {
        System.out.println("Response: " + response.getBody());
    } else {
        System.out.println("HTTP Error: " + response.getStatusCode() + " " + response.getStatusMessage());
    }
} catch (HttpKit.HttpException e) {
    System.err.println("Request failed: " + e.getMessage());
    e.printStackTrace();
}
```


### 8.2 响应状态检查
```java
HttpResponse response = httpKit.execute(request);

// 检查是否成功
if (response.isSuccess()) {
    // 处理成功响应
    processSuccessResponse(response);
} else if (response.getStatusCode() >= 400 && response.getStatusCode() < 500) {
    // 客户端错误
    handleClientError(response);
} else if (response.getStatusCode() >= 500) {
    // 服务器错误
    handleServerError(response);
}
```


## 9. 实际使用示例

### 9.1 REST API调用示例
```java
public class ApiService {
    private final HttpKit httpKit = HttpKit.getInstance();
    
    // 获取用户信息
    public User getUser(String userId) throws HttpKit.HttpException {
        String url = "https://api.example.com/users/" + userId;
        HttpResponse response = httpKit.get(url);
        
        if (response.isSuccess()) {
            // 解析JSON响应
            return JsonMapperProvider.getSharedInstance()
                    .readValue(response.getBody(), User.class);
        } else {
            throw new HttpKit.HttpException("Failed to get user: " + response.getStatusCode());
        }
    }
    
    // 创建用户
    public User createUser(User user) throws HttpKit.HttpException {
        try {
            String url = "https://api.example.com/users";
            String jsonBody = JsonMapperProvider.getSharedInstance().writeValueAsString(user);
            
            HttpRequest request = new HttpRequest(HttpKit.HttpMethod.POST, url);
            request.setBody(jsonBody);
            request.addHeader("Content-Type", "application/json");
            request.addHeader("Authorization", "Bearer " + getAuthToken());
            
            HttpResponse response = httpKit.execute(request);
            
            if (response.isSuccess()) {
                return JsonMapperProvider.getSharedInstance()
                        .readValue(response.getBody(), User.class);
            } else {
                throw new HttpKit.HttpException("Failed to create user: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new HttpKit.HttpException("Error creating user", e);
        }
    }
}
```


### 9.2 文件上传示例
```java
public class FileUploadService {
    private final HttpKit httpKit = HttpKit.getInstance();
    
    public void uploadFile(String filePath, String uploadUrl) throws HttpKit.HttpException {
        try {
            // 读取文件内容
            String fileContent = readFileContent(filePath);
            
            // 创建请求
            HttpRequest request = new HttpRequest(HttpKit.HttpMethod.POST, uploadUrl);
            request.setBody(fileContent);
            request.addHeader("Content-Type", "application/octet-stream");
            
            // 执行上传
            HttpResponse response = httpKit.execute(request);
            
            if (!response.isSuccess()) {
                throw new HttpKit.HttpException("Upload failed: " + response.getStatusCode());
            }
            
            System.out.println("File uploaded successfully");
        } catch (IOException e) {
            throw new HttpKit.HttpException("Failed to read file", e);
        }
    }
    
    private String readFileContent(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
    }
}
```


### 9.3 批量请求示例
```java
public class BatchRequestService {
    private final HttpKit httpKit = HttpKit.getInstance();
    
    public List<HttpResponse> executeBatchRequests(List<HttpRequest> requests) {
        List<HttpResponse> responses = new ArrayList<>();
        
        for (HttpRequest request : requests) {
            try {
                HttpResponse response = httpKit.execute(request);
                responses.add(response);
            } catch (HttpKit.HttpException e) {
                // 记录错误但继续处理其他请求
                System.err.println("Request failed: " + e.getMessage());
                responses.add(null); // 或创建错误响应对象
            }
        }
        
        return responses;
    }
}
```


## 10. 最佳实践

### 10.1 资源管理
```java
// 通常使用单例模式，无需手动关闭
// 但在应用关闭时可以调用
HttpKit.getInstance().close(); // 关闭线程池等资源
```


### 10.2 性能优化建议
1. **复用HttpKit实例**：使用全局单例避免重复创建
2. **合理设置超时**：根据网络环境和业务需求设置合适的超时时间
3. **使用连接池**：HttpKit内置连接池管理，支持并发请求
4. **启用GZIP压缩**：自动处理GZIP压缩响应

### 10.3 安全建议
1. **HTTPS优先**：优先使用HTTPS协议
2. **敏感信息保护**：避免在日志中输出敏感信息如密码、token等
3. **证书验证**：生产环境应使用标准证书验证而非跳过验证

### 10.4 错误处理建议
1. **区分错误类型**：区分网络错误、HTTP错误和业务错误
2. **重试策略**：对5xx错误进行重试，对4xx错误通常不重试
3. **日志记录**：详细记录请求和响应信息便于问题排查

这个文档涵盖了 [HttpKit](file://E:\dossier\others\im-common\src\main\java\com\qtech\im\util\HttpKit.java#L30-L582) HTTP客户端工具类的主要功能和使用方法，可以帮助开发者快速上手并正确使用该工具类。