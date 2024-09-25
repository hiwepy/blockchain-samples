package com.github.hiwepy.dapp;

import com.iwebpp.crypto.TweetNaclFast;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.ton.java.tonconnect.Domain;
import org.ton.java.tonconnect.TonConnect;
import org.ton.java.tonconnect.TonProof;
import org.ton.java.tonconnect.WalletAccount;
import org.ton.java.utils.Utils;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class TonConnectExample {

    @Test
    public void testTonConnectExample() throws Exception {

        String addressStr = "0:2d29bfa071c8c62fa3398b661a842e60f04cb8a915fb3e749ef7c6c41343e16c";

        // backend prepares the request
        TonProof tonProof = TonProof.builder()
                .timestamp(1722999580)
                .domain(Domain.builder()
                        .value("xxx.xxx.com")
                        .lengthBytes(16)
                        .build())
                .payload("doc-example-<BACKEND_AUTH_ID>")
                .build();

        // wallet signs it
        byte[] secretKey = Utils.hexToSignedBytes("F182111193F30D79D517F2339A1BA7C25FDF6C52142F0F2C1D960A1F1D65E1E4");
        TweetNaclFast.Signature.KeyPair keyPair = TweetNaclFast.Signature.keyPair_fromSeed(secretKey);
        byte[] message = TonConnect.createMessageForSigning(tonProof, addressStr);
        byte[] signature = Utils.signData(keyPair.getPublicKey(), secretKey, message);
        log.info("signature: {}", Utils.bytesToHex(signature));

        // update TonProof by adding a signature
        tonProof.setSignature(Utils.bytesToBase64SafeUrl(signature));

        // backend verifies ton proof request
        WalletAccount walletAccount = WalletAccount.builder()
                .chain(-239)
                .address(addressStr)
                .publicKey("82a0b2543d06fec0aac952e9ec738be56ab1b6027fc0c1aa817ae14b4d1ed2fb")
                .build();

        assertThat(TonConnect.checkProof(tonProof, walletAccount)).isTrue();
    }

}
