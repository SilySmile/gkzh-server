package com.gkzh;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.beust.ah.A;
import com.gkzh.school.domain.GkzhSchoolDepartment;
import com.gkzh.school.mapper.GkzhSchoolDepartmentMapper;
import com.gkzh.school.service.IGkzhSchoolDepartmentService;
import com.gkzh.wjyd.service.IBizQuestionService;
import com.gkzh.wjyd.vo.AnswerRequest;
import com.gkzh.wjyd.vo.QuestionVO;
import com.gkzh.wjyd.vo.SubmitAnswerRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
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

    @Test
    public void test1(){
        ArrayList<Long> arrayList = new ArrayList<>();
        arrayList.add(17L);
        arrayList.add(16L);
        QueryWrapper<GkzhSchoolDepartment> query = Wrappers.query();
        query.in("department_id", arrayList);
//        List<GkzhSchoolDepartment> gkzhSchoolDepartments = mapper.selectList( query);
//        System.out.println(gkzhSchoolDepartments);
        Map<Long, String> longListMap = service.batchGetDepartmentFullPaths(arrayList);
        System.out.println(longListMap);
    }

    @Test
    public void test2(){
        String json = "[{\"type\":\"check-in\",\"name\":\"签到\",\"icon\":\"el-icon-s-claim\",\"isBlocking\":false,\"id\":\"module_1759998435320\",\"title\":\"签到\",\"config\":{\"startTime\":\"08:30\",\"endTime\":\"10:00\",\"gpsRequired\":false,\"gpsRange\":100}},{\"type\":\"survey\",\"name\":\"问卷\",\"icon\":\"el-icon-s-order\",\"isBlocking\":false,\"id\":\"module_1759998449719\",\"title\":\"问卷\",\"config\":{\"title\":\"问卷\",\"description\":\"\",\"surveyId\":2}},{\"type\":\"mind-window\",\"name\":\"心愿橱窗\",\"icon\":\"el-icon-s-opportunity\",\"isBlocking\":false,\"id\":\"module_1759998446487\",\"title\":\"心愿橱窗\",\"config\":{\"title\":\"心愿橱窗\",\"description\":\"游戏环节描述\"}},{\"type\":\"lottery\",\"name\":\"抽奖\",\"icon\":\"el-icon-s-gift\",\"isBlocking\":false,\"id\":\"module_1759998452000\",\"title\":\"抽奖\",\"config\":{\"title\":\"抽奖\",\"description\":\"\",\"lotteryId\":1}},{\"type\":\"check-out\",\"name\":\"签退\",\"icon\":\"el-icon-s-unfold\",\"isBlocking\":false,\"id\":\"module_1759998440319\",\"title\":\"签退\"}]";
        JSONArray moduleConfigArray = JSON.parseArray(json);
        for(int i = 0; i < moduleConfigArray.size(); i++){
            JSONObject module = moduleConfigArray.getJSONObject(i);
            if ("check-in".equals(module.get("type"))) {
                JSONObject config = module.getJSONObject("config");
                System.out.println(config);


                System.out.println(config.getOffsetTime("startTime"));

            }
        }
    }
    @Test
    public void test3(){
//        List<QuestionVO> list = questionService.getRandomQuestions(3);
//        System.out.println(list);

    }
}
