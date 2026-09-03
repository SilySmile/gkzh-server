package com.gkzh.school.service.impl;

import java.util.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gkzh.common.core.domain.entity.SysUser;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.common.utils.SecurityUtils;
import com.gkzh.common.utils.StringUtils;
import com.gkzh.common.utils.bean.BeanValidators;
import com.gkzh.common.utils.spring.SpringUtils;
import com.gkzh.school.domain.GkzhSchoolDepartment;
import com.gkzh.system.mapper.SysUserMapper;
import com.gkzh.system.service.ISysConfigService;
import com.gkzh.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.gkzh.school.mapper.GkzhStudentMapper;
import com.gkzh.school.domain.GkzhStudent;
import com.gkzh.school.service.IGkzhStudentService;
import com.gkzh.school.service.IGkzhSchoolDepartmentService;

import javax.validation.Validator;

/**
 * 学生Service业务层处理
 * 
 * @author gkzh
 * @date 2025-06-19
 */
@Slf4j
@Service
public class GkzhStudentServiceImpl implements IGkzhStudentService {
    @Autowired
    private GkzhStudentMapper gkzhStudentMapper;
    @Autowired
    private IGkzhSchoolDepartmentService departmentService;
    @Autowired
    private ISysUserService userService;
    @Autowired
    private ISysConfigService configService;
    @Autowired
    protected Validator validator;
    /**
     * 查询学生
     * 
     * @param studentId 学生主键
     * @return 学生
     */
    @Override
    public GkzhStudent selectGkzhStudentByStudentId(Long studentId) {
        return gkzhStudentMapper.selectGkzhStudentByStudentId(studentId);
    }

    /**
     * 查询学生列表
     * 
     * @param gkzhStudent 学生
     * @return 学生
     */
    @Override
    public List<GkzhStudent> selectGkzhStudentList(GkzhStudent gkzhStudent) {
        return gkzhStudentMapper.selectGkzhStudentList(gkzhStudent);
    }

    /**
     * 新增学生
     * 
     * @param gkzhStudent 学生
     * @return 结果
     */
    @Override
    public int insertGkzhStudent(GkzhStudent gkzhStudent) {
        validateStudentForSave(gkzhStudent);
        gkzhStudent.setCreateTime(DateUtils.getNowDate());
        int i = gkzhStudentMapper.insertGkzhStudent(gkzhStudent);
        if(i > 0){
            //生成用户
            SysUser user = new SysUser();
            user.setUserName(gkzhStudent.getStudentNo());
            user.setNickName(gkzhStudent.getStudentName());
            user.setEmail(gkzhStudent.getEmail());
            String password = configService.selectConfigByKey("sys.user.initPassword");
            user.setPassword(SecurityUtils.encryptPassword(password));
            user.setCreateBy(gkzhStudent.getCreateBy());
            user.setPhonenumber(gkzhStudent.getPhone());
            user.setSex(gkzhStudent.getGender());
            user.setUserType("01");
            userService.insertUser(user);
            gkzhStudent.setUserId(user.getUserId());
            gkzhStudentMapper.updateGkzhStudent(gkzhStudent);
        }
        return i;
    }

    /**
     * 修改学生
     * 
     * @param gkzhStudent 学生
     * @return 结果
     */
    @Override
    public int updateGkzhStudent(GkzhStudent gkzhStudent) {
        if (gkzhStudent.getStudentId() == null) {
            throw new ServiceException("学生ID不能为空");
        }
        validateStudentForSave(gkzhStudent);
        gkzhStudent.setUpdateTime(DateUtils.getNowDate());
        Long userId = gkzhStudent.getUserId();
        if(userId != null){
            SysUser user = new SysUser();
            user.setUserId(userId);
            user.setNickName(gkzhStudent.getStudentName());
            user.setEmail(gkzhStudent.getEmail());
            user.setPhonenumber(gkzhStudent.getPhone());
            user.setSex(gkzhStudent.getGender());
            user.setUpdateBy(SecurityUtils.getUsername());
            userService.updateUser(user);
        }
        return gkzhStudentMapper.updateGkzhStudent(gkzhStudent);
    }

    /** Web 新增、修改时校验学校专业归属及“学校 + 学号”唯一性。 */
    private void validateStudentForSave(GkzhStudent student) {
        if (student.getSchoolId() == null || student.getDepartmentId() == null) {
            throw new ServiceException("请选择学校、院系和专业");
        }
        if (StringUtils.isEmpty(student.getStudentNo())) {
            throw new ServiceException("学号不能为空");
        }
        GkzhSchoolDepartment department = departmentService.selectDepartmentById(student.getDepartmentId());
        if (department == null || !Objects.equals(student.getSchoolId(), department.getSchoolId())
                || !"0".equals(department.getDelFlag())) {
            throw new ServiceException("所选专业不属于当前学校，请重新选择");
        }

        QueryWrapper<GkzhStudent> duplicateQuery = Wrappers.query();
        duplicateQuery.eq("school_id", student.getSchoolId())
                .eq("student_no", student.getStudentNo().trim())
                .eq("del_flag", "0");
        if (student.getStudentId() != null) {
            duplicateQuery.ne("student_id", student.getStudentId());
        }
        if (gkzhStudentMapper.selectCount(duplicateQuery) > 0) {
            throw new ServiceException("该学校下学号已存在");
        }
        student.setStudentNo(student.getStudentNo().trim());
    }

