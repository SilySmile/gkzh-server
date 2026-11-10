package com.gkzh.app.service;

import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.zdyp.domain.ZdypAnswer;
import com.gkzh.zdyp.domain.ZdypAttempt;
import com.gkzh.zdyp.domain.ZdypCard;
import com.gkzh.zdyp.domain.ZdypCareer;
import com.gkzh.zdyp.domain.ZdypRecord;
import com.gkzh.zdyp.domain.ZdypSetting;
import com.gkzh.zdyp.domain.dto.ZdypPlacement;
import com.gkzh.zdyp.mapper.ZdypAnswerMapper;
import com.gkzh.zdyp.mapper.ZdypAttemptMapper;
import com.gkzh.zdyp.mapper.ZdypRecordMapper;
import com.gkzh.zdyp.service.ZdypBehavior;
import com.gkzh.zdyp.service.ZdypConfigService;
import com.gkzh.zdyp.service.ZdypGameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 职得一拼提交判定回归。
 * 覆盖全部放对通关、职业缺点放错归为优势偏好、超时判未通关、卡片重复与失效拒绝、
 * 以及重复提交幂等不重复累计错误次数。
 */
public class ZdypGameServiceTest {

    private static final Long RECORD_ID = 1L;
    private static final Long USER_ID = 7L;
    private static final Long CAREER_ID = 1L;
    private static final Long CARD_CONTENT = 11L;
    private static final Long CARD_SKILL = 12L;
    private static final Long CARD_WEAKNESS = 13L;
    private static final Long CARD_PROSPECT = 14L;

    private ZdypRecordMapper recordMapper;
    private ZdypAttemptMapper attemptMapper;
    private ZdypAnswerMapper answerMapper;
    private ZdypConfigService configService;
    private ZdypGameService service;
    private ZdypRecord record;
    private ZdypAttempt attempt;
    private List<ZdypAnswer> firstAttemptAnswers;

    @BeforeEach
    void setUp() {
        recordMapper = mock(ZdypRecordMapper.class);
        attemptMapper = mock(ZdypAttemptMapper.class);
        answerMapper = mock(ZdypAnswerMapper.class);
        configService = mock(ZdypConfigService.class);
        IActivityWeekService activityWeekService = mock(IActivityWeekService.class);
        service = new ZdypGameService(recordMapper, attemptMapper, answerMapper, configService, activityWeekService);

        ZdypSetting setting = new ZdypSetting();
        setting.setSettingId(1);
        setting.setTimeLimit(45);
        setting.setEnabled("1");
        when(configService.getSetting()).thenReturn(setting);

        ZdypCareer career = new ZdypCareer();
        career.setCareerId(CAREER_ID);
        career.setCareerCode("big_data_engineer");
        career.setCareerName("大数据工程技术人员");
        career.setIntro("岗位定义");
        career.setIndustryValue("行业价值");
        career.setNewcomerAdvice("入门建议");
        when(configService.findActiveCareer(CAREER_ID)).thenReturn(career);

        List<ZdypCard> cards = new ArrayList<>();
        cards.add(card(CARD_CONTENT, "content", "工作内容卡片"));
        cards.add(card(CARD_SKILL, "skill", "核心技能卡片"));
        cards.add(card(CARD_WEAKNESS, "weakness", "职业缺点卡片"));
        cards.add(card(CARD_PROSPECT, "prospect", "发展前景卡片"));
        when(configService.listActiveCards(CAREER_ID)).thenReturn(cards);
        when(configService.listAllCards(CAREER_ID)).thenReturn(cards);
        Map<String, ZdypCard> byDimension = new LinkedHashMap<>();
        for (ZdypCard item : cards) {
            byDimension.put(item.getDimensionCode(), item);
        }
        when(configService.cardsByDimension(CAREER_ID)).thenReturn(byDimension);

        record = new ZdypRecord();
        record.setRecordId(RECORD_ID);
        record.setUserId(USER_ID);
        record.setGameType("zdyp");
        record.setStatus("participating");
        record.setAttemptNo(1);
        record.setFirstAttemptCorrect("0");
        record.setTotalWrong(0);
        record.setTimeoutCount(0);
        record.setCareerId(CAREER_ID);
        record.setCareerCode("big_data_engineer");
        record.setCareerName("大数据工程技术人员");
        when(recordMapper.selectOne(any())).thenReturn(record);

        attempt = new ZdypAttempt();
        attempt.setAttemptId(101L);
        attempt.setRecordId(RECORD_ID);
        attempt.setAttemptNo(1);
        attempt.setCareerId(CAREER_ID);
        attempt.setCorrectCount(0);
        attempt.setWrongCount(0);
        attempt.setFilledCount(0);
        attempt.setIsTimeout("0");
        attempt.setIsPass("0");
        attempt.setCreateTime(new Date(System.currentTimeMillis() - 15_000L));
        when(attemptMapper.selectOne(any())).thenReturn(attempt);

        firstAttemptAnswers = new ArrayList<>();
        when(answerMapper.selectList(any())).thenReturn(firstAttemptAnswers);
        when(answerMapper.insert(any(ZdypAnswer.class))).thenReturn(1);
    }

