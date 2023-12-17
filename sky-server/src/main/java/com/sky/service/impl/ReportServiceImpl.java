package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;

    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        // 用于存放begin-end之间的所有日期
        ArrayList<LocalDate> dateList = new ArrayList<>();
        ArrayList<Double> turnoverList = new ArrayList<>();
        dateList.add(begin);
        do {
            // 查询date日期对应的营业额数据，营业额是指:状态“已完成”的订单 select sum(amount) from orders where order_time > begin and order_time < ? and status = 5
            LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(begin, LocalTime.MAX);
            HashMap<String, Object> map = new HashMap<>();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnOver = orderMapper.sumByMap(map);
            turnOver = turnOver == null ? 0.0 : turnOver;
            turnoverList.add(turnOver);
            dateList.add(begin);
            // 获取begin-end内的所有日期
            begin = begin.plusDays(1);
        } while (!begin.equals(end));
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /**
     * 统计指定时间内的用户数据
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        // 存放begin-end内所有的日期
        ArrayList<LocalDate> dateList = new ArrayList<>();
        // 存放每天新增的用户数量,select count(id) from user create_time between xxx and xxx
        ArrayList<Integer> newUserList = new ArrayList<>();
        // 存放每天用户总数
        ArrayList<Integer> totalUserList = new ArrayList<>();
        do {
            HashMap<String, LocalDateTime> map = new HashMap<>();
            LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(begin, LocalTime.MAX);
            map.put("end", beginTime);
            // 总用户数量
            Integer totalUser = userMapper.countByMap(map);
            map.put("begin", beginTime);
            Integer newUser = userMapper.countByMap(map);
            dateList.add(begin);
            newUserList.add(newUser);
            totalUserList.add(totalUser);
            begin = begin.plusDays(1);
        } while (!begin.equals(end));
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }

    /**
     * 统计指定时间内的订单数据
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        // 存放begin-end内所有的日期
        ArrayList<LocalDate> dateList = new ArrayList<>();
        // 存放所有订单
        ArrayList<Integer> totalOrderList = new ArrayList<>();
        // 存放所有有效订单
        ArrayList<Integer> completedOrderList = new ArrayList<>();
        do {
            LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(begin, LocalTime.MAX);
            HashMap<String, Object> map = new HashMap<>();
            map.put("begin", beginTime);
            map.put("end", endTime);
            // 查询每天订单总数
            Integer totalOrder = orderMapper.countByMap(map);
            map.put("statis", Orders.COMPLETED);
            // 查询每天有效订单数
            Integer completedOrder = orderMapper.countByMap(map);
            dateList.add(begin);
            totalOrderList.add(totalOrder);
            completedOrderList.add(completedOrder);
            begin = begin.plusDays(1);
        } while (!begin.equals(end));
        // 计算时间区间内的订单总数量
        Integer totalOrderCount = totalOrderList.stream().reduce(Integer::sum).get();
        // 计算时间区间内的有效订单总数量
        Integer completedOrderCount = completedOrderList.stream().reduce(Integer::sum).get();
        // 计算订单完成率
        double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = (completedOrderCount.doubleValue() / totalOrderCount);
        }
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(totalOrderList, ","))
                .validOrderCountList(StringUtils.join(completedOrderList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(completedOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 获取指定区间内的销量排名top10
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10Statistics(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(begin, LocalTime.MAX);
        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);
        String nameList = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.joining(","));
        String numberList = salesTop10.stream().map(GoodsSalesDTO::getNumber).map(Objects::toString).collect(Collectors.joining(","));
        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    /**
     * 导出运营数据报表
     *
     * @param response
     */
    @Override
    public void exportBusiness(HttpServletResponse response) {
        // 1.查询数据库，获取营业数据---30天的运营数据
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);
        BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(begin, LocalTime.MIN)
                , LocalDateTime.of(end, LocalTime.MIN));

        // 2.通过POI将数据写入到excel
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        XSSFWorkbook excel = null;
        ServletOutputStream outputStream = null;
        try {
            assert inputStream != null;
            excel = new XSSFWorkbook(inputStream);
            // 获取sheet1页
            XSSFSheet sheet1 = excel.getSheet("Sheet1");
            // 填充数据---时间
            sheet1.getRow(1).getCell(1).setCellValue("时间:" + begin + " 至 " + end);
            // 获得第四行
            XSSFRow row = sheet1.getRow(3);
            // 填充数据---营业额，订单完成率，新增用户数
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessData.getNewUsers());
            // 获得第5行
            row = sheet1.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());
            // 填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = begin.plusDays(i);
                BusinessDataVO dateBusiness = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                // 获得某一行
                row = sheet1.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(dateBusiness.getTurnover());
                row.getCell(3).setCellValue(dateBusiness.getValidOrderCount());
                row.getCell(4).setCellValue(dateBusiness.getOrderCompletionRate());
                row.getCell(5).setCellValue(dateBusiness.getUnitPrice());
                row.getCell(6).setCellValue(dateBusiness.getNewUsers());
            }
            // 3.通过输出流将excel文件下载到客户端浏览器
            outputStream = response.getOutputStream();
            excel.write(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                assert inputStream != null;
                inputStream.close();
                assert outputStream != null;
                outputStream.close();
                excel.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
