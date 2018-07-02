package com.bycuimiao.demo;

import redis.clients.jedis.Jedis;

import java.util.concurrent.TimeUnit;

public class Demo01 {

    public static void main(String[] args) throws Exception{
        Jedis jedis = JedisUtil.getInstance().getJedis();
        jedis.select(0);
        System.out.println("清除数据：" + jedis.flushDB());
        System.out.println("判断某个键是否存在：" + jedis.exists("1"));
        System.out.println("新增{1，a}键值对:" + jedis.set("1", "a"));
        System.out.println(jedis.exists("1"));
        System.out.println("新增{2，b}键值对:" + jedis.set("2", "b"));
        System.out.println("系统中所有的键如下：" + jedis.keys("*").toString());
        System.out.println("删除键 1:" + jedis.del("1"));
        System.out.println("判断键 1是否存在：" + jedis.exists("1"));
        System.out.println("设置键 2的过期时间为5s:" + jedis.expire("2", 5));
        TimeUnit.SECONDS.sleep(2);
        System.out.println("查看键 2的剩余生存时间：" + jedis.ttl("2"));
        System.out.println("移除键 2的生存时间：" + jedis.persist("2"));
        System.out.println("查看键 2的剩余生存时间：" + jedis.ttl("2"));
        System.out.println("查看键 2所存储的值的类型：" + jedis.type("2"));
        System.out.println("查看键 2的值：" + jedis.get("2"));

        //======================
        jedis.select(3);
        jedis.flushDB();
        System.out.println("====列表list功能展示====");
        jedis.lpush("collections", "ArrayList", "LinkedList", "Vector", "Stack", "queue");
        jedis.lpush("collections", "HashMap");
        jedis.lpush("collections", "HashMap");
        jedis.lpush("collections", "HashMap");
        jedis.lpush("collections", "HashMap");
        jedis.lpush("number", "1");
        jedis.lpush("number", "2");
        jedis.lpush("number", "3");
        // -1 代表倒数第一个
        System.out.println("collections 的内容：" + jedis.lrange("collections", 0, -1));
        System.out.println("collections区间0-2内容：" + jedis.lrange("collections", 0, 2));
        System.out.println("=================");
        // 删除列表指定的值 ，第二个参数为删除的个数（有重复时），后add进去的值先被删，类似于出栈
        System.out.println("删除指定元素个数：" + jedis.lrem("collections", 2, "HashMap"));
        System.out.println("collections 的内容：" + jedis.lrange("collections", 0, -1));
        System.out.println("删除区间0-4以外的数据：" + jedis.ltrim("collections", 0, 4));
        System.out.println("collections 的内容：" + jedis.lrange("collections", 0, -1));
        System.out.println("collections列表出栈（左端）：" + jedis.lpop("collections"));
        System.out.println("collections的内容：" + jedis.lrange("collections", 0, -1));
        System.out.println("collections添加元素，从列表右端，与lpush相对应：" + jedis.rpush("collections", "EnumMap"));
        System.out.println("collections的内容：" + jedis.lrange("collections", 0, -1));
        System.out.println("collections列表出栈（右端）：" + jedis.rpop("collections"));
        System.out.println("collections的内容：" + jedis.lrange("collections", 0, -1));
        System.out.println("修改collections指定下标1的内容：" + jedis.lset("collections", 1, "LinkedArrayList"));
        System.out.println("collections的内容：" + jedis.lrange("collections", 0, -1));
        System.out.println("=================");
        System.out.println("collections的长度：" + jedis.llen("collections"));
        System.out.println("获取collections下标为2的元素：" + jedis.lindex("collections", 2));
        System.out.println("=================");
        jedis.lpush("sortedList", "3", "6", "2", "0", "7", "4");
        System.out.println("sortedList排序前：" + jedis.lrange("sortedList", 0, -1));
        System.out.println(jedis.sort("sortedList"));
        System.out.println("sortedList排序后：" + jedis.lrange("sortedList", 0, -1));

    }
}
