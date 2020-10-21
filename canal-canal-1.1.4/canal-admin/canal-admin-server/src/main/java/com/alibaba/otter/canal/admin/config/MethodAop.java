package com.alibaba.otter.canal.admin.config;

import com.alibaba.fastjson.JSONObject;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Rivan
 * @ClassName MethodAop
 * @Description TODO
 * @Date 2020/10/13 11:39
 */
@Component
@Aspect
public class MethodAop {

    private static final Logger LOGGER= LoggerFactory.getLogger(MethodAop.class);

    @Pointcut("execution(* com.alibaba.otter.canal.admin.controller.CanalDbsyncController.*(..))")
    public void pointCut(){}

    @Before("pointCut()")
    public void before(JoinPoint joinPoint){
        Signature signature = joinPoint.getSignature();
        Object target = joinPoint.getTarget();
        JoinPoint.StaticPart staticPart = joinPoint.getStaticPart();
        Object[] args = joinPoint.getArgs();
        LOGGER.info("方法名：{}",signature.getName());
        LOGGER.info("请求参数：{}",JSONObject.toJSONString(args));
    }
}
