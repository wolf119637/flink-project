package com.itcast.pyg.task

import com.itcast.pyg.`trait`.DataProcess
import com.itcast.pyg.bean.{ChannelRealHot, Message}
import com.itcast.pyg.map.RealHotMap
import com.itcast.pyg.reduce.ChannelRealHotReduce
import com.itcast.pyg.sink.ChannelRealHotSink
import org.apache.flink.api.common.functions.FlatMapFunction
import org.apache.flink.streaming.api.scala.{DataStream, KeyedStream, WindowedStream}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector
import org.apache.flink.api.scala._
/**
  * Created by angel
  */
object ChannelRealHotTask extends DataProcess{
  override def process(watermarkData: DataStream[Message]): Unit = {
    /**
    1)：将实时解析出的数据转换成频道热点实体类

    2）：分流（分组）

    3）：时间窗口的划分

    4）： 对频道的点击数进行聚合操作

    5）：将结果数据进行落地操作
      * */
    //将实时解析出的数据转换成频道热点实体类
    val channelRealHot: DataStream[ChannelRealHot] = watermarkData.flatMap(new RealHotMap)
    //分流（分组）
    val keyByData: KeyedStream[ChannelRealHot, String] = channelRealHot.keyBy(line => line.getChannelID)
    //时间窗口的划分
    val window: WindowedStream[ChannelRealHot, String, TimeWindow] = keyByData.timeWindow(Time.seconds(3))
    //对频道的点击数进行聚合操作
    val reduceData: DataStream[ChannelRealHot] = window.reduce(new ChannelRealHotReduce)
    //将结果数据进行落地操作--->hbase
    reduceData.addSink(new ChannelRealHotSink)
  }
}