    @Test
    void fourCorrectCardsPassAndFinishRecord() {
        Map<String, Object> result = service.submit(RECORD_ID, USER_ID, 1, List.of(
                placement("content", CARD_CONTENT),
                placement("skill", CARD_SKILL),
                placement("weakness", CARD_WEAKNESS),
                placement("prospect", CARD_PROSPECT)));

        assertEquals(Boolean.TRUE, result.get("pass"));
        assertEquals("finished", record.getStatus());
        assertEquals("balanced", record.getBehaviorType());
        assertEquals("全面均衡型", record.getBehaviorName());
        assertNotNull(record.getConclusion());
        assertNotNull(record.getFinishTime());
        assertEquals(4, attempt.getCorrectCount());
        assertEquals(0, attempt.getWrongCount());
        verify(answerMapper, times(4)).insert(any(ZdypAnswer.class));
    }

    @Test
    void weaknessMistakeIsAdvantageBehavior() {
        // 职业缺点卡放进了发展前景槽，发展前景卡放进了职业缺点槽：缺点板块放错且最集中。
        firstAttemptAnswers.add(answer("content", "1"));
        firstAttemptAnswers.add(answer("skill", "1"));
        firstAttemptAnswers.add(answer("weakness", "0"));

        Map<String, Object> result = service.submit(RECORD_ID, USER_ID, 1, List.of(
                placement("content", CARD_CONTENT),
                placement("skill", CARD_SKILL),
                placement("weakness", CARD_PROSPECT),
                placement("prospect", CARD_WEAKNESS)));

        assertEquals(Boolean.FALSE, result.get("pass"));
        assertEquals(2, attempt.getWrongCount());
        // 未通关不写完成状态，学生可继续重闯。
        assertEquals("participating", record.getStatus());
        assertNull(record.getBehaviorType());
    }

    @Test
    void timeoutFailsEvenWhenAllCardsCorrect() {
        attempt.setCreateTime(new Date(System.currentTimeMillis() - 60_000L));

        Map<String, Object> result = service.submit(RECORD_ID, USER_ID, 1, List.of(
                placement("content", CARD_CONTENT),
                placement("skill", CARD_SKILL),
                placement("weakness", CARD_WEAKNESS),
                placement("prospect", CARD_PROSPECT)));

        assertEquals(Boolean.FALSE, result.get("pass"));
        assertEquals(Boolean.TRUE, result.get("timeout"));
        assertEquals("participating", record.getStatus());
        assertEquals(1, record.getTimeoutCount());
        assertEquals("1", attempt.getIsTimeout());
    }

    @Test
    void duplicateCardIsRejected() {
        ServiceException error = assertThrows(ServiceException.class, () -> service.submit(
                RECORD_ID, USER_ID, 1, List.of(
                        placement("content", CARD_CONTENT),
                        placement("skill", CARD_CONTENT))));
        assertTrue(error.getMessage().contains("同一张卡片"), error.getMessage());
        verify(answerMapper, never()).insert(any(ZdypAnswer.class));
    }

    @Test
    void unknownCardIsRejected() {
        ServiceException error = assertThrows(ServiceException.class, () -> service.submit(
                RECORD_ID, USER_ID, 1, List.of(placement("content", 999L))));
        assertTrue(error.getMessage().contains("卡片已失效"), error.getMessage());
    }

    @Test
    void alreadySubmittedAttemptIsIdempotent() {
        attempt.setSubmitTime(new Date());
        attempt.setIsPass("1");

        Map<String, Object> result = service.submit(RECORD_ID, USER_ID, 1, List.of(
                placement("content", CARD_CONTENT)));

        assertEquals(Boolean.TRUE, result.get("pass"));
        verify(answerMapper, never()).insert(any(ZdypAnswer.class));
        assertEquals(0, record.getTotalWrong());
    }

    @Test
    void wrongCareerCodeCannotStart() {
        assertThrows(ServiceException.class, () -> service.start(RECORD_ID, USER_ID, "not_exists"));
        assertFalse("finished".equals(record.getStatus()));
    }

    private ZdypCard card(Long cardId, String dimension, String text) {
        ZdypCard card = new ZdypCard();
        card.setCardId(cardId);
        card.setDimensionCode(dimension);
        card.setCardText(text);
        card.setStatus("0");
        return card;
    }

    private ZdypPlacement placement(String slotCode, Long cardId) {
        ZdypPlacement placement = new ZdypPlacement();
        placement.setSlotCode(slotCode);
        placement.setCardId(cardId);
        placement.setElapsedMs(3_000L);
        return placement;
    }

    private ZdypAnswer answer(String slotCode, String correct) {
        ZdypAnswer answer = new ZdypAnswer();
        answer.setSlotCode(slotCode);
        answer.setIsCorrect(correct);
        return answer;
    }
}
