package com.binance.api.client.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CombinedStreamEvent<T> {
  @JsonProperty("stream")
  private String streamName;
  
  @JsonProperty("data")
  private T rawPayload;

  public String getStreamName() {
    return streamName;
  }

  public void setStreamName(String streamName) {
    this.streamName = streamName;
  }

  public T getRawPayload() {
    return rawPayload;
  }

  public void setRawPayload(T rawPayload) {
	this.rawPayload = rawPayload;
  }
}
