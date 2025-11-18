package com.im.inspection.config.druid;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.alibaba.druid.spring.boot.autoconfigure.properties.DruidStatProperties;
import com.alibaba.druid.util.Utils;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.*;
import java.io.IOException;

/**
 * 去除druid连接池监控广告
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2023/10/17 15:35:52
 */

@Configuration
@ConditionalOnWebApplication
@AutoConfigureAfter(DruidDataSourceAutoConfigure.class)
@ConditionalOnProperty(name = "spring.datasource.druid.stat-view-servlet.enabled",
        havingValue = "true", matchIfMissing = true)
public class RemoveDruidAdConfig {

    /**
     * 方法名: removeDruidAdFilterRegistrationBean
     * 方法描述 除去页面底部的广告
     *
     * @param properties com.alibaba.druid.spring.boot.autoconfigure.properties.DruidStatProperties
     * @return org.springframework.boot.web.servlet.FilterRegistrationBean
     */
    @Bean
    public FilterRegistrationBean removeDruidAdFilterRegistrationBean(DruidStatProperties properties) {

        // 获取web监控页面的参数
        DruidStatProperties.StatViewServlet config = properties.getStatViewServlet();
        // 提取common.js的配置路径
        String pattern = config.getUrlPattern() != null ? config.getUrlPattern() : "/druid/*";
        String commonJsPattern = pattern.replaceAll("\\*", "js/common.js");

        final String filePath = "support/http/resources/js/common.js";

        // Preload and process the JS content once
        String processedJsContent;
        try {
            String originalJs = Utils.readFromResource(filePath);
            processedJsContent = removeAds(originalJs);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load or process Druid's common.js", e);
        }

        // Create filter using cached content
        Filter filter = new Filter() {
            @Override
            public void init(FilterConfig filterConfig) {
            }

            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
                chain.doFilter(request, response);
                // Reset buffer before writing custom content
                response.resetBuffer();
                response.getWriter().write(processedJsContent);
            }

            @Override
            public void destroy() {
            }
        };

        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns(commonJsPattern);
        return registrationBean;
    }

    private String removeAds(String jsContent) {
        // Remove banner ad
        jsContent = jsContent.replaceAll("<a.*?banner\"></a><br/>", "");
        // Remove powered-by footer
        jsContent = jsContent.replaceAll("powered.*?shrek.wang</a>", "");
        return jsContent;
    }
}
