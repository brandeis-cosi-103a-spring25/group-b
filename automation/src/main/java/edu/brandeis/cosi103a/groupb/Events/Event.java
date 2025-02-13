package edu.brandeis.cosi103a.groupb.Events;

public sealed interface Event permits EndTurnEvent, GainCardEvent, GameEvent, PlayCardEvent {
    String getDescription();
}