package com.gkzh;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.beust.ah.A;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.school.domain.GkzhSchoolDepartment;
import com.gkzh.school.mapper.GkzhSchoolDepartmentMapper;
import com.gkzh.school.service.IGkzhSchoolDepartmentService;
import com.gkzh.wjyd.service.IBizQuestionService;
import com.gkzh.wjyd.vo.AnswerRequest;
import com.gkzh.wjyd.vo.QuestionVO;
import com.gkzh.wjyd.vo.SubmitAnswerRequest;
import com.gkzh.zycck.domain.ZycckRecord;
import com.gkzh.zycck.mapper.ZycckRecordMapper;
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

    @Autowired
    private ZycckRecordMapper recordMapper;

    @Test
    public void test1(){
        Long id = 1058L;
        ZycckRecord record = recordMapper.selectById(id);
        System.out.println(record);
        recordMapper.deleteById(id);
    }

    @Test
    public void test2(){

    }
    @Test
    public void test3(){


    }
}
