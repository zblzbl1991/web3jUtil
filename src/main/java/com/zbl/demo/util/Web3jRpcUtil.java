package com.zbl.demo.util;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.zbl.demo.entity.EBitCoin;
import com.zbl.demo.entity.ERCBalance;
import org.apache.commons.lang3.StringUtils;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author
 * @Title: Web3RPCUtil
 * @ProjectName kejiaProject
 * @Description: 调用智能合约
 * @date 2018/9/321:09
 */
public class Web3jRpcUtil {
    private static final int SLEEP_DURATION = 15000;
    private static final int ATTEMPTS = 10;
    //合约的转换单位
    private static final BigDecimal UNIT = BigDecimal.TEN.pow(8);
    private static final Web3jRpcUtil web3jRpcUtil = new Web3jRpcUtil();

    private Web3jRpcUtil() {

    }

    public static Web3jRpcUtil getInstance() {
        return web3jRpcUtil;
    }

    public static void main(String[] args) throws Exception {
        Credentials credentials = Credentials.create("14b3440665fe3d11f0ee85fb11f74d8de4d08c3ae7eb81c0b909836e3188d2bb");
        //获取小数位数
//        BigDecimal divide = BigDecimal.valueOf(totalSupply.longValue()).divide(UNIT);
//        System.out.println(divide.toString());
        //获取钱包余额

    }

    /**
     * @author:
     * @description: 获取余额
     * @date: 11:18 2018/9/4
     * @param: address 钱包地址
     * @param: bitCoinList 合约地址集合
     * @retrun： ERC余额实体集合
     */
    public List<ERCBalance> confirmBalance(String address, List<EBitCoin> bitCoinList) throws Exception {
        Function balanceOf = balanceOf(address);
        BigDecimal balance;
        Function decimals = decimals();
        List<ERCBalance> ercBalanceList = new ArrayList<>();
        //获取小数点位数
        List<EBitCoin> decimalsList = batchCallSmartContractFunction(decimals, address, bitCoinList);
        //获取余额
        List<EBitCoin> bitCoins = batchCallSmartContractFunction(balanceOf, address, bitCoinList);
        for (EBitCoin bitCoin : bitCoins) {
            String responseValue = bitCoin.getResponseValue();
            if(StringUtils.isNotBlank(responseValue)){

            }
            List<Type> response = FunctionReturnDecoder.decode(
                        responseValue, balanceOf.getOutputParameters());
                Type type = response.get(0);
                ERCBalance ercBalance=new ERCBalance();
                balance=new BigDecimal(type.getValue().toString());
                ercBalance.setBalance(balance);
                ercBalance.setCoinName(bitCoin.getCoinName());
                ercBalance.setContractAddress(bitCoin.getContractAddress());
            for (EBitCoin coin : decimalsList) {
                if(bitCoin.getContractAddress().equals(coin.getContractAddress())){
                    String decimalResponseValue = coin.getResponseValue();
                    if(StringUtils.isNotBlank(decimalResponseValue)){
                        List<Type> decimalResponse = FunctionReturnDecoder.decode(
                                decimalResponseValue, decimals.getOutputParameters());
                        Type decimalsType = decimalResponse.get(0);
                        ercBalance.setDecimal(Integer.valueOf(decimalsType.getValue().toString()));
                        ercBalance.setRealBalance(ercBalance.getBalance().divide( BigDecimal.TEN.pow(ercBalance.getDecimal())));
                    }


                }
            }
            ercBalanceList.add(ercBalance);
        }
        return ercBalanceList;
    }

    /**
     * @author:
     * @description: 获取总量
     * @date: 11:22 2018/9/4
     * @param: fromAddress
     * @param: contractAddress 合约地址
     */
    public BigInteger getTotalSupply(String fromAddress, String contractAddress) throws Exception {
        Function function = totalSupply();
        String responseValue = callSmartContractFunction(function, fromAddress, contractAddress);

        List<Type> response = FunctionReturnDecoder.decode(
                responseValue, function.getOutputParameters());

        return (BigInteger) response.get(0).getValue();
    }
    public BigDecimal getDecimals(String fromAddress,String contractAddress) throws Exception {
        Function function = decimals();
        String responseValue = callSmartContractFunction(function, fromAddress, contractAddress);

        List<Type> response = FunctionReturnDecoder.decode(
                responseValue, function.getOutputParameters());

        return new BigDecimal(response.get(0).getValue().toString()) ;
    }

