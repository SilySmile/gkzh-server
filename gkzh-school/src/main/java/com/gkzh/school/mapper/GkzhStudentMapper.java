package com.gkzh.school.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gkzh.school.domain.GkzhSchoolDepartment;
import com.gkzh.school.domain.GkzhStudent;
import org.apache.ibatis.annotations.Param;

/**
 * 学生Mapper接口
 * 
 * @author gkzh
 * @date 2025-06-19
 */
public interface GkzhStudentMapper extends BaseMapper<GkzhStudent> {
    /**
     * 查询学生
     * 
     * @param studentId 学生主键
     * @return 学生
     */
    public GkzhStudent selectGkzhStudentByStudentId(Long studentId);

    /**
     * 查询学生列表
     * 
     * @param gkzhStudent 学生
     * @return 学生集合
     */
    public List<GkzhStudent> selectGkzhStudentList(GkzhStudent gkzhStudent);

    /**
     * 新增学生
     * 
     * @param gkzhStudent 学生
     * @return 结果
     */
    public int insertGkzhStudent(GkzhStudent gkzhStudent);

    /**
     * 修改学生
     * 
     * @param gkzhStudent 学生
     * @return 结果
     */
    public int updateGkzhStudent(GkzhStudent gkzhStudent);

    /**
     * 删除学生
     * 
     * @param studentId 学生主键
     * @return 结果
     */
    public int deleteGkzhStudentByStudentId(Long studentId);

    /**
     * 批量删除学生
     * 
     * @param studentIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteGkzhStudentByStudentIds(Long[] studentIds);

    /**
     * 根据学校ID、专业部门ID、学号、姓名查询学生
     * 
     * @param gkzhStudent 学生信息
     * @return 学生
     */
    public GkzhStudent selectGkzhStudentByCheckinInfo(GkzhStudent gkzhStudent);

    public GkzhStudent findByDetails(@Param("deptId") String departmentId, @Param("name") String name, @Param("no") String no);

    /**
     * 根据学校、院系、专业查询部门ID
     *
     * @param schoolName 学校名称
     * @param collegeName 院系名称
     * @param departmentName 专业名称
     * @return 部门ID
     */
    public GkzhSchoolDepartment selectDepartmentBySchoolAndCollegeAndDepartment(@Param("schoolName") String schoolName,
                                                                                @Param("collegeName") String collegeName,
                                                                                @Param("departmentName") String departmentName);

    /**
     * 根据学号和部门ID查询学生
     *
     * @param studentNo 学号
     * @param departmentId 部门ID
     * @return 学生信息
     */
    public GkzhStudent selectGkzhStudentByStudentNoAndDepartmentId(@Param("studentNo") String studentNo,
                                                                   @Param("departmentId") Long departmentId);

} 