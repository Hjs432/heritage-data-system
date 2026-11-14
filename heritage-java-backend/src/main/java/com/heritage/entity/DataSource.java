package com.heritage.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 数据来源集合（集合名：data_source）
 * 独立文档，为非遗项目提供溯源来源
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "data_source")
public class DataSource {

    /** 来源文档ID */
    @Id
    private String id;

    /** 来源平台名称 */
    @NotBlank(message = "来源平台名称不能为空")
    @Field("source_name")
    private String sourceName;

    /** 原始网页链接 */
    @Field("original_url")
    private String originalUrl;

    /** 备注说明 */
    @Field("source_note")
    private String sourceNote;
}