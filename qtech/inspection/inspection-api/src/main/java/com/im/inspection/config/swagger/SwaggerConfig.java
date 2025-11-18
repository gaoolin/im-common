package com.im.inspection.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger Bean 访问：ip:port/swagger-ui.html
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2023/07/14 14:30:40
 */

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Api Documentation")
                        .version("1.0")
                        .description("Api Documentation")
                        .termsOfService("urn:tos")
                        .contact(new Contact().name("").email("").url(""))
                        .license(new License().name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0")));
    }
}
