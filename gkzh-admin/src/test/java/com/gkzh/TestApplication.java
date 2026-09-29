package com.gkzh;

import cn.hutool.core.lang.TypeReference;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.beust.ah.A;
import com.gkzh.app.service.ZycckPrintService;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.domain.model.StudentCheckin;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.school.domain.GkzhSchoolDepartment;
import com.gkzh.school.mapper.GkzhSchoolDepartmentMapper;
import com.gkzh.school.service.IGkzhSchoolDepartmentService;
import com.gkzh.wjyd.service.IBizQuestionService;
import com.gkzh.wjyd.vo.AnswerRequest;
import com.gkzh.wjyd.vo.QuestionVO;
import com.gkzh.wjyd.vo.SubmitAnswerRequest;
import com.gkzh.zycck.domain.ZycckPrintTask;
import com.gkzh.zycck.domain.ZycckRecord;
import com.gkzh.zycck.dto.ZycckPrintTaskView;
import com.gkzh.zycck.mapper.ZycckRecordMapper;
import com.hprt.config.HPRTConfig;
import com.hprt.domain.HPRTResult;
import com.hprt.utils.HttpUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class TestApplication {
    @Autowired
    private GkzhSchoolDepartmentMapper mapper;
    @Autowired
    private IGkzhSchoolDepartmentService service;

    @Autowired
    private IBizQuestionService questionService;

    @Autowired
    private ZycckRecordMapper recordMapper;

    @Autowired
    private ZycckPrintService printService;

    @Test
    public void test1(){
        Long id = 1058L;
        ZycckRecord record = recordMapper.selectById(id);
        System.out.println(record);
        recordMapper.deleteById(id);
    }

    @Test
    public void test2() throws IOException {
        Map<String, Object> body = new HashMap<>();
        /**
         * createTime: "2026-09-22T15:16:33.489+08:00"
         * equipmentSn: "D35X0025480116"
         * errorMessage: null
         * orderNo: "ZYCCK-ce19e2264b11479681dc12df02de78ce"
         * printId: "e59278fd-74fe-413a-935e-b07d1bcb8fec"
         * printTime: null
         * printType: "TSPL"
         * printerId: 1
         * recordId: 3756
         * retryCount: 0
         * sourceImageUrl: "/profile/zycck-print/ce19e2264b11479681dc12df02de78ce/page-1.png"
         * sourcePdfUrl: "/profile/zycck-print/ce19e2264b11479681dc12df02de78ce/report.pdf"
         * status: "submitted"
         * taskId: 2
         * updateTime: "2026-09-22T15:16:36.848+08:00"
         */
        body.put("createTime","2026-09-22T15:16:33.489+08:00");
        body.put("equipmentSn","D35X0025480116");
        body.put("errorMessage",null);
        body.put("orderNo","ZYCCK-ce19e2264b11479681dc12df02de78ce");
        body.put("printId","e59278fd-74fe-413a-935e-b07d1bcb8fec");
        body.put("printTime",null);
        body.put("printType","TSPL");
        body.put("printerId",1);
        body.put("recordId",3756);
        body.put("retryCount",0);
        body.put("sourceImageUrl","/profile/zycck-print/ce19e2264b11479681dc12df02de78ce/page-1.png");
        body.put("sourcePdfUrl","/profile/zycck-print/ce19e2264b11479681dc12df02de78ce/report.pdf");
        body.put("status","submitted");
        body.put("taskId",2);
        body.put("updateTime","2026-09-22T15:16:36.848+08:00");
        Long printerId = id(body.get("printerId"));
        // 此调试入口不模拟手机蓝牙扫描，第四个参数传空信标证据。
        toView(printService.print(3756L, 16515L, printerId, null));

    }
    @Test
    public void test3(){
        StringBuilder content = new StringBuilder();
        content.append("<SIZE>76,128</SIZE>\r\n")
                .append("<GAP>2,0</GAP>\r\n")
                .append("<CLS>\r\n")
                .append("<TEXT x=10 y=100 font=\"9\" r=0 w=20 h=20 a=2>你好</TEXT>\r\n")
//                .append("<BITMAP x=0 y=0 m=1>")
//                .append(toBase64(image,"png"))
//                .append("</BITMAP>\r\n")
                .append("<PRINT x=1>\r\n");

        sendPrinterTask("D35X0025480116",content.toString(),"TSPL","");

    }

    private ZycckPrintTaskView toView(ZycckPrintTask task) {
        return ZycckPrintTaskView.from(task);
    }

    private static Long id(Object value) {
        if (value == null) throw new ServiceException("缺少打印机编号");
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            throw new ServiceException("打印机编号格式错误");
        }
    }

    public HPRTResult<String> sendPrinterTask(String equipmentSn, String content, String printType, String orderNo) {
        Map<String, Object> body = this.hprtConfig.build();
        body.put("equipment_sn", equipmentSn);
        body.put("content", content);
        body.put("print_type", printType);
        body.put("order_no", orderNo);
        return HttpUtils.post(BASE_URL + "printTask", body, new TypeReference<String>() { });
    }

    private static final String BASE_URL = "https://openapi.hprtcloud.com/api/v1/";

    private final HPRTConfig hprtConfig;
    public TestApplication(HPRTConfig hprtConfig) {
        this.hprtConfig = hprtConfig;
    }
}
