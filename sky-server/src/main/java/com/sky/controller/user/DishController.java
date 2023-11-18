package com.sky.controller.user;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.jsonwebtoken.lang.Collections;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {
	@Autowired
	private DishService dishService;
	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	/**
	 * 根据分类id查询菜品
	 *
	 * @param categoryId
	 * @return
	 */
	@GetMapping("/list")
	@ApiOperation("根据分类id查询菜品")
	public Result<List<DishVO>> list(Long categoryId) {
		// 构建redis key，dish:categoryId
		String key = "dish:" + categoryId;
		// 查询redis中是否有数据
		String value = redisTemplate.opsForValue().get(key);
		Gson gson = new Gson();
		List<DishVO> list = gson.fromJson(value, new TypeToken<ArrayList<DishVO>>() {
		}.getType());
		// 存在则直接返回
		if (!Collections.isEmpty(list)) {
			return Result.success(list);
		}
		// 不存在查询数据库并且写入到redis
		Dish dish = new Dish();
		dish.setCategoryId(categoryId);
		dish.setStatus(StatusConstant.ENABLE);// 查询起售中的菜品
		list = dishService.listWithFlavor(dish);
		String json = gson.toJson(list);
		redisTemplate.opsForValue().set(key, json);
		return Result.success(list);
	}

}
