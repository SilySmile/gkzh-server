package com.gkzh.app.dto;


/** 学生端抽奖结果，包含中奖记录信息，便于抽奖后直接展示核销码或奖品名称。 */
public class LotteryPrizeResult {
    /** 奖品编号 */
    private Long prizeId;
    /** 奖品名称 */
    private String title;
    /** 奖品图片 */
    private String imageUrl;
    /** 奖品类型：1实物奖品，2虚拟奖品，3谢谢参与 */
    private Integer prizeType;
    /** 是否需核销：1需核销，0无需核销 */
    private String needRedemption;
    /** 本次中奖记录编号 */
    private Long recordId;
    /** 本次中奖核销码 */
    private String redemptionCode;

    public Long getPrizeId() {
        return prizeId;
    }

    public void setPrizeId(Long prizeId) {
        this.prizeId = prizeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getPrizeType() {
        return prizeType;
    }

    public void setPrizeType(Integer prizeType) {
        this.prizeType = prizeType;
    }

    public String getNeedRedemption() {
        return needRedemption;
    }

    public void setNeedRedemption(String needRedemption) {
        this.needRedemption = needRedemption;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public String getRedemptionCode() {
        return redemptionCode;
    }

    public void setRedemptionCode(String redemptionCode) {
        this.redemptionCode = redemptionCode;
    }
}
