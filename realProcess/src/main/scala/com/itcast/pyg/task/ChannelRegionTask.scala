package com.itcast.pyg.task

import com.itcast.pyg.`trait`.DataProcess
import com.itcast.pyg.bean.{ChannelRegion, Message}
import com.itcast.pyg.map.ChannelRegionMap
import com.itcast.pyg.reduce.ChannelRegionReduce
import com.itcast.pyg.sink.ChannelRegionSink
import org.apache.flink.streaming.api.scala.{DataStream, KeyedStream, WindowedStream}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.api.scala._
/**
  * Created by angel
  */
object ChannelRegionTask extends DataProcess{
  override def process(watermarkData: DataStream[Message]): Unit = {
    //1）：通过水印数据封装成频道地域数据
    val mapData: DataStream[ChannelRegion] = watermarkData.flatMap(new ChannelRegionMap)
    //2):分流
    val keyByData = mapData.keyBy(line => line.getAggregateField)
    //3):时间窗口划分
    val window: WindowedStream[ChannelRegion, String, TimeWindow] = keyByData.timeWindow(Time.seconds(3))
    //4)：进行指标的聚合操作---pvuv  新鲜度
    val result = window.reduce(new ChannelRegionReduce)
    //5）：指标落地
    result.addSink(new ChannelRegionSink)
  }
}
