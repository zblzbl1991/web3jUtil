package com.zbl.demo.entity;

/**
 * @author
 * @Title: EBitCoin
 * @ProjectName kejiaProject
 * @Description: 币种/合约实体类
 * @date 2018/9/510:34
 */
public class EBitCoin {
    private Integer id;
    private String contractAddress;
    private String coinName;
    private String responseValue;



    public String getResponseValue() {
        return responseValue;
    }

    public void setResponseValue(String responseValue) {
        this.responseValue = responseValue;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getCoinName() {
        return coinName;
    }

    public void setCoinName(String coinName) {
        this.coinName = coinName;
    }
}
