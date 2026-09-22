package com.hprt.domain;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONNull;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class HPRTResult<T extends Object> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private int code;
    private Boolean status;
    private String msg;
    private T data;

    public HPRTResult(String resultJSON, TypeReference<T> typeReference) {
        System.out.println(resultJSON);
        JSONObject response = JSONUtil.parseObj(resultJSON);
        this.code = response.getInt("code");
        this.status = response.getBool("status");
        this.msg = response.getStr("msg");
        Object dataObj = response.getObj("data");
        if (dataObj != null) {
            if (dataObj instanceof JSONObject dataJson) {
                if (dataJson.containsKey("list")) {
                    this.data = JSONUtil.toBean(JSONUtil.toJsonStr(dataJson.get("list")), typeReference.getType(), false);
                } else if (dataJson.containsKey("print_id")) {
                    this.data = (T) dataJson.get("print_id");
                } else if (dataJson.containsKey("status")) {
                    this.data = (T) dataJson.get("status");
                }
            } else if (!(dataObj instanceof JSONNull)) {
                this.data = JSONUtil.toBean(JSONUtil.toJsonStr(dataObj), typeReference.getType(), false);
            }
        }
    }


}
