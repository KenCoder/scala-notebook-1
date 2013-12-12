import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.thrift.ThriftClientFramedCodec
import com.bwater.notebook.api._

/**
 * Author: Ken
 */
class ThriftCalcService extends UserStorage.FinagledService {

}



object EvalService {
  
      private lazy val service = ClientBuilder()
      .hosts(s"localhost:0")
      .codec(ThriftClientFramedCodec())
      .hostConnectionLimit(1)
      .build()
  
      lazy val client = new UserStorage$FinagleClient(service)
  }