package com.renxingbao.spring_fivth.domain;

import java.util.concurrent.TimeUnit;

public class DelayTest {

    public static void main(String[] args) {
       DelayOrderWorker work1 = new DelayOrderWorker();// 任务1
        DelayOrderQueueManager manager = DelayOrderQueueManager.getInstance();
       manager.put(work1, 5000, TimeUnit.MILLISECONDS);
        /*DelayOrderWorker work2 = new DelayOrderWorker();// 任务2
        DelayOrderWorker work3 = new DelayOrderWorker();// 任务3
        DelayOrderWorker work4 = new DelayOrderWorker();// 任务3
        // 延迟队列管理类，将任务转化消息体并将消息体放入延迟对列中等待执行

        manager.put(work2, 6000, TimeUnit.MILLISECONDS);
        manager.put(work3, 9000, TimeUnit.MILLISECONDS);
        manager.put(work4, 10000, TimeUnit.MILLISECONDS);*/
    }


}
