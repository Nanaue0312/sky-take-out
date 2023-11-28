package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐业务实现
 */
@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

	@Autowired
	private SetmealMapper setmealMapper;
	@Autowired
	private SetmealDishMapper setmealDishMapper;
	@Autowired
	private DishMapper dishMapper;

	/**
	 * 条件查询
	 *
	 * @param setmeal
	 * @return
	 */
	public List<Setmeal> list(Setmeal setmeal) {
		List<Setmeal> list = setmealMapper.list(setmeal);
		return list;
	}

	/**
	 * 根据id查询菜品选项
	 *
	 * @param id
	 * @return
	 */
	public List<DishItemVO> getDishItemById(Long id) {
		return setmealMapper.getDishItemBySetmealId(id);
	}

	/**
	 * 新增套餐
	 *
	 * @param setmealDTO 套餐DTO
	 * @return
	 */
	@Override
	@Transactional
	public void save(SetmealDTO setmealDTO) {
		Setmeal setmeal = new Setmeal();
		BeanUtils.copyProperties(setmealDTO, setmeal);
		// 保存套餐信息
		setmealMapper.save(setmeal);
		// 保存套餐中的菜品
		ArrayList<SetmealDish> setmealDishes = new ArrayList<>();
		for (SetmealDish setmealDish : setmealDTO.getSetmealDishes()) {
			setmealDish.setSetmealId(setmeal.getId());
			setmealDishes.add(setmealDish);
		}
		setmealDishMapper.save(setmealDishes);
	}

	@Override
	public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
		PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
		Page<SetmealVO> page = setmealMapper.page(setmealPageQueryDTO);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 根据id批量删除套餐
	 *
	 * @param ids id集合
	 * @return
	 */
	@Override
	@Transactional
	public void deleteBatch(List<Long> ids) {
		ids.forEach(id -> {
			Setmeal setmeal = setmealMapper.getById(id);
			if (StatusConstant.ENABLE.equals(setmeal.getStatus())) {
				throw new DeletionNotAllowedException("起售中的上品不允许删除");
			}
		});
		setmealMapper.deleteBatch(ids);
		setmealDishMapper.deleteBatch(ids);
	}

	/**
	 * 更新菜品信息
	 *
	 * @param setmealDTO 更新后的菜品信息
	 * @return
	 */
	@Override
	@Transactional
	public void update(SetmealDTO setmealDTO) {
		Setmeal setmeal = new Setmeal();
		BeanUtils.copyProperties(setmealDTO, setmeal);
		setmealMapper.update(setmeal);
		List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
		setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmeal.getId()));
		List<Long> setmealDishIds = setmealDishes.stream().map(SetmealDish::getSetmealId).collect(Collectors.toList());
		setmealDishMapper.deleteBatch(setmealDishIds);
		setmealDishMapper.save(setmealDishes);
	}

	/**
	 * 根据id查询套餐及套餐餐品信息
	 *
	 * @param id
	 * @return
	 */
	@Override
	@Transactional
	public SetmealVO getSetmeal(Long id) {
		Setmeal setmeal = setmealMapper.getById(id);
		List<SetmealDish> setmealDishs = setmealDishMapper.getBySetmealId(id);
		SetmealVO setmealVO = new SetmealVO();
		BeanUtils.copyProperties(setmeal, setmealVO);
		setmealVO.setSetmealDishes(setmealDishs);
		return setmealVO;
	}

	@Override
	public void updateStatus(Integer status, Long setmealId) {
		// 起售套餐时，判断套餐内是否有停售菜品，有停售菜品提示"套餐内包含未启售菜品，无法启售"
		if (status.equals(StatusConstant.ENABLE)) {
			// select a.* from dish a left join setmeal_dish b on a.id = b.dish_id where b.setmeal_id = ?
			List<Dish> dishList = dishMapper.getBySetmealId(setmealId);
			if (!CollectionUtils.isEmpty(dishList)) {
				dishList.forEach(dish -> {
					if (StatusConstant.DISABLE == dish.getStatus()) {
						throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
					}
				});
			}
		}

		setmealMapper.updateStatus(status, setmealId);
	}
}
