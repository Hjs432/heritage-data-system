package com.heritage.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 非遗项目集合（集合名：project）
 * <p>
 * 注意：
 * 1. media_list 为内嵌对象数组（MediaItem 为 Project 的静态内部类，不是独立 @Document）
 * 2. inheritor_ref_list / activity_ref_list 保存其它文档的 ObjectId 字符串数组（多对多，不使用中间表）
 * 3. source_ref 保存单个 DataSource 文档的 ObjectId 字符串
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "project")
public class Project {

    /** MongoDB 文档唯一标识 _id（ObjectId 以 String 形式存储） */
    @Id
    private String id;

    /** 非遗项目名称 */
    @NotBlank(message = "非遗项目名称不能为空")
    @Field("project_name")
    private String projectName;

    /** 非遗类别：传统舞蹈、传统戏剧、传统医药、民俗 */
    @NotBlank(message = "非遗类别不能为空")
    @Field("project_category")
    private String projectCategory;

    /** 级别：国家级 / 省级 */
    @NotBlank(message = "非遗级别不能为空")
    @Field("project_level")
    private String projectLevel;

    /** 所属地区，如：西藏拉萨、青海玉树 */
    @NotBlank(message = "所属地区不能为空")
    @Field("region")
    private String region;

    /** 项目简介 */
    @Field("project_intro")
    private String projectIntro;

    /** 传承现状 */
    @Field("inherit_status")
    private String inheritStatus;

    /** 引用 DataSource 文档 _id（一对多：一个项目持有一个数据源） */
    @Field("source_ref")
    private String sourceRef;

    /** 内嵌多媒体素材数组（只存 URL 字符串，不存二进制） */
    @Field("media_list")
    private List<MediaItem> mediaList;

    /** 引用 Inheritor 的 _id 数组（多对多） */
    @Field("inheritor_ref_list")
    private List<String> inheritorRefList;

    /** 引用 FolkActivity 的 _id 数组（多对多） */
    @Field("activity_ref_list")
    private List<String> activityRefList;

    /** 通俗检索标签：用口语描述项目场景，供模糊检索使用 */
    @Field("search_tags")
    private List<String> searchTags;

    /** 文档录入时间 */
    @Field("create_time")
    private LocalDateTime createTime;

    /**
     * 内嵌多媒体素材 POJO
     * 仅为 Project 的内部静态类，不映射为独立集合
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MediaItem {

        /** 素材类型：图片 / 视频 */
        @Field("media_type")
        private String mediaType;

        /** 素材网络链接 */
        @Field("media_url")
        private String mediaUrl;

        /** 素材说明 */
        @Field("media_desc")
        private String mediaDesc;
    }
}