package com.itcast.pyg.tools

import java.util.Date

import org.apache.commons.lang.time.FastDateFormat

/**
  * Created by angel
  */
object TimeUtils {

  def getData(timeStamp:Long , format:String):String = {
    val time = new Date(timeStamp)
    val instance = FastDateFormat.getInstance(format)
    val format1 = instance.format(time)
    format1
  }
}
