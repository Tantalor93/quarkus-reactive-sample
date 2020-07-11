package com.github.tantalor93;

import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;

@ApplicationScoped
public class HelloService {

	private static Logger LOGGER = LoggerFactory.getLogger(HelloService.class);

	@Inject
	private Vertx vertx;


	public Uni<String> getHello() {
		WebClient localhostClient = WebClient
				.create(vertx, new WebClientOptions().setDefaultHost("localhost").setDefaultPort(8888));

		LOGGER.info("service call");

		return callWebStub(localhostClient)
				.onItem()
					.produceUni(e -> applyBlockingSleep())
				.onItem()
					.produceUni(e -> callWebStub(localhostClient));
	}

	private Uni<String> callWebStub(WebClient localhostClient) {
		return localhostClient
				.get("/benky/jede2")
				.send()
				.map(e -> {
					LOGGER.info("webclient call done");
					return e.bodyAsString();
				});
	}

	private Uni<String> applyBlockingSleep() {
		return Uni.createFrom()
				.item("benky")
				.map(e -> sleep())
				.runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
				.onItem().apply(e -> {
					LOGGER.info("apply on");
					return e + "1";
				});
	}

	private String sleep() {
		LOGGER.info("doing sleep");
		try {
			TimeUnit.MILLISECONDS.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		LOGGER.info("sleep complete");
		return "hello";
	}
}