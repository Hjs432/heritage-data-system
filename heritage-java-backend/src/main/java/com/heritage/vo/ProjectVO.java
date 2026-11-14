package com.heritage.vo;

import com.heritage.entity.Project;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 非遗项目输出 VO
 * 在原实体字段基础上，拼装关联的传承人、民俗活动、数据源信息
 */
@Data
public class ProjectVO {

    private String id;
    private String projectName;
    private String projectCategory;
    private String projectLevel;
    private String region;
    private String projectIntro;
    private String inheritStatus;
    private LocalDateTime createTime;

    /** 通俗检索标签 */
    private List<String> searchTags;

    /** 引用的数据源ID */
    private String sourceRef;
    /** 关联的数据源详情 */
    private DataSourceVO source;

    /** 内嵌多媒体素材（图片 / 视频 URL） */
    private List<Project.MediaItem> mediaList;

    /** 引用的传承人ID数组 */
    private List<String> inheritorRefList;
    /** 关联的传承人详情 */
    private List<InheritorVO> inheritorList;

    /** 引用的民俗活动ID数组 */
    private List<String> activityRefList;
    /** 关联的民俗活动详情 */
    private List<FolkActivityVO> activityList;
}