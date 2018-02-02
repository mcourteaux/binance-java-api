package com.binance.api.client.impl;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import com.binance.api.client.BinanceApiCallback;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.constant.BinanceApiConstants;
import com.binance.api.client.domain.event.AggTradeEvent;
import com.binance.api.client.domain.event.AllMarketTickersEvent;
import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.event.CombinedStreamEvent;
import com.binance.api.client.domain.event.DepthEvent;
import com.binance.api.client.domain.event.UserDataUpdateEvent;
import com.binance.api.client.domain.market.CandlestickInterval;

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
  public void onDepthEvent(String symbol, BinanceApiCallback<DepthEvent> callback) {
    final String channel = String.format("%s@depth", symbol);
    createNewWebSocket(channel, new BinanceApiWebSocketListener<>(callback, DepthEvent.class));
  }

  @Override
  public void onCandlestickEvent(String symbol, CandlestickInterval interval, BinanceApiCallback<CandlestickEvent> callback) {
    final String channel = String.format("%s@kline_%s", symbol, interval.getIntervalId());
    createNewWebSocket(channel, new BinanceApiWebSocketListener<>(callback, CandlestickEvent.class));
  }
  
  @Override
  public void onCandlestickEvent(List<String> symbols, CandlestickInterval interval, BinanceApiCallback<CandlestickEvent> callback) {
	List<String> channels = symbols.stream().map(symbol -> String.format("%s@kline_%s", symbol, interval.getIntervalId())).collect(Collectors.toList());
	createNewWebSocket(channels, new BinanceApiCombinedWebSocketListener<>(callback, CandlestickEvent.class));
  }

  @Override
  public void onAggTradeEvent(String symbol, BinanceApiCallback<AggTradeEvent> callback) {
    final String channel = String.format("%s@aggTrade", symbol);
    createNewWebSocket(channel, new BinanceApiWebSocketListener<>(callback, AggTradeEvent.class));
  }

  @Override
  public void onUserDataUpdateEvent(String listenKey, BinanceApiCallback<UserDataUpdateEvent> callback) {
    createNewWebSocket(listenKey, new BinanceApiWebSocketListener<>(callback, UserDataUpdateEvent.class));
  }

  @Override
  public void onAllMarketTickersEvent(BinanceApiCallback<List<AllMarketTickersEvent>> callback) {
    final String channel = "!ticker@arr";
    createNewWebSocket(channel, new BinanceApiWebSocketListener<>(callback));
  }

  private void createNewWebSocket(String channel, BinanceApiWebSocketListener<?> listener) {
	String streamingUrl = String.format("%s/ws/%s", BinanceApiConstants.WS_API_BASE_URL, channel);
    createNewWebSocketForUrl(streamingUrl, listener);
  }
  
  /* For combined streams */
  private void createNewWebSocket(List<String> channels, BinanceApiCombinedWebSocketListener<?> listener) {
	String combinedChannels = channels.stream().reduce((s1, s2) -> s1 + "/" + s2).get();
	String streamingUrl = String.format("%s/stream?streams=%s", BinanceApiConstants.WS_API_BASE_URL, combinedChannels);
	createNewWebSocketForUrl(streamingUrl, listener);
  }
  
  private void createNewWebSocketForUrl(String streamingUrl, BinanceApiWebSocketListener<?> listener) {
	  Request request = new Request.Builder().url(streamingUrl).build();
	  client.newWebSocket(request, listener);
  }

  @Override
  public void close() throws IOException {
    client.dispatcher().executorService().shutdown();
  }

}
