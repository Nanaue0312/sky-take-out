package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务类
 */
@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    @Scheduled(cron = "0 0/1 * * * ?")
    public void executeUnpaid() {
        log.info("定时处理超时订单：{}", LocalDateTime.now());
        List<Orders> ordersList = orderMapper.getByStatusAndOverTimeLT(Orders.PENDING_PAYMENT, LocalDateTime.now().minusMinutes(15));
        if (!CollectionUtils.isEmpty(ordersList)) {
            ordersList.forEach(orders -> {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时，自动取消");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            });
        }
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder() {
        log.info("处理派送中的订单：{}", LocalDateTime.now());
        List<Orders> ordersList = orderMapper.getByStatusAndOverTimeLT(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().minusMinutes(60));
        if (!CollectionUtils.isEmpty(ordersList)) {
            ordersList.forEach(orders -> {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            });
        }

    }
}
