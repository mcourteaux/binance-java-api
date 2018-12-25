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

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Arrays;

/**
 * Binance API WebSocket client implementation using OkHttp.
 */
public class BinanceApiWebSocketClientImpl implements BinanceApiWebSocketClient, Closeable {

    private final OkHttpClient client;

    public BinanceApiWebSocketClientImpl(OkHttpClient client) {
        this.client = client;
    }

    @Override
    public Closeable onDepthEvent(String symbols, BinanceApiCallback<DepthEvent> callback) {
        final String channel = Arrays.stream(symbols.split(","))
                .map(String::trim)
                .map(s -> String.format("%s@depth", s))
                .collect(Collectors.joining("/"));
        return createNewWebSocket(channel, new BinanceApiWebSocketListener<>(callback, DepthEvent.class));
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
    public Closeable onCandlestickEvent(String symbols, CandlestickInterval interval, BinanceApiCallback<CandlestickEvent> callback) {
        final String channel = Arrays.stream(symbols.split(","))
                .map(String::trim)
                .map(s -> String.format("%s@kline_%s", s, interval.getIntervalId()))
                .collect(Collectors.joining("/"));
        return createNewWebSocket(channel, new BinanceApiWebSocketListener<>(callback, CandlestickEvent.class));
    }

    public Closeable onAggTradeEvent(String symbols, BinanceApiCallback<AggTradeEvent> callback) {
        final String channel = Arrays.stream(symbols.split(","))
                .map(String::trim)
                .map(s -> String.format("%s@aggTrade", s))
                .collect(Collectors.joining("/"));
        return createNewWebSocket(channel, new BinanceApiWebSocketListener<>(callback, AggTradeEvent.class));
    }

	@Override
	public Closeable onCandlestickEvent(List<String> symbols, CandlestickInterval interval, BinanceApiCallback<CandlestickEvent> callback) {
		List<String> channels = symbols.stream().map(symbol -> String.format("%s@kline_%s", symbol, interval.getIntervalId())).collect(Collectors.toList());
		return createNewWebSocket(channels, new BinanceApiCombinedWebSocketListener<>(callback, CandlestickEvent.class));
	}

	public Closeable onUserDataUpdateEvent(String listenKey, BinanceApiCallback<UserDataUpdateEvent> callback) {
		return createNewWebSocket(listenKey, new BinanceApiWebSocketListener<>(callback, UserDataUpdateEvent.class));
	}

	/* For combined streams */
	private <T> Closeable createNewWebSocket(List<String> channels, BinanceApiCombinedWebSocketListener<?> listener) {
		String combinedChannels = channels.stream().reduce((s1, s2) -> s1 + "/" + s2).get();
		String streamingUrl = String.format("%s/stream?streams=%s", BinanceApiConstants.WS_API_BASE_URL, combinedChannels);
		return createNewWebSocketForUrl(streamingUrl, listener);
	}

	private Closeable createNewWebSocketForUrl(String streamingUrl, BinanceApiWebSocketListener<?> listener) {
		System.out.println(streamingUrl);
		Request request = new Request.Builder().url(streamingUrl).build();
		final WebSocket webSocket = client.newWebSocket(request, listener);
		return () -> {
			final int code = 1000;
			listener.onClosing(webSocket, code, null);
			webSocket.close(code, null);
			listener.onClosed(webSocket, code, null);
		};
	}

    public Closeable onAllMarketTickersEvent(BinanceApiCallback<List<AllMarketTickersEvent>> callback) {
        final String channel = "!ticker@arr";
        return createNewWebSocket(channel, new BinanceApiWebSocketListener<>(callback, new TypeReference<List<AllMarketTickersEvent>>() {}));
    }

    /**
     * @deprecated This method is no longer functional. Please use the returned {@link Closeable} from any of the other methods to close the web socket.
     */
    @Override
	public void close() {
		// client.dispatcher().executorService().shutdown();
	}

	private Closeable createNewWebSocket(String channel, BinanceApiWebSocketListener<?> listener) {
        String streamingUrl = String.format("%s/ws/%s", BinanceApiConstants.WS_API_BASE_URL, channel);
        Request request = new Request.Builder().url(streamingUrl).build();
        final WebSocket webSocket = client.newWebSocket(request, listener);
        return () -> {
            final int code = 1000;
            listener.onClosing(webSocket, code, null);
            webSocket.close(code, null);
            listener.onClosed(webSocket, code, null);
        };
    }
}
