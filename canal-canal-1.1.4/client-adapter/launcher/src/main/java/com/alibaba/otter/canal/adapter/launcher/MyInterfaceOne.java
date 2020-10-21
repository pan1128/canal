package com.alibaba.otter.canal.adapter.launcher;

import org.springframework.stereotype.Service;

@Service(value = "one")
public class MyInterfaceOne implements MyInterface {
    @Override
    public void say() {
        System.out.println("MyInterfaceOne");
    }
}
