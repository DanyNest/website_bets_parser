package freedomtask.aspiroparser.service;


import static freedomtask.aspiroparser.service.EventQueueStorage.eventQueue;

import freedomtask.aspiroparser.models.Event;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.stereotype.Service;

/**
 * @Author danynest
 * @CreateAt 09.04.25
 */

@Service
public class ConsoleOutputService {

  private final ExecutorService executorService = Executors.newFixedThreadPool(3);


  @PostConstruct
  public void startConsumers() {
    for (int i = 0; i < 3; i++) {
      executorService.submit(() -> {
        while (!Thread.currentThread().isInterrupted()) {
          try {
            Event event = eventQueue.take();
            processEvent(event);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        }
      });
    }
  }

  public void processEvent(Event event) {
    StringBuilder builder = new StringBuilder();
    builder.append(event.getSportName())
        .append(", ")
        .append(event.getLeagueName())
        .append("\n")
        .append("\t")
        .append(event.getEventName())
        .append(", ")
        .append(event.getStartDate())
        .append(", ")
        .append(event.getEventId());
    event.getMarkets().forEach(market -> {
      builder.append("\n")
          .append("\t")
          .append("\t")
          .append(market.getName());
      market.getRunners().forEach(runner -> {
        builder.append("\n")
            .append("\t")
            .append("\t")
            .append("\t")
            .append("\t")
            .append(runner.getName())
            .append(", ")
            .append(runner.getPrice())
            .append(", ")
            .append(runner.getId());
      });
    });
    builder
        .append("\n")
        .append("\n");
    System.out.println(builder);
  }
}
