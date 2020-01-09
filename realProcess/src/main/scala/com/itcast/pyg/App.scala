package com.itcast.pyg

import java.util.Properties

import com.alibaba.fastjson.JSON
import com.itcast.pyg.bean.{Message, UserScan}
import com.itcast.pyg.task._
import com.itcast.pyg.tools.GlobalConfigUtils
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.runtime.state.filesystem.FsStateBackend
import org.apache.flink.streaming.api.environment.CheckpointConfig
import org.apache.flink.streaming.api.{CheckpointingMode, TimeCharacteristic}
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer09
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks
import org.apache.flink.streaming.api.watermark.Watermark
/**
  * Created by angel
  */
object App {
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    //处理进行checkpoint和水印
    env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime)
    //保证程序长时间运行的安全性进行checkpoint操作
    env.enableCheckpointing(5000)//checkpoint的时间间隔
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)
    env.getCheckpointConfig.setMinPauseBetweenCheckpoints(1000)//checkpoint最小的停顿间隔
    env.getCheckpointConfig.setCheckpointTimeout(60000)//checkpoint超时的时长
    env.getCheckpointConfig.setMaxConcurrentCheckpoints(1)//允许的最大checkpoint并行度
    //当程序关闭的时候偶，会不会出发额外的checkpoint
    env.getCheckpointConfig.enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    //设置checkpoint的地址
    env.setStateBackend(new FsStateBackend("hdfs://hadoop01:9000/flink-checkpoint/"))
    System.setProperty("hadoop.home.dir" , "/")

    //对接kafka
    val properties = new Properties()
    properties.setProperty("bootstrap.servers" , GlobalConfigUtils.bootstrapServers)
    properties.setProperty("zookeeper.connect" , GlobalConfigUtils.zookeeperConnect)
    properties.setProperty("gruop.id" , GlobalConfigUtils.gruopId)
    properties.setProperty("enable.auto.commit" , GlobalConfigUtils.enableAutoCommit)
    properties.setProperty("auto.commit.interval.ms" , GlobalConfigUtils.commitInterval)

    //下次重新消费的话，偶从哪里开始消费 latest：从上一次提交的offset位置开始的  earlist：从头开始进行
    properties.setProperty("auto.offset.reset" , GlobalConfigUtils.offsetReset)
    //序列化
    properties.setProperty("key.serializer" , "org.apache.kafka.common.serialization.StringSerializer")
    properties.setProperty("key.deserializer" , "org.apache.kafka.common.serialization.StringDeserializer")

    val consumer: FlinkKafkaConsumer09[String] = new FlinkKafkaConsumer09[String](
      GlobalConfigUtils.inputTopic,
      new SimpleStringSchema(),
      properties
    )
    val source: DataStream[String] = env.addSource(consumer)
    //将kafka获取到的json数据解析封装成message类
    val message = source.map{
      line =>
        val value = JSON.parseObject(line)
        val count = value.get("count").toString.toInt
        val message = value.get("message").toString
        val timeStamp = value.get("timeStamp").toString.toLong
        val userScan:UserScan = UserScan.toBean(message)
        Message(userScan , count , timeStamp)
    }
    //添加flink的水印处理 , 允许得最大延迟时间是2S
    val watermarkData: DataStream[Message] = message.assignTimestampsAndWatermarks(new AssignerWithPeriodicWatermarks[Message] {
      var currentTimestamp: Long = 0L
      val maxDelayTime = 2000L
      var watermark: Watermark = null

      //获取当前的水印
      override def getCurrentWatermark = {
        watermark = new Watermark(currentTimestamp - maxDelayTime)
        watermark
      }

      //时间戳抽取操作
      override def extractTimestamp(t: Message, l: Long) = {
        val timeStamp = t.timeStamp
        currentTimestamp = Math.max(timeStamp, currentTimestamp)
        currentTimestamp
      }
    })


    //业务类1：实时频道的热点
//    ChannelRealHotTask.process(watermarkData)
    //业务类2：频道的PVUV实时分析
//    ChannelPVUVTask.process(watermarkData)
    //业务类3：频道的新鲜度分析
//    ChannelUserFreshnessTask.process(watermarkData)
    //业务类4：频道的地域指标分析
//    ChannelRegionTask.process(watermarkData)
    //业务类5：用户每秒钟的上网方式分析
//    UserNetworkTask.process(watermarkData)
    //业务类5：用户浏览器分析
    UserBrowserTask.process(watermarkData)


    env.execute("app")
  }
}
