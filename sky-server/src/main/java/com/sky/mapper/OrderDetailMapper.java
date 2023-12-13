package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface OrderDetailMapper {
    /**
     * 批量插入订单明细数据
     *
     * @param orderDetails 订单明细集合
     */
    void insertBatch(List<OrderDetail> orderDetails);

    @Select("select * from order_detail where order_id = #{orderId}")
    List<OrderDetail> getByOrderId(Long orderId);
}
