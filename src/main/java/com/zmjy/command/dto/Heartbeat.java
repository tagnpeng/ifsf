package com.zmjy.command.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Heartbeat {

    private String ip;
    private Integer port;
    private Integer node;
}