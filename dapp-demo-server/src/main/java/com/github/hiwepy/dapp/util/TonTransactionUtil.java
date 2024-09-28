package com.github.hiwepy.dapp.util;


import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.hiwepy.api.util.JacksonUtils;
import com.github.hiwepy.api.util.OkHttpClientCreater;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.ton.java.address.Address;
import org.ton.java.tonlib.Tonlib;
import org.ton.java.tonlib.types.RawMessage;
import org.ton.java.tonlib.types.RawTransaction;
import org.ton.java.tonlib.types.RawTransactions;
import org.ton.java.tonlib.types.VerbosityLevel;
import org.ton.java.utils.Utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
public class TonTransactionUtil {

    protected final static String APPLICATION_JSON_VALUE = "application/json";
    protected static OkHttpClient httpClient = OkHttpClientCreater.createOKHttpClient(HttpLoggingInterceptor.Level.BODY);

    /**
     * toncenter主网的API方法
      */
    protected static String TON_HTTP_API_HOST = "https://toncenter.com";
    /**
     * toncenter测试网的API方法
     */
    protected static String TON_HTTP_API_TEST_HOST = "https://testnet.toncenter.com";
    protected static String TON_API_KEY = "••••••";


    /**
     * 检查交易是否由指定地址发起
     * @param hashAddress 交易hash地址
     * @param address 发起交易的钱包地址
     * @return true: 交易由指定地址发起；false: 交易不是由指定地址发起
     */
    public static boolean checkTransaction(String hashAddress, String address) {

        // TODO 待调试通

        Tonlib tonlib = Tonlib.builder()
                .pathToTonlibSharedLib("/mnt/tonlibjson.so")
                .pathToGlobalConfig("/mnt/testnet-global.config.json")
                .verbosityLevel(VerbosityLevel.FATAL)
                .testnet(true)
                .build();

        Address addressObj = Address.of(hashAddress.getBytes(StandardCharsets.UTF_8));
        log.info("address: " + addressObj.toString(true));
        RawTransactions rawTransactions = tonlib.getRawTransactions(addressObj.toString(false),null,null);
        log.info("total txs: {}", rawTransactions.getTransactions().size());

        for(RawTransaction tx:rawTransactions.getTransactions()) {
            if (Objects.nonNull(tx.getIn_msg()) && (!tx.getIn_msg().getSource().getAccount_address().equals(address))) {
                log.info("{}, {} <<<<< {} : {} ", Utils.toUTC(tx.getUtime()), tx.getIn_msg().getSource().getAccount_address(), tx.getIn_msg().getDestination().getAccount_address(), tx.getIn_msg().getValue());
                return false;
            }
            if (Objects.nonNull(tx.getOut_msgs())) {
                for (RawMessage msg : tx.getOut_msgs()) {
                    log.info("{}, {} >>>>> {} : {} ", Utils.toUTC(tx.getUtime()), msg.getSource().getAccount_address(), msg.getDestination().getAccount_address(), msg.getValue());
                }
            }
        }
        return true;
    }

