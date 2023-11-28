package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Api(tags = "套餐管理相关接口")
@Slf4j
public class SetmealController {
	@Autowired
	private SetmealService setmealService;

	/**
	 * 新增套餐
	 *
	 * @param setmealDTO 套餐DTO
	 * @return
	 */
	@PostMapping
	@ApiOperation("新增套餐")
	public Result<String> save(@RequestBody SetmealDTO setmealDTO) {
		log.info("新增套餐：{}", setmealDTO);
		setmealService.save(setmealDTO);
		return Result.success();
	}

	/**
	 * 根据id查询套餐及套餐餐品信息
	 *
	 * @param id
	 * @return
	 */
	@GetMapping("/{id}")
	@ApiOperation("根据id查询套餐及套餐餐品信息")
	public Result<SetmealVO> getById(@PathVariable("id") Long id) {
		log.info("查询套餐信息：{}", id);
		return Result.success(setmealService.getSetmeal(id));
	}

	/**
	 * 分页查询套餐信息
	 *
	 * @param setmealPageQueryDTO 查询DTO
	 * @return
	 */
	@GetMapping("/page")
	public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
		log.info("分页查询：{}", setmealPageQueryDTO);
		PageResult pageResult = setmealService.page(setmealPageQueryDTO);
		return Result.success(pageResult);
	}

	/**
	 * 根据id批量删除套餐
	 *
	 * @param ids id集合
	 * @return
	 */
	@DeleteMapping
	@ApiOperation("批量删除套餐")
	public Result deleteBatch(@RequestParam List<Long> ids) {
		log.info("删除套餐id：{}", ids);
		setmealService.deleteBatch(ids);
		return Result.success();
	}

	/**
	 * 更新菜品信息
	 *
	 * @param setmealDTO 更新后的菜品信息
	 * @return
	 */
	@PutMapping
	@ApiOperation("更新菜品信息")
	public Result update(@RequestBody SetmealDTO setmealDTO) {
		log.info("更新菜品:{}", setmealDTO);
		setmealService.update(setmealDTO);
		return Result.success();
	}

	/**
	 * 修改套餐销售状态
	 *
	 * @param status    销售状态
	 * @param setmealId 套餐id
	 * @return
	 */
	@PostMapping("/status/{status}")
	@ApiOperation("修改套餐销售状态")
	public Result updateStatus(@PathVariable("status") Integer status, @RequestParam("id") Long setmealId) {
		log.info("修改销售状态:{}", status);
		setmealService.updateStatus(status, setmealId);
		return Result.success();
	}
}
