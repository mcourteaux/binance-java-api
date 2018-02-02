package com.binance.api.client.impl;

import com.binance.api.client.BinanceApiCallback;
import com.binance.api.client.domain.event.CombinedStreamEvent;

public class BinanceApiCombinedWebSocketListener<Y, T extends CombinedStreamEvent<Y>> extends BinanceApiWebSocketListener<T> {  
  public BinanceApiCombinedWebSocketListener(BinanceApiCallback<Y> callback) {
	super(combinedEvent -> callback.onResponse(combinedEvent.getRawPayload()));
  }
}
