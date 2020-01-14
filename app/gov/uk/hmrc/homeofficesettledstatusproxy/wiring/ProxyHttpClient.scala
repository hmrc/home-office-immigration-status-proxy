package gov.uk.hmrc.homeofficesettledstatusproxy.wiring

import akka.actor.ActorSystem
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.libs.ws.{WSClient, WSProxyServer}
import uk.gov.hmrc.play.audit.http.HttpAuditing
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import uk.gov.hmrc.play.http.ws.{WSProxy, WSProxyConfiguration}

@Singleton
class ProxyHttpClient @Inject()(
  conf: Configuration,
  httpAuditing: HttpAuditing,
  wsClient: WSClient,
  actorSystem: ActorSystem)
    extends DefaultHttpClient(conf, httpAuditing, wsClient, actorSystem) with WSProxy {

  override val wsProxyServer: Option[WSProxyServer] = {
    val server = WSProxyConfiguration(configPrefix = "proxy", configuration = conf)
    server.foreach(s =>
      println(s"Proxy connection: ${s.host}:${s.port} ${s.principal}:${s.password}"))
    server
  }
}
