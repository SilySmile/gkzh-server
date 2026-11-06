package com.hprt.domain;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

@Data
@Builder
public class HPRTPrinter {
    private String name;
    private String equipment_sn;
    @Builder.Default
    private String equipment_secret = "";
    private String model_name;
    private Integer status;
    private String print_id;

    @Tolerate
    public HPRTPrinter() {
    }
}