    /**
     * @author:
     * @description: 发送交易
     * @date: 14:48 2018/9/4
     * @param: credentials 发送地址
     * @param: to 接收地址
     * @param: contractAddress 合约地址
     * @param: qty 交易金额
     * @retrun：
     */
    public String sendTransferTokensTransaction(
            Credentials credentials, String to, String contractAddress, BigDecimal qty)
            throws Exception {
        //获取小数点位数
        BigDecimal decimals = getDecimals(credentials.getAddress(), contractAddress);
        Function function = transfer(to, qty.multiply(BigDecimal.TEN.pow(decimals.intValue())).toBigInteger());
        //执行交易
        String functionHash = execute(credentials, function, contractAddress);
        TransactionReceipt transferTransactionReceipt = waitForTransactionReceipt(functionHash);
        //设置转json的过滤器
        SimplePropertyPreFilter filter = new SimplePropertyPreFilter(TransactionReceipt.class,
                "transactionHash", "cumulativeGasUsed", "gasUsed", "contractAddress", "status", "from", "to");
        String jsonString = JSONObject.toJSONString(transferTransactionReceipt, filter);
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        List<Log> logs = transferTransactionReceipt.getLogs();
        //验证交易金额
        Log log = logs.get(0);
        // verify the event was called with the function parameters
        List<String> topics = log.getTopics();
        Event transferEvent = transferEvent();
        // check function signature - we only have a single topic our event signature,
        // there are no indexed parameters in this example
        // verify qty transferred
        List<Type> results = FunctionReturnDecoder.decode(
                log.getData(), transferEvent.getNonIndexedParameters());
        jsonObject.put("num", results.get(0).getValue());
        return jsonObject.toJSONString();
    }

    /**
     * @author:
     * @description: 等待接收交易数据
     * @date: 14:32 2018/9/4
     * @param: transactionHash 交易的hash值
     * @retrun：
     */
    private TransactionReceipt waitForTransactionReceipt(
            String transactionHash) throws Exception {

        Optional<TransactionReceipt> transactionReceiptOptional =
                getTransactionReceipt(transactionHash, SLEEP_DURATION, ATTEMPTS);

        if (!transactionReceiptOptional.isPresent()) {
            throw new RuntimeException("Transaction receipt not generated after " + ATTEMPTS + " attempts");
        }

        return transactionReceiptOptional.get();
    }

    /**
     * @author:
     * @description: 获取交易结果
     * @date: 14:33 2018/9/4
     * @param: transactionHash 交易hash
     * @param: sleepDuration 查询等待时间
     * @param: attempts 查询次数
     * @retrun：
     */
    private Optional<TransactionReceipt> getTransactionReceipt(
            String transactionHash, int sleepDuration, int attempts) throws Exception {

        Optional<TransactionReceipt> receiptOptional =
                sendTransactionReceiptRequest(transactionHash);
        for (int i = 0; i < attempts; i++) {
            if (!receiptOptional.isPresent()) {
                Thread.sleep(sleepDuration);
                receiptOptional = sendTransactionReceiptRequest(transactionHash);
            } else {
                break;
            }
        }

        return receiptOptional;
    }

    /**
     * @author:
     * @description: 查询交易结果
     * @date: 14:36 2018/9/4
     * @param: transactionHash 交易hash
     * @retrun： 交易结果
     */
    private Optional<TransactionReceipt> sendTransactionReceiptRequest(
            String transactionHash) throws Exception {
        Web3j web3j = Web3j.build(new HttpService("https://mainnet.infura.io/fd664fb592904f92a2de25bbb8b4de7cfd664fb592904f92a2de25bbb8b4de7c"));
        EthGetTransactionReceipt transactionReceipt =
                web3j.ethGetTransactionReceipt(transactionHash).sendAsync().get();

        return transactionReceipt.getTransactionReceipt();
    }

    /**
     * @author:
     * @description: 发送一个允许交易
     * @date: 14:37 2018/9/4
     * @param: credentials 钱包地址
     * @param: spender 要存入的地址
     * @param: value 金额
     * @param: contractAddress 合约地址
     * @retrun：
     */
    private void sendApproveTransaction(
            Credentials credentials, String spender, BigInteger value,
            String contractAddress) throws Exception {
        Function function = approve(spender, value);
        String functionHash = execute(credentials, function, contractAddress);

        TransactionReceipt transferTransactionReceipt =
                waitForTransactionReceipt(functionHash);
        assertThat(transferTransactionReceipt.getTransactionHash(), is(functionHash));

        List<Log> logs = transferTransactionReceipt.getLogs();
        assertFalse(logs.isEmpty());
        Log log = logs.get(0);

        // verify the event was called with the function parameters
        List<String> topics = log.getTopics();

        // event Transfer(address indexed _from, address indexed _to, uint256 _value);
        Event event = approvalEvent();

        // check function signature - we only have a single topic our event signature,
        // there are no indexed parameters in this example
        String encodedEventSignature = EventEncoder.encode(event);
        assertThat(topics.get(0), is(encodedEventSignature));
        assertThat(new Address(topics.get(1)), is(new Address(credentials.getAddress())));
        assertThat(new Address(topics.get(2)), is(new Address(spender)));

        // verify our two event parameters
        List<Type> results = FunctionReturnDecoder.decode(
                log.getData(), event.getNonIndexedParameters());
        assertThat(results, equalTo(Collections.singletonList(new Uint256(value))));
    }

