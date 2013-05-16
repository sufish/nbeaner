nbeaner 
=======

A Java beanstalk client.
we are using beanstalkd as our task queue, we found good java clients such as TrendrrBeanstalk, but they can not fully meet our
needs, so i rolled our own beanstalkd client thanks to the simple protocol of beanstalkd. 

it provides

1.  connection pooling
2.  sync interface and aysnc interface

it based on 

1.  JDK7
2.  Netty 4

now it only supports limited beanstalkd commands just to fullfill project needs and it will grow up along with our project

###example###

Sync call
```java
  BeanstalkClient client = new BeanstalkClient(11300, "localhost");
  BeanstalkConnection connection = client.getConnection();
  try{
    int jobId = connection.put(new Job("test".getBytes(), 65535, 0, 120));
    System.out.println("Inserted job with Id:" + jobId);
  }finally{
    connection.release();
  }
```

Async call
```java
  BeanstalkClient client = new BeanstalkClient(11300, "localhost");
  final BeanstalkConnection connection = client.getConnection();
  connection.put(new Job("test".getBytes(), 65535, 0, 120), new PutRequestCallback() {

            @Override
            public void onPutSuccess(int jobId) {
                System.out.println("Inserted job with Id:" + jobId);
                connection.release();
            }

            @Override
            public void onException(Exception cause) {
              cause.printStackTrace();
            }
  });
```
###Exception handling###
one reason that i decided to roll my own client is exception handling, we need rubust error handling for client to recovery
from server crashes. so basically there are 2 sorts of exception will be thrown/retrieved in the interfaces. FailedResponseException and BeanstalkConnectionException.
FailedResponseException will be  raised when beanstalkd server return response with failed status, client can still send command via the same channel.
BeanstalkConnectionException will be raised when there are I/O, protocol handling exceptions, in such case the underlying socket will be closed and client will not 
be able send commands via the same channel and has to fetch a new connection from the connection pool.

###Async callbacks###
Callbacks in Async interfaces will be executed in threads other than the I/O threads, so blocking operation like database calls will not 
impact I/O proformance. but by implemention these threads are from Netty4 event-loops so the callbacks invoked on same channcel will be executed on same threads
sequentially and hence blocking operations in callback may impact the execution of other callbacks even the callbacks will be invoked on a different channel(different channels may share the same thread).
so be careful and if possible you can move the blocking operation to your own thread-pool executors. 



