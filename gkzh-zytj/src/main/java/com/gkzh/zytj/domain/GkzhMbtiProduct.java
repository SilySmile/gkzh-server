package com.gkzh.zytj.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.gkzh.common.annotation.Excel;
import com.gkzh.common.core.domain.BaseEntity;

/**
 * 职愿探究-MBTI商品对象 gkzh_mbti_product
 * 
 * @author gkzh
 * @date 2026-06-02
 */
public class GkzhMbtiProduct extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 商品ID */
    private Long productId;

    /** 商品名称 */
    @Excel(name = "商品名称")
    private String productName;

    /** 商品图片URL（完整路径） */
    @Excel(name = "商品图片URL", readConverterExp = "完=整路径")
    private String productImage;

    /** MBTI维度：E/I/S/N/T/F/J/P */
    @Excel(name = "MBTI维度：E/I/S/N/T/F/J/P")
    private String mbtiDimension;

    /** 列序号：1=E/I, 2=S/N, 3=T/F, 4=J/P */
    @Excel(name = "列序号：1=E/I, 2=S/N, 3=T/F, 4=J/P")
    private Long columnIndex;

    /** 同列内排序（数字越小越靠前） */
    @Excel(name = "同列内排序", readConverterExp = "数=字越小越靠前")
    private Long sortOrder;

    /** 状态：0=正常 1=停用 */
    @Excel(name = "状态：0=正常 1=停用")
    private String status;

    public void setProductId(Long productId) 
    {
        this.productId = productId;
    }

    public Long getProductId() 
    {
        return productId;
    }

    public void setProductName(String productName) 
    {
        this.productName = productName;
    }

    public String getProductName() 
    {
        return productName;
    }

    public void setProductImage(String productImage) 
    {
        this.productImage = productImage;
    }

    public String getProductImage() 
    {
        return productImage;
    }

    public void setMbtiDimension(String mbtiDimension) 
    {
        this.mbtiDimension = mbtiDimension;
    }

    public String getMbtiDimension() 
    {
        return mbtiDimension;
    }

    public void setColumnIndex(Long columnIndex) 
    {
        this.columnIndex = columnIndex;
    }

    public Long getColumnIndex() 
    {
        return columnIndex;
    }

    public void setSortOrder(Long sortOrder) 
    {
        this.sortOrder = sortOrder;
    }

    public Long getSortOrder() 
    {
        return sortOrder;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("productId", getProductId())
            .append("productName", getProductName())
            .append("productImage", getProductImage())
            .append("mbtiDimension", getMbtiDimension())
            .append("columnIndex", getColumnIndex())
            .append("sortOrder", getSortOrder())
            .append("status", getStatus())
            .append("remark", getRemark())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
