package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController {
	@Autowired
	private DishService dishService;
	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@PostMapping
	@ApiOperation("新增菜品")
	public Result save(@RequestBody DishDTO dishDTO) {
		log.info("新增菜品：{}", dishDTO);
		dishService.saveWithFlavor(dishDTO);
		// 删除对应分类的缓存
		String key = "dish:" + dishDTO.getCategoryId();
		cleanCache(key);
		return Result.success();
	}

	/**
	 * 菜品分页查询
	 *
	 * @param dishPageQueryDTO
	 * @return
	 */
	@GetMapping("/page")
	@ApiOperation("菜品分页查询")
	public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
		log.info("菜品分页查询:{}", dishPageQueryDTO);
		PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
		return Result.success(pageResult);
	}

	/**
	 * 菜品批量删除
	 *
	 * @param ids 删除的菜品id
	 * @return
	 */
	@DeleteMapping
	@ApiOperation("菜品批量删除")
	public Result delete(@RequestParam List<Long> ids) {
		// 删除缓存数据
		cleanCache("dish:*");
		log.info("菜品批量删除:{}", ids);
		dishService.deleteBatch(ids);
		return Result.success();
	}

	/**
	 * 根据id查询菜品信息和口味信息
	 *
	 * @param id 菜品id
	 * @return DishVO
	 */
	@GetMapping("/{id}")
	@ApiOperation("根据菜品id查询菜品信息")
	public Result<DishVO> getById(@PathVariable("id") Long id) {
		log.info("根据id查询菜品：{}", id);
		DishVO dishVO = dishService.getByIdWithFlavor(id);
		return Result.success(dishVO);
	}

	/**
	 * 修改菜品信息
	 *
	 * @param dishDTO 修改的菜品信息
	 * @return
	 */
	@PutMapping
	@ApiOperation("更新菜品信息")
	public Result update(@RequestBody DishDTO dishDTO) {
		// 删除缓存数据
		cleanCache("dish:*");

		log.info("修改菜品:{}", dishDTO);
		dishService.updateWithFlavor(dishDTO);
		return Result.success();
	}

	/**
	 * 起售或停售菜品
	 *
	 * @param status 状态
	 * @param id     菜品id
	 * @return
	 */
	@PostMapping("/status/{status}")
	@ApiOperation("起售或停售菜品")
	public Result<Integer> startOrStop(@PathVariable("status") int status, @RequestParam("id") Long id) {
		dishService.startOrStop(status, id);
		// 删除缓存数据
		cleanCache("dish:*");
		return Result.success(status);
	}

	private void cleanCache(String pattern) {
		Set<String> keys = redisTemplate.keys(pattern);
		redisTemplate.delete(keys);
	}
}
