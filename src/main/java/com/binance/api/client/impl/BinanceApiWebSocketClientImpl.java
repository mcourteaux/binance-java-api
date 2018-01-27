package com.binance.api.client.impl;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.binance.api.client.BinanceApiCallback;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.constant.BinanceApiConstants;
import com.binance.api.client.domain.event.AggTradeEvent;
import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.event.DepthEvent;
import com.binance.api.client.domain.event.UserDataUpdateEvent;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.domain.market.TickerStatistics;

import okhttp3.ConnectionPool;
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
  public WebSocket onDepthEvent(String symbol, BinanceApiCallback<DepthEvent> callback) {
    final String channel = String.format("%s@depth", symbol);
    return createNewWebSocket(channel, new BinanceApiWebSocketListener<>(callback, DepthEvent.class));
  }
  
  @Override
  public WebSocket onDepthEvent(String symbol, int level, BinanceApiCallback<DepthEvent> callback) {
    final String channel = String.format("%s@depth" + level, symbol);
    return createNewWebSocket(channel, new BinanceApiWebSocketListener<>(evt -> {
    		evt.setSymbol(symbol);
    		callback.onResponse(evt);
    }, DepthEvent.class));
  }

  @Override
  public WebSocket onCandlestickEvent(String symbol, CandlestickInterval interval, BinanceApiCallback<CandlestickEvent> callback) {
    final String channel = String.format("%s@kline_%s", symbol, interval.getIntervalId());
    return createNewWebSocket(channel, new BinanceApiWebSocketListener<>(callback, CandlestickEvent.class));
  }

  @Override
  public WebSocket onAggTradeEvent(String symbol, BinanceApiCallback<AggTradeEvent> callback) {
    final String channel = String.format("%s@aggTrade", symbol);
    return createNewWebSocket(channel, new BinanceApiWebSocketListener<>(callback, AggTradeEvent.class));
  }

  @Override
  public WebSocket onUserDataUpdateEvent(String listenKey, BinanceApiCallback<UserDataUpdateEvent> callback) {
    return createNewWebSocket(listenKey, new BinanceApiWebSocketListener<>(callback, UserDataUpdateEvent.class));
  }

  @Override
  public WebSocket onMarketTickersEvent(BinanceApiCallback<TickerStatistics[]> callback) {
    final String channel = String.format("!ticker@arr");
    return createNewWebSocket(channel, new BinanceApiWebSocketListener<>(callback, TickerStatistics[].class));
  }

  private WebSocket createNewWebSocket(String channel, BinanceApiWebSocketListener<?> listener) {
    String streamingUrl = String.format("%s/%s", BinanceApiConstants.WS_API_BASE_URL, channel);
    Request request = new Request.Builder().url(streamingUrl).build();
    return client.newWebSocket(request, listener);
  }

  @Override
  public void close() throws IOException {
    client.dispatcher().executorService().shutdown();
  }
}
