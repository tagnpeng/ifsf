package com.zmjy.command.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Mapping {

    /**
     * 油机固定的ip
     */
    private String ip;
    /**
     * 节点号(NODE:1-9)
     */
    private Integer node;
    /**
     * 子节点号(S_NODE:1-9)
     */
    private Integer subNode;
    /**
     * 加油点(FP:1-4)
     */
    private Integer fpId;
    /**
     * 加油模式(1-8)
     */
    private Integer fmId;
    /**
     * 逻辑油枪号(1-8)
     */
    private Integer lnId;
    /**
     * 物理油枪号(1-8)
     */
    private Integer gunNo;
    /**
     * 第三方油品编码（1-99999999）
     */
    private Integer prodNb;
    /**
     * 油品单价
     */
    private BigDecimal unitPrice;
}
