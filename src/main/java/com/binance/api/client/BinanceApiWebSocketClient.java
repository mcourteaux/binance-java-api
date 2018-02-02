package com.binance.api.client;

import java.io.Closeable;
import java.util.List;

import com.binance.api.client.domain.event.AggTradeEvent;
import com.binance.api.client.domain.event.AllMarketTickersEvent;
import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.event.DepthEvent;
import com.binance.api.client.domain.event.UserDataUpdateEvent;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.domain.market.TickerStatistics;

/**
 * Binance API data streaming façade, supporting streaming of events through web sockets.
 */
public interface BinanceApiWebSocketClient extends Closeable {

  Closeable onDepthEvent(String symbol, BinanceApiCallback<DepthEvent> callback);

  Closeable onDepthEvent(String symbol, int level, BinanceApiCallback<DepthEvent> callback);

  Closeable onCandlestickEvent(String symbol, CandlestickInterval interval, BinanceApiCallback<CandlestickEvent> callback);
  
  Closeable onCandlestickEvent(List<String> symbols, CandlestickInterval interval, BinanceApiCallback<CandlestickEvent> callback);

  Closeable onMarketTickersEvent(BinanceApiCallback<TickerStatistics[]> callback);

  Closeable onAggTradeEvent(String symbol, BinanceApiCallback<AggTradeEvent> callback);

  Closeable onUserDataUpdateEvent(String listenKey, BinanceApiCallback<UserDataUpdateEvent> callback);

  Closeable onAllMarketTickersEvent(BinanceApiCallback<List<AllMarketTickersEvent>> callback);
  
  void close();
}
