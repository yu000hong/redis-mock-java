package org.rarefiedredis.redis;

import java.util.List;

public class Test {

    public static void main(String[] args) {
//        String name = "yh";
//        System.out.println(name.codePointAt(0));
//        System.out.println(name.codePointAt(1));

//        testSetbit(15);
        testBitfield();
    }

    private static void testSetbit(int n) {
        RedisMock redis = new RedisMock();
        String key = "yh";
        char x = 0x00FF;
        char y = 0xFFFF;
        redis.set(key, String.valueOf(new char[]{x, y}));
        System.out.println(redis.get(key));
        System.out.println(redis.get(key).length());
        System.out.println(redis.bitcount(key));

        System.out.println("pos:" + redis.bitpos(key, 1));

        redis.setbit(key, 16, true);
        System.out.println(redis.get(key));
        System.out.println(redis.get(key).length());
        System.out.println(redis.bitcount(key));
    }

    private static void testBitfield() {
        RedisMock redis = new RedisMock();
        String key = "key";
        long value = 0x7ff3ff1bffffffffL;
        System.out.println("value: " + value);
        List<Long> result = redis.bitfield(key, "set", "u64", "17", String.valueOf(value));
        System.out.println("set: " + result.get(0));
        result = redis.bitfield(key, "get", "u64", "17");
        System.out.println("get: " + result.get(0));
    }

    private static void testBitfield2() {
        RedisMock redis = new RedisMock();
        String key = "key";
        long value = 76445;
        String type = "u60";
        String offset = "32";
        System.out.println("value: " + value);

        List<Long> result = redis.bitfield(key, "set", type, offset, String.valueOf(value));
        System.out.println("set: " + result.get(0));

        result = redis.bitfield(key, "get", type, offset);
        System.out.println("get: " + result.get(0));

        result = redis.bitfield(key, "incrby", type, offset, "36");
        System.out.println("incrby: " + result.get(0));

        result = redis.bitfield(key, "get", type, offset);
        System.out.println("get: " + result.get(0));
    }

}
