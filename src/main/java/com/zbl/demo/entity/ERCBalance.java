package com.zbl.demo.entity;

import java.math.BigDecimal;

/**
 * @author
 * @Title: ERCBalance
 * @ProjectName kejiaProject
 * @Description: ERC余额实体
 * @date 2018/9/510:50
 */
public class ERCBalance {
    /**
     * 合约币种名称
     */
    private String coinName;
    /**
     * 合约地址
     */
    private String contractAddress;
    /**
     * 余额,单位是wei,需要根据下面的转换单位进行转换
     */
    private BigDecimal balance;
    /**
     * 从wei转换到实际数额的单位 转换方式 余额除以单位:  balance.divide( BigDecimal.TEN.pow(decimal))
     */
    private Integer decimal;
    /**
     * 实际余额 转换后得到的
     */
    private BigDecimal realBalance;

    public BigDecimal getRealBalance() {
        return realBalance;
    }

    public void setRealBalance(BigDecimal realBalance) {
        this.realBalance = realBalance;
    }

    public Integer getDecimal() {
        return decimal;
    }

    public void setDecimal(Integer decimal) {
        this.decimal = decimal;
    }

    public String getCoinName() {
        return coinName;
    }

    public void setCoinName(String coinName) {
        this.coinName = coinName;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "ERCBalance{" +
                "coinName='" + coinName + '\'' +
                ", contractAddress='" + contractAddress + '\'' +
                ", balance=" + balance +
                ", decimal=" + decimal +
                ", realBalance=" + realBalance +
                '}';
    }
}
