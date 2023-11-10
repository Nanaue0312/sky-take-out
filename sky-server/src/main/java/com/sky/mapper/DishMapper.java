package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {

	/**
	 * 根据分类id查询菜品数量
	 *
	 * @param categoryId
	 * @return
	 */
	@Select("select count(id) from dish where category_id = #{categoryId}")
	Integer countByCategoryId(Long categoryId);

	/**
	 * 新增菜品
	 *
	 * @param dish 菜品信息
	 */
	@AutoFill(OperationType.INSERT)
	void insert(Dish dish);

	/**
	 * 分页查询菜品
	 *
	 * @param dishPageQueryDTO 查询信息
	 * @return
	 */
	Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

	/**
	 * 根据id查询菜品
	 *
	 * @param id 菜品id
	 * @return
	 */
	@Select("select * from dish where id=#{id}")
	Dish getById(Long id);

	/**
	 * 根据id集合批量删除菜品
	 *
	 * @param ids 菜品id集合
	 */
	void deleteByIds(List<Long> ids);

	@AutoFill(OperationType.UPDATE)
	void update(Dish dish);
}
