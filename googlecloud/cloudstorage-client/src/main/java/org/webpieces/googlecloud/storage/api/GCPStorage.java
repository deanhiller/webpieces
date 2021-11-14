package org.webpieces.googlecloud.storage.api;

import com.google.inject.ImplementedBy;
import org.webpieces.googlecloud.storage.impl.GCPStorageImpl;

/**
 * MOCK GCPRawStorage, NOT this class so you are mocking the LOWEST level and testing
 * your client assertions at test time!!!
 */
@ImplementedBy(GCPStorageImpl.class)
public interface GCPStorage extends GCPRawStorage {

}
