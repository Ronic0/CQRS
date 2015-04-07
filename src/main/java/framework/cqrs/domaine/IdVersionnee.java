package java.framework.cqrs.domaine;

import java.util.UUID;

/**
 * Created by Nicolas Caline on 07-04-15.
 */
public class IdVersionnee extends ValueObject {

    private static final long serialVersionUID = 1L;

    public static final long INITIAL_VERSION = 0;
    private static final long LATEST_VERSION = Long.MAX_VALUE;

    private final UUID id;
    private final long version;

    private IdVersionnee(UUID id, long version) {
        this.id = id;
        this.version = version;
    }

    public static IdVersionnee aleatoire() {
        return pourVersionInitiale(UUID.randomUUID());
    }

    public static IdVersionnee pourVersionInitiale(UUID id) {
        return pourVersionSpecifique(id, INITIAL_VERSION);
    }

    public static  IdVersionnee pourDerniereVersion(UUID id) {
        return pourVersionSpecifique(id, LATEST_VERSION);
    }

    public static IdVersionnee pourVersionSpecifique(UUID id, long version) {
        return new IdVersionnee(id, version);
    }

    public UUID getId() {
        return id;
    }

    public long getVersion() {
        return version;
    }

    public boolean isPourVersionInitiale() {
        return version == INITIAL_VERSION;
    }

    public boolean isPourDerniereVersion() {
        return version == LATEST_VERSION;
    }

    public boolean isPourVersionSpecifique() {
        return !isPourDerniereVersion();
    }

    public IdVersionnee avecVersion(long version) {
        return IdVersionnee.pourVersionSpecifique(id, version);
    }

    public IdVersionnee versionSuivante() {
        if (isPourDerniereVersion()) {
            return this;
        } else {
            return avecVersion(version + 1);
        }
    }

    public boolean equalsIgnoreVersion(IdVersionnee other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        return id.equals(other.id);
    }

    public boolean isCompatible(IdVersionnee other) {
        if (isPourDerniereVersion()) {
            return equalsIgnoreVersion(other);
        } else {
            return equals(other);
        }
    }

    @Override
    public String toString() {
        if (isPourDerniereVersion()) {
            return id.toString();
        } else {
            return id + "#" + version;
        }
    }
}
