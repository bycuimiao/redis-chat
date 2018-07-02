package com.bycuimiao.demo;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.*;
import java.util.*;

/**
 * redis工具类
 *
 * @author lc
 */
public class JedisUtilZset {
    private static JedisPool jedisPool = null;

    private JedisUtilZset() {

    }

    //写成静态代码块形式，只加载一次，节省资源
    static {
        Properties properties = PropertyUtil.loadProperties("redis.properties");
        String host = properties.getProperty("redis.host");
        String port = properties.getProperty("redis.port");
        //String pass = properties.getProperty("redis.pass");
        String timeout = properties.getProperty("redis.timeout");
        String maxIdle = properties.getProperty("redis.maxIdle");
        String maxTotal = properties.getProperty("redis.maxTotal");
        String maxWaitMillis = properties.getProperty("redis.maxWaitMillis");
        String testOnBorrow = properties.getProperty("redis.testOnBorrow");

        JedisPoolConfig config = new JedisPoolConfig();
        //控制一个pool可分配多少个jedis实例，通过pool.getResource()来获取；
        //如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
        config.setMaxTotal(Integer.parseInt(maxTotal));
        //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例。
        config.setMaxIdle(Integer.parseInt(maxIdle));
        //表示当borrow(引入)一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；
        config.setMaxWaitMillis(Long.parseLong(maxWaitMillis));
        //在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
        config.setTestOnBorrow(Boolean.valueOf(testOnBorrow));

        jedisPool = new JedisPool(config, host, Integer.parseInt(port), Integer.parseInt(timeout));
    }

    /**
     * 从jedis连接池中获取获取jedis对象
     *
     * @return
     */
    public Jedis getJedis() {
        return jedisPool.getResource();
    }

    private static final JedisUtilZset jedisUtil = new JedisUtilZset();

    /**
     * 获取JedisUtil实例
     *
     * @return
     */
    public static JedisUtilZset getInstance() {
        return jedisUtil;
    }

    /**
     * 回收jedis(放到finally中)
     *
     * @param jedis
     */
    private void returnJedis(Jedis jedis) {
        if (null != jedis && null != jedisPool) {
            jedisPool.returnResource(jedis);
        }
    }

    /**
     * 销毁连接(放到catch中)
     *
     * @param jedis
     */
    private static void returnBrokenResource(Jedis jedis) {
        if (null != jedis && null != jedisPool) {
            jedisPool.returnResource(jedis);
        }
    }

    /**
     * 添加sorted set
     *
     * @param key
     * @param value
     * @param score
     */
    public void zadd(String key, String value, double score) {
        Jedis jedis = getJedis();
        jedis.zadd(key, score, value);
        returnJedis(jedis);
    }

    /**
     * 返回指定位置的集合元素,0为第一个元素，-1为最后一个元素
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Set<String> zrange(String key, int start, int end) {
        Jedis jedis = getJedis();
        Set<String> set = jedis.zrange(key, start, end);
        returnJedis(jedis);
        return set;
    }

    /**
     * 获取给定区间的元素，原始按照权重由高到低排序
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Set<String> zrevrange(String key, int start, int end) {
        Jedis jedis = getJedis();
        Set<String> set = jedis.zrevrange(key, start, end);
        returnJedis(jedis);
        return set;
    }

    /**
     * 添加对应关系，如果对应关系已存在，则覆盖
     *
     * @param key
     * @param map 对应关系
     * @return 状态，成功返回OK
     */
    public String hmset(String key, Map<String, String> map) {
        Jedis jedis = getJedis();
        String s = jedis.hmset(key, map);
        returnJedis(jedis);
        return s;
    }

    /**
     * 向List头部追加记录
     *
     * @param key
     * @param value
     * @return 记录总数
     */
    public long rpush(String key, String value) {
        Jedis jedis = getJedis();
        long count = jedis.rpush(key, value);
        returnJedis(jedis);
        return count;
    }

    /**
     * 向List头部追加记录
     *
     * @param key
     * @param value
     * @return 记录总数
     */
    private long rpush(byte[] key, byte[] value) {
        Jedis jedis = getJedis();
        long count = jedis.rpush(key, value);
        returnJedis(jedis);
        return count;
    }

    /**
     * 删除
     *
     * @param key
     * @return
     */
    public long del(String key) {
        Jedis jedis = getJedis();
        long s = jedis.del(key);
        returnJedis(jedis);
        return s;
    }

