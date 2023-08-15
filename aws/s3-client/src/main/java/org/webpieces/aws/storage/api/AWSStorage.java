package org.webpieces.aws.storage.api;

import com.google.inject.ImplementedBy;
import org.webpieces.aws.storage.impl.AWSStorageImpl;

/**
 * MOCK GCPRawStorage, NOT this class so you are mocking the LOWEST level and testing
 * your client assertions at test time!!!
 */
@ImplementedBy(AWSStorageImpl.class)
public interface AWSStorage extends AWSRawStorage {

}
