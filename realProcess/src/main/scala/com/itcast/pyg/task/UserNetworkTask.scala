package com.itcast.pyg.task

import com.itcast.pyg.`trait`.DataProcess
import com.itcast.pyg.bean.{Message, UserNetwork}
import com.itcast.pyg.map.UserNetworkMap
import com.itcast.pyg.reduce.UserNetworkReduce
import com.itcast.pyg.sink.UserNetworkSink
import org.apache.flink.streaming.api.scala.{DataStream, KeyedStream, WindowedStream}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.api.scala._
/**
  * Created by angel
  */
object UserNetworkTask extends DataProcess{
  override def process(watermarkData: DataStream[Message]): Unit = {
    //1）：将水印数据转换成UserNetwork数据
    val mapData: DataStream[UserNetwork] = watermarkData.flatMap(new UserNetworkMap)
    //2）：分流
    val keyByData: KeyedStream[UserNetwork, String] = mapData.keyBy(line => line.getDataField)
    //3）：时间窗口划分
    val window: WindowedStream[UserNetwork, String, TimeWindow] = keyByData.timeWindow(Time.seconds(1))
    //4）：指标数据聚合
    val result = window.reduce(new UserNetworkReduce)
    //5）：数据指标落地
    result.addSink(new UserNetworkSink)
  }
}
