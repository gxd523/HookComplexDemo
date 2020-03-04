package com.demo.hook;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by guoxiaodong on 2020/3/4 13:52
 */
public class MyTest {
    @Test
    public void test() {
        LinkedHashMap<Integer, Integer> linkedHashMap = new LinkedHashMap<>(0, 0.75f, true);
        for (int i = 0; i < 5; i++) {
            linkedHashMap.put(i, i);
        }
        System.out.println(linkedHashMap.get(1));
        System.out.println(linkedHashMap.get(2));

        for (Map.Entry<Integer, Integer> entry : linkedHashMap.entrySet()) {
            System.out.println(entry.getKey() + "..." + entry.getValue());
        }
    }
}
