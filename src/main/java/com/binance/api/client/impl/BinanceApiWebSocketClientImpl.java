package com.binance.api.client.impl;

import java.io.Closeable;
import java.util.List;
import java.util.stream.Collectors;

import com.binance.api.client.BinanceApiCallback;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.constant.BinanceApiConstants;
import com.binance.api.client.domain.event.AggTradeEvent;
import com.binance.api.client.domain.event.AllMarketTickersEvent;
import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.event.DepthEvent;
import com.binance.api.client.domain.event.UserDataUpdateEvent;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.domain.market.TickerStatistics;

import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

/**
 * Binance API WebSocket client implementation using OkHttp.
 */
public class BinanceApiWebSocketClientImpl implements BinanceApiWebSocketClient, Closeable {

  private OkHttpClient client;

  public BinanceApiWebSocketClientImpl() {
    Dispatcher d = new Dispatcher();
    d.setMaxRequestsPerHost(100);
    this.client = new OkHttpClient.Builder().dispatcher(d).build();
  }

  @Override
  public Closeable onDepthEvent(String symbol, BinanceApiCallback<DepthEvent> callback) {
    final String channel = String.format("%s@depth", symbol);
    return createNewWebSocket(channel, new BinanceApiWebSocketListener<>(callback));
  }

  @Override
  public Closeable onDepthEvent(String symbol, int level, BinanceApiCallback<DepthEvent> callback) {
    final String channel = String.format("%s@depth" + level, symbol);
    return createNewWebSocket(channel, new BinanceApiWebSocketListener<>(evt -> {
         evt.setSymbol(symbol);
         callback.onResponse(evt);
    }, DepthEvent.class));
  }

  @Override
  public Closeable onCandlestickEvent(String symbol, CandlestickInterval interval, BinanceApiCallback<CandlestickEvent> callback) {
    final String channel = String.format("%s@kline_%s", symbol, interval.getIntervalId());
    return createNewWebSocket(channel, new BinanceApiWebSocketListener<>(callback));
  }
  
  @Override
  public Closeable onCandlestickEvent(List<String> symbols, CandlestickInterval interval, BinanceApiCallback<CandlestickEvent> callback) {
	List<String> channels = symbols.stream().map(symbol -> String.format("%s@kline_%s", symbol, interval.getIntervalId())).collect(Collectors.toList());
	return createNewWebSocket(channels, new BinanceApiCombinedWebSocketListener<>(callback));
  }

  @Override
  public Closeable onAggTradeEvent(String symbol, BinanceApiCallback<AggTradeEvent> callback) {
    final String channel = String.format("%s@aggTrade", symbol);
    return createNewWebSocket(channel, new BinanceApiWebSocketListener<>(callback));
  }

  @Override
  public Closeable onMarketTickersEvent(BinanceApiCallback<TickerStatistics[]> callback) {
    final String channel = String.format("!ticker@arr");
    return createNewWebSocket(channel, new BinanceApiWebSocketListener<>(callback));
  }

  public Closeable onUserDataUpdateEvent(String listenKey, BinanceApiCallback<UserDataUpdateEvent> callback) {
    return createNewWebSocket(listenKey, new BinanceApiWebSocketListener<>(callback));
  }
  
  public Closeable onAllMarketTickersEvent(BinanceApiCallback<List<AllMarketTickersEvent>> callback) {
    final String channel = "!ticker@arr";
    return createNewWebSocket(channel, new BinanceApiWebSocketListener<List<AllMarketTickersEvent>>(callback));
  }

  private Closeable createNewWebSocket(String channel, BinanceApiWebSocketListener<?> listener) {
	String streamingUrl = String.format("%s/ws/%s", BinanceApiConstants.WS_API_BASE_URL, channel);
    return createNewWebSocketForUrl(streamingUrl, listener);
  }
  
  /* For combined streams */
  private <T> Closeable createNewWebSocket(List<String> channels, BinanceApiCombinedWebSocketListener<T> listener) {
	String combinedChannels = channels.stream().reduce((s1, s2) -> s1 + "/" + s2).get();
	String streamingUrl = String.format("%s/stream?streams=%s", BinanceApiConstants.WS_API_BASE_URL, combinedChannels);
	return createNewWebSocketForUrl(streamingUrl, listener);
  }
  
  private Closeable createNewWebSocketForUrl(String streamingUrl, BinanceApiWebSocketListener<?> listener) {
	  Request request = new Request.Builder().url(streamingUrl).build();
	  final WebSocket webSocket = client.newWebSocket(request, listener);
	    return () -> {
	      final int code = 1000;
	      listener.onClosing(webSocket, code, null);
	      webSocket.close(code, null);
	      listener.onClosed(webSocket, code, null);
	    };
  }
  
  @Override
  public void close() {
    client.dispatcher().executorService().shutdown();
  }
}