    /**
     * 通过 TON HTTP API 检查交易是否由指定地址发起
     * 生产环境：https://toncenter.com/api/v2/#/transactions/get_transactions_getTransactions_get
     * 测试环境：https://testnet.toncenter.com/api/v2/#/transactions/get_transactions_getTransactions_get
     * @param address 发起交易的钱包地址
     * @param thisDayStartAt 今日开始时间 utc
     * @return true: 交易由指定地址发起；false: 交易不是由指定地址发起
     */
    public static boolean checkByTonHttpApi(String address, long thisDayStartAt) {
        long startTime = System.currentTimeMillis();
        try {
            // 1、创建RequestBody对象
            MediaType mediaType = MediaType.parse(APPLICATION_JSON_VALUE);
            RequestBody body = RequestBody.create(mediaType, "");
            // 2、创建HttpUrl对象
            HttpUrl httpUrl = new HttpUrl.Builder()
                    .scheme("https")
                    .host(TON_HTTP_API_TEST_HOST)
                    .addPathSegment("/api/v2/getTransactions")
                    .addQueryParameter("address", address)
                    .addQueryParameter("limit", "1")
                    .addQueryParameter("to_lt", "0")
                    .addQueryParameter("archival", "false")
                    .build();
            // 3、创建Request.Builder对象
            Request request = new Request.Builder()
                    .url(httpUrl)
                    .method("GET", body)
                    .addHeader("Accept", "application/json")
                    .addHeader("X-API-Key", TON_API_KEY)
                    .build();
            // 4、创建Call对象
            Response response = httpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                log.info("OkHttp3 >> Request Success : code : {}, use time : {} ", response.code(), System.currentTimeMillis() - startTime);
                String responseBody = response.body().string();
                return checkResponse(responseBody, address, thisDayStartAt);
            } else {
                log.error("OkHttp3 >> Request Failure : code : {}, message : {}, use time : {} ", response.code(), response.message(), System.currentTimeMillis() - startTime);
                return Boolean.FALSE;
            }
        } catch (IOException e) {
            log.error("OkHttp3 Request Error : {}, use time : {}", e.getMessage(), System.currentTimeMillis() - startTime);
            return Boolean.FALSE;
        }
    }

    /**
     * 检查交易是否由指定地址发起
     * @param responseBody 响应体
     * @param address 发起交易的钱包地址
     * @param thisDayStartAt 今日开始时间 utc
     * @return true: 交易由指定地址发起；false: 交易不是由指定地址发起
     */
    public static boolean checkResponse(String responseBody, String address, long thisDayStartAt) {
        try {
            JsonNode jsonNode = JacksonUtils.json2node(responseBody);
            Boolean ok = jsonNode.get("ok").asBoolean();
            if (ok){
                JsonNode result = jsonNode.get("result");
                if(Objects.isNull(result) || result.size() == 0){
                    return Boolean.FALSE;
                }
                JsonNode[] transactions = JacksonUtils.node2Array(result);
                return Stream.of(transactions).filter(Objects::nonNull).anyMatch(transaction -> checkTransaction(transaction, address, thisDayStartAt));
            } else {
                log.info("get transactions failed from ton，code: {}, error : {}", jsonNode.get("error").asText());
                return Boolean.FALSE;
            }
        } catch (JacksonException e) {
            log.error("Jackson Parser Error : {}", e.getMessage());
            return Boolean.FALSE;
        }
    }

    public static boolean checkTransaction(JsonNode transaction, String address, long thisDayStartAt) {
        // 检查交易ID是否为空
        if (!transaction.has("transaction_id")) {
            return Boolean.FALSE;
        }
        // 检查交易地址是否为空
        if (!transaction.has("address")) {
            return Boolean.FALSE;
        }
        JsonNode addressNode = transaction.get("address");
        // 筛选出指定地址发起的交易
        if(!addressNode.has("account_address") && addressNode.get("account_address").asText().equals(address)){
            return Boolean.FALSE;
        }
        // 检查交易是否包含out_msgs
        if (transaction.has("out_msgs")) {

            JsonNode[] out_msgs = JacksonUtils.node2Array(transaction.get("out_msgs"));

            return Stream.of(out_msgs).filter(Objects::nonNull).anyMatch(out_msg -> {

                String source = out_msg.hasNonNull("source") ? out_msg.get("source").asText() : StringUtils.EMPTY;
                String destination = out_msg.hasNonNull("destination") ? out_msg.get("destination").asText() : StringUtils.EMPTY;
                long created_lt = out_msg.hasNonNull("created_lt") ? out_msg.get("created_lt").asLong() : 0L;

                /*
                 *
                 * 1、from 地址和 to 地址均不能为空
                 * 2、检查交易是否由指定地址发起
                 * 3、交易时间大于等于今日开始时间  && created_lt >= thisDayStartAt
                 */
                return StringUtils.isNotBlank(source) && StringUtils.isNotBlank(destination)
                        && source.equals(address)
                        && created_lt >= thisDayStartAt;
            });

        }
        return Boolean.FALSE;
    }


}
