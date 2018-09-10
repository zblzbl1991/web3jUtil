package com.zbl.demo.entity;

/**
 * @author
 * @Title:  Web3Result
 * @ProjectName  kejiaProject
 * @Description:  web3用的Result
 * @date 2018/9/315:00
 */
public enum Web3Result {
    //实例为空
    web3j_is_null(-1,"web3j为空"),
    //交易失败
    trade_fail(-2,"交易失败"),
    //交易成功
    trade_success(0,"交易成功");


    /**
     * 错误代码
     */
    private Integer errorCode;
    /**
     * 错误返回信息
     */
    private String errorMsg;

    Web3Result(Integer errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
