package com.gkzh.zdyp.service;

import com.gkzh.zdyp.domain.ZdypCard;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 职得一拼卡片乱序回归。
 * 历史实现用「31 * hash + 字符」累加，同一前缀的编号（41、42、43、44）排序键只差最后一位，
 * 导致每局顺序都等于原顺序，等于没有随机；这里锁定修复后的行为。
 */
public class ZdypCardOrderTest {

    private List<ZdypCard> cards() {
        List<ZdypCard> cards = new ArrayList<>();
        for (long id = 41L; id <= 44L; id++) {
            ZdypCard card = new ZdypCard();
            card.setCardId(id);
            card.setDimensionCode("content");
            cards.add(card);
        }
        return cards;
    }

    private String orderOf(long seed) {
        StringBuilder builder = new StringBuilder();
        for (ZdypCard card : ZdypGameService.orderedCards(cards(), seed)) {
            builder.append(card.getCardId()).append(',');
        }
        return builder.toString();
    }

    @Test
    void differentAttemptsProduceDifferentOrders() {
        Set<String> orders = new LinkedHashSet<>();
        for (long seed = 1L; seed <= 8L; seed++) {
            orders.add(orderOf(seed));
        }
        assertTrue(orders.size() >= 3, "8 次尝试至少应出现 3 种排列，实际：" + orders);
    }

    @Test
    void orderIsNotAlwaysTheOriginalOrder() {
        // 旧实现的排列恒为 41,42,43,44
        String original = "41,42,43,44,";
        int changed = 0;
        for (long seed = 1L; seed <= 8L; seed++) {
            if (!original.equals(orderOf(seed))) {
                changed++;
            }
        }
        assertTrue(changed >= 4, "8 次尝试中至少 4 次应偏离原顺序，实际 " + changed);
    }

    @Test
    void sameSeedIsStable() {
        assertEquals(orderOf(7L), orderOf(7L));
    }

    @Test
    void allCardsKept() {
        List<ZdypCard> ordered = ZdypGameService.orderedCards(cards(), 3L);
        assertEquals(4, ordered.size());
        Set<Long> ids = new LinkedHashSet<>();
        for (ZdypCard card : ordered) {
            ids.add(card.getCardId());
        }
        assertEquals(4, ids.size());
    }
}
