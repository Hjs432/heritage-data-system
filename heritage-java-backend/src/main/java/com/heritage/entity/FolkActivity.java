package com.heritage.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 民俗活动集合（集合名：folk_activity）
 * 独立文档，多个非遗项目可以引用同一个活动
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "folk_activity")
public class FolkActivity {

    /** 活动唯一ID */
    @Id
    private String id;

    /** 民俗活动名称 */
    @NotBlank(message = "民俗活动名称不能为空")
    @Field("activity_name")
    private String activityName;

    /** 举办地点 */
    @Field("activity_addr")
    private String activityAddr;

    /** 举办时间描述 */
    @Field("activity_time")
    private String activityTime;

    /** 活动描述 */
    @Field("activity_desc")
    private String activityDesc;
}