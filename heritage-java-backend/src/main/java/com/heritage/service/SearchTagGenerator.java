package com.heritage.service;

import com.heritage.entity.Project;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 通俗检索标签生成器
 * <p>
 * 新增 / 修改非遗项目时，按「类别基础标签 + 名称与简介关键词」自动生成 search_tags，
 * 保证新录入的数据在口语化检索中同样能被标签维度命中，不必再手工补标签。
 */
@Service
public class SearchTagGenerator {

    /** 每个类别必带的基础标签 */
    private static final Map<String, List<String>> CATEGORY_TAGS = new LinkedHashMap<>();

    /** 关键词命中后追加的标签（按声明顺序生效） */
    private static final Map<Pattern, List<String>> KEYWORD_TAGS = new LinkedHashMap<>();

    /** 单条项目最多保留的标签数 */
    private static final int MAX_TAGS = 8;

    static {
        CATEGORY_TAGS.put("传统舞蹈", List.of("跳舞", "一群人跳舞"));
        CATEGORY_TAGS.put("传统歌舞", List.of("唱歌", "跳舞", "一群人跳舞"));
        CATEGORY_TAGS.put("传统音乐", List.of("唱歌", "乐器演奏"));
        CATEGORY_TAGS.put("传统戏剧", List.of("演戏", "看戏"));
        CATEGORY_TAGS.put("传统美术", List.of("画画", "手工艺术"));
        CATEGORY_TAGS.put("传统技艺", List.of("手工制作", "传统手艺"));
        CATEGORY_TAGS.put("民俗", List.of("节日", "民俗活动"));
        CATEGORY_TAGS.put("传统医药", List.of("治病", "藏药"));
        CATEGORY_TAGS.put("民间文学", List.of("讲故事", "说唱"));
        CATEGORY_TAGS.put("传统体育游艺", List.of("比赛", "游戏"));
        CATEGORY_TAGS.put("传统知识", List.of("传统知识", "生活经验"));

        addRule("唐卡|绘画|壁画|堆绣|彩画|颜料|掐丝", "画画", "手工艺术");
        addRule("酥油花", "酥油做的");
        addRule("糌粑|青稞粉|面食", "吃的", "食物");
        addRule("青稞酒|酸奶|宴席|敬酒|喝酒|饮食", "吃的", "食物", "喝酒");
        addRule("藏香|线香|香囊", "点香", "藏香");
        addRule("寺庙|寺院|僧人|喇嘛|法会|诵经|佛事|佛殿", "寺庙", "宗教活动");
        addRule("面具|羌姆|跳神|曹盖|法舞", "戴面具跳舞", "跳神");
        addRule("赛马|马术|马背|马帮", "骑马比赛");
        addRule("射箭|响箭|毕秀|摔跤|抱石", "比赛", "游戏");
        addRule("锅庄|谐钦|热巴|卓舞|弦子|集体舞", "一群人跳舞", "集体舞蹈");
        addRule("藏医|藏药|药浴|疗法|养生|诊断", "治病", "藏药");
        addRule("服饰|围裙|藏袍|帽子|邦典|藏装", "做衣服", "穿的衣服");
        addRule("铜|银|铁|錾|鎏金|锻造|金属", "打金属", "做器具");
        addRule("陶|砂器|陶器|黑陶", "做陶器", "器具");
        addRule("罐|壶|碗|器皿", "器具");
        addRule("造纸|藏文|书法|经文|刻版|印刷|写字", "写字", "造纸");
        addRule("石刻|摩崖|玛尼|石碑", "刻石头", "石刻");
        addRule("建筑|碉楼|碉房|民居|帐篷|营造|木构", "房子", "建筑");
        addRule("节日|新年|雪顿|望果|庙会|六月会|采花节", "节日", "过节");
        addRule("婚礼|婚宴|祝词|赞词|祝福", "婚礼", "说祝词");
        addRule("史诗|格萨尔|传说|故事|说唱|谚语|民歌", "讲故事", "说唱");
        addRule("牦牛|游牧|牧场|草原|放牧", "游牧", "草原");
        addRule("鼓", "打鼓", "鼓舞");
        addRule("编织|织造|纺织|氆氇|刺绣|绣", "编织", "做衣服");
        addRule("风筝|棋|游戏|竞技", "比赛", "游戏");
        addRule("藏戏|戏剧|剧目|戏班", "演戏", "看戏");
        addRule("唱歌|山歌|弹唱|乐器|琴|笛|唢呐", "唱歌", "乐器演奏");
        addRule("制作|技艺|手工|工艺", "手工制作", "传统手艺");
    }

    private static void addRule(String regex, String... tags) {
        KEYWORD_TAGS.put(Pattern.compile(regex), List.of(tags));
    }

    /**
     * 根据项目内容生成通俗检索标签
     * 顺序：类别基础标签优先，其次按关键词规则追加，去重后最多 8 个
     */
    public List<String> generate(Project project) {
        if (project == null) {
            return new ArrayList<>();
        }
        Set<String> tags = new LinkedHashSet<>();

        List<String> baseTags = CATEGORY_TAGS.get(safe(project.getProjectCategory()));
        if (baseTags != null) {
            tags.addAll(baseTags);
        }

        String text = safe(project.getProjectName()) + " " + safe(project.getProjectIntro())
                + " " + safe(project.getProjectCategory()) + " " + safe(project.getRegion());
        for (Map.Entry<Pattern, List<String>> entry : KEYWORD_TAGS.entrySet()) {
            if (entry.getKey().matcher(text).find()) {
                tags.addAll(entry.getValue());
            }
        }

        // 兜底：连类别都没识别出时，至少用项目名称兜一个标签，避免检索时标签维度全为空
        if (tags.isEmpty() && StringUtils.hasText(project.getProjectName())) {
            tags.add(project.getProjectName().trim());
        }

        List<String> result = new ArrayList<>(tags);
        return result.size() > MAX_TAGS ? new ArrayList<>(result.subList(0, MAX_TAGS)) : result;
    }

    private String safe(String text) {
        return text == null ? "" : text;
    }
}
