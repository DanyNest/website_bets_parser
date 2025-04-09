package freedomtask.aspiroparser.service;

import freedomtask.aspiroparser.models.Event;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author danynest
 * @CreateAt 09.04.25
 */


public class EventQueueStorage {
  public static final BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<>();
}
