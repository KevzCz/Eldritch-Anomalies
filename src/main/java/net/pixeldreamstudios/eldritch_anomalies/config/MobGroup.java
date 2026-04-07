package net.pixeldreamstudios.eldritch_anomalies.config;

public enum MobGroup {
    DEFAULT("default"),
    ALLOWED("allowed"),
    BLACKLISTED("blacklisted"),
    ALWAYS_ELITE("always_elite"),
    ALWAYS_ULTRA("always_ultra"),
    ALWAYS_ELDRITCH("always_eldritch");

    private final String key;

    MobGroup(String key) {
        this.key = key;
    }

    public String translationKey() {
        return "mobgroup.eldritch-anomalies." + key;
    }
}
