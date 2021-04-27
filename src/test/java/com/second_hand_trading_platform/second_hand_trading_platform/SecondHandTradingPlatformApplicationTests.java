package com.second_hand_trading_platform.second_hand_trading_platform;

import org.apache.tomcat.util.buf.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class SecondHandTradingPlatformApplicationTests {

    @Test
    void contextLoads() {
        List<String> t = new ArrayList<>();
        t.add("dsfa");
        t.add("dddd");
        System.out.println(StringUtils.join(t,','));
    }

}
