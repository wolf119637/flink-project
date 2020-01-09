package com.pyg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 验证springboot工程是否创建成功
 * */
@SpringBootApplication
@Controller
@RequestMapping("ReportApplication")
public class ReportApplication {
	@RequestMapping("retriveData")
	public void retriveData(String json , HttpServletRequest request , HttpServletResponse response){
		System.out.println("当前接收到 ---》" + json);
	}

}
