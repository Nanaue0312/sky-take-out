package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
	@Autowired
	private WeChatProperties weChatProperties;
	@Autowired
	private UserMapper userMapper;
	private static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

	/**
	 * 微信登录
	 *
	 * @param userLoginDTO
	 * @return
	 */
	@Override
	public User wxLogin(UserLoginDTO userLoginDTO) {
		// 调用微信接口服务，获取当前用户的openid
		String openid = getOpenid(userLoginDTO.getCode());
		// 判断openid是否为空,如果为空则登录失败,抛出业务异常
		if (openid.equals("")) {
			throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
		}
		// 判断微信用户是否已经注册过
		User user = userMapper.getByOpenid(openid);

		// 如果是新用户,自动完成注册
		if (user == null) {
			user = User.builder()
					.openid(openid)
					.createTime(LocalDateTime.now())
					.build();
			userMapper.insert(user);
		}
		// 返回user对象
		return user;
	}

	private String getOpenid(String code) {
		HashMap<String, String> map = new HashMap<>();
		map.put("appid", weChatProperties.getAppid());
		map.put("secret", weChatProperties.getSecret());
		map.put("js_code", code);
		map.put("grant_type", "authorization_code");
		String response = HttpClientUtil.doGet(WX_LOGIN, map);
		JSONObject jsonObject = JSON.parseObject(response);
		return jsonObject.getString("openid");
	}
}
