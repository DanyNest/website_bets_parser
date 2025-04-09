package freedomtask.aspiroparser.service;

import static freedomtask.aspiroparser.service.EventQueueStorage.eventQueue;

import com.fasterxml.jackson.databind.JsonNode;
import freedomtask.aspiroparser.models.Event;
import freedomtask.aspiroparser.models.League;
import freedomtask.aspiroparser.models.Markets;
import freedomtask.aspiroparser.models.Runners;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

/**
 * @Author danynest
 * @CreateAt 09.04.25
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EventMarketsDataParser {
  private final ExecutorService executorService = Executors.newFixedThreadPool(3);

  @Value("${leon.league.data.url.prefix}")
  private String URL_PREFIX;
  @Value("${leon.league.data.url.sufix}")
  private String URL_SUFFIX;
  @Value("${id.node.key}")
  private String ID_NODE_KEY;
  @Value("${name.node.key}")
  private String NAME_NODE_KEY;
  @Value("${data.node.key}")
  private String DATA_NODE_KEY;
  @Value("${markets.node.key}")
  private String MARKETS_NODE_KEY;
  @Value("${runners.node.key}")
  private String RUNNERS_NODE_KEY;
  @Value("${price.node.key}")
  private String PRICE_NODE_KEY;
  @Value("${start-date.node.key}")
  private String START_DATE_NODE_KEY;
  private final WebClient webClient;
  private final TopLeagueDataParser topLeagueDataReceiver;
  private static final String LEAGUE_EXTRACTING_DATA_MESSAGE = "Error occurred during extracting data for league with ID: ";

  public void retrieveAndQueueEvents() {
    Map<String, List<League>> leagues = topLeagueDataReceiver.receiveData();
    for (Map.Entry<String, List<League>> entry : leagues.entrySet()) {
      String sport = entry.getKey();
      List<League> leagueList = entry.getValue();
      CompletableFuture.runAsync(() -> {
        List<Event> events = new ArrayList<>();
        for (League league : leagueList) {
          AtomicInteger counter = new AtomicInteger(0);
          String url = URL_PREFIX + league.getLeagueId() + URL_SUFFIX;
          try {
            JsonNode json = webClient.get()
                .uri(url)
                .retrieve().bodyToMono(JsonNode.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)))
                .block();
            if (json != null && json.has(DATA_NODE_KEY)) {
                for (JsonNode dataNode : json.get(DATA_NODE_KEY)) {
                if (counter.getAndIncrement() >= 2) {
                  break;
                }
                Event event = parseEvent(dataNode, league.getLeagueName(), sport);
                events.add(event);
                eventQueue.offer(event);
              }
            }
          } catch (Exception e) {
            log.error(LEAGUE_EXTRACTING_DATA_MESSAGE + league.getLeagueId());
          }
        }
      }, executorService);
    }
  }

  private Event parseEvent(JsonNode node, String leagueName, String sportName) {
    Event event = new Event();
    event.setMarkets(new ArrayList<>());
    event.setEventId(node.get(ID_NODE_KEY).asLong());
    event.setEventName(node.get(NAME_NODE_KEY).asText());
    event.setStartDate(new Date(node.get(START_DATE_NODE_KEY).asLong()));
    event.setLeagueName(leagueName);
    event.setSportName(sportName);
    if (node.has(MARKETS_NODE_KEY)) {
      for (JsonNode marketNode : node.get(MARKETS_NODE_KEY)) {
        Markets market = new Markets();
        market.setName(marketNode.get(NAME_NODE_KEY).asText());
        market.setRunners(new ArrayList<>());
        for (JsonNode runnerNode : marketNode.get(RUNNERS_NODE_KEY)) {
          Runners runner = new Runners();
          runner.setId(runnerNode.get(ID_NODE_KEY).asLong());
          runner.setName(runnerNode.get(NAME_NODE_KEY).asText());
          runner.setPrice(runnerNode.get(PRICE_NODE_KEY).asDouble());
          market.getRunners().add(runner);
        }
        event.getMarkets().add(market);
      }
    }
    return event;
  }
}