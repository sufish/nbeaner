package com.sufish.nbeaner;

import com.sufish.nbeaner.handlers.callback.FailedResponseException;
import com.sufish.nbeaner.handlers.callback.PutRequestCallback;
import com.sufish.nbeaner.pool.BeanstalkClient;
import com.sufish.nbeaner.pool.BeanstalkConnection;
import com.sufish.nbeaner.pool.BeanstalkException;
import com.sufish.nbeaner.protocol.Beanstalk;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

public class BeanstalkClientTest {

    private BeanstalkClient client = new BeanstalkClient(11300, "localhost");
    private BeanstalkConnection connection;

    @Before
    public void setUp() throws Exception {
        connection = client.getConnection();
    }

    @After
    public void tearDown() throws InterruptedException, BeanstalkException {
        while (true) {
            try {
                Job job = connection.reserve(1);
                connection.delete(job.getId());
            } catch (BeanstalkException e) {
                if (e instanceof FailedResponseException) {
                    FailedResponseException fe = (FailedResponseException) e;
                    if (fe.getResponseText().equals(Beanstalk.TIMED_OUT)) {
                        break;
                    } else {
                        throw e;
                    }
                }
            }
        }
        connection.release();
    }

    @Test
    public void testPut() throws Exception {
        int jobId = connection.put(new Job("fuck".getBytes(), 65535, 0, 120));
        TubeStatus tubeStatus = connection.statsTube("default");
        assertThat(tubeStatus.currentReadyJobs()).isEqualTo(1);
        connection.delete(jobId);
        tubeStatus = connection.statsTube("default");
        assertThat(tubeStatus.currentReadyJobs()).isEqualTo(0);
    }

    @Test
    public void testPutAsync() throws Exception {
        final int[] newJobId = {-1};
        connection.put(new Job("test".getBytes(), 65535, 0, 120), new PutRequestCallback() {

            @Override
            public void onPutSuccess(int jobId) {
                newJobId[0] = jobId;
            }

            @Override
            public void onException(Exception cause) {
            }
        });
        Thread.sleep(1000);
        assertThat(newJobId[0]).isNotEqualTo(-1);
        TubeStatus tubeStatus = connection.statsTube("default");
        assertThat(tubeStatus.currentReadyJobs()).isEqualTo(1);
        connection.delete(newJobId[0]);
        tubeStatus = connection.statsTube("default");
        assertThat(tubeStatus.currentReadyJobs()).isEqualTo(0);
    }

    @Test
    public void testReserve() throws Exception {
        int jobId = connection.put(new Job("test".getBytes(), 65535, 0, 120));
        TubeStatus tubeStatus = connection.statsTube("default");
        assertThat(tubeStatus.currentReadyJobs()).isEqualTo(1);
        Job job = connection.reserve();
        assertThat(job.getId()).isEqualTo(jobId);
        assertThat(job.getJobData()).isEqualTo("test".getBytes());
        tubeStatus = connection.statsTube("default");
        assertThat(tubeStatus.currentReadyJobs()).isEqualTo(0);
        assertThat(tubeStatus.currentReservedJobs()).isEqualTo(1);
        connection.delete(jobId);
        tubeStatus = connection.statsTube("default");
        assertThat(tubeStatus.currentReadyJobs()).isEqualTo(0);
        assertThat(tubeStatus.currentReservedJobs()).isEqualTo(0);
    }

    @Test
    public void testRelease() throws Exception {
        int jobId = connection.put(new Job("test".getBytes(), 65535, 0, 120));
        TubeStatus tubeStatus = connection.statsTube("default");
        assertThat(tubeStatus.currentReadyJobs()).isEqualTo(1);
        Job job = connection.reserve();
        assertThat(job.getId()).isEqualTo(jobId);
        assertThat(job.getJobData()).isEqualTo("test".getBytes());
        tubeStatus = connection.statsTube("default");
        assertThat(tubeStatus.currentReadyJobs()).isEqualTo(0);
        assertThat(tubeStatus.currentReservedJobs()).isEqualTo(1);

        connection.releaseJob(jobId, 62225, 0);
        tubeStatus = connection.statsTube("default");
        assertThat(tubeStatus.currentReadyJobs()).isEqualTo(1);
        assertThat(tubeStatus.currentReservedJobs()).isEqualTo(0);

        connection.delete(jobId);
        tubeStatus = connection.statsTube("default");
        assertThat(tubeStatus.currentReadyJobs()).isEqualTo(0);
        assertThat(tubeStatus.currentReservedJobs()).isEqualTo(0);
    }

    @Test
    public void testDeleteException() throws Exception {
        try {
            BeanstalkConnection connection = client.getConnection();
            connection.delete(10000);
        } catch (FailedResponseException e) {
            if (!e.getResponseText().equals(Beanstalk.NOT_FOUND)) {
                fail("should get " + Beanstalk.NOT_FOUND);
            }
        }
    }

    @Test
    public void testSyncPutMultipleCalls() throws BeanstalkException, InterruptedException {
        final List<Integer> jobList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final int finalI = i;
            connection.put(new Job("test".getBytes(), 65535, 0, 120), new PutRequestCallback() {

                @Override
                public void onPutSuccess(int jobId) {
                    jobList.add(finalI);
                }

                @Override
                public void onException(Exception cause) {
                }
            });
        }
        Thread.sleep(1000);
        assertThat(jobList.size()).isEqualTo(10);
        int i = 0;
        for (Integer jobId : jobList) {
            assertThat(jobId.intValue()).isEqualTo(i++);
        }
        TubeStatus tubeStatus = connection.statsTube("default");
        assertThat(tubeStatus.currentReadyJobs()).isEqualTo(10);
    }

    @Test
    public void testMiltThread() throws InterruptedException, BeanstalkException {
        ExecutorService executor1 = Executors.newSingleThreadExecutor();
        ExecutorService executor2 = Executors.newSingleThreadExecutor();
        executor1.submit(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    try {
                        connection.put(new Job("test".getBytes(), 65535, 0, 120));
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (BeanstalkException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
        });

        executor2.submit(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    try {
                        connection.put(new Job("test".getBytes(), 65535, 0, 120));
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (BeanstalkException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
        });

        Thread.sleep(2000);
        TubeStatus tubeStatus = connection.statsTube("default");
        assertThat(tubeStatus.currentReadyJobs()).isEqualTo(20);
    }
}
