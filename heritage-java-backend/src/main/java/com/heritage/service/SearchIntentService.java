package com.heritage.service;

import com.heritage.vo.SearchIntentVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 意图归一化：把“一群人跳舞”“吃什么东西”这类口语描述，
 * 映射为「类别 + 通俗标签 + 关键词」，供后续检索打分使用。
 */
@Service
public class SearchIntentService {

    /** 一条规则：多个口语说法 -> 对应类别与标签 */
    private static final class Rule {
        private final List<String> categories;
        private final List<String> tags;

        private Rule(List<String> categories, List<String> tags) {
            this.categories = categories;
            this.tags = tags;
        }
    }

    /** 口语说法 -> 规则 */
    private static final Map<String, Rule> RULES = new LinkedHashMap<>();

    /** 修饰词口语说法 -> 归一化概念（同一概念的不同说法视为等价） */
    private static final Map<String, String> MODIFIER_ALIAS = new LinkedHashMap<>();

    /** 修饰概念 -> 全部说法，供检索层判断项目是否具备该特征 */
    public static final Map<String, List<String>> MODIFIER_ALIASES = new LinkedHashMap<>();

    private static void rule(List<String> aliases, List<String> categories, List<String> tags) {
        Rule r = new Rule(categories, tags);
        for (String alias : aliases) {
            RULES.put(alias, r);
        }
    }

    private static void modifier(String concept, String... aliases) {
        List<String> list = List.of(aliases);
        MODIFIER_ALIASES.put(concept, list);
        for (String alias : list) {
            MODIFIER_ALIAS.put(alias, concept);
        }
    }

