package test.java;

import com.xrtb.common.HttpPostGet;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.WinObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

/**
 * Created by ben on 7/3/17.
 */
public class TestC1X {

    String price = null;
    String adId = null;
    String creativeId = null;

    @BeforeClass
    public static void setup() {
        try {
            Config.setup();
            System.out.println("******************  TestCampaignProcessor");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void stop() {
        Config.teardown();
    }


    @Test
    public void testPiggyBackedWins() throws Exception {

        HttpPostGet http = new HttpPostGet();
        final CountDownLatch latch = new CountDownLatch(1);
        com.xrtb.jmq.RTopic channel = new com.xrtb.jmq.RTopic("tcp://*:5572&wins");
        channel.subscribe("wins");

        channel.addListener(new com.xrtb.jmq.MessageListener<WinObject>() {
            @Override
            public void onMessage(String channel, WinObject win) {
                price = win.price;
                adId = win.adId;
                creativeId = win.cridId;
                latch.countDown();
            }
        });


        String pixel = "http://localhost:8080/pixel/exchange=c1x/ad_id=thead/creative_id=thecreative/bid_id=123456/price=0.23";
        http.sendPost(pixel, "",300000,300000);
        latch.await(5, TimeUnit.SECONDS);

        List<Map> maps = BidRequest.getExchangeCounts();
        Map x = (Map)maps.get(0);

        System.out.println("=================>" + x);
        long test = (Long)x.get("wins");

        assertTrue(price.equals("0.23"));
        assertTrue(creativeId .equals("thecreative"));
        assertTrue(adId.equals("thead"));
    }

}
