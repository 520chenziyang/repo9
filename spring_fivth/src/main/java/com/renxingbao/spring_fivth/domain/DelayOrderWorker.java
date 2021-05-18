package com.renxingbao.spring_fivth.domain;

public class DelayOrderWorker implements  Runnable {

    @Override
    public void run() {
        //相关业务逻辑处理
        System.out.println(Thread.currentThread().getName()+" do something ……");
    }
}
