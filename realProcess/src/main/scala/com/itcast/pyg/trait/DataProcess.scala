package com.itcast.pyg.`trait`

import com.itcast.pyg.bean.Message
import org.apache.flink.streaming.api.scala.DataStream

/**
  * Created by angel
  */
trait DataProcess {
  def process(watermarkData:DataStream[Message])
}
