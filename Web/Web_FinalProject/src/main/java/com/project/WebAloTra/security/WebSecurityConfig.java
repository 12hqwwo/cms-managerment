package com.project.WebAloTra.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public HttpFirewall allowDoubleSlashHttpFirewall() {
		StrictHttpFirewall firewall = new StrictHttpFirewall();
		firewall.setAllowUrlEncodedDoubleSlash(true);
		firewall.setAllowSemicolon(true); // (tùy chọn, tránh lỗi khi URL có ;)
		return firewall;
	}

	@Configuration
	public static class AppConfiguration {
		@Bean
		public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationSuccessHandler successHandler) throws Exception {
			http
					// ✅ Bật CORS trước
					.cors().configurationSource(request -> {
						var cors = new org.springframework.web.cors.CorsConfiguration();
						cors.setAllowedOriginPatterns(java.util.List.of("http://localhost:8080", // nếu FE chạy chung
																									// host với BE
								"http://127.0.0.1:8080", "http://localhost:5173" // nếu bạn chạy Vite/React local FE
						));
						cors.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
						cors.setAllowedHeaders(java.util.List.of("*"));
						cors.setAllowCredentials(true); // ✅ cho phép gửi cookie/token
						return cors;
					}).and()

					// Tắt CSRF
					.csrf().disable().authorizeRequests()
					// Tài nguyên tĩnh, public pages
					.antMatchers("/css/**", "/js/**", "/images/**", "/vendor/**", "/plugins/**", "/webjars/**",
							"/favicon.ico", "/error")
					.permitAll().antMatchers("/", "/home/**", "/product/**", "/about/**", "/contact/**").permitAll()
					.antMatchers("/admin/vendors/**", "/admin/assets/**", "/vendors/**", "/assets/**", "/css/**",
							"/js/**", "/images/**", "/webjars/**")
					.permitAll()

					// Quyền của User và Vendor (User chức năng cơ bản, Vendor có thêm các chức năng
					// shop)
					.antMatchers("/profile/**", "/orders/**", "/checkout/**", "/comment/**", "/discount/**",
							"/favorite/**", "/cart/**", "/payment/**", "/history/**")
					.hasAnyRole("USER", "STAFF", "VENDOR", "ADMIN")

					// Chức năng admin & vendor dùng chung (shop, sản phẩm, đơn hàng, hóa đơn, doanh
					// thu, size, brand, bill-return, discount)
					.antMatchers("/admin/thong-ke-doanh-thu", "/admin/bill-list", "/admin/chi-tiet-san-pham/**",
							"/admin/product/**", "/admin/product-all", "/admin/product-create", "/admin/brand-all",
							"/admin/brand-create", "/admin/brand-detail/**", "/admin/size-all", "/admin/size-create",
							"/admin/size-detail/**", "/admin/color-list", "/admin/color-create", "/admin/edit-color/**",
							"/admin/pos", "/admin/getbill-detail/**", "/admin/export-bill", "/admin/generate-pdf/**", "/admin/export-pdf/**", "/admin/api/product/**/bill", "/admin-only/bill-return",
							"/admin-only/bill-return-create", "/admin-only/bill-return-detail/**",
							"/admin-only/product-discount", "/admin-only/product-discount-create",
							"/admin/chat", "/admin/api/chat/**")
					.hasAnyRole("STAFF", "VENDOR", "ADMIN")

					.antMatchers("/api/orderAdmin").hasAnyRole("STAFF", "VENDOR", "ADMIN")

					// Các chức năng chỉ dành cho ADMIN (quản lý user, danh mục, vận chuyển, chiết
					// khấu app, giảm phí vận chuyển,...)
					.antMatchers("/admin/**", "/management/**", "/system/**").hasRole("ADMIN")

					// Các request khác thì cho phép (để login form không bị chặn)
					.anyRequest().permitAll()

					.and().formLogin().loginPage("/user-login") // trang login
					.loginProcessingUrl("/user_login") // action form login
					.usernameParameter("email") // dùng email làm username
					.successHandler(successHandler) // chuyển hướng theo vai trò
					.permitAll().and().logout().logoutUrl("/user_logout").logoutSuccessUrl("/").permitAll().and()
					.rememberMe().key("AbcDefgHijklmnOp_123456789").rememberMeParameter("remember-me")
					.tokenValiditySeconds(7 * 24 * 60 * 60);

			http.headers().frameOptions().disable();
			return http.build();
		}

		@Bean
		public AuthenticationSuccessHandler successHandler() {
			return (request, response, authentication) -> {
				java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities = authentication.getAuthorities();
				String redirectUrl = "/";
				for (org.springframework.security.core.GrantedAuthority authority : authorities) {
					String role = authority.getAuthority();
					if (role.equals("ROLE_ADMIN")) {
						redirectUrl = "/admin/thong-ke-doanh-thu";
						break;
					} else if (role.equals("ROLE_VENDOR")) {
						redirectUrl = "/admin/thong-ke-doanh-thu";
						break;
					} else if (role.equals("ROLE_STAFF")) {
						redirectUrl = "/admin/pos";
						break;
					}
				}
				response.sendRedirect(redirectUrl);
			};
		}

		@Bean
		public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
			return authConfig.getAuthenticationManager();
		}

		@Bean
        public WebSecurityCustomizer webSecurityCustomizer(HttpFirewall firewall) {
            return (web) -> web
                    .httpFirewall(firewall)
                    .ignoring()
                    .antMatchers("/img/**", "/js/**", "/css/**", "/fonts/**", "/plugins/**",
                            "/vendor/**", "/static/**", "/webjars/**", "/images/**", "/favicon.ico",
                            "/error", "/uploads/**", "/upload-barcode/**");
        }
	}
}