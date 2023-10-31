package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;

public interface EmployeeService {

	/**
	 * 员工登录
	 *
	 * @param employeeLoginDTO
	 * @return
	 */
	Employee login(EmployeeLoginDTO employeeLoginDTO);

	/**
	 * 新增员工
	 *
	 * @param employeeDTO
	 */
	void save(EmployeeDTO employeeDTO);

	/**
	 * 分页查询员工
	 *
	 * @param employeePageQueryDTO
	 * @return
	 */
	PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

	/**
	 * 修改员工账号状态
	 *
	 * @param status 账号状态
	 * @param id     员工id
	 */
	void updateStatus(int status, Long id);

	/**
	 * 根据id查询员工
	 *
	 * @param id 员工id
	 * @return
	 */
	Employee getById(Long id);

	/**
	 * 更新员工信息
	 *
	 * @param employeeDTO
	 * @return
	 */
	void update(EmployeeDTO employeeDTO);
}
