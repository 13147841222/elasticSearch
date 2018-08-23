package com.elasticsearch.demo.config;

import com.elasticsearch.demo.security.AuthProvider;
import com.elasticsearch.demo.security.LoginAuthFailHandler;
import com.elasticsearch.demo.security.LoginUrlEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * @author zhumingli
 * @create 2018-08-22 下午10:59
 * @desc
 **/
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * @param http
     * @throws Exception
     * @dscp  HTTP权限控制
     *
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 资源访问权限
        http.authorizeRequests()
                // 管理员登陆入口
                .antMatchers("/admin/login").permitAll()
                // 静态资源
                .antMatchers("/static/**").permitAll()
                // 用户登陆
                .antMatchers("/user/login").permitAll()
                //
                .antMatchers("/admin/**").hasRole("ADMIN")
                //
                .antMatchers("/user/**").hasAnyRole("ADMIN","USER")
                //
                .antMatchers("/api/user/**").hasAnyRole("ADMIN","USER")
                //
                .and()
                .formLogin()
                //配置角色登陆处理入口
                .loginProcessingUrl("/login")
                //失败处理
                .failureHandler(authFailHandler())
                .and()
                //配置角色登出处理入口
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/logout/page")
                //删除 cookie
                .deleteCookies("JSESSIONID").
                invalidateHttpSession(true)
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(loginUrlEntryPoint())
                .accessDeniedPage("/403")

        ;

        http.csrf().disable();
        http.headers().frameOptions().sameOrigin();
    }


    /**
     *自定义认证策略
     */
    @Autowired
    public void configGlobal(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
//        authenticationManagerBuilder.inMemoryAuthentication().withUser("admin").password("admin").roles("ADMIN").and();
        authenticationManagerBuilder.authenticationProvider(authProvider()).eraseCredentials(true);
    }

    @Bean
    public AuthProvider authProvider(){
        return new AuthProvider();
    }


    @Bean
    public LoginUrlEntryPoint loginUrlEntryPoint(){
        return new LoginUrlEntryPoint("/user/login");
    }

    @Bean
    public LoginAuthFailHandler authFailHandler(){
        return new LoginAuthFailHandler(loginUrlEntryPoint());
    }
}
