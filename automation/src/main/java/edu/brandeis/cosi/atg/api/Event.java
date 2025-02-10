package edu.brandeis.cosi.atg.api.event;

public sealed interface Event permits EndTurnEvent, GainCardEvent, GameEvent, PlayCardEvent {
    String getDescription();
}