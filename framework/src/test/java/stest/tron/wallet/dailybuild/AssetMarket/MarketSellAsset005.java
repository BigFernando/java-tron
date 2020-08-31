package stest.tron.wallet.dailybuild.AssetMarket;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.tron.api.WalletGrpc;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.Block;
import org.tron.protos.Protocol.MarketPriceList;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.MarketOrderList;
import org.tron.protos.Protocol.TransactionInfo;
import org.tron.protos.Protocol.TransactionInfo.code;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.utils.PublicMethed;
import zmq.socket.pubsub.Pub;

@Slf4j

public class MarketSellAsset005 {

  private static final long now = System.currentTimeMillis();
  private static final String name = "testAssetIssue003_" + Long.toString(now);
  private static final String shortname = "a";
  private final String foundationKey001 = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key1");
  private final String foundationKey002 = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] foundationAddress001 = PublicMethed.getFinalAddress(foundationKey001);
  private final byte[] foundationAddress002 = PublicMethed.getFinalAddress(foundationKey002);
  String description = Configuration.getByPath("testng.conf")
      .getString("defaultParameter.assetDescription");
  String url = Configuration.getByPath("testng.conf")
      .getString("defaultParameter.assetUrl");

  byte [] trx = ByteArray.fromString("_");


  ECKey ecKey001 = new ECKey(Utils.getRandom());
  byte[] testAddress001 = ecKey001.getAddress();
  String testKey001 = ByteArray.toHexString(ecKey001.getPrivKeyBytes());
  byte[] assetAccountId001;
  ByteString assetAccountId;

  ECKey ecKey002 = new ECKey(Utils.getRandom());
  byte[] testAddress002 = ecKey002.getAddress();
  String testKey002 = ByteArray.toHexString(ecKey002.getPrivKeyBytes());
  byte[] assetAccountId002;


  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private String fullnode = Configuration.getByPath("testng.conf").getStringList("fullnode.ip.list")
      .get(1);

  @BeforeClass(enabled = true)
  public void beforeClass() {
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);

    PublicMethed.printAddress(testKey001);
    PublicMethed.printAddress(testKey002);

    Assert.assertTrue(PublicMethed.sendcoin(testAddress001,2024_000000L,foundationAddress001,
        foundationKey001,blockingStubFull));
    Assert.assertTrue(PublicMethed.sendcoin(testAddress002,1000_000000L,foundationAddress001,
        foundationKey001,blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Long start = System.currentTimeMillis() + 5000;
    Long end = System.currentTimeMillis() + 1000000000;
    Assert.assertTrue(PublicMethed.createAssetIssue(testAddress001,name,10000_000000L,1,1,start,
        end,1,description,url,10000L,10000L,1L, 1L,testKey001,blockingStubFull));


    assetAccountId001 =
        PublicMethed.queryAccount(testAddress001, blockingStubFull).getAssetIssuedID().toByteArray();

    assetAccountId = PublicMethed.queryAccount(testAddress001, blockingStubFull).getAssetIssuedID();


  }


  @Test(enabled = true,description = "Create an order to sell Trx and buy Trc10")
  void test01SellTrxBuyTrc10() {
    long balanceAfter = PublicMethed.queryAccount(testKey001, blockingStubFull).getBalance();
    Long devAssetCountAfter = PublicMethed
        .getAssetIssueValue(testAddress001,assetAccountId, blockingStubFull);


    String txid = PublicMethed.marketSellAsset(testAddress002,testKey002,trx,1,assetAccountId001,10,blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Optional<Transaction> transaction = PublicMethed
        .getTransactionById(txid, blockingStubFull);
    logger.info("transaction: " + transaction);
    Assert.assertEquals(transaction.get().getRet(0).getRet().toString(), "SUCESS");

    Optional<MarketOrderList> orderList = PublicMethed
        .getMarketOrderByAccount(testAddress002, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Assert.assertTrue(orderList.get().getOrdersCount() == 1);
    transaction = PublicMethed
        .getTransactionById(txid, blockingStubFull);
    Assert.assertEquals(transaction.get().getRet(0).getRet().toString(), "SUCESS");

  }

  @Test(enabled = true,description = "Create an order to sell Trc10 and buy Trx")
  void test02SellTrc10BuyTrx() {
    long balanceAfter = PublicMethed.queryAccount(testKey001, blockingStubFull).getBalance();
    Long devAssetCountAfter = PublicMethed
        .getAssetIssueValue(testAddress001,assetAccountId, blockingStubFull);


    String txid = PublicMethed.marketSellAsset(testAddress001,testKey001,assetAccountId001,5,trx,1,blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> TransactionInfo = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    logger.info("transaction: " + TransactionInfo);
    Optional<Transaction> transaction = PublicMethed
        .getTransactionById(txid, blockingStubFull);
    logger.info("transaction: " + transaction);
    Assert.assertEquals(transaction.get().getRet(0).getRet().toString(), "SUCESS");

    Optional<MarketOrderList> orderList = PublicMethed
        .getMarketOrderByAccount(testAddress001, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Assert.assertTrue(orderList.get().getOrdersCount() == 1);
    transaction = PublicMethed
        .getTransactionById(txid, blockingStubFull);
    Assert.assertEquals(transaction.get().getRet(0).getRet().toString(), "SUCESS");

  }








}