    /**
     * 批量删除学生
     * 
     * @param studentIds 需要删除的学生主键
     * @return 结果
     */
    @Override
    public int deleteGkzhStudentByStudentIds(Long[] studentIds) {
        QueryWrapper<GkzhStudent> query = Wrappers.query();
        query.in("student_id", studentIds);
        List<GkzhStudent> gkzhStudents = gkzhStudentMapper.selectList(query);
        ArrayList<Long> userIds = new ArrayList<>();
        for (GkzhStudent gkzhStudent : gkzhStudents) {
            Long userId = gkzhStudent.getUserId();
            if(userId != null){
                userIds.add(userId);
            }
        }

        SysUserMapper userMapper = SpringUtils.getBean(SysUserMapper.class);
        QueryWrapper<SysUser> query1 = Wrappers.query();
        query1.in("user_id", userIds);
        SysUser sysUser = new SysUser();
        sysUser.setDelFlag("2");
        userMapper.update(sysUser,query1);

        GkzhStudent gkzhStudent = new GkzhStudent();
        gkzhStudent.setDelFlag("2");
        return gkzhStudentMapper.update(gkzhStudent, query);
    }

    /**
     * 删除学生信息
     * 
     * @param studentId 学生主键
     * @return 结果
     */
    @Override
    public int deleteGkzhStudentByStudentId(Long studentId) {
        return gkzhStudentMapper.deleteById(studentId);
    }

    /**
     * 根据签到信息查询学生
     * 
     * @param gkzhStudent 学生信息
     * @return 学生
     */
    @Override
    public GkzhStudent selectGkzhStudentByCheckinInfo(GkzhStudent gkzhStudent) {
        return gkzhStudentMapper.selectGkzhStudentByCheckinInfo(gkzhStudent);
    }

    @Override
    public String importStudent(List<GkzhStudent> stuList, Boolean isUpdateSupport, String operName) {
        if (StringUtils.isNull(stuList) || stuList.size() == 0) {
            throw new ServiceException("导入学生数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();

        for (GkzhStudent student : stuList) {
            try {
                // 验证学生信息
                BeanValidators.validateWithException(validator, student);

                // 根据学校、院系、专业识别学生的department_id
                GkzhSchoolDepartment department = gkzhStudentMapper.selectDepartmentBySchoolAndCollegeAndDepartment(
                        student.getSchoolName(), student.getCollegeName(), student.getDepartmentName());

                if (department == null) {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、学生 " + student.getStudentName() +
                            " 识别院系专业失败（学校：" + student.getSchoolName() +
                            "，院系：" + student.getCollegeName() +
                            "，专业：" + student.getDepartmentName() + "）");
                    continue;
                }
                // 设置department_id
                student.setSchoolId(department.getSchoolId());
                student.setDepartmentId(department.getDepartmentId());

                // 检查在相同school_id下是否存在学号相同的学生

                QueryWrapper<GkzhStudent> query = Wrappers.query();
                query.eq("school_id", student.getSchoolId());
                query.eq("student_no", student.getStudentNo());
                GkzhStudent existingStudent = gkzhStudentMapper.selectOne(query);

                if (StringUtils.isNull(existingStudent)) {
                    // 新增学生
                    student.setStatus("0");
                    student.setCreateBy(operName);
                    student.setCreateTime(DateUtils.getNowDate());
                    gkzhStudentMapper.insertGkzhStudent(student);

                    SysUser user = new SysUser();

                    user.setUserName(student.getStudentNo());
                    user.setNickName(student.getStudentName());
                    user.setEmail(student.getEmail());
                    String password = configService.selectConfigByKey("sys.user.initPassword");
                    user.setPassword(SecurityUtils.encryptPassword(password));
                    user.setCreateBy(operName);
                    user.setPhonenumber(student.getPhone());
                    user.setSex(student.getGender());
                    user.setUserType("01");
                    userService.insertUser(user);
                    student.setUserId(user.getUserId());
                    gkzhStudentMapper.updateGkzhStudent(student);

                    successNum++;
                    successMsg.append("<br/>" + successNum + "、学生 " + student.getStudentName() + " 导入成功");
                } else if (isUpdateSupport) {
                    // 更新学生信息
                    student.setStudentId(existingStudent.getStudentId());
                    student.setUpdateBy(operName);
                    student.setUpdateTime(DateUtils.getNowDate());
                    gkzhStudentMapper.updateGkzhStudent(student);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、学生 " + student.getStudentName() + " 更新成功");
                } else {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、学生 " + student.getStudentName() + " 已存在");
                }
            } catch (Exception e) {
                failureNum++;
                String msg = "<br/>" + failureNum + "、学生 " + (student.getStudentName() != null ? student.getStudentName() : "未知") + " 导入失败：";
                failureMsg.append(msg + e.getMessage());
                log.error(msg, e);
            }
        }

        if (failureNum > 0) {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new ServiceException(failureMsg.toString());
        } else {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();
    }

}
