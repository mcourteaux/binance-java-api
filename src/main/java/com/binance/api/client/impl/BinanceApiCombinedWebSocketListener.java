package com.binance.api.client.impl;

import com.binance.api.client.BinanceApiCallback;
import com.binance.api.client.domain.event.CombinedStreamEvent;

public class BinanceApiCombinedWebSocketListener<T> extends BinanceApiWebSocketListener<CombinedStreamEvent<T>> {
  
  public BinanceApiCombinedWebSocketListener(BinanceApiCallback<T> callback) {
	super(combinedEvent -> callback.onResponse(combinedEvent.getRawPayload()));
  }
}
