package com.heritage.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 传承人集合（集合名：inheritor）
 * 独立文档，可以被多个非遗项目引用
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "inheritor")
public class Inheritor {

    /** 传承人唯一ID */
    @Id
    private String id;

    /** 传承人姓名 */
    @NotBlank(message = "传承人姓名不能为空")
    @Field("inheritor_name")
    private String inheritorName;

    /** 性别 */
    @Field("gender")
    private String gender;

    /** 民族 */
    @Field("nation")
    private String nation;

    /** 简介、传承贡献 */
    @Field("intro")
    private String intro;
}