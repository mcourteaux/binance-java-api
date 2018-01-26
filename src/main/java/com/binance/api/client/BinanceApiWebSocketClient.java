package com.binance.api.client;

import com.binance.api.client.domain.event.AggTradeEvent;
import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.event.DepthEvent;
import com.binance.api.client.domain.event.UserDataUpdateEvent;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.domain.market.TickerStatistics;

import okhttp3.WebSocket;

/**
 * Binance API data streaming façade, supporting streaming of events through web sockets.
 */
public interface BinanceApiWebSocketClient {

  WebSocket onDepthEvent(String symbol, BinanceApiCallback<DepthEvent> callback);

  WebSocket onCandlestickEvent(String symbol, CandlestickInterval interval, BinanceApiCallback<CandlestickEvent> callback);

  WebSocket onAggTradeEvent(String symbol, BinanceApiCallback<AggTradeEvent> callback);
  
  WebSocket onMarketTickersEvent(BinanceApiCallback<TickerStatistics[]> callback);

  WebSocket onUserDataUpdateEvent(String listenKey, BinanceApiCallback<UserDataUpdateEvent> callback);
}
