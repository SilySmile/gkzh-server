<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="学生ID" prop="studentId">
        <el-input
          v-model="queryParams.studentId"
          placeholder="请输入学生ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="学生姓名" prop="studentName">
        <el-input
          v-model="queryParams.studentName"
          placeholder="请输入学生姓名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="学号" prop="studentNo">
        <el-input
          v-model="queryParams.studentNo"
          placeholder="请输入学号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="活动ID" prop="activityId">
        <el-input
          v-model="queryParams.activityId"
          placeholder="请输入活动ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="生成的MBTI代码" prop="choiceCode">
        <el-input
          v-model="queryParams.choiceCode"
          placeholder="请输入生成的MBTI代码"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="选择的商品ID" prop="productIds">
        <el-input
          v-model="queryParams.productIds"
          placeholder="请输入选择的商品ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="用时" prop="choiceTime">
        <el-input
          v-model="queryParams.choiceTime"
          placeholder="请输入用时"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="是否已兑换盲盒：0=否 1=是" prop="isRedeemed">
        <el-input
          v-model="queryParams.isRedeemed"
          placeholder="请输入是否已兑换盲盒：0=否 1=是"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="兑换时间" prop="redeemTime">
        <el-date-picker clearable
          v-model="queryParams.redeemTime"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择兑换时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['lottery:choice:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['lottery:choice:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['lottery:choice:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['lottery:choice:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="choiceList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="选择ID" align="center" prop="choiceId" />
      <el-table-column label="学生ID" align="center" prop="studentId" />
      <el-table-column label="学生姓名" align="center" prop="studentName" />
      <el-table-column label="学号" align="center" prop="studentNo" />
      <el-table-column label="活动ID" align="center" prop="activityId" />
      <el-table-column label="生成的MBTI代码" align="center" prop="choiceCode" />
      <el-table-column label="选择的商品ID" align="center" prop="productIds" />
      <el-table-column label="用时" align="center" prop="choiceTime" />
      <el-table-column label="是否已兑换盲盒：0=否 1=是" align="center" prop="isRedeemed" />
      <el-table-column label="兑换时间" align="center" prop="redeemTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.redeemTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['lottery:choice:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['lottery:choice:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改职愿探究-学生选择记录对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="学生ID" prop="studentId">
          <el-input v-model="form.studentId" placeholder="请输入学生ID" />
        </el-form-item>
        <el-form-item label="学生姓名" prop="studentName">
          <el-input v-model="form.studentName" placeholder="请输入学生姓名" />
        </el-form-item>
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="form.studentNo" placeholder="请输入学号" />
        </el-form-item>
        <el-form-item label="活动ID" prop="activityId">
          <el-input v-model="form.activityId" placeholder="请输入活动ID" />
        </el-form-item>
        <el-form-item label="生成的MBTI代码" prop="choiceCode">
          <el-input v-model="form.choiceCode" placeholder="请输入生成的MBTI代码" />
        </el-form-item>
        <el-form-item label="选择的商品ID" prop="productIds">
          <el-input v-model="form.productIds" placeholder="请输入选择的商品ID" />
        </el-form-item>
        <el-form-item label="用时" prop="choiceTime">
          <el-input v-model="form.choiceTime" placeholder="请输入用时" />
        </el-form-item>
        <el-form-item label="是否已兑换盲盒：0=否 1=是" prop="isRedeemed">
          <el-input v-model="form.isRedeemed" placeholder="请输入是否已兑换盲盒：0=否 1=是" />
        </el-form-item>
        <el-form-item label="兑换时间" prop="redeemTime">
          <el-date-picker clearable
            v-model="form.redeemTime"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择兑换时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listChoice, getChoice, delChoice, addChoice, updateChoice } from "@/api/lottery/choice"

export default {
  name: "Choice",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 职愿探究-学生选择记录表格数据
      choiceList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        studentId: null,
        studentName: null,
        studentNo: null,
        activityId: null,
        choiceCode: null,
        productIds: null,
        choiceTime: null,
        isRedeemed: null,
        redeemTime: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        studentId: [
          { required: true, message: "学生ID不能为空", trigger: "blur" }
        ],
        activityId: [
          { required: true, message: "活动ID不能为空", trigger: "blur" }
        ],
        choiceCode: [
          { required: true, message: "生成的MBTI代码不能为空", trigger: "blur" }
        ],
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询职愿探究-学生选择记录列表 */
    getList() {
      this.loading = true
      listChoice(this.queryParams).then(response => {
        this.choiceList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        choiceId: null,
        studentId: null,
        studentName: null,
        studentNo: null,
        activityId: null,
        choiceCode: null,
        productIds: null,
        choiceTime: null,
        isRedeemed: null,
        redeemTime: null,
        remark: null,
        createTime: null
      }
      this.resetForm("form")
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.choiceId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加职愿探究-学生选择记录"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const choiceId = row.choiceId || this.ids
      getChoice(choiceId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改职愿探究-学生选择记录"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.choiceId != null) {
            updateChoice(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addChoice(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const choiceIds = row.choiceId || this.ids
      this.$modal.confirm('是否确认删除职愿探究-学生选择记录编号为"' + choiceIds + '"的数据项？').then(function() {
        return delChoice(choiceIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('lottery/choice/export', {
        ...this.queryParams
      }, `choice_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
