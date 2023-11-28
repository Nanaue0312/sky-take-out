package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SetmealMapper {

	/**
	 * 根据分类id查询套餐的数量
	 *
	 * @param id
	 * @return
	 */
	@Select("select count(id) from setmeal where category_id = #{categoryId}")
	Integer countByCategoryId(Long id);

	/**
	 * 动态条件查询套餐
	 *
	 * @param setmeal
	 * @return
	 */
	List<Setmeal> list(Setmeal setmeal);

	/**
	 * 根据套餐id查询菜品选项
	 *
	 * @param setmealId
	 * @return
	 */
	@Select("select sd.name, sd.copies, d.image, d.description " +
			"from setmeal_dish sd left join dish d on sd.dish_id = d.id " +
			"where sd.setmeal_id = #{setmealId}")
	List<DishItemVO> getDishItemBySetmealId(Long setmealId);

	/**
	 * 新增套餐
	 *
	 * @param setmeal 套餐
	 */
	@AutoFill(OperationType.INSERT)
	void save(Setmeal setmeal);

	/**
	 * 分页查询套餐
	 *
	 * @param setmealPageQueryDTO 查询DTO
	 * @return
	 */
	Page<SetmealVO> page(SetmealPageQueryDTO setmealPageQueryDTO);

	/**
	 * 批量删除套餐
	 *
	 * @param ids 套餐id集合
	 */
	void deleteBatch(List<Long> ids);

	/**
	 * 更新套餐信息
	 *
	 * @param setmeal 套餐信息
	 */
	@AutoFill(OperationType.UPDATE)
	void update(Setmeal setmeal);

	/**
	 * 根据id查询套餐
	 *
	 * @param id 套餐id
	 * @return
	 */
	@Select("select * from setmeal where id=#{id}")
	Setmeal getById(Long id);

	/**
	 * 更新套餐销售状态
	 *
	 * @param status    销售状态
	 * @param setmealId 套餐id
	 */
	@Update("update setmeal set status=#{status} where id=#{setmealId}")
	void updateStatus(Integer status, Long setmealId);
}
