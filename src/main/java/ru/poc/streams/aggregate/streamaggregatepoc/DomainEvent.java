package ru.poc.streams.aggregate.streamaggregatepoc;

public class DomainEvent {
    String eventType;

    String boardUuid;

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getBoardUuid() {
        return boardUuid;
    }

    public void setBoardUuid(String boardUuid) {
        this.boardUuid = boardUuid;
    }
}
