package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {

	/**
	 * 条件查询
	 *
	 * @param setmeal
	 * @return
	 */
	List<Setmeal> list(Setmeal setmeal);

	/**
	 * 根据id查询菜品选项
	 *
	 * @param id
	 * @return
	 */
	List<DishItemVO> getDishItemById(Long id);

	/**
	 * 新增套餐
	 *
	 * @param setmealDTO 套餐DTO
	 * @return
	 */
	void save(SetmealDTO setmealDTO);

	/**
	 * 分页查询套餐信息
	 *
	 * @param setmealPageQueryDTO 查询DTO
	 * @return
	 */
	PageResult page(SetmealPageQueryDTO setmealPageQueryDTO);

	/**
	 * 根据id批量删除套餐
	 *
	 * @param ids id集合
	 * @return
	 */
	void deleteBatch(List<Long> ids);

	/**
	 * 更新菜品信息
	 *
	 * @param setmealDTO 更新后的菜品信息
	 * @return
	 */
	void update(SetmealDTO setmealDTO);

	/**
	 * 根据id查询套餐及套餐餐品信息
	 *
	 * @param id
	 * @return
	 */
	SetmealVO getSetmeal(Long id);

	/**
	 * 修改套餐销售状态
	 *
	 * @param status    销售状态
	 * @param setmealId 套餐id
	 * @return
	 */
	void updateStatus(Integer status, Long setmealId);
}
