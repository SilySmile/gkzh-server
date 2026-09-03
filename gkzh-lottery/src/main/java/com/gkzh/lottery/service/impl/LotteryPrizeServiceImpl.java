package com.gkzh.lottery.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.security.SecureRandom;
import java.math.BigDecimal;
import java.math.RoundingMode;

import com.gkzh.activity.domain.GkzhActivityParticipationRecord;
import com.gkzh.activity.service.IGkzhActivityParticipationRecordService;
import com.gkzh.activity.service.IGkzhActivityService;
import com.gkzh.common.core.domain.model.StudentCheckin;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.lottery.domain.LotteryRecord;
import com.gkzh.lottery.mapper.LotteryRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.gkzh.lottery.mapper.LotteryPrizeMapper;
import com.gkzh.lottery.domain.LotteryPrize;
import com.gkzh.lottery.service.ILotteryPrizeService;

/**
 * 抽奖奖品Service业务层处理
 * 
 * @author gkzh
 * @date 2025-06-17
 */
@Service
public class LotteryPrizeServiceImpl implements ILotteryPrizeService 
{
    private static final String REDEMPTION_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom REDEMPTION_RANDOM = new SecureRandom();

    private String createRedemptionCode() {
        StringBuilder code = new StringBuilder("GKZH-");
        for (int i = 0; i < 10; i++) code.append(REDEMPTION_CHARS.charAt(REDEMPTION_RANDOM.nextInt(REDEMPTION_CHARS.length())));
        return code.toString();
    }
    @Autowired
    private LotteryPrizeMapper lotteryPrizeMapper;

    @Autowired
    private IGkzhActivityService gkzhActivityService;

    @Autowired
    private LotteryRecordMapper lotteryRecordMapper;

    @Autowired
    private IGkzhActivityParticipationRecordService activityParticipationRecordService;
    /**
     * 查询抽奖奖品
     * 
     * @param prizeId 抽奖奖品主键
     * @return 抽奖奖品
     */
    @Override
    public LotteryPrize selectLotteryPrizeByPrizeId(Long prizeId)
    {
        return lotteryPrizeMapper.selectLotteryPrizeByPrizeId(prizeId);
    }

    /**
     * 查询抽奖奖品列表
     * 
     * @param lotteryPrize 抽奖奖品
     * @return 抽奖奖品
     */
    @Override
    public List<LotteryPrize> selectLotteryPrizeList(LotteryPrize lotteryPrize)
    {
        return lotteryPrizeMapper.selectLotteryPrizeList(lotteryPrize);
    }

    /**
     * 查询活动奖品列表（包含实际概率计算）
     * 
     * @param activityId 活动ID
     * @return 抽奖奖品集合
     */
    @Override
    public List<LotteryPrize> selectActivityPrizesWithProbability(Long activityId)
    {
        //需要根据活动ID找到对应的抽奖活动的id

        List list = gkzhActivityService.selectActivityModulesByActivityId(activityId);


        for (Object o : list) {
            if (o instanceof Map) {
                Map module = (Map) o;
                if ("lottery".equals(module.get("type"))) {
                    Map config = (Map) module.get("config");
                    if (config != null && config.get("lotteryId") != null) {
                        Long lotteryActivityId = Long.valueOf(config.get("lotteryId").toString());
                        LotteryPrize queryPrize = new LotteryPrize();
                        queryPrize.setLotteryId(lotteryActivityId);
                        queryPrize.setIsEnabled("0");

                        List<LotteryPrize> prizes = lotteryPrizeMapper.selectLotteryPrizeList(queryPrize);
                        return calculateActualProbability(prizes);
                    }
                }
            }
        }

        return null;
    }

