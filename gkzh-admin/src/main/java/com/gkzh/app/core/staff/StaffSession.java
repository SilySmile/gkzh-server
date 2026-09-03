package com.gkzh.app.core.staff;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
public class StaffSession implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long staffId;
    private Long schoolId;
    private String staffName;
    private String schoolName;
    private String userName;
    private Integer canRedeem;
    private String token;
    private long expireTime;
    private Date loginTime;
}
