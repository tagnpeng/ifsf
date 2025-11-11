package com.zmjy.command.dto;

@lombok.Data
public class Data {

    /**
     * 数据地址
     */
    private byte[] dataAdd;
    /**
     * 数据编号
     */
    private byte dataId;

    //获取数据库地址长度
    public byte getDataAddLength() {
        return  (byte) (dataAdd.length & 0xFF);
    }
}
