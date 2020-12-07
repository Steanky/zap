package io.github.zap.zombies;

import lombok.Getter;

public enum MessageKeys {
    /*
    These are all error message resources that should be displayed to the player when they fail to join an arena.
     */
    GENERIC_ARENA_REJECTION("zombies.arena.reject.generic"), //typically because the game is ongoing or full
    NEW_ARENA_REJECTION("zombies.arena.reject.new"), //new arena was created but still did not accept the player(s)
    OFFLINE_ARENA_REJECTION("zombies.arena.reject.offline"), //one or more of the players joining are offline
    UNKNOWN_ARENA_REJECTION("zombies.arena.reject.unknown"), //nonspecific error message

    ARENA_TERMINATION("zombies.arena.terminate"), //displayed when an arena is not gracefully terminated

    WINDOW_REPAIR_FAIL_MOB("zombies.game.window.fail.mob"), //shown when a mob blocks window repair
    WINDOW_REPAIR_FAIL_PLAYER("zombies.game.window.fail.player"), //shown when a player blocks window repair

    ADD_GOLD("zombies.game.gold.add"), //shown when gold is given to the player
    SUBTRACT_GOLD("zombies.game.gold.subtract"), //shown when gold is given to the player

    CANT_AFFORD("zombies.game.gold.unaffordable"); //generic message shown when something is too expensive to purchase

    @Getter
    private String key;

    MessageKeys(String key) {
        this.key = key;
    }
}