    static {
        rule(List.of("跳舞", "舞蹈", "集体舞", "围圈", "群舞", "跳起来"),
                List.of("传统舞蹈", "传统歌舞"),
                List.of("一群人跳舞", "集体舞蹈", "跳舞"));
        rule(List.of("唱歌", "唱山歌", "音乐", "听歌", "乐器", "弹唱", "演奏", "山歌", "民歌", "情歌", "祝酒歌"),
                List.of("传统音乐", "传统歌舞"),
                List.of("唱歌", "乐器演奏", "听歌"));
        rule(List.of("演戏", "看戏", "戏剧", "藏戏", "剧目", "戏班"),
                List.of("传统戏剧"),
                List.of("演戏", "看戏"));
        rule(List.of("面具", "跳神", "羌姆", "曹盖", "法舞"),
                List.of("传统舞蹈", "传统戏剧"),
                List.of("戴面具跳舞", "跳神"));
        rule(List.of("吃", "食物", "好吃", "主食", "喝的", "酸奶", "糌粑", "青稞酒", "酥油茶", "饮食", "宴席", "敬酒"),
                List.of("民俗", "传统技艺"),
                List.of("吃的", "食物", "饮食"));
        rule(List.of("治病", "看病", "药", "藏医", "养生", "疗法", "诊断"),
                List.of("传统医药"),
                List.of("治病", "藏药"));
        rule(List.of("画画", "绘画", "唐卡", "美术", "版画", "颜料"),
                List.of("传统美术"),
                List.of("画画", "手工艺术"));
        rule(List.of("手工", "制作", "手艺", "工艺", "编织", "织", "绣", "做东西"),
                List.of("传统技艺"),
                List.of("手工制作", "传统手艺"));
        rule(List.of("刻", "石头", "石刻", "玛尼", "石碑"),
                List.of("传统美术", "传统技艺"),
                List.of("刻石头", "石刻"));
        rule(List.of("点香", "藏香", "香囊"),
                List.of("传统技艺", "传统医药"),
                List.of("点香", "藏香"));
        rule(List.of("节日", "过节", "节庆", "庙会", "盛会", "活动", "新年"),
                List.of("民俗"),
                List.of("节日", "过节", "民俗活动"));
        rule(List.of("婚礼", "结婚", "婚宴", "祝词", "祝福", "赞词"),
                List.of("民俗", "民间文学"),
                List.of("婚礼", "说祝词"));
        rule(List.of("故事", "传说", "说唱", "格萨尔", "史诗", "抄写"),
                List.of("民间文学"),
                List.of("讲故事", "说唱"));
        rule(List.of("骑马", "赛马", "马术", "马背", "马帮"),
                List.of("传统体育游艺", "民俗"),
                List.of("骑马比赛", "草原聚会"));
        rule(List.of("比赛", "竞技", "射箭", "游戏", "棋", "风筝", "斗"),
                List.of("传统体育游艺"),
                List.of("比赛", "游戏"));
        rule(List.of("盖房子", "房子", "建筑", "民居", "碉房", "碉楼", "帐篷", "住"),
                List.of("传统技艺"),
                List.of("房子", "建筑", "住帐篷"));
        rule(List.of("船", "渡", "过江"),
                List.of("传统技艺"),
                List.of("坐船"));
        rule(List.of("衣服", "服饰", "帽子", "围裙", "穿", "刺绣"),
                List.of("传统技艺", "民俗"),
                List.of("做衣服", "穿的衣服"));
        rule(List.of("陶", "罐", "壶", "器皿", "碗"),
                List.of("传统技艺"),
                List.of("做陶器", "器具"));
        rule(List.of("金属", "铜", "银", "铁", "錾", "鎏", "锻造"),
                List.of("传统技艺"),
                List.of("打金属", "做器具"));
        rule(List.of("造纸", "纸", "写字", "书法", "抄写", "经文"),
                List.of("传统技艺", "传统美术"),
                List.of("写字", "造纸"));
        rule(List.of("鼓", "打鼓", "鼓舞"),
                List.of("传统舞蹈", "传统音乐"),
                List.of("打鼓", "鼓舞"));
        rule(List.of("牦牛", "牛毛", "游牧", "草原", "放牧"),
                List.of("传统技艺", "民俗"),
                List.of("游牧", "草原"));

        // 寺庙 / 宗教活动：寺院、法会、诵经等现象分散在音乐、舞蹈、美术、民俗多个类别
        rule(List.of("寺庙", "寺院", "僧人", "喇嘛", "法会", "诵经", "转经", "佛事", "宗教", "供佛", "朝拜", "佛殿"),
                List.of("民俗", "传统音乐", "传统美术", "传统舞蹈", "传统技艺"),
                List.of("宗教活动", "寺庙", "诵经"));

        // 场景修饰词：把"双人对舞/两个人跳舞""穿着铠甲跳舞"等细分形态单独识别出来，
        // 避免只落到笼统的"跳舞"，从而区分同类别下的不同表现形式
        // 人数形态
        modifier("双人", "双人", "两个人", "两人", "二人", "对舞", "对跳", "成对", "双人舞");
        modifier("单人", "单人", "一个人跳", "独舞", "自己跳", "单独表演");
        modifier("群舞", "群舞", "一群人", "大家一起", "集体", "多人一起");
        modifier("男女", "一男一女", "男女", "男女搭配", "男一女");
        // 穿戴道具
        modifier("铠甲", "铠甲", "盔甲", "甲胄", "战甲", "披甲");
        modifier("面具", "面具", "戴面具", "脸谱", "傩面", "跳神", "羌姆", "曹盖");
        modifier("服饰", "服饰", "服装", "衣服", "盛装", "藏袍", "围裙", "邦典", "帽子", "穿戴");
        // 乐器与器物
        modifier("鼓", "打鼓", "敲鼓", "鼓乐", "鼓舞", "法鼓", "腰鼓");
        modifier("乐器", "乐器", "弹琴", "琴", "笛", "唢呐", "二胡", "琵琶", "弦子", "弹唱");
        modifier("陶器", "陶器", "陶罐", "黑陶", "砂器", "制陶", "陶壶");
        modifier("金属", "铜", "银", "铁", "鎏金", "錾刻", "锻造", "打制");
        // 技艺材料
        modifier("文书", "造纸", "纸张", "藏纸", "藏文", "书法", "经文", "刻版", "印刷", "写字");
        modifier("石刻", "石刻", "摩崖", "玛尼石", "石碑", "刻石");
        modifier("建筑", "碉楼", "碉房", "民居", "帐篷", "建筑", "营造", "建房");
        // 场景与活动
        modifier("围圈", "围圈", "转圈", "绕圈", "圆圈舞", "手拉手");
        modifier("寺庙", "寺庙", "寺院", "僧人", "喇嘛", "法会", "诵经", "转经", "佛事", "宗教", "供佛", "朝拜", "佛殿");
        modifier("骑马", "骑马", "赛马", "马术", "马背", "马帮");
        modifier("射箭", "射箭", "响箭", "毕秀", "弓箭");
        modifier("牦牛", "牦牛", "牛毛", "放牧", "游牧");
    }

