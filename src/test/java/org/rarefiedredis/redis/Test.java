package org.rarefiedredis.redis;

public class Test {

    public static void main(String[] args) {
//        String name = "yh";
//        System.out.println(name.codePointAt(0));
//        System.out.println(name.codePointAt(1));

        testSetbit(15);
    }

    private static void testSetbit(int n) {
        RedisMock redis = new RedisMock();
        String key = "key";
        redis.set(key, "你好");
        redis.setbit(key, n, true);
        System.out.println(redis.get(key));
        System.out.println(redis.get(key).length());
    }

}
