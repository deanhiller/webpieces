this is for later.  Basically, for any one implmenting XXXXManagement interface, if it is registered with
this piece (embeddable management server), then it should

1. expose a management web page (through JMX or some means)
2. save any changes to a storage abstraction so changes are persisted