    /**
     * @author:
     * @description: 发送一个交易
     * @date: 14:12 2018/9/4
     * @param: credentials 钱包
     * @param: from 发送地址
     * @param: to 接收地址
     * @param: value 金额
     * @param: contractAddress 合约地址
     * @retrun：
     */
    private void sendTransferFromTransaction(
            Credentials credentials, String from, String to, BigInteger value,
            String contractAddress) throws Exception {

        Function function = transferFrom(from, to, value);
        String functionHash = execute(credentials, function, contractAddress);

        TransactionReceipt transferTransactionReceipt =
                waitForTransactionReceipt(functionHash);
        assertThat(transferTransactionReceipt.getTransactionHash(), is(functionHash));

        List<Log> logs = transferTransactionReceipt.getLogs();
        assertFalse(logs.isEmpty());
        Log log = logs.get(0);

        Event transferEvent = transferEvent();
        List<String> topics = log.getTopics();

        // check function signature - we only have a single topic our event signature,
        // there are no indexed parameters in this example
        String encodedEventSignature = EventEncoder.encode(transferEvent);
        assertThat(topics.get(0), is(encodedEventSignature));
        assertThat(new Address(topics.get(1)), is(new Address(from)));
        assertThat(new Address(topics.get(2)), is(new Address(to)));

        // verify qty transferred
        List<Type> results = FunctionReturnDecoder.decode(
                log.getData(), transferEvent.getNonIndexedParameters());
        assertThat(results, equalTo(Collections.singletonList(new Uint256(value))));
    }

    /**
     * @author:
     * @description: 查看配额
     * @date: 14:42 2018/9/4
     * @param: owner 合约拥有者地址
     * @param: spender 合约消费者地址
     * @param: contractAddress 合约地址
     * @param: expected
     * @retrun：
     */
    public String confirmAllowance(String owner, String spender, String contractAddress) throws Exception {
        Function function = allowance(owner, spender);
        String responseValue = callSmartContractFunction(function, owner, contractAddress);

        List<Type> response = FunctionReturnDecoder.decode(
                responseValue, function.getOutputParameters());
        return JSONObject.toJSONString(response);
//        assertThat(response.size(), is(function.getOutputParameters().size()));
//        assertThat(response.get(0), equalTo(new Uint256(expected)));
    }

    /**
     * @author:
     * @description: 执行交易方法
     * @date: 14:45 2018/9/4
     * @param: credentials 钱包实例
     * @param: function 传入参数
     * @param: contractAddress 合约地址
     * @retrun： 交易hash值
     */
    private String execute(
            Credentials credentials, Function function, String contractAddress) throws Exception {
        Web3j web3j = Web3j.build(new HttpService("https://mainnet.infura.io/fd664fb592904f92a2de25bbb8b4de7cfd664fb592904f92a2de25bbb8b4de7c"));
        try {

            BigInteger nonce = Web3jUtil.getNonce(credentials.getAddress());
            String encodedFunction = FunctionEncoder.encode(function);

            RawTransaction rawTransaction = RawTransaction.createTransaction(
                    nonce,
                    DefaultGasProvider.GAS_PRICE,
                    DefaultGasProvider.GAS_LIMIT,
                    contractAddress,
                    encodedFunction);

            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            String hexValue = Numeric.toHexString(signedMessage);

            EthSendTransaction transactionResponse = web3j.ethSendRawTransaction(hexValue)
                    .sendAsync().get();

            return transactionResponse.getTransactionHash();
        } finally {
            web3j.shutdown();
        }
    }

    /**
     * @author:
     * @description: 调用智能合约方法
     * @date: 14:46 2018/9/4
     * @param: function
     * @param: fromAddress
     * @param: contractAddress
     * @retrun：
     */
    private String callSmartContractFunction(Function function, String fromAddress, String contractAddress) throws Exception {
        String encodedFunction = FunctionEncoder.encode(function);
        Web3j web3j = Web3j.build(new HttpService("https://mainnet.infura.io/fd664fb592904f92a2de25bbb8b4de7cfd664fb592904f92a2de25bbb8b4de7c"));
        try {
            org.web3j.protocol.core.methods.response.EthCall response = web3j.ethCall(
                    Transaction.createEthCallTransaction(
                            fromAddress, contractAddress, encodedFunction),
                    DefaultBlockParameterName.LATEST)
                    .sendAsync().get();

            return response.getValue();
        } finally {
            web3j.shutdown();
        }

    }

