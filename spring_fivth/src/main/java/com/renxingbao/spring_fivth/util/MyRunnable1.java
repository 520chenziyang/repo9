package com.renxingbao.spring_fivth.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;
import java.util.UUID;

public class MyRunnable1 implements Runnable {
    private Jedis jedis = RedisPoolUtil.getJedis();

    @Override
    public void run() {

        try {
            // 查询剩余奖品总数
            String key = getPrize();
            System.err.println("线程" + Thread.currentThread().getName() + "中奖奖品id为：" + key);

        } catch (Exception e) {
            System.err.println("算法计算异常：异常原因 = " + e.getMessage());
        } finally {
            RedisPoolUtil.returnResourceObject(jedis);
        }
    }

    private String getPrize() {

        String key = LotteryUtil.lottery(jedis);                                      //获取中奖奖品ID
        jedis.watch(key,TestDemo.RESIDUAL_QUANTITY);                 //精确监控单个奖品剩余数
        String ASA=   jedis.get(key);
        if("-1".equals(key) || "0".equals(jedis.get(key)) ){
            jedis.unwatch();
            if("0".equals(jedis.get(key))){
               key = getPrize();
            }

        }else{

//			key = AvailablePrize(key);

            Transaction tx = jedis.multi();                                              //开启redis事物
            tx.incrBy(TestDemo.RESIDUAL_QUANTITY, -1);                  //减少总库存
            tx.incrBy(key, -1);                                                                //减少中奖奖品总库存
            List<Object> listObj = tx.exec();                                         //提交事务，如果此时watch key被改动了，则返回null
            if (listObj != null) {                                                              //多个进程同时 key>0 key相等时
//				String useId = UUID.randomUUID().toString();
                jedis.sadd("failuse", UUID.randomUUID().toString() + key);
                System.out.println("用户中奖成功！！！");                       //中奖成功业务逻辑
            } else {
                key = getPrize();                                                             //重新计算奖品
            }
        }
        return key;
    }

}
