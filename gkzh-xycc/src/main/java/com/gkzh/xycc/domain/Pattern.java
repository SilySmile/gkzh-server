package com.gkzh.xycc.domain;

import com.gkzh.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.gkzh.common.annotation.Excel;

/**
 * 心愿橱窗对象 xycc_pattern
 * 
 * @author gkzh
 * @date 2025-06-12
 */
public class Pattern extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long patternId;


    /** 行编号（R、I) */
    @Excel(name = "行编号")
    private String rowCode;
    /** 图案名称，如“激光测距仪” */
    @Excel(name = "图案名称")
    private String description;

    /** 图案图片地址 */
    @Excel(name = "图案图片地址")
    private String imgUrl;

    /** 源素材地址（EPS/SVG） */
    private String materialUrl;

    /** 行号 */
    @Excel(name = "行号")
    private Integer rowIndex;

    /** 列号 */
    @Excel(name = "列号")
    private Integer colIndex;



    public void setPatternId(Long patternId) 
    {
        this.patternId = patternId;
    }

    public Long getPatternId() 
    {
        return patternId;
    }

    public void setRowIndex(Integer rowIndex) 
    {
        this.rowIndex = rowIndex;
    }

    public Integer getRowIndex() 
    {
        return rowIndex;
    }

    public void setRowCode(String rowCode) 
    {
        this.rowCode = rowCode;
    }

    public String getRowCode() 
    {
        return rowCode;
    }

    public void setColIndex(Integer colIndex) 
    {
        this.colIndex = colIndex;
    }

    public Integer getColIndex() 
    {
        return colIndex;
    }

    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getDescription() 
    {
        return description;
    }

    public void setImgUrl(String imgUrl) 
    {
        this.imgUrl = imgUrl;
    }

    public String getImgUrl() 
    {
        return imgUrl;
    }

    public void setMaterialUrl(String materialUrl)
    {
        this.materialUrl = materialUrl;
    }

    public String getMaterialUrl()
    {
        return materialUrl;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("patternId", getPatternId())
            .append("rowIndex", getRowIndex())
            .append("rowCode", getRowCode())
            .append("colIndex", getColIndex())
            .append("description", getDescription())
            .append("imgUrl", getImgUrl())
            .append("materialUrl", getMaterialUrl())
            .toString();
    }
}
