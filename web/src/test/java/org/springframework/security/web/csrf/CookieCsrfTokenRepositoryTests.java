/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.security.web.csrf;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * @author Rob Winch
 * @since 4.1
 */
public class CookieCsrfTokenRepositoryTests {

	CookieCsrfTokenRepository repository;

	MockHttpServletResponse response;

	MockHttpServletRequest request;

	@BeforeEach
	public void setup() {
		this.repository = new CookieCsrfTokenRepository();
		this.request = new MockHttpServletRequest();
		this.response = new MockHttpServletResponse();
		this.request.setContextPath("/context");
	}

	@Test
	public void generateToken() {
		CsrfToken generateToken = this.repository.generateToken(this.request);
		assertThat(generateToken).isNotNull();
		assertThat(generateToken.getHeaderName()).isEqualTo(CookieCsrfTokenRepository.DEFAULT_CSRF_HEADER_NAME);
		assertThat(generateToken.getParameterName()).isEqualTo(CookieCsrfTokenRepository.DEFAULT_CSRF_PARAMETER_NAME);
		assertThat(generateToken.getToken()).isNotEmpty();
	}

	@Test
	public void generateTokenCustom() {
		String headerName = "headerName";
		String parameterName = "paramName";
		this.repository.setHeaderName(headerName);
		this.repository.setParameterName(parameterName);
		CsrfToken generateToken = this.repository.generateToken(this.request);
		assertThat(generateToken).isNotNull();
		assertThat(generateToken.getHeaderName()).isEqualTo(headerName);
		assertThat(generateToken.getParameterName()).isEqualTo(parameterName);
		assertThat(generateToken.getToken()).isNotEmpty();
	}

	@Test
	public void saveToken() {
		CsrfToken token = this.repository.generateToken(this.request);
		this.repository.saveToken(token, this.request, this.response);
		Cookie tokenCookie = this.response.getCookie(CookieCsrfTokenRepository.DEFAULT_CSRF_COOKIE_NAME);
		assertThat(tokenCookie.getMaxAge()).isEqualTo(-1);
		assertThat(tokenCookie.getName()).isEqualTo(CookieCsrfTokenRepository.DEFAULT_CSRF_COOKIE_NAME);
		assertThat(tokenCookie.getPath()).isEqualTo(this.request.getContextPath());
		assertThat(tokenCookie.getSecure()).isEqualTo(this.request.isSecure());
		assertThat(tokenCookie.getValue()).isEqualTo(token.getToken());
		assertThat(tokenCookie.isHttpOnly()).isEqualTo(true);
	}

	@Test
	public void saveTokenSecure() {
		this.request.setSecure(true);
		CsrfToken token = this.repository.generateToken(this.request);
		this.repository.saveToken(token, this.request, this.response);
		Cookie tokenCookie = this.response.getCookie(CookieCsrfTokenRepository.DEFAULT_CSRF_COOKIE_NAME);
		assertThat(tokenCookie.getSecure()).isTrue();
	}

	@Test
	public void saveTokenSecureFlagTrue() {
		this.request.setSecure(false);
		this.repository.setSecure(Boolean.TRUE);
		CsrfToken token = this.repository.generateToken(this.request);
		this.repository.saveToken(token, this.request, this.response);
		Cookie tokenCookie = this.response.getCookie(CookieCsrfTokenRepository.DEFAULT_CSRF_COOKIE_NAME);
		assertThat(tokenCookie.getSecure()).isTrue();
	}

	@Test
	public void saveTokenSecureFlagFalse() {
		this.request.setSecure(true);
		this.repository.setSecure(Boolean.FALSE);
		CsrfToken token = this.repository.generateToken(this.request);
		this.repository.saveToken(token, this.request, this.response);
		Cookie tokenCookie = this.response.getCookie(CookieCsrfTokenRepository.DEFAULT_CSRF_COOKIE_NAME);
		assertThat(tokenCookie.getSecure()).isFalse();
	}

	@Test
	public void saveTokenNull() {
		this.request.setSecure(true);
		this.repository.saveToken(null, this.request, this.response);
		Cookie tokenCookie = this.response.getCookie(CookieCsrfTokenRepository.DEFAULT_CSRF_COOKIE_NAME);
		assertThat(tokenCookie.getMaxAge()).isZero();
		assertThat(tokenCookie.getName()).isEqualTo(CookieCsrfTokenRepository.DEFAULT_CSRF_COOKIE_NAME);
		assertThat(tokenCookie.getPath()).isEqualTo(this.request.getContextPath());
		assertThat(tokenCookie.getSecure()).isEqualTo(this.request.isSecure());
		assertThat(tokenCookie.getValue()).isEmpty();
	}

	@Test
	public void saveTokenHttpOnlyTrue() {
		this.repository.setCookieHttpOnly(true);
		CsrfToken token = this.repository.generateToken(this.request);
		this.repository.saveToken(token, this.request, this.response);
		Cookie tokenCookie = this.response.getCookie(CookieCsrfTokenRepository.DEFAULT_CSRF_COOKIE_NAME);
		assertThat(tokenCookie.isHttpOnly()).isTrue();
	}

	@Test
	public void saveTokenHttpOnlyFalse() {
		this.repository.setCookieHttpOnly(false);
		CsrfToken token = this.repository.generateToken(this.request);
		this.repository.saveToken(token, this.request, this.response);
		Cookie tokenCookie = this.response.getCookie(CookieCsrfTokenRepository.DEFAULT_CSRF_COOKIE_NAME);
		assertThat(tokenCookie.isHttpOnly()).isFalse();
	}

