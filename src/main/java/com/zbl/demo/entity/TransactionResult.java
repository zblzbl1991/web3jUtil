package com.zbl.demo.entity;

import java.math.BigInteger;

/**
 * @author
 * @Title: TransactionResult
 * @ProjectName kejiaProject
 * @Description: 交易返回结果实体
 * @date 2018/9/417:11
 */
public class TransactionResult {
    private String transactionHash;
    private BigInteger gasUsed;
    private String from;
    private String to;
    private BigInteger num;
    private String status;
    private String contractAddress;

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public BigInteger getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(BigInteger gasUsed) {
        this.gasUsed = gasUsed;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public BigInteger getNum() {
        return num;
    }

    public void setNum(BigInteger num) {
        this.num = num;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    @Override
    public String toString() {
        return "TransactionResult{" +
                "transactionHash='" + transactionHash + '\'' +
                ", gasUsed=" + gasUsed +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", num=" + num +
                ", status='" + status + '\'' +
                ", contractAddress='" + contractAddress + '\'' +
                '}';
    }
}
