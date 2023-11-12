package com.sky.controller.admin;

import com.sky.constant.RedisConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "店铺相关接口")
@Slf4j
public class ShopController {
	@Autowired
	private RedisTemplate<Object, Object> redisTemplate;

	/**
	 * 设置店铺营业状态
	 *
	 * @param status 店铺营业状态
	 * @return
	 */
	@PutMapping("/{status}")
	@ApiOperation("设置店铺营业状态")
	public Result setStatus(@PathVariable Integer status) {
		log.info("设置店铺营业状态为:{}", status == 1 ? "营业中" : "打洋中");
		redisTemplate.opsForValue().set(RedisConstant.SHOP_STATUS, status);
		return Result.success();
	}

	@GetMapping("/status")
	@ApiOperation("获取店铺营业状态")
	public Result<String> getStats() {
		Integer status = (Integer) redisTemplate.opsForValue().get(RedisConstant.SHOP_STATUS);
		log.info("获取店铺营业状态为：{}", status == 1 ? "营业中" : "打洋中");
		return Result.success();
	}
}
