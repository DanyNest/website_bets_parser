package freedomtask.aspiroparser;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @Author danynest
 * @CreateAt 09.04.25
 */
@Component
@RequiredArgsConstructor
public class EventPollingScheduler {

  private final EventMarketsDataParser receiver;

  @Scheduled(fixedRate = 60000)
  public void scheduledPolling() {
    receiver.retrieveAndQueueEvents();
  }
}
