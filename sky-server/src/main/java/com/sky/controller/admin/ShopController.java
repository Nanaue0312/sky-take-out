package com.sky.controller.admin;

import com.sky.constant.RedisConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "店铺相关接口")
@Slf4j
public class ShopController {
	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	/**
	 * 设置店铺营业状态
	 *
	 * @param status 店铺营业状态
	 * @return
	 */
	@PutMapping("/{status}")
	@ApiOperation("设置店铺营业状态")
	public Result<String> setStatus(@PathVariable Integer status) {
		log.info("设置店铺营业状态为:{}", status == 1 ? "营业中" : "打洋中");
		redisTemplate.opsForValue().set(RedisConstant.SHOP_STATUS, String.valueOf(status));
		return Result.success(String.valueOf(status));
	}

	@GetMapping("/status")
	@ApiOperation("获取店铺营业状态")
	public Result<Integer> getStats() {
		int status = Integer.parseInt(Objects.requireNonNull(redisTemplate.opsForValue().get(RedisConstant.SHOP_STATUS)));
		log.info("获取店铺营业状态为：{}", status == 1 ? "营业中" : "打洋中");
		return Result.success(status);
	}
}
