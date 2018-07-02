package com.bycuimiao.chat;

import com.bycuimiao.demo.JedisUtilZset;
import com.bycuimiao.demo.Message;
import redis.clients.jedis.Jedis;

import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class ShowMsgDemo {
    public static void main(String[] args) {
        showMsg();
    }
    public static void showMsg(){
        JedisUtilZset jedisUtil = JedisUtilZset.getInstance();
        Jedis jedis = jedisUtil.getJedis();
        Scanner scan = new Scanner(System.in);
        System.out.println("请输入聊天室Code");
        String chatCode = scan.nextLine();
        System.out.println("请输入msgId（返回队列中比msgId大的所有消息）");
        int msgId = scan.nextInt();

        Set<byte[]> set = JedisUtilZset.myPopZset(jedis,JedisUtilZset.serialize(chatCode),msgId);
        Iterator<byte[]> setIt = set.iterator();
        while (setIt.hasNext()) {
            Message m = (Message) JedisUtilZset.unserizlize(setIt.next());
            System.out.println(m.getUserCode() + " : " + m.getMsg() + "--- msgId:" + m.getId());
        }
    }
}
