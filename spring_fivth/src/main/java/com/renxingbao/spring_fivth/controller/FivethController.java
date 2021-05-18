package com.renxingbao.spring_fivth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.renxingbao.spring_fivth.domain.DelayOrderQueueManager;
import com.renxingbao.spring_fivth.domain.DelayOrderWorker;
import com.renxingbao.spring_fivth.domain.User;
import com.renxingbao.spring_fivth.mapper.UserMapper;
import com.renxingbao.spring_fivth.mapper.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


import java.util.List;
import java.util.concurrent.TimeUnit;

@Controller
public class FivethController {

    private static  int anum=0;
    @Autowired
    private UserMapper userMapper;


    @Autowired
   private RedisTemplate<String, String> redisTemplate;



    @Autowired
    private UserRepository userRepository;

    @RequestMapping("/query")
    @ResponseBody
    public List<User> queryUserList() throws JsonProcessingException {
        List<User> users = userRepository.kaqi();
        String userListData = redisTemplate.boundValueOps("user.findAll").get();
        //如果redis中没有数据的话撒旦撒
        if(null==userListData){
        //查询数据库获得数据
            List<User> all = userRepository.findAll();
     //转换成json格式字符串
            ObjectMapper om = new ObjectMapper();
            userListData = om.writeValueAsString(all);
          //将数据存储到redis中，下次在查询直接从redis中获得数据，不用在查询数据库
            redisTemplate.boundValueOps("user.findAll").set(userListData);
            System.out.println("===============从数据库获得数据===============");
        }else{
            System.out.println("===============从redis缓存中获得数据===============");
        }
        System.out.println(userListData);


        return users;
    }


    @RequestMapping("/lajisd")
    @ResponseBody
    public String quick(long time){
        DelayOrderWorker work1 = new DelayOrderWorker();// 任务1
        DelayOrderQueueManager manager =DelayOrderQueueManager.getInstance();

        manager.put(work1, time, TimeUnit.MILLISECONDS);
        anum++;
        return "springboot 访问成功ssssd!+任务1";
    }

    @RequestMapping("/lajisds")
    @ResponseBody
    public String quicks(long times){
        DelayOrderWorker work1 = new DelayOrderWorker();// 任务1
        DelayOrderQueueManager manager = DelayOrderQueueManager.getInstance();
        manager.put(work1, times, TimeUnit.MILLISECONDS);
        anum++;
        return "springboot 访问成功ssssd!+任务2"+manager;
    }

}
