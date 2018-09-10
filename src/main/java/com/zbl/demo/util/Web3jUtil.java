package com.zbl.demo.util;

import com.alibaba.fastjson.JSONObject;
import com.zbl.demo.entity.EUser;
import com.zbl.demo.entity.Web3Result;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * @author
 * @Title: Web3jUtil
 * @ProjectName kejiaProject
 * @Description: 用web3j交易比特币
 * @date 2018/9/314:30
 */
public class Web3jUtil {

    private static final Web3jUtil web3jUtil =new Web3jUtil();

    private Web3jUtil() {
    }

    /**
     * @author:
     * @description: 进行实例化
     * @date: 17:50 2018/9/4
     * @param:
     * @retrun： 实例
     */
    public static Web3jUtil getInstance() {
        return web3jUtil;
    }
    /**
     * @author: ZhaoBaoLong
     * @description: 获取随机数
     * @date: 14:43 2018/9/3
     * @param: address 请求随机数的地址
     * @param: web3j web3j实例
     * @retrun：
     */
    public static BigInteger getNonce(String address) throws Exception {
        Web3j web3j = Web3j.build(new HttpService("https://mainnet.infura.io/fd664fb592904f92a2de25bbb8b4de7cfd664fb592904f92a2de25bbb8b4de7c"));
        try {
            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                    address, DefaultBlockParameterName.LATEST).sendAsync().get();

            return ethGetTransactionCount.getTransactionCount();
        } finally {
                web3j.shutdown();
        }
    }

    /**
     * @author:
     * @description: 进行以太币交易
     * @date: 14:42 2018/9/3
     * @param: web3j web3j连接infura的实例
     * @param: credentials 包含地址和私钥的实体
     * @param: to 接收的地址
     * @param: num 交易金额
     * @retrun: 交易结果
     */
    public  String transferEther(BigInteger nonce, Credentials credentials, String to, BigDecimal num) {
        JSONObject jsonObject = new JSONObject();
        Web3j web3j = Web3j.build(new HttpService("https://mainnet.infura.io/fd664fb592904f92a2de25bbb8b4de7cfd664fb592904f92a2de25bbb8b4de7c"));
        try {
            nonce = getNonce(credentials.getAddress());
            System.out.println("交易随机数:" + nonce);
            BigInteger value = Convert.toWei(String.valueOf(num), Convert.Unit.ETHER).toBigInteger();
            RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                    nonce, DefaultGasProvider.GAS_PRICE, DefaultGasProvider.GAS_LIMIT, to, value);
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            String hexValue = Numeric.toHexString(signedMessage);
            EthSendTransaction ethSendTransaction =
                    web3j.ethSendRawTransaction(hexValue).sendAsync().get();
            String transactionHash = ethSendTransaction.getTransactionHash();
            jsonObject.put("transactionHash", transactionHash);
            jsonObject.put("error_code", Web3Result.trade_success.getErrorCode());
            jsonObject.put("error_msg", Web3Result.trade_success.getErrorMsg());
            return jsonObject.toJSONString();
        } catch (Exception e) {
            jsonObject.put("error_code", Web3Result.trade_fail.getErrorCode());
            jsonObject.put("error_msg", Web3Result.trade_fail.getErrorMsg());
            return jsonObject.toJSONString();
        } finally {
                web3j.shutdown();
        }

    }

    /**
     * @author:
     * @description: 计算手续费
     * @date: 14:47 2018/9/3
     * @param: credentials 用户信息
     * @param: to 接收地址
     * @param: num 交易金额
     * @retrun： 手续费和随机数
     */
    public  String findHandlingfee(Credentials credentials, String to, Double num) throws Exception {
        Web3j web3j = Web3j.build(new HttpService("https://mainnet.infura.io/fd664fb592904f92a2de25bbb8b4de7cfd664fb592904f92a2de25bbb8b4de7c"));
        try {
            JSONObject jsonObject = new JSONObject();
            BigInteger value = Convert.toWei(String.valueOf(num), Convert.Unit.ETHER).toBigInteger();
            BigInteger nonce = getNonce(credentials.getAddress());
            Transaction etherTransaction = Transaction.createEtherTransaction(credentials.getAddress(), nonce, DefaultGasProvider.GAS_PRICE, DefaultGasProvider.GAS_LIMIT, to, value);
            EthEstimateGas send = web3j.ethEstimateGas(etherTransaction).send();
            BigInteger amountUsed = send.getAmountUsed();
            BigInteger handlingfee = amountUsed.multiply(DefaultGasProvider.GAS_PRICE);
            jsonObject.put("handlingfee", handlingfee);
            jsonObject.put("nonce", nonce);
            return jsonObject.toJSONString();
        } finally {
                web3j.shutdown();
        }
    }

    /**
     * @author:
     * @description: 创建钱包地址和私钥
     * @date: 9:39 2018/9/3
     * @param: seed
     * @retrun： 地址,私钥,公钥的json字符串
     */
     public EUser createAddress() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, CipherException {
         String seed= UUID.randomUUID().toString();
        JSONObject processJson = new JSONObject();
        ECKeyPair ecKeyPair = Keys.createEcKeyPair();
        BigInteger privateKeyInDec = ecKeyPair.getPrivateKey();
        String sPrivatekeyInHex = privateKeyInDec.toString(16);
        WalletFile aWallet = Wallet.createLight(seed, ecKeyPair);
        String sAddress = aWallet.getAddress();
        processJson.put("address", "0x" + sAddress);
        processJson.put("privatekey", sPrivatekeyInHex);
        processJson.put("publickey", ecKeyPair.getPublicKey());
         EUser eUser = new EUser( "0x" + sAddress,sPrivatekeyInHex,ecKeyPair.getPublicKey().toString());
         return eUser;
    }
    /**
     * @author:
     * @description: 根据hash查找数据
     * @date: 16:14 2018/9/5
     * @param: transactionHash
     * @retrun：
     */
    public String findTransction(String transactionHash) throws ExecutionException, InterruptedException {
        Web3j web3j = Web3j.build(new HttpService("https://mainnet.infura.io/fd664fb592904f92a2de25bbb8b4de7cfd664fb592904f92a2de25bbb8b4de7c"));
        EthTransaction ethTransaction = web3j.ethGetTransactionByHash(transactionHash).sendAsync().get();
        org.web3j.protocol.core.methods.response.Transaction transaction = ethTransaction.getTransaction().get();
        return JSONObject.toJSONString(transaction);
    }

    /**
     * @author:
     * @description: 查询以太币余额
     * @date: 16:32 2018/9/3
     * @param: address
     * @retrun：
     */
    public  String findBalance(String address) throws IOException, ExecutionException, InterruptedException {
        Web3j web3j = Web3j.build(new HttpService("https://mainnet.infura.io/fd664fb592904f92a2de25bbb8b4de7cfd664fb592904f92a2de25bbb8b4de7c"));
        try {
            EthGetBalance ethGetBalance = web3j
                    .ethGetBalance(address, DefaultBlockParameterName.LATEST)
                    .sendAsync()
                    .get();
            return ethGetBalance.getBalance().toString();
        } finally {
                web3j.shutdown();
        }
    }



    public static void main(String[] args) throws Exception {
        Web3jUtil web3jUtil = Web3jUtil.getInstance();
        String handlingfee = web3jUtil.findHandlingfee(Credentials.create("14b3440665fe3d11f0ee85fb11f74d8de4d08c3ae7eb81c0b909836e3188d2bb"), "0x8B42d79CcC3aa0D13f370FC6ff7f442C8e8aa5aF", 0.4);
        System.out.println(handlingfee);
        JSONObject jsonObject = JSONObject.parseObject(handlingfee);
        String ha = jsonObject.getString("handlingfee");
        BigDecimal bigDecimal = Convert.fromWei(ha, Convert.Unit.ETHER);
        System.out.println(bigDecimal.toString());
//        Web3jRpcUtil web3jRpcUtil = Web3jRpcUtil.getInstance();
//        String transaction = web3jRpcUtil.sendTransferTokensTransaction(Credentials.create("14b3440665fe3d11f0ee85fb11f74d8de4d08c3ae7eb81c0b909836e3188d2bb"), "0x8B42d79CcC3aa0D13f370FC6ff7f442C8e8aa5aF", "0xd5cab6a6060c67642cf2a434050ed48ba83ebb67",BigDecimal.valueOf(0.2));
//        System.out.println(transaction);
    }

}
