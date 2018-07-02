package com.bycuimiao.chat;

import com.bycuimiao.demo.JedisUtilZset;
import com.bycuimiao.demo.Message;
import redis.clients.jedis.Jedis;

import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class SendMsgDemo {
    public static void main(String[] args) {
        JedisUtilZset jedisUtil = JedisUtilZset.getInstance();
        Jedis jedis = jedisUtil.getJedis();
        Scanner scan = new Scanner(System.in);
        while (true){

            System.out.println("请输入聊天内容");
            String msg = scan.nextLine();
            Message message = new Message();
            System.out.println("请输入用户名");
            String userCode = scan.nextLine();
            System.out.println("请输入聊天室Code");
            String chatCode = scan.nextLine();
            message.setTime(System.currentTimeMillis());
            message.setMsg(msg);
            message.setUserCode(userCode);
            message.setChatCode(chatCode);
            JedisUtilZset.myPushZset(jedis,JedisUtilZset.serialize(message.getChatCode()),JedisUtilZset.serialize(message));
            System.out.println("发送成功");
            showAll(jedis,chatCode);
            System.out.println();
        }

    }

    public static void showAll(Jedis jedis , String chatCode){
        Set<byte[]> set = JedisUtilZset.myPopZset(jedis,JedisUtilZset.serialize(chatCode),0);
        Iterator<byte[]> setIt = set.iterator();
        while (setIt.hasNext()) {
            Message m = (Message) JedisUtilZset.unserizlize(setIt.next());
            System.out.println(m.getUserCode() + " : " + m.getMsg() + "--- msgId:" + m.getId());
        }
    }
}
