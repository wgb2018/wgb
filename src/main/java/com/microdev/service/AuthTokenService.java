package com.microdev.service;

import io.swagger.client.ApiException;

public interface AuthTokenService {
	/**
	 * Request an Authentication Token
	 * @return String
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 */
	Object getAuthToken();
}