    /**
     * 从集合中删除成员
     *
     * @param key
     * @param value
     * @return 返回1成功
     */
    public long zrem(String key, String... value) {
        Jedis jedis = getJedis();
        long s = jedis.zrem(key, value);
        returnJedis(jedis);
        return s;
    }

    public void saveValueByKey(int dbIndex, byte[] key, byte[] value, int expireTime)
            throws Exception {
        Jedis jedis = null;
        boolean isBroken = false;
        try {
            jedis = getJedis();
            jedis.select(dbIndex);
            jedis.set(key, value);
            if (expireTime > 0)
                jedis.expire(key, expireTime);
        } catch (Exception e) {
            isBroken = true;
            throw e;
        } finally {
            returnResource(jedis, isBroken);
        }
    }

    public byte[] getValueByKey(int dbIndex, byte[] key) throws Exception {
        Jedis jedis = null;
        byte[] result = null;
        boolean isBroken = false;
        try {
            jedis = getJedis();
            jedis.select(dbIndex);
            result = jedis.get(key);
        } catch (Exception e) {
            isBroken = true;
            throw e;
        } finally {
            returnResource(jedis, isBroken);
        }
        return result;
    }

    public void deleteByKey(int dbIndex, byte[] key) throws Exception {
        Jedis jedis = null;
        boolean isBroken = false;
        try {
            jedis = getJedis();
            jedis.select(dbIndex);
            jedis.del(key);
        } catch (Exception e) {
            isBroken = true;
            throw e;
        } finally {
            returnResource(jedis, isBroken);
        }
    }

    public void returnResource(Jedis jedis, boolean isBroken) {
        if (jedis == null)
            return;
        if (isBroken)
            jedisPool.returnBrokenResource(jedis);
        else
            jedisPool.returnResource(jedis);
    }

    /**
     * 获取总数量
     *
     * @param key
     * @return
     */
    public long zcard(String key) {
        Jedis jedis = getJedis();
        long count = jedis.zcard(key);
        returnJedis(jedis);
        return count;
    }

    /**
     * 是否存在KEY
     *
     * @param key
     * @return
     */
    public boolean exists(String key) {
        Jedis jedis = getJedis();
        boolean exists = jedis.exists(key);
        returnJedis(jedis);
        return exists;
    }

    /**
     * 重命名KEY
     *
     * @param oldKey
     * @param newKey
     * @return
     */
    public String rename(String oldKey, String newKey) {
        Jedis jedis = getJedis();
        String result = jedis.rename(oldKey, newKey);
        returnJedis(jedis);
        return result;
    }

    /**
     * 设置失效时间
     *
     * @param key
     * @param seconds
     */
    public void expire(String key, int seconds) {
        Jedis jedis = getJedis();
        jedis.expire(key, seconds);
        returnJedis(jedis);
    }

    /**
     * 删除失效时间
     *
     * @param key
     */
    public void persist(String key) {
        Jedis jedis = getJedis();
        jedis.persist(key);
        returnJedis(jedis);
    }

    /**
     * 添加一个键值对，如果键存在不在添加，如果不存在，添加完成以后设置键的有效期
     *
     * @param key
     * @param value
     * @param timeOut
     */
    public void setnxWithTimeOut(String key, String value, int timeOut) {
        Jedis jedis = getJedis();
        if (0 != jedis.setnx(key, value)) {
            jedis.expire(key, timeOut);
        }
        returnJedis(jedis);
    }

    /**
     * 返回指定key序列值
     *
     * @param key
     * @return
     */
    public long incr(String key) {
        Jedis jedis = getJedis();
        long l = jedis.incr(key);
        returnJedis(jedis);
        return l;
    }

    /**
     * 获取当前时间
     *
     * @return 秒
     */
    public long currentTimeSecond() {
        Long l = 0l;
        Jedis jedis = getJedis();
        Object obj = jedis.eval("return redis.call('TIME')", 0);
        if (obj != null) {
            List<String> list = (List) obj;
            l = Long.valueOf(list.get(0));
        }
        returnJedis(jedis);
        return l;
    }

