package org.rarefiedredis.redis;

import org.rarefiedredis.redis.exception.NotFloatException;
import org.rarefiedredis.redis.exception.NotFloatMinMaxException;
import org.rarefiedredis.redis.exception.NotImplementedException;
import org.rarefiedredis.redis.exception.NotIntegerException;
import org.rarefiedredis.redis.exception.NotValidStringRangeItemException;
import org.rarefiedredis.redis.exception.SyntaxErrorException;
import org.rarefiedredis.redis.exception.WrongTypeException;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public interface IRedisSortedSet {

    public final class ZsetPair {

        public String member;
        public Double score;

        public ZsetPair() {
            this.member = null;
            this.score = null;
        }

        public ZsetPair(String member) {
            this.member = member;
            this.score = null;
        }

        public ZsetPair(String member, Double score) {
            this.member = member;
            this.score = score;
        }

        public ZsetPair(Double score, String member) {
            this.member = member;
            this.score = score;
        }

        public static Set<String> members(Set<ZsetPair> pairs) {
            Set<String> set = new HashSet<String>();
            for (ZsetPair pair : pairs) {
                set.add(pair.member);
            }
            return set;
        }

        public static Map<String, Double> asMap(Set<ZsetPair> pairs) {
            Map<String, Double> map = new HashMap<String, Double>();
            for (ZsetPair pair : pairs) {
                map.put(pair.member, pair.score);
            }
            return map;
        }

        public static Comparator<ZsetPair> comparator() {
            return new Comparator<ZsetPair>() {
                @Override public int compare(ZsetPair a, ZsetPair b) {
                    if (a == null) {
                        if (b == null) {
                            return 0;
                        }
                        return 1;
                    }
                    if (b == null) {
                        return -1;
                    }
                    if (a.score != null && b.score != null) {
                        if (a.score < b.score) {
                            return -1;
                        }
                        if (a.score > b.score) {
                            return 1;
                        }
                    }
                    return a.member.compareTo(b.member);
                }
            };
        }

        public static Comparator<ZsetPair> descendingComparator() {
            return new Comparator<ZsetPair>() {
                @Override public int compare(ZsetPair a, ZsetPair b) {
                    if (a == null) {
                        if (b == null) {
                            return 0;
                        }
                        return 1;
                    }
                    if (b == null) {
                        return -1;
                    }
                    if (a.score != null && b.score != null) {
                        if (a.score < b.score) {
                            return 1;
                        }
                        if (a.score > b.score) {
                            return -1;
                        }
                    }
                    return b.member.compareTo(a.member);
                }
            };
        }
    }

    Long zadd(String key, ZsetPair scoremember, ZsetPair ... scoresmembers) throws WrongTypeException, NotImplementedException;

    Long zadd(String key, double score, String member, Object ... scoresmembers) throws WrongTypeException, NotImplementedException, SyntaxErrorException, NotFloatException;

    Long zcard(String key) throws WrongTypeException, NotImplementedException;

    Long zcount(String key, double min, double max) throws WrongTypeException, NotImplementedException;

    String zincrby(String key, double increment, String member) throws WrongTypeException, NotImplementedException;

    Long zinterstore(String destination, int numkeys, String ... options) throws WrongTypeException, SyntaxErrorException, NotImplementedException;

    //Long zinterstore(String destination, int numkeys, Object ... options) throws WrongTypeException, SyntaxErrorException, NotImplementedException;

    Long zlexcount(String key, String min, String max) throws WrongTypeException, NotValidStringRangeItemException, NotImplementedException;

    Set<ZsetPair> zrange(String key, long start, long stop, String ... options) throws WrongTypeException, NotImplementedException;

    Set<ZsetPair> zrangebylex(String key, String min, String max, String ... options) throws WrongTypeException, NotValidStringRangeItemException, NotImplementedException;

    Set<ZsetPair> zrevrangebylex(String key, String max, String min, String ... options) throws WrongTypeException, NotValidStringRangeItemException, NotImplementedException;

    Set<ZsetPair> zrangebyscore(String key, String min, String max, String ... options) throws WrongTypeException, NotFloatMinMaxException, NotIntegerException, SyntaxErrorException, NotImplementedException;

    Long zrank(String key, String member) throws WrongTypeException, NotImplementedException;

    Long zrem(String key, String member, String ... members) throws WrongTypeException, NotImplementedException;

    Long zremrangebylex(String key, String min, String max) throws WrongTypeException, NotValidStringRangeItemException, NotImplementedException;

    Long zremrangebyrank(String key, long start, long stop) throws WrongTypeException, NotImplementedException;

    Long zremrangebyscore(String key, String min, String max) throws WrongTypeException, NotFloatMinMaxException, NotImplementedException;

    Set<ZsetPair> zrevrange(String key, long start, long stop, String ... options) throws WrongTypeException, NotImplementedException;

    Set<ZsetPair> zrevrangebyscore(String key, String max, String min, String ... options) throws WrongTypeException, NotFloatMinMaxException, NotIntegerException, SyntaxErrorException, NotImplementedException;

    Long zrevrank(String key, String member) throws WrongTypeException, NotImplementedException;

    Double zscore(String key, String member) throws WrongTypeException, NotImplementedException;

    Long zunionstore(String destination, int numkeys, String ... options) throws WrongTypeException, SyntaxErrorException, NotImplementedException;

    ScanResult<Set<ZsetPair>> zscan(String key, long cursor, String ... options) throws WrongTypeException, NotImplementedException;

}
