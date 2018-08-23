package com.microdev.service.impl;

import com.microdev.common.utils.HXTokenUtil;
import com.microdev.service.AuthTokenService;
import org.springframework.stereotype.Service;

@Service
public class AuthTokenServiceImpl implements AuthTokenService {

	@Override
	public Object getAuthToken(){
		return HXTokenUtil.getAccessToken();
	}
}
