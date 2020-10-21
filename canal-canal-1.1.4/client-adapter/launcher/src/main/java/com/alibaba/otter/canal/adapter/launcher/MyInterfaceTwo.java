package com.alibaba.otter.canal.adapter.launcher;

import org.springframework.stereotype.Service;

@Service(value = "two")
public class MyInterfaceTwo implements MyInterface{
    @Override
    public void say() {

    }
}
