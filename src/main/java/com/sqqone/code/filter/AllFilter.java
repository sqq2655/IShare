package com.sqqone.code.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.*;
import java.io.IOException;

/**
 * @author sqq
 * @version 1.0
 * @date 2020/4/17 16:21
 */
@Configuration
public class AllFilter implements Filter {

    @Bean
    public FilterRegistrationBean<AllFilter> getAllFilter(){
        FilterRegistrationBean<AllFilter> frb = new FilterRegistrationBean<>();
        frb.setFilter(new AllFilter());
        frb.addUrlPatterns("/*");
        return frb;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        filterChain.doFilter(servletRequest,servletResponse);
    }

    @Override
    public void destroy() {

    }
}
