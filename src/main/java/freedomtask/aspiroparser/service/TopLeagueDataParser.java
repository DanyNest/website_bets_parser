package freedomtask.aspiroparser.service;

import com.fasterxml.jackson.databind.JsonNode;
import freedomtask.aspiroparser.models.League;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @Author danynest
 * @CreateAt 09.04.25
 */
@Service
@RequiredArgsConstructor
public class TopLeagueDataParser {

  private final WebClient webClient;

  public Map<String, List<League>> receiveData() {
    Map<String, List<League>> topLeagues = new ConcurrentHashMap<>();
    String url = "/api-2/betline/sports?ctag=ru-RU&flags=urlv2";
    JsonNode root = webClient.get().uri(url).retrieve().bodyToMono(JsonNode.class).block();
    for (JsonNode sport : root) {
      String sportName = sport.get("name").asText();
      for (JsonNode region : sport.get("regions")) {
        for (JsonNode league : region.get("leagues")) {
          if (league.has("top") && league.get("top").asBoolean()) {
            topLeagues.computeIfAbsent(sportName, k -> new ArrayList<>())
                .add(new League(league.get("name").asText(), league.get("id").asText()));
          }
        }
      }
    }
    return topLeagues;
  }
}
