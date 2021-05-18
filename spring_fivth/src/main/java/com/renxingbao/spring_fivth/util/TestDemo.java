package com.renxingbao.spring_fivth.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestDemo {
    /**
     * 安慰奖
     */
    public static final String CONSOLATION_PRIZE_BY_ID= "2017112400005";

    /**
     * 缓存总次数 安慰奖+奖品
     */
    public static final String RESIDUAL_QUANTITY= "residualQuantity";

    /**
     * 奖品列表（ID）
     */
    public static final String PRIZE_LIST= "prizeList";

    /**
     * 每日抽奖所需积分
     */
    public static final String 	POINTS_REQUIRED_FOR_THE_LOTTERY = "pointsRequiredForTheLlottery";

    /**
     * 轮询成功次数
     */
    public static final String 	POLLING_SUCCESS_NUM = "POLLING_SUCCESS_NUM";

    /**
     * 轮询失败次数
     */
    public static final String 	POLLING_FAIL_NUM = "POLLING_FAIL_NUM";

    /**
     * 轮询奖品数量快照
     */
    public static final String 	SNAPSHOT_LIST = "SNAPSHOT_LIST";

    /**
     *中奖用户记录
     */
    public static final String 	FAILUSE = "FAILUSE";

    /**
     * 从redis连接池获取连接
     */
    private static Jedis jedis = RedisPoolUtil.getJedis();


    public static void main(String[] args) {
        System.out.println("dsdsa");
        try {
//			jedis.del(POLLING_SUCCESS_NUM,POLLING_FAIL_NUM,SNAPSHOT_LIST,FAILUSE);
//			jedis.set(RESIDUAL_QUANTITY, "0");
            //jedis.flushAll();//清除数据库
            // 初始化奖品参数
           // initData(jedis);

            ExecutorService executor = Executors.newFixedThreadPool(1);

            for (int i = 0; i <50; i++) {
                executor.execute(new MyRunnable1());
            }
            executor.shutdown();
        } catch (Exception e) {
            System.err.println("ERROR_MSG = " + e.getMessage());
        }finally{
            if(jedis!=null){
                RedisPoolUtil.returnResourceObject(jedis);//释放redis资源
            }
        }
    }


    /**
     * 初始化奖品参数
     */
    public static void initData(Jedis jedis) {
        Award award1 = new Award("2017112400001", 1, 2000, 1);  //一等奖
        Award award2 = new Award("2017112400002", 1, 1500, 1);  //二等奖
        Award award3 = new Award("2017112400003", 1, 1000, 1);  //三等奖
        Award award4 = new Award("2017112400004", 1, 500, 1);    //四等奖

        List<Award> list  = new ArrayList<Award>();
        list.add(award1);
        list.add(award2);
        list.add(award3);
        list.add(award4);

        int tailCount = 0; //总奖品数

        int tailPoint = 0; //总奖品总积分值

        /**
         * 读取redis 缓存参数（使用watch 确保数据准确性）
         */
        Set<String> set = jedis.smembers(TestDemo.PRIZE_LIST);  //获取奖品列表（id）

        //轮询快照
        Pipeline p = jedis.pipelined();

        for (String id : set) {
            p.get(id);//获取换成奖品数量
        }
        p.get(RESIDUAL_QUANTITY);
        List<Object> temp = p.syncAndReturnAll();//获取所有奖品的剩余数量

        //开始初始化缓存奖品数据
        for (int i = 0; i < list.size(); i++) {
            jedis.sadd(PRIZE_LIST, list.get(i).id);  //缓存奖品列表

            jedis.set(list.get(i).id, String.valueOf(list.get(i).count));//缓存奖品数量

            tailCount +=list.get(i).count; //计算总奖品数

            tailPoint += list.get(i).count* list.get(i).pointVal; //计算奖品总积分值
        }

        jedis.set(POINTS_REQUIRED_FOR_THE_LOTTERY, "500");  //每次抽奖所需积分

        int residualQuantity = tailPoint / Integer.valueOf(jedis.get(POINTS_REQUIRED_FOR_THE_LOTTERY)); 		//计算未中奖次数（安慰奖）

        int missesNum = residualQuantity - tailCount;  //安慰剂次数

        jedis.watch(RESIDUAL_QUANTITY);
        int count = 0;

        //判断是否是初次轮询
        if(jedis.exists(RESIDUAL_QUANTITY)){
            count= Integer.valueOf(jedis.get(RESIDUAL_QUANTITY));
        }

        if(count == 0){

            Transaction tx = jedis.multi();

            tx.set(CONSOLATION_PRIZE_BY_ID, String.valueOf(missesNum));  //缓存安慰奖次数

            tx.sadd(PRIZE_LIST, CONSOLATION_PRIZE_BY_ID);  //缓存安慰奖到奖品列表

            tx.set(RESIDUAL_QUANTITY, String.valueOf(residualQuantity));  // 缓存总次数 安慰奖+奖品

            List<Object> obj = tx.exec();
            if (obj != null) { // 多个进程同时 key>0 key相等时
                jedis.incrBy(POLLING_SUCCESS_NUM,1);
                jedis.sadd(SNAPSHOT_LIST, jedis.get(POLLING_SUCCESS_NUM)+temp.toString());
                System.out.println("初始化成功=============================》！！！");
            } else {
                jedis.incrBy(POLLING_FAIL_NUM,1);
                System.err.println("初始化失败=============================》！！！");
            }
        }else{
            jedis.unwatch();
        }
    }

}
