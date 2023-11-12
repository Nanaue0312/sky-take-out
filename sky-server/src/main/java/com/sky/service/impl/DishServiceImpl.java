package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
	@Autowired
	private DishMapper dishMapper;
	@Autowired
	private DishFlavorMapper dishFlavorMapper;
	@Autowired
	private SetmealDishMapper setmealDishMapper;

	@Override
	@Transactional
	public void saveWithFlavor(DishDTO dishDTO) {
		Dish dish = new Dish();
		BeanUtils.copyProperties(dishDTO, dish);
		// 向菜品表插入一条数据
		dishMapper.insert(dish);
		// 获取insert语句生成的主键值
		Long dishId = dish.getId();
		List<DishFlavor> flavors = dishDTO.getFlavors();
		if (CollectionUtils.isEmpty(flavors)) {
			flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishId));
			dishFlavorMapper.insertBatch(flavors);
		}
		// 向口味表插入多条数据
	}

	/**
	 * 菜品分页查询
	 *
	 * @param dishPageQueryDTO
	 * @return
	 */
	@Override
	public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
		PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
		Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);

		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 菜品批量删除
	 *
	 * @param ids 要删除的菜品id
	 */
	@Override
	@Transactional
	public void deleteBatch(List<Long> ids) {
		// 判断当前菜品能否删除---是否存在起售中的菜品
		for (Long id : ids) {
			Dish dish = dishMapper.getById(id);
			if (dish.getStatus() == StatusConstant.ENABLE) {
				// 当前菜品处于起售中，不能删除
				throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
			}
		}
		// 判断当前菜品能否删除---是否被套餐关联
		List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
		if (!CollectionUtils.isEmpty(setmealIds)) {
			throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
		}
		// 批量删除菜品表的菜品数据
		dishMapper.deleteByIds(ids);
		// 批量删除菜品关联的口味数据
		dishFlavorMapper.deleteByIds(ids);
	}

	/**
	 * 根据id查询菜品信息和口味信息
	 *
	 * @param id 菜品id
	 * @return DishVO
	 */
	@Override
	public DishVO getByIdWithFlavor(Long id) {
		// 根据id查询菜品数据
		Dish dish = dishMapper.getById(id);
		// 根据菜品id查询口味数据
		List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);
		// 将查询到的数据封装为DishVO
		DishVO dishVO = new DishVO();
		BeanUtils.copyProperties(dish, dishVO);
		dishVO.setFlavors(dishFlavors);
		return dishVO;
	}

	/**
	 * 修改菜品信息
	 *
	 * @param dishDTO 修改的菜品信息和口味信息
	 * @return
	 */
	@Override
	public void updateWithFlavor(DishDTO dishDTO) {
		// 修改菜品表基本信息
		Dish dish = new Dish();
		BeanUtils.copyProperties(dishDTO, dish);
		dishMapper.update(dish);
		// 删除原有口味数据
		dishFlavorMapper.deleteById(dishDTO.getId());
		// 重新插入口味数据
		List<DishFlavor> flavors = dishDTO.getFlavors();
		if (!CollectionUtils.isEmpty(flavors)) {
			flavors.forEach(flavor -> flavor.setDishId(dishDTO.getId()));
			dishFlavorMapper.insertBatch(flavors);
		}
	}
}