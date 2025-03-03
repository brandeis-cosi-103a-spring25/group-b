package edu.brandeis.cosi103a.groupb.Game;

import java.util.List;

import edu.brandeis.cosi103a.groupb.Player.Player;
import edu.brandeis.cosi103a.groupb.Player.PlayerViolationException;

public interface Engine {
    List<Player.ScorePair> play() throws PlayerViolationException;
}
