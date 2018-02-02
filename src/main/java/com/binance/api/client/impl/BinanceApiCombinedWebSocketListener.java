package com.binance.api.client.impl;

import com.binance.api.client.BinanceApiCallback;
import com.binance.api.client.domain.event.CombinedStreamEvent;

public class BinanceApiCombinedWebSocketListener<Y, T extends CombinedStreamEvent<Y>> extends BinanceApiWebSocketListener<T> {  
  public BinanceApiCombinedWebSocketListener(BinanceApiCallback<Y> callback) {
	super(new BinanceApiCallback<T>() {
		@Override
		public void onResponse(T response) {
			callback.onResponse(response.getRawPayload());
		}
		
		@Override
		public void onFailure(Throwable cause) {
			callback.onFailure(cause);
		}
	});
  }
}
