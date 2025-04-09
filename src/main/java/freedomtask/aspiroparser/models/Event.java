package freedomtask.aspiroparser.models;

import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 * @Author danynest
 * @CreateAt 08.04.25
 */

@Data
public class Event {
  Long eventId;
  String leagueName;
  String sportName;
  Date startDate;
  String eventName;
  List<Markets> markets;
}
