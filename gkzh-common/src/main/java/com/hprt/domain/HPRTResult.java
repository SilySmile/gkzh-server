package com.hprt.domain;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONNull;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 汉印云打印统一响应。 */
@Data
public class HPRTResult<T extends Object> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int code;
    private Boolean status;
    private String msg;
    private T data;

    @SuppressWarnings("unchecked")
    public HPRTResult(String resultJSON, TypeReference<T> typeReference) {
        JSONObject response = JSONUtil.parseObj(resultJSON);
        this.code = response.getInt("code");
        this.status = response.getBool("status");
        this.msg = response.getStr("msg");
        Object dataObj = response.getObj("data");
        if (dataObj == null || dataObj instanceof JSONNull) {
            return;
        }
        if (dataObj instanceof JSONObject dataJson) {
            if (dataJson.containsKey("list")) {
                this.data = JSONUtil.toBean(JSONUtil.toJsonStr(dataJson.get("list")), typeReference.getType(), false);
            } else if (dataJson.containsKey("print_id")) {
                this.data = (T) dataJson.getStr("print_id");
            } else if (dataJson.containsKey("status")) {
                this.data = (T) dataJson.get("status");
            }
            return;
        }
        this.data = JSONUtil.toBean(JSONUtil.toJsonStr(dataObj), typeReference.getType(), false);
    }
}
