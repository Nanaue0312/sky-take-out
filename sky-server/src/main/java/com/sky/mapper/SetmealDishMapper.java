package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
	/**
	 * 根据菜品id查询对应的套餐id
	 *
	 * @param dishIds 菜品id
	 * @return 查询到的套餐id
	 */
	List<Long> getSetmealIdsByDishIds(List<Long> dishIds);
}
