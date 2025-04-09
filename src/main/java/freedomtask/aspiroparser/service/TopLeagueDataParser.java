package freedomtask.aspiroparser.service;

import com.fasterxml.jackson.databind.JsonNode;
import freedomtask.aspiroparser.models.League;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

/**
 * @Author danynest
 * @CreateAt 09.04.25
 */
@Service
@RequiredArgsConstructor
public class TopLeagueDataParser {

  @Value("${name.node.key}")
  private String NAME_NODE_KEY;
  @Value("${regions.node.key}")
  private String REGIONS_NODE_KEY;
  @Value("${leagues.node.key}")
  private String LEAGUES_NODE_KEY;
  @Value("${top.node.key}")
  private String TOP_NODE_KEY;
  @Value("${id.node.key}")
  private String ID_NODE_KEY;
  @Value("${leon.top.league.url}")
  private String LEON_TOP_LEAGUE_URL;
  private final WebClient webClient;

  public Map<String, List<League>> receiveData() {
    Map<String, List<League>> topLeagues = new ConcurrentHashMap<>();
    JsonNode root = webClient.get().uri(LEON_TOP_LEAGUE_URL)
        .retrieve()
        .bodyToMono(JsonNode.class)
        .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)))
        .block();
    for (JsonNode sport : root) {
      String sportName = sport.get(NAME_NODE_KEY).asText();
      for (JsonNode region : sport.get(REGIONS_NODE_KEY)) {
        for (JsonNode league : region.get(LEAGUES_NODE_KEY)) {
          if (league.has(TOP_NODE_KEY) && league.get(TOP_NODE_KEY).asBoolean()) {
            topLeagues.computeIfAbsent(sportName, k -> new ArrayList<>())
                .add(new League(league.get(NAME_NODE_KEY).asText(),
                    league.get(ID_NODE_KEY).asText()));
          }
        }
      }
    }
    return topLeagues;
  }
}