	@Test
	public void saveTokenWithHttpOnlyFalse() {
		this.repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
		CsrfToken token = this.repository.generateToken(this.request);
		this.repository.saveToken(token, this.request, this.response);
		Cookie tokenCookie = this.response.getCookie(CookieCsrfTokenRepository.DEFAULT_CSRF_COOKIE_NAME);
		assertThat(tokenCookie.isHttpOnly()).isFalse();
	}

	@Test
	public void saveTokenCustomPath() {
		String customPath = "/custompath";
		this.repository.setCookiePath(customPath);
		CsrfToken token = this.repository.generateToken(this.request);
		this.repository.saveToken(token, this.request, this.response);
		Cookie tokenCookie = this.response.getCookie(CookieCsrfTokenRepository.DEFAULT_CSRF_COOKIE_NAME);
		assertThat(tokenCookie.getPath()).isEqualTo(this.repository.getCookiePath());
	}

	@Test
	public void saveTokenEmptyCustomPath() {
		String customPath = "";
		this.repository.setCookiePath(customPath);
		CsrfToken token = this.repository.generateToken(this.request);
		this.repository.saveToken(token, this.request, this.response);
		Cookie tokenCookie = this.response.getCookie(CookieCsrfTokenRepository.DEFAULT_CSRF_COOKIE_NAME);
		assertThat(tokenCookie.getPath()).isEqualTo(this.request.getContextPath());
	}

	@Test
	public void saveTokenNullCustomPath() {
		String customPath = null;
		this.repository.setCookiePath(customPath);
		CsrfToken token = this.repository.generateToken(this.request);
		this.repository.saveToken(token, this.request, this.response);
		Cookie tokenCookie = this.response.getCookie(CookieCsrfTokenRepository.DEFAULT_CSRF_COOKIE_NAME);
		assertThat(tokenCookie.getPath()).isEqualTo(this.request.getContextPath());
	}

	@Test
	public void saveTokenWithCookieDomain() {
		String domainName = "example.com";
		this.repository.setCookieDomain(domainName);
		CsrfToken token = this.repository.generateToken(this.request);
		this.repository.saveToken(token, this.request, this.response);
		Cookie tokenCookie = this.response.getCookie(CookieCsrfTokenRepository.DEFAULT_CSRF_COOKIE_NAME);
		assertThat(tokenCookie.getDomain()).isEqualTo(domainName);
	}

	@Test
	public void saveTokenWithCookieMaxAge() {
		int maxAge = 1200;
		this.repository.setCookieMaxAge(maxAge);
		CsrfToken token = this.repository.generateToken(this.request);
		this.repository.saveToken(token, this.request, this.response);
		Cookie tokenCookie = this.response.getCookie(CookieCsrfTokenRepository.DEFAULT_CSRF_COOKIE_NAME);
		assertThat(tokenCookie.getMaxAge()).isEqualTo(maxAge);
	}

	@Test
	public void loadTokenNoCookiesNull() {
		assertThat(this.repository.loadToken(this.request)).isNull();
	}

	@Test
	public void loadTokenCookieIncorrectNameNull() {
		this.request.setCookies(new Cookie("other", "name"));
		assertThat(this.repository.loadToken(this.request)).isNull();
	}

	@Test
	public void loadTokenCookieValueEmptyString() {
		this.request.setCookies(new Cookie(CookieCsrfTokenRepository.DEFAULT_CSRF_COOKIE_NAME, ""));
		assertThat(this.repository.loadToken(this.request)).isNull();
	}

	@Test
	public void loadToken() {
		CsrfToken generateToken = this.repository.generateToken(this.request);
		this.request
				.setCookies(new Cookie(CookieCsrfTokenRepository.DEFAULT_CSRF_COOKIE_NAME, generateToken.getToken()));
		CsrfToken loadToken = this.repository.loadToken(this.request);
		assertThat(loadToken).isNotNull();
		assertThat(loadToken.getHeaderName()).isEqualTo(generateToken.getHeaderName());
		assertThat(loadToken.getParameterName()).isEqualTo(generateToken.getParameterName());
		assertThat(loadToken.getToken()).isNotEmpty();
	}

	@Test
	public void loadTokenCustom() {
		String cookieName = "cookieName";
		String value = "value";
		String headerName = "headerName";
		String parameterName = "paramName";
		this.repository.setHeaderName(headerName);
		this.repository.setParameterName(parameterName);
		this.repository.setCookieName(cookieName);
		this.request.setCookies(new Cookie(cookieName, value));
		CsrfToken loadToken = this.repository.loadToken(this.request);
		assertThat(loadToken).isNotNull();
		assertThat(loadToken.getHeaderName()).isEqualTo(headerName);
		assertThat(loadToken.getParameterName()).isEqualTo(parameterName);
		assertThat(loadToken.getToken()).isEqualTo(value);
	}

	@Test
	public void setCookieNameNullIllegalArgumentException() {
		assertThatIllegalArgumentException().isThrownBy(() -> this.repository.setCookieName(null));
	}

	@Test
	public void setParameterNameNullIllegalArgumentException() {
		assertThatIllegalArgumentException().isThrownBy(() -> this.repository.setParameterName(null));
	}

	@Test
	public void setHeaderNameNullIllegalArgumentException() {
		assertThatIllegalArgumentException().isThrownBy(() -> this.repository.setHeaderName(null));
	}

	@Test
	public void setCookieMaxAgeZeroIllegalArgumentException() {
		assertThatIllegalArgumentException().isThrownBy(() -> this.repository.setCookieMaxAge(0));
	}

}
