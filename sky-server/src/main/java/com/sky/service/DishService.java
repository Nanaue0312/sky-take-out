package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
	/**
	 * 新增菜品和对应的口味
	 *
	 * @param dishDTO
	 */
	public void saveWithFlavor(DishDTO dishDTO);

	/**
	 * 菜品分页查询
	 *
	 * @param dishPageQueryDTO
	 * @return
	 */
	PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

	/**
	 * 菜品批量删除
	 *
	 * @param ids 要删除的菜品id
	 */
	void deleteBatch(List<Long> ids);

	/**
	 * 根据id查询菜品信息和口味信息
	 *
	 * @param id 菜品id
	 * @return DishVO
	 */
	DishVO getByIdWithFlavor(Long id);

	/**
	 * 修改菜品信息
	 *
	 * @param dishDTO 修改的菜品信息和口味信息
	 * @return
	 */
	void updateWithFlavor(DishDTO dishDTO);

	/**
	 * 条件查询菜品和口味
	 *
	 * @param dish
	 * @return
	 */
	List<DishVO> listWithFlavor(Dish dish);
}
