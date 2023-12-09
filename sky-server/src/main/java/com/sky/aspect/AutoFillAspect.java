package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面，实现公共字段自动填充
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    /**
     * 切入点
     * 任意的返回值类型 包名.所有的类.所有的方法(所有的参数类型)
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {
    }

    /**
     * 前置通知，在目标方法执行前执行
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始进行公共字段的自动填充");
        // 获取当前拦截到的方法所对数据库进行的操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();// 方法签名对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);// 获取方法上的注解对象
        OperationType operationType = autoFill.value();// 数据库操作类型

        // 获取到当前被拦截方法的参数 -- 实体对象
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }
        Object entity = args[0];
        // 准备赋值的属性
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        // 根据当前不同的操作类型，为对应的属性进行反射来赋值
        Class<?> entityClass = entity.getClass();
        Method setCreateTime = null;
        Method setUpdateTime = null;
        Method setCreateUser = null;
        Method setUpdateUser = null;
        if (operationType == OperationType.INSERT) {
            // 为四个公共字段赋值
            try {
                setCreateTime = entityClass.getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                setUpdateTime = entityClass.getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                setCreateUser = entityClass.getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                setUpdateUser = entityClass.getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                // 通过反射为对象属性赋值
                setCreateTime.invoke(entity, now);
                setUpdateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateUser.invoke(entity, currentId);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }

        } else if (operationType == OperationType.UPDATE) {
            // 为两个公共字段赋值
            try {
                setUpdateTime = entityClass.getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                setUpdateUser = entityClass.getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
