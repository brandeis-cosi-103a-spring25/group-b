package edu.brandeis.cosi.atg.api;

import com.google.common.collect.ImmutableList;

public interface Engine {
    ImmutableList<Player.ScorePair> play() throws PlayerViolationException;
}