    /**
     * 根据权重随机选择奖品
     * 
     * @param activityId 活动ID
     * @return 中奖奖品
     */
    @Override
    public LotteryPrize selectRandomPrizeByWeight(Long activityId)
    {
        List<LotteryPrize> prizes = selectActivityPrizesWithProbability(activityId);
        if (prizes == null || prizes.isEmpty()) {
            return null;
        }

        // 计算总权重
        int totalWeight = prizes.stream()
                .mapToInt(prize -> prize.getWeight() != null ? prize.getWeight() : 1)
                .sum();

        if (totalWeight <= 0) {
            return null;
        }

        // 生成随机数
        Random random = new Random();
        int randomValue = random.nextInt(totalWeight) + 1;

        // 根据权重选择奖品
        int currentWeight = 0;
        for (LotteryPrize prize : prizes) {
            currentWeight += prize.getWeight() != null ? prize.getWeight() : 1;
            if (randomValue <= currentWeight) {
                if(prize.getStock() <= 0) {
                    return prizes.get(prizes.size() - 1);
                }
                return prize;
            }
        }

        // 兜底返回最后一个奖品
        return prizes.get(prizes.size() - 1);
    }

    /**
     * 计算奖品实际概率
     * 
     * @param prizes 奖品列表
     * @return 包含实际概率的奖品列表
     */
    @Override
    public List<LotteryPrize> calculateActualProbability(List<LotteryPrize> prizes)
    {
        if (prizes.isEmpty()) {
            return prizes;
        }

        // 计算总权重
        int totalWeight = prizes.stream()
                .mapToInt(prize -> prize.getWeight() != null ? prize.getWeight() : 1)
                .sum();

        if (totalWeight <= 0) {
            return prizes;
        }

        // 计算每个奖品的实际概率
        for (LotteryPrize prize : prizes) {
            int weight = prize.getWeight() != null ? prize.getWeight() : 1;
            BigDecimal probability = BigDecimal.valueOf(weight)
                    .divide(BigDecimal.valueOf(totalWeight), 4, RoundingMode.HALF_UP);
            prize.setActualProbability(probability);
        }

        return prizes;
    }

    /**
     * 新增抽奖奖品
     * 
     * @param lotteryPrize 抽奖奖品
     * @return 结果
     */
    @Override
    public int insertLotteryPrize(LotteryPrize lotteryPrize)
    {
        return lotteryPrizeMapper.insertLotteryPrize(lotteryPrize);
    }

    /**
     * 修改抽奖奖品
     * 
     * @param lotteryPrize 抽奖奖品
     * @return 结果
     */
    @Override
    public int updateLotteryPrize(LotteryPrize lotteryPrize)
    {
        return lotteryPrizeMapper.updateLotteryPrize(lotteryPrize);
    }

    /**
     * 批量删除抽奖奖品
     * 
     * @param prizeIds 需要删除的抽奖奖品主键
     * @return 结果
     */
    @Override
    public int deleteLotteryPrizeByPrizeIds(Long[] prizeIds)
    {
        return lotteryPrizeMapper.deleteLotteryPrizeByPrizeIds(prizeIds);
    }

    /**
     * 删除抽奖奖品信息
     * 
     * @param prizeId 抽奖奖品主键
     * @return 结果
     */
    @Override
    public int deleteLotteryPrizeByPrizeId(Long prizeId)
    {
        return lotteryPrizeMapper.deleteLotteryPrizeByPrizeId(prizeId);
    }


