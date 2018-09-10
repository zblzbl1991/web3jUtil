package com.zbl.demo.entity;

/**
 * @author
 * @Title: EUser
 * @ProjectName kejiaProject
 * @Description: 以太坊用户实体
 * @date 2018/9/59:18
 */
public class EUser {
    /**
     * @author:
     * @description: 钱包地址
     * @date: 11:14 2018/9/5
     */
    private String address;
    /**
     * @author:
     * @description: 钱包私钥
     * @date: 11:14 2018/9/5
     */
    private String privateKey;
    /**
     * 钱包公钥
     */
    private String publicKey;

    public EUser(String address, String privateKey, String publicKey) {
        this.address = address;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public String toString() {
        return "EUser{" +
                "address='" + address + '\'' +
                ", privateKey='" + privateKey + '\'' +
                ", publicKey='" + publicKey + '\'' +
                '}';
    }
}
