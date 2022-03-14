package me.profelements.dynatech.items.misc;

import java.util.UUID;

public class FlyingPlayer {
    private UUID uuid;
    private int flyCount;

    public FlyingPlayer(UUID uuid) {
        this.uuid = uuid;
        this.flyCount = 0;
    }

    public UUID getId() {
        return this.uuid;
    }

    public void increase() {
        this.flyCount++;
    }

    public void decrease() {
        this.flyCount--;
    }

    public void reset() {
        this.flyCount = 0;
    }

    public boolean canFly() {
        return this.flyCount > 0;
    }
}
