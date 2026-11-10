package com.gkzh.zdyp.service;

/**
 * 职得一拼行为分型：按首次尝试（尚未看到答案）的表现判定四类职业认知特征。
 *
 * <p>判定顺序固定，避免同一份数据落入多个类型：</p>
 * <ol>
 *   <li>全面均衡型：首次尝试零错误；</li>
 *   <li>优势偏好型：职业缺点卡槽放错，且其余卡槽错误数不超过它（即缺点板块错得最集中）；</li>
 *   <li>急躁粗放型：首次尝试用时不超过 20 秒且错误数不少于 2；</li>
 *   <li>信息薄弱型：其余情况（多维度错误、用时较长或多次重闯）。</li>
 * </ol>
 *
 * <p>结论与建议文案取自《职业速览拼图.docx》“四类学生专属个性化结论与提升建议”，
 * 首次通关时写入记录快照，后台改文案不影响历史报告。</p>
 */
public final class ZdypBehavior {

    /** 全面均衡型：高正确率、无明显短板、耗时适中。 */
    public static final String BALANCED = "balanced";

    /** 优势偏好型：回避职业缺点、只看优势与前景。 */
    public static final String ADVANTAGE = "advantage";

    /** 信息薄弱型：多板块错配、新业态职业认知储备不足。 */
    public static final String WEAK = "weak";

    /** 急躁粗放型：速度快、错配多、细节甄别不足。 */
    public static final String RUSHED = "rushed";

    /** 急躁粗放型的用时阈值（毫秒）。 */
    private static final long RUSHED_MILLIS = 20_000L;

    private ZdypBehavior() {
    }

    /**
     * 判定行为分型。
     *
     * @param totalWrong    首次尝试放错的卡槽数量（0-4）
     * @param weaknessWrong 首次尝试「职业缺点」卡槽是否放错（0 或 1）
     * @param otherWrong    首次尝试其余卡槽放错的合计数量
     * @param durationMs    首次尝试用时（毫秒），为空按 0 处理
     * @return 行为分型编码
     */
    public static String classify(int totalWrong, int weaknessWrong, int otherWrong, Long durationMs) {
        if (totalWrong <= 0) {
            return BALANCED;
        }
        if (weaknessWrong > 0 && otherWrong <= weaknessWrong) {
            return ADVANTAGE;
        }
        long duration = durationMs == null ? 0L : durationMs;
        if (duration <= RUSHED_MILLIS && totalWrong >= 2) {
            return RUSHED;
        }
        return WEAK;
    }

    /** 行为分型中文名。 */
    public static String name(String type) {
        if (ADVANTAGE.equals(type)) {
            return "优势偏好型";
        }
        if (WEAK.equals(type)) {
            return "信息薄弱型";
        }
        if (RUSHED.equals(type)) {
            return "急躁粗放型";
        }
        return "全面均衡型";
    }

    /** 个人结论文案，取自需求文档原文。 */
    public static String conclusion(String type) {
        if (ADVANTAGE.equals(type)) {
            return "你对新兴职业的优势、前景认知较为清晰，但存在典型的职业认知片面偏差。"
                    + "闯关数据显示，你更容易关注职业优点与发展红利，回避、忽视职业缺点与岗位压力，职业信息加工维度缺失。"
                    + "在未来择业中，容易因过度美化职业、忽略岗位风险，出现择业预期不符、就业适配落差等问题。";
        }
        if (WEAK.equals(type)) {
            return "你对工信部新兴职业的整体认知储备较为薄弱，职业信息积累不足。"
                    + "在职业内容、技能要求、行业现状等多个维度存在认知盲区，信息整合、甄别、加工能力有待提升。"
                    + "生涯探索处于初级阶段，对新就业形态的了解较为片面、模糊，缺乏系统化的职业认知体系。";
        }
        if (RUSHED.equals(type)) {
            return "你生涯探索主动性较强，但探索心态较为浮躁，职业信息加工不够严谨。"
                    + "闯关耗时较短但多处板块匹配错误，说明你对职业细节、岗位差异、行业短板的甄别能力不足，"
                    + "习惯于快速浅层认知，缺乏深度梳理与精准判断，容易形成模糊、片面的职业印象。";
        }
        return "你具备优秀的职业信息加工能力，能够全面、辩证地认知新兴职业，"
                + "既了解岗位工作内容、核心技能要求，也能客观正视职业短板与行业风险，职业认知完整度高，"
                + "完全契合 CIP 生涯信息加工的科学思维。你的生涯探索心态严谨、信息整合能力强，"
                + "不易出现盲目择业、跟风择业的问题。";
    }

    /** 提升建议文案，取自需求文档原文。 */
    public static String suggestion(String type) {
        if (ADVANTAGE.equals(type)) {
            return "刻意强化职业辩证探索，在了解岗位优势的同时，主动调研岗位工作压力、行业痛点、岗位短板，"
                    + "建立“优劣双向认知”的择业思维，规避生涯决策偏差。";
        }
        if (WEAK.equals(type)) {
            return "主动开展新业态职业通识学习，系统了解数字经济、智能制造、AI 应用类新兴岗位，"
                    + "补齐职业认知短板，逐步建立完整的职业信息知识库，为后续职业抉择奠定基础。";
        }
        if (RUSHED.equals(type)) {
            return "放慢职业探索节奏，注重精细化信息梳理，养成“先了解、再甄别、再判断”的生涯探索习惯，"
                    + "提升职业认知的严谨度与精准度。";
        }
        return "可继续拓展多品类新兴职业认知，结合自身特质匹配适配岗位，进一步细化个人职业能力提升路径，"
                + "将职业认知转化为长期生涯行动。";
    }
}
