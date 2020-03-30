package com.atguigu.gmall.beans;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;
@Data
public class UmsMember implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private Long memberLevelId;
    private String username;
    private String password;
    private String nickname;
    private String phone;
    private Integer status;
    private Date createTime;
    private String icon;
    private String gender;
    private Date birthday;
    private String city;
    private String job;
    private String personalizedSignature;
    private String sourceUid;
    private String sourceType;
    private Integer integration;
    private Integer growth;
    private Integer luckeyCount;
    private String accessToken;
    private String accessCode;
    private Integer historyIntegration;
}