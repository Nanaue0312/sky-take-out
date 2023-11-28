package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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

	/**
	 * 批量插入套餐菜品
	 *
	 * @param setmealDishes 套餐菜品集合
	 */
	void save(List<SetmealDish> setmealDishes);

	/**
	 * 根据套餐id批量删除套餐菜品
	 *
	 * @param ids 套餐id集合
	 */
	void deleteBatch(List<Long> ids);


	@Select("select * from setmeal_dish where setmeal_id=#{id}")
	List<SetmealDish> getBySetmealId(Long id);
}
