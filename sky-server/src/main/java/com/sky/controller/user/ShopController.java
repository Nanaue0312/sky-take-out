package com.sky.controller.user;

import com.sky.constant.RedisConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Api(tags = "店铺相关接口")
@Slf4j
public class ShopController {
	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@GetMapping("/status")
	@ApiOperation("获取店铺营业状态")
	public Result<String> getStats() {
		int status = Integer.parseInt(Objects.requireNonNull(redisTemplate.opsForValue().get(RedisConstant.SHOP_STATUS)));
		log.info("获取店铺营业状态为：{}", status == 1 ? "营业中" : "打洋中");
		return Result.success(String.valueOf(status));
	}
}
