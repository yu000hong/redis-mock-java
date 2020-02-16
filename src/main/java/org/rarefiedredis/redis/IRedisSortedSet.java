package org.rarefiedredis.redis;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public interface IRedisSortedSet {

    final class ZsetPair {
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

        @Override
        public int hashCode() {
            return Objects.hash(member);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || !(o instanceof ZsetPair)) {
                return false;
            }
            ZsetPair other = (ZsetPair) o;
            return member.equals(other.member);
        }

        public static Set<String> members(Set<ZsetPair> pairs) {
            return pairs.stream()
                .map(pair -> pair.member)
                .collect(Collectors.toSet());
        }

        public static Map<String, Double> asMap(Set<ZsetPair> pairs) {
            Map<String, Double> map = new HashMap<String, Double>();
            for (ZsetPair pair : pairs) {
                map.put(pair.member, pair.score);
            }
            return map;
        }

        public static Comparator<ZsetPair> comparator() {
            return (a, b) -> {
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
            };
        }

        public static Comparator<ZsetPair> descendingComparator() {
            return (a, b) -> {
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
            };
        }
    }

    Long zadd(String key, ZsetPair pair, ZsetPair... pairs);

    Long zadd(String key, double score, String member, Object... scoreAndMembers);

    Long zcard(String key);

    Long zcount(String key, double min, double max);

    String zincrby(String key, double increment, String member);

    Long zinterstore(String destination, int numkeys, String... options);

    Long zlexcount(String key, String min, String max);

    List<ZsetPair> zpopmax(String key, long count);

    List<ZsetPair> zpopmin(String key, long count);

    List<ZsetPair> zrange(String key, long start, long stop, String... options);

    List<ZsetPair> zrangebylex(String key, String min, String max, String... options);

    List<ZsetPair> zrevrangebylex(String key, String max, String min, String... options);

    List<ZsetPair> zrangebyscore(String key, String min, String max, String... options);

    Long zrank(String key, String member);

    Long zrem(String key, String member, String... members);

    Long zremrangebylex(String key, String min, String max);

    Long zremrangebyrank(String key, long start, long stop);

    Long zremrangebyscore(String key, String min, String max);

    List<ZsetPair> zrevrange(String key, long start, long stop, String... options);

    List<ZsetPair> zrevrangebyscore(String key, String max, String min, String... options);

    Long zrevrank(String key, String member);

    Double zscore(String key, String member);

    Long zunionstore(String destination, int numkeys, String... options);

    ScanResult<List<ZsetPair>> zscan(String key, long cursor, String... options);

}
