package com.heritage.service;

import com.heritage.dto.SearchRequest;
import com.heritage.entity.Project;
import com.heritage.repository.ProjectRepository;
import com.heritage.vo.SearchIntentVO;
import com.heritage.vo.SearchMatchVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 项目模糊检索：
 * 1. 先用 SearchIntentService 把口语描述归一化为类别 + 标签
 * 2. 再对全部项目做多特征打分（名称 / 通俗标签 / 简介文本 / 类别先验）
 * 3. 返回 Top-K 与归一化权重，并给出匹配理由
 */
@Service
@RequiredArgsConstructor
public class ProjectSearchService {

    private final ProjectRepository projectRepository;
    private final ProjectService projectService;
    private final SearchIntentService searchIntentService;

    public List<SearchMatchVO> search(SearchRequest request) {
        String query = request.getQuery() == null ? "" : request.getQuery().trim();
        if (!StringUtils.hasText(query)) {
            return Collections.emptyList();
        }
        int topK = request.getTopK() == null || request.getTopK() <= 0 ? 10 : Math.min(request.getTopK(), 50);
        double minScore = request.getMinScore() == null ? 0.15 : request.getMinScore();

        SearchIntentVO intent = searchIntentService.parse(query);
        Set<String> intentTags = new LinkedHashSet<>(intent.getTags());
        Set<String> intentCategories = new HashSet<>(intent.getCategories());
        Set<String> intentModifiers = intent.getModifiers() == null
                ? Collections.emptySet() : new LinkedHashSet<>(intent.getModifiers());
        Set<String> queryGrams = bigrams(clean(query));
        int intentSize = intentTags.size() + intent.getKeywords().size() + intentModifiers.size();

        List<SearchMatchVO> matches = new ArrayList<>();
        for (Project project : projectRepository.findAll()) {
            List<String> projectTags = project.getSearchTags() == null
                    ? Collections.emptyList() : project.getSearchTags();
            String name = safe(project.getProjectName());
            String text = name + " " + safe(project.getProjectIntro()) + " "
                    + safe(project.getProjectCategory()) + " " + safe(project.getRegion());
            Set<String> textGrams = bigrams(clean(text));

            // 1) 名称命中
            double nameScore = 0;
            for (String keyword : intent.getKeywords()) {
                if (keyword.length() >= 2 && name.contains(keyword)) {
                    nameScore = 1.0;
                    break;
                }
            }

            // 2) 通俗标签命中：阈值 0.6，避免"双人对舞"被"一群人跳舞"按字符重合模糊命中
            int tagHits = 0;
            List<String> matchedTags = new ArrayList<>();
            for (String tag : intentTags) {
                boolean hit = projectTags.stream().anyMatch(pt -> similarity(pt, tag) >= 0.6);
                if (hit) {
                    tagHits++;
                    matchedTags.add(tag);
                }
            }
            double tagScore = intentTags.isEmpty() ? 0 : (double) tagHits / intentTags.size();

            // 3) 文本 2-gram 重合度
            double textScore = queryGrams.isEmpty() ? 0
                    : (double) queryGrams.stream().filter(textGrams::contains).count() / queryGrams.size();

            // 4) 类别先验
            double categoryScore = intentCategories.contains(project.getProjectCategory()) ? 1.0 : 0.0;

            // 5) 场景修饰词（双人 / 铠甲 / 群舞等）：只用包含关系命中，避免"双人对舞"被"一群人跳舞"蹭分
            String modifierText = text + " " + String.join(" ", projectTags);
            int modifierHits = 0;
            List<String> matchedModifiers = new ArrayList<>();
            for (String concept : intentModifiers) {
                List<String> aliases = SearchIntentService.MODIFIER_ALIASES
                        .getOrDefault(concept, Collections.singletonList(concept));
                boolean hit = aliases.stream().anyMatch(modifierText::contains);
                if (hit) {
                    modifierHits++;
                    matchedModifiers.add(concept);
                }
            }
            double modifierScore = intentModifiers.isEmpty() ? 0
                    : (double) modifierHits / intentModifiers.size();

            // 覆盖度：命中的意图要素越多，排名越靠前
            double coverage = intentSize == 0 ? 0
                    : (double) (tagHits + (nameScore > 0 ? 1 : 0) + modifierHits) / intentSize;

            double score = (0.35 * nameScore + 0.30 * tagScore + 0.20 * textScore + 0.15 * categoryScore)
                    * (0.5 + 0.5 * coverage)
                    + 0.18 * modifierScore;

            if (score < minScore) {
                continue;
            }

            SearchMatchVO vo = new SearchMatchVO();
            vo.setProject(projectService.toVO(project));
            vo.setScore(round(score));
            vo.setMatchedTags(matchedTags);
            vo.setReasons(buildReasons(project, nameScore, categoryScore, matchedTags, textScore, matchedModifiers));
            matches.add(vo);
        }

        matches.sort(Comparator.comparing(SearchMatchVO::getScore).reversed());
        List<SearchMatchVO> top = matches.stream().limit(topK).collect(Collectors.toList());

        double sum = top.stream().mapToDouble(SearchMatchVO::getScore).sum();
        for (SearchMatchVO vo : top) {
            vo.setWeight(sum <= 0 ? 0 : round(vo.getScore() / sum));
        }
        return top;
    }

    private List<String> buildReasons(Project project, double nameScore, double categoryScore,
                                      List<String> matchedTags, double textScore,
                                      List<String> matchedModifiers) {
        List<String> reasons = new ArrayList<>();
        if (nameScore > 0) {
            reasons.add("项目名称直接命中");
        }
        if (categoryScore > 0) {
            reasons.add("类别匹配：" + project.getProjectCategory());
        }
        if (!matchedTags.isEmpty()) {
            reasons.add("通俗标签命中：" + String.join("、", matchedTags));
        }
        if (!matchedModifiers.isEmpty()) {
            reasons.add("形态修饰命中：" + String.join("、", matchedModifiers));
        }
        if (textScore > 0.2) {
            reasons.add("简介内容相关");
        }
        return reasons;
    }

    /** 字符串相似度：包含关系直接判 1，否则按字符重合比例 */
    private double similarity(String a, String b) {
        if (!StringUtils.hasText(a) || !StringUtils.hasText(b)) {
            return 0;
        }
        if (a.contains(b) || b.contains(a)) {
            return 1.0;
        }
        Set<Character> charsA = chars(a);
        Set<Character> charsB = chars(b);
        int intersection = 0;
        for (Character c : charsA) {
            if (charsB.contains(c)) {
                intersection++;
            }
        }
        return (double) intersection / Math.min(charsA.size(), charsB.size());
    }

    private Set<Character> chars(String text) {
        Set<Character> set = new HashSet<>();
        for (char c : text.toCharArray()) {
            set.add(c);
        }
        return set;
    }

    private Set<String> bigrams(String text) {
        Set<String> grams = new LinkedHashSet<>();
        for (int i = 0; i + 1 < text.length(); i++) {
            grams.add(text.substring(i, i + 2));
        }
        return grams;
    }

    private String clean(String text) {
        return text == null ? "" : text.replaceAll("[\\s\\p{Punct}，。！？、；：“”‘’（）《》]+", "");
    }

    private String safe(String text) {
        return text == null ? "" : text;
    }

    private double round(double value) {
        return Math.round(value * 10000) / 10000.0;
    }
}
