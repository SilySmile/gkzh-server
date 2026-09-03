package com.gkzh.qdqt.service.impl;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gkzh.activity.domain.GkzhActivity;
import com.gkzh.activity.domain.GkzhActivityParticipationRecord;
import com.gkzh.activity.mapper.GkzhActivityMapper;
import com.gkzh.activity.service.IGkzhActivityParticipationRecordService;
import com.gkzh.activity.service.IGkzhActivityService;
import com.gkzh.activity.service.impl.GkzhActivityServiceImpl;
import com.gkzh.common.constant.CacheConstants;
import com.gkzh.common.core.domain.model.StudentCheckin;
import com.gkzh.common.core.redis.RedisCache;
import com.gkzh.common.exception.user.StuNotExistsException;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.common.utils.ServletUtils;
import com.gkzh.common.utils.ip.AddressUtils;
import com.gkzh.common.utils.ip.IpUtils;
import com.gkzh.qdqt.dto.*;
import com.gkzh.qdqt.service.StuTokenService;
import com.gkzh.qdqt.vo.GkzhStudentCheckinExportVO;
import com.gkzh.school.domain.GkzhSchoolDepartment;
import com.gkzh.school.domain.GkzhStudent;
import com.gkzh.school.mapper.GkzhStudentMapper;
import com.gkzh.school.service.IGkzhSchoolDepartmentService;
import com.gkzh.school.service.IGkzhSchoolService;
import com.gkzh.school.service.IGkzhStudentService;
import eu.bitwalker.useragentutils.UserAgent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.gkzh.qdqt.mapper.GkzhStudentCheckinMapper;
import com.gkzh.qdqt.domain.GkzhStudentCheckin;
import com.gkzh.qdqt.service.IGkzhStudentCheckinService;

/**
 * 签到签退Service业务层处理
 * 
 * @author gkzh
 * @date 2025-06-22
 */
@Service
public class GkzhStudentCheckinServiceImpl implements IGkzhStudentCheckinService
{

    @Autowired
    private GkzhActivityMapper gkzhActivityMapper;
    @Autowired
    private IGkzhActivityService activityService;
    @Autowired
    private GkzhStudentCheckinMapper gkzhStudentCheckinMapper;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private IGkzhSchoolDepartmentService departmentService;
    @Autowired
    private IGkzhSchoolService schoolService;
    @Autowired
    private GkzhStudentMapper gkzhStudentMapper;
    @Autowired
    private IGkzhStudentService studentService;
    @Autowired
    private IGkzhActivityParticipationRecordService activityParticipationRecordService;
    @Autowired
    private RedisCache redisCache;

    @Autowired
    private StuTokenService stuTokenService;

    /**
     * 查询签到签退
     * 
     * @param checkinId 签到签退主键
     * @return 签到签退
     */
    @Override
    public GkzhStudentCheckin selectGkzhStudentCheckinByCheckinId(Long checkinId)
    {
        return gkzhStudentCheckinMapper.selectGkzhStudentCheckinByCheckinId(checkinId);
    }

