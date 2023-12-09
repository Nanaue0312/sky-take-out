package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.websocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * 用户下单
     *
     * @param ordersSubmitDTO 订单dto
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        // 处理业务异常（购物车数据为空，地址簿为空）
        // 获取地址簿信息
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        // 检查购物车是否为空
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(shoppingCart);
        if (CollectionUtils.isEmpty(shoppingCarts)) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        // 向订单表插入数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);
        orderMapper.insert(orders);
        // 向订单明细表插入多条数据
        ArrayList<OrderDetail> orderDetails = new ArrayList<>();
        for (ShoppingCart cart : shoppingCarts) {
            OrderDetail orderDetail = new OrderDetail();// 订单明细
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());// 设置当前订单明细管理的订单id
            orderDetails.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetails);
        // 用户下单成功后，清空购物车
        shoppingCartMapper.deleteByUserId(userId);

        return OrderSubmitVO.
                builder()
                .id(orders.getId()).
                orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount()).build();
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        // 调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), // 商户订单号
                new BigDecimal(0.01), // 支付金额，单位 元
                "苍穹外卖订单", // 商品描述
                user.getOpenid() // 微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
        // 通过websocket向客户端浏览器发送消息 orderId type content
        HashMap<Object, Object> map = new HashMap<>();
        map.put("type", 1); // 1表示来单提醒，2表示客户催单
        map.put("orderId", ordersDB.getId());
        map.put("content", "订单号：" + outTradeNo);
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }

    /**
     * 用户催单
     *
     * @param id 订单号
     */
    @Override
    public void remind(Long id) {
        Orders order = orderMapper.getByNumber(String.valueOf(id));
        // 校验订单是否存在
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        HashMap<Object, Object> map = new HashMap<>();
        map.put("type", 2); // 1来单提醒，2用户催单
        map.put("orderId", id);
        map.put("content", "订单号：" + order.getNumber());
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }
}
