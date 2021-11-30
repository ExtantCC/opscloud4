package com.baiyi.opscloud.datasource.dingtalk.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Author baiyi
 * @Date 2021/11/29 4:20 下午
 * @Version 1.0
 */
public class DingtalkUser {

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserResponse extends DingtalkResponse.Query implements Serializable {
        private static final long serialVersionUID = -917033148762911968L;
        private Result result;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Result extends DingtalkResponse.Result implements Serializable {
        private static final long serialVersionUID = -2508135692366815261L;
        private List<User> list;
    }

    @Data
    public static class User implements Serializable {

        private static final long serialVersionUID = 161013059944723592L;
        @JsonProperty("dept_order")
        private Long deptOrder;
        private Boolean leader;
        private String extension;
        private String unionid;
        private Boolean boss;
        @JsonProperty("exclusive_account")
        private Boolean exclusiveAccount;
        private String mobile;
        private Boolean active;
        private Boolean admin;
        private String telephone;
        private String remark;
        private String avatar;
        @JsonProperty("hide_mobile")
        private Boolean hideMobile;
        private String title;
        @JsonProperty("hired_date")
        private Date hiredDate;
        private String userid;
        @JsonProperty("org_email")
        private String orgEmail;
        private String name;
        @JsonProperty("dept_id_list")
        private List<Integer> deptIdList;
        @JsonProperty("job_number")
        private String jobNumber;
        @JsonProperty("state_code")
        private String stateCode;
        private String email;

    }
}