    @Override
    /**
     * 参与抽奖
     *
     * */
    public LotteryPrize drawPrize(Long activityId, StudentCheckin studentCheckin)
    {
        //判断当前用户是否已经参加过当前活动的抽奖环节
        GkzhActivityParticipationRecord r = activityParticipationRecordService.selectGkzhActivityParticipationRecordByUserIdAndActivityId(studentCheckin.getUserId(), activityId,3);
        if(r != null)
        {
            throw new ServiceException("您已经参与过抽奖环节，请勿重复参与！");
        }

        LotteryPrize lotteryPrize = selectRandomPrizeByWeight(activityId);
        if(lotteryPrize != null)
        {
            lotteryPrize.setStock(lotteryPrize.getStock() - 1);
            updateLotteryPrize(lotteryPrize);

            //添加中奖记录
            LotteryRecord record = new LotteryRecord();
            record.setPrizeId(lotteryPrize.getPrizeId());
            record.setLotteryId(lotteryPrize.getLotteryId());
            record.setActivityId(activityId);
            record.setPrizeTitle(lotteryPrize.getTitle());
            record.setUserId(studentCheckin.getUserId());
            record.setUserName(studentCheckin.getStuName());
            record.setDrawTime(DateUtils.getNowDate());
            record.setCreateTime(DateUtils.getNowDate());
            record.setRedemptionCode(createRedemptionCode());
            lotteryRecordMapper.insertLotteryRecord(record);
            Long recordId = record.getRecordId();

            //添加参与活动记录
            GkzhActivityParticipationRecord activityRecord = new GkzhActivityParticipationRecord();
            activityRecord.setModuleId(Long.valueOf(recordId));
            activityRecord.setActivityId(activityId);
            activityRecord.setUserId(studentCheckin.getUserId());
            activityRecord.setUserCode(studentCheckin.getStuNo());
            activityRecord.setUserName(studentCheckin.getStuName());
            activityRecord.setParticipationType(3);
            activityRecord.setParticipationTime(DateUtils.getNowDate());
            activityRecord.setResult("抽奖完成");
            activityRecord.setStatus(1);
            activityParticipationRecordService.insertGkzhActivityParticipationRecord(activityRecord);

            return lotteryPrize;
        }
        return null;
    }

    @Override
    public List<LotteryPrize> selectPrizesByLotteryId(Long lotteryId) {
        LotteryPrize queryPrize = new LotteryPrize();
        queryPrize.setLotteryId(lotteryId);
        queryPrize.setIsEnabled("0");
        return calculateActualProbability(lotteryPrizeMapper.selectLotteryPrizeList(queryPrize));
    }

    @Override
    public LotteryPrize drawPrizeByLottery(Long lotteryId, StudentCheckin studentCheckin, String bizType, Long activityId) {
        List<LotteryPrize> prizes = selectPrizesByLotteryId(lotteryId);
        if (prizes == null || prizes.isEmpty()) {
            return null;
        }
        int totalWeight = prizes.stream().mapToInt(prize -> prize.getWeight() != null ? prize.getWeight() : 1).sum();
        if (totalWeight <= 0) {
            return null;
        }
        Random random = new Random();
        int randomValue = random.nextInt(totalWeight) + 1;
        LotteryPrize lotteryPrize = null;
        int currentWeight = 0;
        for (LotteryPrize prize : prizes) {
            currentWeight += prize.getWeight() != null ? prize.getWeight() : 1;
            if (randomValue <= currentWeight) {
                if (prize.getStock() != null && prize.getStock() <= 0) {
                    lotteryPrize = prizes.get(prizes.size() - 1);
                } else {
                    lotteryPrize = prize;
                }
                break;
            }
        }
        if (lotteryPrize == null) {
            lotteryPrize = prizes.get(prizes.size() - 1);
        }
        lotteryPrize.setStock(lotteryPrize.getStock() - 1);
        updateLotteryPrize(lotteryPrize);

        LotteryRecord record = new LotteryRecord();
        record.setPrizeId(lotteryPrize.getPrizeId());
        record.setLotteryId(lotteryId);
        record.setActivityId(activityId);
        record.setBizType(bizType);
        record.setPrizeTitle(lotteryPrize.getTitle());
        record.setUserId(studentCheckin.getUserId());
        record.setUserName(studentCheckin.getStuName());
        record.setDrawTime(DateUtils.getNowDate());
        record.setCreateTime(DateUtils.getNowDate());
        record.setRedemptionCode(createRedemptionCode());
        lotteryRecordMapper.insertLotteryRecord(record);
        return lotteryPrize;
    }
}
