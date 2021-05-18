package com.renxingbao.spring_fivth.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;



public class LotteryUtil {
    public static  String lottery(Jedis jedis) {

        try {
            /**
             * 读取redis 缓存参数（使用watch 确保数据准确性）
             */
            Set<String> set = jedis.smembers(TestDemo.PRIZE_LIST);  //获取奖品列表（id）

            List<String>  list = new ArrayList<String>(set.size()+1); //奖品列表（有序）

            //使用管道技术一次性获取所有奖品数量确保数据完整性和概率计算的正确性
            Pipeline p = jedis.pipelined();

            for (String id : set) {
                list.add(id); //增加奖品列表
                p.get(id);//获取换成奖品数量
            }

            list.add(TestDemo.RESIDUAL_QUANTITY);

            p.get(TestDemo.RESIDUAL_QUANTITY);

            List<Object> list1= p.syncAndReturnAll();//获取所有奖品的剩余数量

            int totailCount = Integer.valueOf(String.valueOf(list1.get(list1.size()-1))); //获取剩余奖品总数

            if (totailCount == 0) {
                // 重置奖品
                //TestDemo.initData(jedis);
                return "-1";
            }

            // 存储每个奖品新的概率区间
            List<Float> proSection = new ArrayList<Float>();

            proSection.add(0f); //起始区间

            float totalPro = 0f; // 总的概率区间

            for (int i = 0; i < list1.size()-1; i++) {
//				awardCount += Float.valueOf(jedis.get(id)); //计算奖品现有总数量
                //弹性计算每个奖品的概率（剩余奖品数量/剩余总奖品数量） 每个概率区间为奖品概率乘以1000（把三位小数换为整）
                totalPro += (Float.valueOf(String.valueOf(list1.get(i))) / Float.valueOf(String.valueOf(list1.get(list1.size()-1)))) * 1000;
                proSection.add(totalPro);
            }

            // 获取总的概率区间中的随机数
            Random random = new Random();
            float randomPro = (float) random.nextInt((int) totalPro);

            for (int i = 0, size = proSection.size(); i < size; i++) {
                if (randomPro >= proSection.get(i) && randomPro < proSection.get(i + 1)) {
                    return list.get(i);
                }
            }
        } catch (Exception e) {
            System.err.println("概率之外计算错误" + e.getMessage());
            return null;
        }
        return null;
    }



}
