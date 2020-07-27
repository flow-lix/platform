package learn.flow.dao.entity;

import lombok.Data;

import java.util.Date;

@Data
public class SysUserInfo {
    private Integer userId;

    private String userName;

    private String password;

    private String realName;

    private String telephone;

    private String email;

    private Byte userType;

    private Byte active;

    private Byte enable;

    private Integer createBy;

    private Date createTime;

    private Date lastLogin;

    private String userLogo;

}