    //序列化
    public static byte[] serialize(Object obj) {
        ObjectOutputStream obi = null;
        ByteArrayOutputStream bai = null;
        try {
            bai = new ByteArrayOutputStream();
            obi = new ObjectOutputStream(bai);
            obi.writeObject(obj);
            byte[] byt = bai.toByteArray();
            return byt;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //反序列化
    public static Object unserizlize(byte[] byt) {
        ObjectInputStream oii = null;
        ByteArrayInputStream bis = null;
        bis = new ByteArrayInputStream(byt);
        try {
            oii = new ObjectInputStream(bis);
            Object obj = oii.readObject();
            return obj;
        } catch (Exception e) {

            e.printStackTrace();
        }


        return null;
    }


    public static void main(String[] args) {
        String queue = "queue";
        Jedis jedis = jedisUtil.getJedis();
        /*jedis.flushDB();
        Message message01 = new Message(1, System.currentTimeMillis(), "cuimiaoCode01", "我是第一条消息");
        myPushZset(jedis, queue.getBytes(), serialize(message01));
        Message message02 = new Message(2, System.currentTimeMillis(), "cuimiaoCode02", "我是第二条消息");
        myPushZset(jedis, queue.getBytes(), serialize(message02));
        Message message03 = new Message(3, System.currentTimeMillis(), "cuimiaoCode03", "我是第三条消息");
        myPushZset(jedis, queue.getBytes(), serialize(message03));
        Message message04 = new Message(4, System.currentTimeMillis(), "cuimiaoCode04", "我是第四条消息");
        myPushZset(jedis, queue.getBytes(), serialize(message04));
        //List<Message> messages = new ArrayList<Message>();
        //messages.add(message01);
        //messages.add(message02);
        System.out.println();
        jedis.expire(queue.getBytes(), 120);
        System.out.println("push success");*/
        //List<byte[]> byt = jedis.lrange(queue.getBytes(), 0, 10);
        //System.out.println(((Message) unserizlize(byt.get(0))).getMsg());
        //System.out.println(((Message) unserizlize(byt.get(1))).getMsg());
//        jedis.zadd("name",10,"cuimiao10");
//        jedis.zadd("name",10,"cuimiao10");
//        jedis.zincrby("name",10,"cuimiao12");
        //System.out.println("success");

        Message message05 = new Message(5, System.currentTimeMillis(), "cuimiaoCode05", "我是第五条消息");
        myPushZset(jedis, queue.getBytes(), serialize(message05));
        Set<byte[]> set = myPopZset(jedis,queue.getBytes(),3);
        Iterator<byte[]> setIt = set.iterator();
        while (setIt.hasNext()){
            Message m = (Message) unserizlize(setIt.next());
            System.out.println(m.getMsg());
        }
    }

    public synchronized static void myPushZset(Jedis jedis, byte[] key, byte[] value) {
        //jedis.expire(key, 120);
        reSetExpire(jedis,key);
        int sorceMax = 0;
        //long len = jedis.zcard(key);//当前zset长度

        //获取sorce最大值，并检查过期时间
        Set<byte[]> set = jedis.zrange(key, 0, jedis.zcard(key));
        Iterator<byte[]> setIt = set.iterator();
        while (setIt.hasNext()) {
            Message m = (Message) unserizlize(setIt.next());
            if(System.currentTimeMillis() - m.getTime() > 60000){
                //检查超时元素，并移除
                jedis.zrem(key,serialize(m));
            }
            sorceMax = m.getId();
        }


        int lengthMax = 5; //这里应该是从配置中取的值，信息最大存储量
        if (jedis.zcard(key) < lengthMax) {
            //jedis.rpush(key,value);
            Message messageTemp = (Message) unserizlize(value);
            messageTemp.setId(sorceMax + 1);
            jedis.zadd(key, sorceMax + 1, serialize(messageTemp));
            //jedis.zincrby();
        } else {
            //jedis.lpop(key);
            Message messageDel = (Message) unserizlize((jedis.zrange(key, 0, 1)).iterator().next());
            jedis.zrem(key, serialize(messageDel));

            //jedis.zadd(key, sorceMax + 1, value);
            Message messageTemp = (Message) unserizlize(value);
            messageTemp.setId(sorceMax + 1);
            jedis.zadd(key, sorceMax + 1, serialize(messageTemp));
        }
    }

    public static Set<byte[]> myPopZset(Jedis jedis, byte[] key, int msgId) {//获取比msgId之后的所有消息
        reSetExpire(jedis,key);
        Set<byte[]> set = jedis.zrangeByScore(key,msgId,Integer.MAX_VALUE);
        return set;
    }

    public static void reSetExpire(Jedis jedis , byte[] key){
        jedis.expire(key, 120);
    }

}