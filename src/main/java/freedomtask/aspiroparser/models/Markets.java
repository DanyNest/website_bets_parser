package freedomtask.aspiroparser.models;

import java.util.List;
import lombok.Data;

/**
 * @Author danynest
 * @CreateAt 08.04.25
 */

@Data
public class Markets {
  String name;
  List<Runners> runners;
}
