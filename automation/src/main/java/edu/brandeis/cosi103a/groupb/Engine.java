package edu.brandeis.cosi103a.groupb;

import java.util.List;

public interface Engine {
    List<Player.ScorePair> play() throws PlayerViolationException;
}
