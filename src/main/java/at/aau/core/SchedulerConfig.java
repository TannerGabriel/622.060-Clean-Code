package at.aau.core;

import java.util.Arrays;
import java.util.Objects;

public record SchedulerConfig(String[] urls, int depthLimit, String domainFilter, String targetLang) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchedulerConfig that = (SchedulerConfig) o;
        return depthLimit == that.depthLimit && Arrays.equals(urls, that.urls) && Objects.equals(domainFilter, that.domainFilter) && Objects.equals(targetLang, that.targetLang);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(depthLimit, domainFilter, targetLang);
        result = 31 * result + Arrays.hashCode(urls);
        return result;
    }

    @Override
    public String toString() {
        return "SchedulerConfig{" +
                "urls=" + Arrays.toString(urls) +
                ", depthLimit=" + depthLimit +
                ", domainFilter='" + domainFilter + '\'' +
                ", targetLang='" + targetLang + '\'' +
                '}';
    }
}
