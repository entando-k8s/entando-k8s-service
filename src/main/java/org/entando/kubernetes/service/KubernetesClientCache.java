package org.entando.kubernetes.service;

import io.fabric8.kubernetes.client.KubernetesClient;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@SuppressWarnings("java:S2160")
//because this class should never be checked for equality. It is conceptually a singleton
public class KubernetesClientCache extends ConcurrentHashMap<String, KubernetesClient> {

    final transient Timer timer = new Timer();
    final transient ConcurrentHashMap<String, Instant> accessTimes = new ConcurrentHashMap<>();
    private final int maximumAgeSeconds;
    private transient Function<String, KubernetesClient> kubernetesClientSupplier;

    //For tests
    public KubernetesClientCache(Function<String, KubernetesClient> kubernetesClientSupplier, int maximumAgeSeconds, long scanInterval) {
        //Remove after 1 hour
        //Scan every minute
        this(maximumAgeSeconds, scanInterval);
        this.kubernetesClientSupplier = kubernetesClientSupplier;
    }

    public KubernetesClientCache(Function<String, KubernetesClient> kubernetesClientSupplier) {
        //Remove after 1 hour
        //Scan every minute
        this(kubernetesClientSupplier, 3600, 60 * 1000L);
    }

    private KubernetesClientCache(int maximumAgeSeconds, long scanInterval) {
        super();
        this.maximumAgeSeconds = maximumAgeSeconds;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                removeStaleEntries();
            }

        }, 0, scanInterval);
    }

    @Override
    public KubernetesClient get(Object tokenAsObject) {
        final String token = (String) tokenAsObject;
        accessTimes.put(token,Instant.now());
        return super.computeIfAbsent(token, this.kubernetesClientSupplier);
    }

    private void removeStaleEntries() {
        Instant cutoffInstant = Instant.now().minusSeconds(maximumAgeSeconds);
        keySet().stream().filter(s -> accessTimes.get(s).isBefore(cutoffInstant)).forEach(token -> {
            //Synchronization risk here is minimal
            accessTimes.remove(token);
            remove(token).close();
        });
    }
}