    /**
     * @author:
     * @description: 批量处理合约内容
     * @date: 10:59 2018/9/5
     * @param: function 方法
     * @param: fromAddress 获取的地址
     * @param: bitCoinList 合约集合
     * @retrun：
     */
    private List<EBitCoin> batchCallSmartContractFunction(Function function, String fromAddress, List<EBitCoin> bitCoinList) throws Exception {
        String encodedFunction = FunctionEncoder.encode(function);
        Web3j web3j = Web3j.build(new HttpService("https://mainnet.infura.io/fd664fb592904f92a2de25bbb8b4de7cfd664fb592904f92a2de25bbb8b4de7c"));
        try {
            List<EBitCoin> responseList =new ArrayList<>();
            for (EBitCoin bitCoin : bitCoinList) {
                if (StringUtils.isNotBlank(bitCoin.getContractAddress()) && StringUtils.isNotBlank(bitCoin.getCoinName())) {
                    org.web3j.protocol.core.methods.response.EthCall response = web3j.ethCall(
                            Transaction.createEthCallTransaction(
                                    fromAddress, bitCoin.getContractAddress(), encodedFunction),
                            DefaultBlockParameterName.LATEST)
                            .sendAsync().get();

                    String responseValue = response.getValue();
                    EBitCoin newBitCoin = new EBitCoin();
                    newBitCoin.setId(bitCoin.getId());
                    newBitCoin.setContractAddress(bitCoin.getContractAddress());
                    newBitCoin.setCoinName(bitCoin.getCoinName());
                    newBitCoin.setResponseValue(responseValue);
                    responseList.add(newBitCoin);

                }
            }

            return responseList;
        } finally {
            web3j.shutdown();
        }

    }

    /**
     * @author:
     * @description: 查询代币发行的总量
     * @date: 13:39 2018/9/4
     * @param:
     * @retrun：
     */
    private Function totalSupply() {
        return new Function(
                "totalSupply",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Uint256>() {
                }));
    }
    /**
     * @author:
     * @description: 获取币种的单位
     * @date: 11:21 2018/9/5
     * @param:
     * @retrun：
     */
    private Function decimals() {
        return new Function(
                "decimals",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Uint256>() {
                }));
    }

    /**
     * @author:
     * @description: 查询A帐户下的代币数目
     * @date: 13:40 2018/9/4
     * @param: owner
     * @retrun：
     */
    private Function balanceOf(String owner) {
        return new Function(
                "balanceOf",
                Collections.singletonList(new Address(owner)),
                Collections.singletonList(new TypeReference<Uint256>() {
                }));
    }


    /**
     * @author:
     * @description: 发送x个代币到A帐户
     * @date: 13:40 2018/9/4
     * @param: to
     * @param: value
     * @retrun：
     */
    private Function transfer(String to, BigInteger value) {
        return new Function(
                "transfer",
                Arrays.asList(new Address(to), new Uint256(value)),
                Collections.singletonList(new TypeReference<Bool>() {
                }));
    }

    /**
     * @author:
     * @description: 查询B帐户可以从A帐户提取多少代币
     * @date: 13:42 2018/9/4
     * @param: owner
     * @param: spender
     * @retrun：
     */
    private Function allowance(String owner, String spender) {
        return new Function(
                "allowance",
                Arrays.asList(new Address(owner), new Address(spender)),
                Collections.singletonList(new TypeReference<Uint256>() {
                }));
    }

    /**
     * @author:
     * @description: 同意A帐户从我的帐户中提取代币
     * @date: 13:41 2018/9/4
     * @param: spender
     * @param: value
     * @retrun：
     */
    private Function approve(String spender, BigInteger value) {
        return new Function(
                "approve",
                Arrays.asList(new Address(spender), new Uint256(value)),
                Collections.singletonList(new TypeReference<Bool>() {
                }));
    }

    /**
     * @author:
     * @description: 从A帐户提取x个代币
     * @date: 13:41 2018/9/4
     * @param: from
     * @param: to
     * @param: value
     * @retrun：
     */
    private Function transferFrom(String from, String to, BigInteger value) {
        return new Function(
                "transferFrom",
                Arrays.asList(new Address(from), new Address(to), new Uint256(value)),
                Collections.singletonList(new TypeReference<Bool>() {
                }));
    }


    private Event transferEvent() {
        return new Event(
                "Transfer",
                Arrays.asList(
                        new TypeReference<Address>(true) {
                        },
                        new TypeReference<Address>(true) {
                        },
                        new TypeReference<Uint256>() {
                        }));
    }

    private Event approvalEvent() {
        return new Event(
                "Approval",
                Arrays.asList(
                        new TypeReference<Address>(true) {
                        },
                        new TypeReference<Address>(true) {
                        },
                        new TypeReference<Uint256>() {
                        }));
    }


}
