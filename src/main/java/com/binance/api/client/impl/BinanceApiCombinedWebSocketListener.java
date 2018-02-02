package com.binance.api.client.impl;

import java.io.IOException;

import com.binance.api.client.BinanceApiCallback;
import com.binance.api.client.domain.event.CombinedStreamEvent;
import com.binance.api.client.exception.BinanceApiException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.WebSocket;

public class BinanceApiCombinedWebSocketListener<T> extends BinanceApiWebSocketListener<T> {  
  public BinanceApiCombinedWebSocketListener(BinanceApiCallback<T> callback, Class<T> eventClass) {
	super(callback, eventClass);
  }
  
  @Override
  public void onMessage(WebSocket webSocket, String text) {
	ObjectMapper mapper = new ObjectMapper();
	JavaType type = mapper.getTypeFactory().constructParametricType(CombinedStreamEvent.class, eventClass);
	try {
	  CombinedStreamEvent<T> event = mapper.readValue(text, type);
	  callback.onResponse(event.getRawPayload());
	} catch (IOException e) {
	  throw new BinanceApiException(e);
	}
  }
}
