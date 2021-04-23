package de.fraunhofer.isst.dataspaceconnector.repositories;

import java.util.List;

import de.fraunhofer.isst.dataspaceconnector.model.Data;
import de.fraunhofer.isst.dataspaceconnector.model.LocalData;
import de.fraunhofer.isst.dataspaceconnector.model.RemoteData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * The repository containing all objects of type {@link Data}.
 */
public interface DataRepository extends JpaRepository<Data, Long> {

    /**
     * Find all data stored locally.
     * @return All local objects.
     */
    @Query("select d from LocalData d")
    List<LocalData> findAllLocalData();

    /**
     * Find all data stored remotely.
     * @return All remote objects.
     */
    @Query("select d from RemoteData d")
    List<RemoteData> findAllRemoteData();

    @Modifying
    @Query("update LocalData a set a.value = :data where a.id = :entityId")
    void setLocalData(Long entityId, String data);
}
