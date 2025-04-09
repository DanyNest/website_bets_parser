package freedomtask.aspiroparser;

import static freedomtask.aspiroparser.service.EventQueueStorage.eventQueue;

import com.fasterxml.jackson.databind.JsonNode;
import freedomtask.aspiroparser.models.Event;
import freedomtask.aspiroparser.models.League;
import freedomtask.aspiroparser.models.Markets;
import freedomtask.aspiroparser.models.Runners;
import freedomtask.aspiroparser.service.EventQueueStorage;
import freedomtask.aspiroparser.service.TopLeagueDataParser;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @Author danynest
 * @CreateAt 09.04.25
 */
@Service
public class EventMarketsDataParser {
  private final WebClient webClient;
  private final TopLeagueDataParser topLeagueDataReceiver;

  public EventMarketsDataParser(WebClient webClient, TopLeagueDataParser topLeagueDataReceiver) {
    this.webClient = webClient;
    this.topLeagueDataReceiver = topLeagueDataReceiver;
  }

  public void retrieveAndQueueEvents() {
    Map<String, List<League>> leagues = topLeagueDataReceiver.receiveData();
    for (Map.Entry<String, List<League>> entry : leagues.entrySet()) {
      String sport = entry.getKey();
      List<League> leagueList = entry.getValue();
      CompletableFuture.runAsync(() -> {
        List<Event> events = new ArrayList<>();
        for (League league : leagueList) {
          AtomicInteger counter = new AtomicInteger(0);
          String url = "/api-2/betline/changes/all?ctag=en-US&vtag=9c2cd386-31e1-4ce9-a140-28e9b63a9300&league_id="
              + league.getLeagueId() + "&hideClosed=true&flags=reg,urlv2,mm2,rrc,nodup";
          try {
            JsonNode json = webClient.get().uri(url).retrieve().bodyToMono(JsonNode.class).block();
            if (json != null && json.has("data")) {
              for (JsonNode dataNode : json.get("data")) {
                if (counter.getAndIncrement() >= 2) break;
                Event event = parseEvent(dataNode, league.getLeagueName(), sport);
                events.add(event);
                eventQueue.offer(event);
              }
            }
          } catch (Exception e) {
            System.err.println("Ошибка при получении данных для лиги: " + league.getLeagueId());
          }
        }
      });
    }
  }

  private Event parseEvent(JsonNode node, String leagueName, String sportName) {
    Event event = new Event();
    event.setMarkets(new ArrayList<>());
    event.setEventId(node.get("id").asLong());
    event.setEventName(node.get("name").asText());
    event.setStartDate(new Date(node.get("kickoff").asLong()));
    event.setLeagueName(leagueName);
    event.setSportName(sportName);
    if (node.has("markets")) {
      for (JsonNode marketNode : node.get("markets")) {
        Markets market = new Markets();
        market.setName(marketNode.get("name").asText());
        market.setRunners(new ArrayList<>());
        for (JsonNode runnerNode : marketNode.get("runners")) {
          Runners runner = new Runners();
          runner.setId(runnerNode.get("id").asLong());
          runner.setName(runnerNode.get("name").asText());
          runner.setPrice(runnerNode.get("price").asDouble());
          market.getRunners().add(runner);
        }
        event.getMarkets().add(market);
      }
    }
    return event;
  }
}