    /** 解析用户输入 */
    public SearchIntentVO parse(String query) {
        String q = clean(query);
        Set<String> keywords = new LinkedHashSet<>();
        Set<String> categories = new LinkedHashSet<>();
        Set<String> tags = new LinkedHashSet<>();
        Set<String> modifiers = new LinkedHashSet<>();

        for (Map.Entry<String, Rule> entry : RULES.entrySet()) {
            if (q.contains(entry.getKey())) {
                keywords.add(entry.getKey());
                categories.addAll(entry.getValue().categories);
                tags.addAll(entry.getValue().tags);
            }
        }

        // 场景修饰词：命中的口语说法归一为概念名，同时把概念名一并作为关键词，
        // 让项目名称里写"双人"、用户说"两个人"时也能直接命中名称
        for (Map.Entry<String, String> entry : MODIFIER_ALIAS.entrySet()) {
            if (q.contains(entry.getKey())) {
                keywords.add(entry.getKey());
                keywords.add(entry.getValue());
                modifiers.add(entry.getValue());
            }
        }

        // 兜底：没有任何规则与修饰词命中时，用 2-gram 做模糊匹配
        if (keywords.isEmpty()) {
            Set<String> grams = bigrams(q);
            keywords.addAll(grams);
            tags.addAll(grams);
        }

        SearchIntentVO vo = new SearchIntentVO();
        vo.setQuery(query);
        vo.setKeywords(new ArrayList<>(keywords));
        vo.setCategories(new ArrayList<>(categories));
        vo.setTags(new ArrayList<>(tags));
        vo.setModifiers(new ArrayList<>(modifiers));
        vo.setSummary(buildSummary(keywords, categories, tags, modifiers));
        return vo;
    }

    private String buildSummary(Set<String> keywords, Set<String> categories,
                                Set<String> tags, Set<String> modifiers) {
        List<String> parts = new ArrayList<>();
        if (!categories.isEmpty()) {
            parts.add("类别：" + String.join("、", categories));
        }
        if (!modifiers.isEmpty()) {
            parts.add("修饰：" + String.join("、", modifiers));
        }
        if (!tags.isEmpty()) {
            parts.add("扩展标签：" + String.join("、", tags));
        }
        if (!keywords.isEmpty()) {
            parts.add("关键词：" + String.join("、", keywords));
        }
        return parts.isEmpty() ? "未识别到明确意图，将按原文模糊匹配" : String.join("；", parts);
    }

    /** 去标点与空白 */
    private String clean(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        return text.replaceAll("[\\s\\p{Punct}，。！？、；：“”‘’（）《》]+", "");
    }

    /** 中文 2-gram */
    private Set<String> bigrams(String text) {
        Set<String> grams = new LinkedHashSet<>();
        for (int i = 0; i + 1 < text.length(); i++) {
            grams.add(text.substring(i, i + 2));
        }
        return grams;
    }
}
