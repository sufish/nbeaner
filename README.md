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