    /**
     * 查询签到签退列表
     * 
     * @param gkzhStudentCheckin 签到签退
     * @return 签到签退
     */
    /**
     * 查询签到签退列表
     *
     * @param gkzhStudentCheckinDTO 签到签退查询条件
     * @return 签到签退导出VO列表
     */
    @Override
    public List<GkzhStudentCheckinExportVO> selectGkzhStudentCheckinList(GkzhStudentCheckinDTO gkzhStudentCheckinDTO) {
        // 1. 查询原始数据列表
        List<GkzhStudentCheckinExportVO> gkzhStudentCheckins = gkzhStudentCheckinMapper.selectGkzhStudentCheckinList2(gkzhStudentCheckinDTO);

        // 2. 如果查询结果为空，直接返回空列表
        if (CollectionUtils.isEmpty(gkzhStudentCheckins)) {
            return new ArrayList<>();
        }

        // 3. 提取需要关联查询的ID集合


        Set<Long> departmentIds = gkzhStudentCheckins.stream()
                .map(GkzhStudentCheckinExportVO::getDepartmentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 4. 批量查询关联数据（避免在循环中查询数据库）
        Map<Long, String> departmentMajorMap = new HashMap<>();



        if (!departmentIds.isEmpty()) {
            // 假设有对应的方法查询院系专业信息
             departmentMajorMap = departmentService.batchGetDepartmentFullPaths(new ArrayList<>(departmentIds));
        }
        for (GkzhStudentCheckinExportVO studentCheckin : gkzhStudentCheckins) {
           studentCheckin.setDepartmentMajor(departmentMajorMap.get(studentCheckin.getDepartmentId()));
        }
        return gkzhStudentCheckins;
    }



    /**
     * 拷贝BaseEntity中的公共字段
     */
    private void copyBaseEntityFields(GkzhStudentCheckin source, GkzhStudentCheckinExportVO target) {
        // 根据您的BaseEntity实际字段进行拷贝
        // 例如：target.setCreateTime(source.getCreateTime());
        // target.setUpdateTime(source.getUpdateTime());
        // target.setRemark(source.getRemark());
    }

    /**
     * 新增签到签退
     * 
     * @param gkzhStudentCheckin 签到签退
     * @return 结果
     */
    @Override
    public int insertGkzhStudentCheckin(GkzhStudentCheckin gkzhStudentCheckin)
    {
        gkzhStudentCheckin.setCreateTime(DateUtils.getNowDate());
        return gkzhStudentCheckinMapper.insertGkzhStudentCheckin(gkzhStudentCheckin);
    }

    /**
     * 修改签到签退
     * 
     * @param gkzhStudentCheckin 签到签退
     * @return 结果
     */
    @Override
    public int updateGkzhStudentCheckin(GkzhStudentCheckin gkzhStudentCheckin)
    {
        gkzhStudentCheckin.setUpdateTime(DateUtils.getNowDate());
        return gkzhStudentCheckinMapper.updateGkzhStudentCheckin(gkzhStudentCheckin);
    }

    /**
     * 批量删除签到签退
     * 
     * @param checkinIds 需要删除的签到签退主键
     * @return 结果
     */
    @Override
    public int deleteGkzhStudentCheckinByCheckinIds(Long[] checkinIds)
    {
        return gkzhStudentCheckinMapper.deleteGkzhStudentCheckinByCheckinIds(checkinIds);
    }

    /**
     * 删除签到签退信息
     * 
     * @param checkinId 签到签退主键
     * @return 结果
     */
    @Override
    public int deleteGkzhStudentCheckinByCheckinId(Long checkinId)
    {
        return gkzhStudentCheckinMapper.deleteGkzhStudentCheckinByCheckinId(checkinId);
    }

    @Override
    public StudentTokenResponse checkin(StudentLoginRequest request) {

        Long activityId = request.getActivityId();

        if(activityId.equals(0L) || activityId == null){
            throw new RuntimeException("活动不存在");
        }
        QueryWrapper<GkzhActivity> query = Wrappers.query();
        query.eq("activity_id", activityId);
        GkzhActivity gkzhActivity = gkzhActivityMapper.selectOne(query);
        if(gkzhActivity == null){
            throw new RuntimeException("活动不存在");
        }
        if(gkzhActivity.getStatus().equals("1")){
            throw new RuntimeException("活动已关闭");
        }
        if(gkzhActivity.getStartTime().after(new Date())){
            throw new RuntimeException("活动未开始");
        }
        if(gkzhActivity.getEndTime().before(new Date())){
            throw new RuntimeException("活动已结束");
        }
        QueryWrapper<GkzhStudent> studentCondition = Wrappers.query();
        studentCondition.eq("student_no", request.getNo());
        if(request.getDeptId() != null){
            studentCondition.eq("department_id", request.getDeptId());
        }
        studentCondition.eq("student_name", request.getName());
        GkzhStudent s = gkzhStudentMapper.selectOne(studentCondition);

        if( s == null ){
            GkzhStudent newStudent = new GkzhStudent();
            newStudent.setSchoolId(request.getSchoolId());
            if(request.getDeptId() != null){
                newStudent.setDepartmentId(request.getDeptId());
            }
            newStudent.setStudentNo(request.getNo());
            newStudent.setStudentName(request.getName());
            if(request.getGender() != null){
                newStudent.setGender(request.getGender());
            }
            if(request.getGrade() != null){
                newStudent.setGrade(request.getGrade());
            }
            if(request.getLqfs() != null){
                newStudent.setLqfs(request.getLqfs());
            }
            if(request.getSyd() != null){
                newStudent.setSyd(request.getSyd());
            }
            newStudent.setCreateTime(DateUtils.getNowDate());
            newStudent.setCreateBy(request.getName()); //
            studentService.insertGkzhStudent(newStudent);
            s = newStudent;
        }

        //添加签到记录
        GkzhStudentCheckin checkin = new GkzhStudentCheckin();
        checkin.setStudentId(s.getStudentId());
        checkin.setActivityId(request.getActivityId());
        checkin.setDepartmentId(s.getDepartmentId());
        checkin.setStudentNo(s.getStudentNo());
        checkin.setStudentName(s.getStudentName());
        checkin.setCheckinTime(DateUtils.getNowDate());
        checkin.setCheckinIp(IpUtils.getIpAddr());
        String address = AddressUtils.getRealAddressByIP(IpUtils.getIpAddr());
        checkin.setCheckinLocation(address);
        HashMap<String, String> device = new HashMap<>();
        UserAgent userAgent = UserAgent.parseUserAgentString(ServletUtils.getRequest().getHeader("User-Agent"));
        device.put("os",userAgent.getOperatingSystem().getName());
        device.put("browser",userAgent.getBrowser().getName());
        checkin.setCheckinDevice(JSONObject.toJSONString(device));

        checkin.setCreateTime(DateUtils.getNowDate());
        checkin.setCreateBy(s.getStudentName());
        //判断一下当前用户当前活动是否已经签到
        GkzhStudentCheckin checkinRecord = gkzhStudentCheckinMapper.selectGkzhStudentCheckinByStudentIdAndActivityId(s.getStudentId(), request.getActivityId());
        if (checkinRecord == null) {
            //判断是否在签到时间内
            String moduleConfig = gkzhActivity.getModuleConfig();
            JSONArray moduleConfigArray = JSON.parseArray(moduleConfig);
            for(int i = 0; i < moduleConfigArray.size(); i++){
                JSONObject module = moduleConfigArray.getJSONObject(i);
                if ("check-in".equals(module.get("type"))) {
                    JSONObject config = module.getJSONObject("config");
                    String startTimeStr = config.getString("startTime");
                    String endTimeStr = config.getString("endTime");
                    LocalTime startTime = LocalTime.parse(startTimeStr);
                    LocalTime endTime = LocalTime.parse(endTimeStr);
                    LocalTime currentTime = LocalTime.now();
                    // 判断当前时间是否在时间段内
                    if (currentTime.isBefore(startTime) || currentTime.isAfter(endTime)) {
                        throw new RuntimeException("不在签到时间段内");
                    }
                }
            }
            // 将签到记录插入数据库
            int checkId = gkzhStudentCheckinMapper.insertGkzhStudentCheckin(checkin);
            if(!activityId.equals("0") && activityId != null){
                //添加到活动参与记录表
                GkzhActivityParticipationRecord record = new GkzhActivityParticipationRecord();

                record.setModuleId(Long.valueOf(checkId));
                record.setParticipationTime(DateUtils.getNowDate());
                record.setResult("签到完成");
                record.setParticipationType(1);
                record.setActivityId(activityId);
                record.setUserId(s.getUserId());
                record.setUserCode(s.getStudentNo());
                record.setUserName(s.getStudentName());
                activityParticipationRecordService.insertGkzhActivityParticipationRecord(record);
            }
        }

        // 可选：将 token 缓存到 Redis 或本地 Map 中，设置过期时间
        StudentCheckin studentCheckin = new StudentCheckin();
        studentCheckin.setActivityId(request.getActivityId());
        studentCheckin.setLoginTime(System.currentTimeMillis());
        studentCheckin.setIpaddr(IpUtils.getIpAddr());
        studentCheckin.setLoginLocation(checkin.getCheckinLocation());
        studentCheckin.setBrowser(device.get("browser"));
        studentCheckin.setOs(device.get("os"));
        studentCheckin.setStuId(s.getStudentId());
        studentCheckin.setStuName(s.getStudentName());
        studentCheckin.setDeptId(s.getDepartmentId());
        studentCheckin.setStuNo(s.getStudentNo());
        studentCheckin.setUserId(s.getUserId());
        String token = stuTokenService.createToken(studentCheckin);


        return new StudentTokenResponse(token);
    }
    private String getCacheKey(Long stuId)
    {
        return CacheConstants.STU_TOKEN_KEY + stuId.toString();
    }

    @Override
    public boolean signout(StudentCheckin studentCheckin, Long activityId) {
        GkzhStudentCheckin checkin = gkzhStudentCheckinMapper.selectGkzhStudentCheckinByStudentIdAndActivityId(studentCheckin.getStuId(), activityId);
        if (checkin == null) {
            return false;
        }
        checkin.setUpdateTime(DateUtils.getNowDate());
        checkin.setCheckoutTime(DateUtils.getNowDate());
        int result = gkzhStudentCheckinMapper.updateGkzhStudentCheckin(checkin);
        if (result > 0) {
            GkzhActivityParticipationRecord record = new GkzhActivityParticipationRecord();
            record.setModuleId(checkin.getCheckinId());
            record.setParticipationTime(DateUtils.getNowDate());
            record.setResult("签退完成");
            record.setParticipationType(2); // 2-签退
            record.setActivityId(checkin.getActivityId());
            record.setUserCode(checkin.getStudentNo());
            record.setUserName(checkin.getStudentName());
            record.setStatus(1); // 有效
            record.setUserId(studentCheckin.getUserId());

            activityParticipationRecordService.insertGkzhActivityParticipationRecord(record);
        }
        return result > 0;
    }

    

    @Override
    public StudentTokenResponse register(StudentRegisterRequest request) {
        if (request.getSchoolId() == null || request.getStudentNo() == null || request.getStudentNo().trim().isEmpty()) {
            throw new RuntimeException("请选择学校并填写学号");
        }
        validateRegistrationDepartment(request);
        // 后台学生管理预先录入的“学校 + 学号”是注册白名单；同学号可存在于不同学校。
        QueryWrapper<GkzhStudent> existQuery = Wrappers.query();
        existQuery.eq("school_id", request.getSchoolId())
                .eq("student_no", request.getStudentNo())
                .eq("del_flag", "0");
        GkzhStudent existStudent = gkzhStudentMapper.selectOne(existQuery);
        if (existStudent == null) {
            throw new RuntimeException("该学校未录入此学号，请联系学校管理员后再注册");
        }
        // 密码只会由注册或后台重置写入；因此一旦存在即视为该学校+学号已经注册。
        if (existStudent.getPassword() != null && !existStudent.getPassword().trim().isEmpty()) {
            throw new RuntimeException("该学号已被注册");
        }
        if ("1".equals(existStudent.getStatus())) {
            throw new RuntimeException("该学生账号已被停用，请联系学校管理员");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new RuntimeException("密码至少 6 位");
        }
        existStudent.setPassword(passwordEncoder.encode(request.getPassword()));
        // 注册资料以用户本次填写为准，修正批量导入或后台预录时的错误资料。
        existStudent.setDepartmentId(request.getDepartmentId());
        existStudent.setClassName(request.getClassName());
        existStudent.setEnrollmentYear(request.getEnrollmentYear());
        existStudent.setStudentName(request.getStudentName());
        existStudent.setPhone(request.getPhone());
        existStudent.setGender(request.getGender());
        existStudent.setUpdateTime(DateUtils.getNowDate());
        existStudent.setUpdateBy("小程序注册");
        // 注册接口无需后台登录态，直接更新学生档案，避免后台账号同步逻辑要求管理员会话。
        gkzhStudentMapper.updateGkzhStudent(existStudent);
        return issueToken(existStudent, null);
    }

    /** 注册只能选择学校下的一级院系及其直属二级专业。 */
    private void validateRegistrationDepartment(StudentRegisterRequest request) {
        if (request.getCollegeId() == null || request.getDepartmentId() == null) {
            throw new RuntimeException("请选择院系和二级专业");
        }
        GkzhSchoolDepartment college = departmentService.selectDepartmentById(request.getCollegeId());
        GkzhSchoolDepartment major = departmentService.selectDepartmentById(request.getDepartmentId());
        boolean valid = college != null && major != null
                && Objects.equals(request.getSchoolId(), college.getSchoolId())
                && Objects.equals(request.getSchoolId(), major.getSchoolId())
                && (college.getParentId() == null || college.getParentId() == 0L)
                && Objects.equals(college.getDepartmentId(), major.getParentId())
                && "0".equals(college.getStatus()) && "0".equals(major.getStatus())
                && "0".equals(college.getDelFlag()) && "0".equals(major.getDelFlag());
        if (!valid) {
            throw new RuntimeException("院系或专业选择无效，请重新选择二级专业");
        }
    }

    @Override
    public StudentTokenResponse login(StudentLoginSimpleRequest request) {
        QueryWrapper<GkzhStudent> query = Wrappers.query();
        query.eq("school_id", request.getSchoolId())
                .eq("student_no", request.getStudentNo())
                .eq("del_flag", "0");
        GkzhStudent student = gkzhStudentMapper.selectOne(query);
        if (student == null) {
            throw new RuntimeException("学号未注册，请先注册");
        }
        if (student.getPassword() == null) {
            throw new RuntimeException("该账号未设置密码，请先通过活动签到注册");
        }
        if (!passwordEncoder.matches(request.getPassword(), student.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        if ("1".equals(student.getStatus())) {
            throw new RuntimeException("账号已被停用");
        }

        return issueToken(student, null);
    }

    // ... existing code ...
    private StudentTokenResponse issueToken(GkzhStudent student, Long activityId) {
        StudentCheckin studentCheckin = new StudentCheckin();
        studentCheckin.setActivityId(activityId);
        studentCheckin.setLoginTime(System.currentTimeMillis());
        studentCheckin.setIpaddr(IpUtils.getIpAddr());
        studentCheckin.setStuId(student.getStudentId());
        studentCheckin.setStuName(student.getStudentName());
        studentCheckin.setDeptId(student.getDepartmentId());
        studentCheckin.setStuNo(student.getStudentNo());
        studentCheckin.setUserId(student.getUserId());
        String token = stuTokenService.createToken(studentCheckin);

        // 查询专业名称
        String departmentName = null;
        if (student.getDepartmentId() != null) {
            Map<Long, String> pathMap = departmentService.batchGetDepartmentFullPaths(
                    Collections.singletonList(student.getDepartmentId()));
            departmentName = pathMap.get(student.getDepartmentId());
        }

        return new StudentTokenResponse(token, student.getStudentName(), student.getStudentNo(),
                departmentName, student.getGender());    }
// ... existing code ...


}
